package com.remo.core.control.backing;

import com.remo.annotations.ThreadPool;
import com.remo.bootstrap.app.CmdPvServiceStarter;
import com.remo.core.control.gui.SeriesSample;
import com.remo.core.control.pid.PidControllerServiceImpl;
import com.remo.core.control.pid.dao.PidDAO;
import com.remo.core.control.pid.model.Pid;
import com.remo.core.control.pid.model.PidDirection;
import com.remo.core.control.pid.model.PidMode;
import com.remo.core.control.tf.dao.TransferFunctionDAO;
import com.remo.core.control.tf.model.TransferFunction;
import com.remo.core.shiro.backing.UserBacking;
import com.remo.utils.JobManagerUtils;
import org.apache.log4j.Logger;
import org.ilu.cmd.client.Command;
import org.ilu.cmd.client.CommandFeedback;
import org.ilu.cmd.client.CommandFeedbackException_Exception;
import org.ilu.cmd.client.CommandService;
import org.ilu.cmd.service.PidCmdServiceConsumer;
import org.ilu.common.XMLCalendarToDate;
import org.ilu.pv.client.*;
import org.ilu.pv.simulation.ProcessValueServiceConsumer;
import org.omnifaces.util.Messages;
import org.primefaces.model.chart.LineChartModel;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.ws.WebServiceException;
import java.io.Serializable;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Named
@SessionScoped
@SuppressWarnings("CdiUnproxyableBeanTypesInspection")
public class GuiBacking implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int DELAY = 3;
    private static final boolean MAY_INTERRUPT_IF_RUNNING = true;

    private static final long DEFAULT_POLL_INTERVAL_SECONDS = 1L;
    private static final int CAPACITY = 3000;

    private static final long DEMO_SAMPLES_TO_SHOW = 50L;
    private static final int DEMO_SET_POINT = 80;
    private static final int MAX_TIME_TO_WAIT = 1;

    @Inject
    private transient Logger logger;

    @Inject
    private ChartsBacking chartsBacking;

    @Inject
    private UserBacking userBacking;

    private transient Lock lock;

    private PidControllerServiceImpl pidController;

    @Inject
    private PidDAO pidDAO;

    /**
     * PID transfer object (Selected/edited from GUI)
     */
    private Pid pid;

    /**
     * Defined list of pids.
     */
    private List<Pid> pids;

    @Inject
    private TransferFunctionDAO transferFunctionDAO;

    /**
     * Transfer function object (Selected/edited from GUI)
     */
    private TransferFunction transferFunction;

    /**
     * Defined list of transfer functions.
     */
    private List<TransferFunction> transferFunctions;

    /**
     * Flag which tells if PID simulation is started or not.
     */
    private volatile boolean serviceStarted;

    /**
     * Flag which tells if the whole installation is started or not.
     */
    private volatile boolean installationStarted;

    /**
     * Flag which tells if polling is started or not.
     */
    private volatile boolean pollingStarted;

    private volatile boolean pollingStoppedDueToTabSwitchStarted;

    private Date demoDateSinceSetPointWasChanged;

    /**
     * Polling used to poll from client side and also to submit workers to {@code pidSchedulerExecutor}
     * Represents the sample which PID is using for computing output at every step.
     * <p/>
     * However, this is also used for polling from client side (If PID is computing a new value with a rate of minimum this
     * value, it has to be displayed on GUI with the same rate. Example: PID is calculating at every 1 second a new output for its
     * PV and SP. So therefore, GUI will be updated at every second too.
     * <p/>
     * The way how PID is computing this value at this rate is by using a {@link java.util.concurrent.ScheduledExecutorService}
     * which submits at every rate of this value a {@link java.lang.Runnable} to perform PID calculation and which updates
     * charts models which are then displayed on the GUI.
     */
    private volatile long pidSampleSeconds = DEFAULT_POLL_INTERVAL_SECONDS;


    /**
     * Connection manager executor - used to connect to remote service.
     */
    @ThreadPool
    @Inject
    private transient ThreadPoolExecutor connectionManagerExecutor;

    /**
     * Command remote connection service.
     */
    private volatile CommandService cmdRemoteService;

    /**
     * Process value remote connection service.
     */
    private volatile ProcessValueService processValueService;

    @Inject
    private transient ScheduledExecutorService pidSchedulerExecutor;

    /**
     * Submitted futures.
     */
    private ScheduledFuture<?> existingFutures;

    @Inject
    private CmdPvServiceStarter cmdPvServiceStarter;

    /**
     * Sample queue
     */
    private BlockingQueue<SeriesSample> sampleQueue = new LinkedBlockingQueue<>(CAPACITY);

    /**
     * Worker which tries to compute PID output.
     */
    private Runnable pidComputerWorker = new Runnable() {

        @Override
        public void run() {
            try {
                doWork();
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        /**
         * Does the related work.
         */
        private void doWork() {
            if (isServiceStarted()) {

                // If is DEMO mode
                if (installationStarted) {
                    generateNewSetPoint();
                }

                // 1. We get the PV from remote.
                double lastComputedCommand = pidController.getOutput();
                double sensorValue = tryToReadProcessValueFromRemoteSensor(lastComputedCommand);
                pidController.setInput(sensorValue);

                // 2. We compute the new output.
                boolean pidComputed = pidController.compute();

                if (pidComputed) {

                    // 3. We send the command.
                    double appliedCommand = tryToSendComputedCommandToRemoteActuatorGetFeedback();

                    // 4. We show results in GUI
                    SeriesSample sample = new SeriesSample();
                    sample.setSampleTimeEntry(pidController.getSampleCount());
                    sample.setReceivedProcessValue(sensorValue);
                    sample.setSetPoint(pidController.getSetPoint());

                    // 5. Command that was sent
                    sample.setSentCommandOutput(pidController.getOutput());

                    // 6. Command that was confirmed was received
                    sample.setReceivedCommandOutput(appliedCommand);

                    // 7. Mark sample to be added to GUI chart.
                    sampleQueue.offer(sample);
                }
            }
        }
    };

    @PostConstruct
    public void initializeGui() {
        this.lock = new ReentrantLock();
        this.pid = new Pid();
        this.transferFunction = new TransferFunction();

        this.pidController = new PidControllerServiceImpl();
        this.pidController.setPid(pid);

        initOrReload();
    }

    /**
     * Loads or reloads the defined PIDs and Transfer functions from the database.
     */
    public void initOrReload() {
        this.pids = pidDAO.getUserPidsOf(userBacking.getUser());
        this.transferFunctions = transferFunctionDAO.getUserTransferFunctionOf(userBacking.getUser());
    }

    /**
     * Cleanup hook method after the object is garbage collected.
     */
    @PreDestroy
    public void destroy() {
        JobManagerUtils.closeScheduledThread(connectionManagerExecutor, "Close connection manager executor", logger);
        JobManagerUtils.closeScheduledThread(pidSchedulerExecutor, "Close pid scheduler executor", logger);
    }

    private void editPid() {
        pidController.setPid(pid);
        long currentSampleTimeInMillis = pidController.getSampleTime();
        long givenSampleTimeInMillis = TimeUnit.SECONDS.toMillis(pidSampleSeconds);

        if (currentSampleTimeInMillis != givenSampleTimeInMillis) {
            pidController.setSampleTime(givenSampleTimeInMillis);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // CMD related - begin
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Try to send computed command to remote actuator. If connection is lost, close the remote service. It will be started by
     * the polling method checkConnections();
     *
     * @return received command from remote actuator.
     */
    private double tryToSendComputedCommandToRemoteActuatorGetFeedback() {
        if (isRemoteConsumerConnectionEstablished()) {

            Command commandQuery = new Command();
            commandQuery.setProcessId(1);
            commandQuery.setCommandPercent(pidController.getOutput());
            commandQuery.setSentDate(XMLCalendarToDate.XMLCalendarToDate());
            commandQuery.setMaximumTimeToExecuteCommand(XMLCalendarToDate.XMLCalendarToDate());
            try {
                CommandFeedback feedback = cmdRemoteService.submit(commandQuery);

                return feedback.getReceivedCommand().getCommandPercent();
            } catch (WebServiceException | CommandFeedbackException_Exception ex) {
                if (ex.getCause() instanceof ConnectException) {
                    logger.error("Connection lost to the CMD remote service - closing existing service", ex);

                    cmdRemoteService = null;
                } else {
                    logger.error(ex.getMessage(), ex);
                }
            }
        } else {
            tryToConnectToRemoteConsumer();
        }

        logger.debug("Remote consumer not connected - command SKIPPED");

        return 0.0;
    }

    public boolean isRemoteConsumerConnectionEstablished() {
        return cmdRemoteService != null;
    }

    /**
     * Worker which tries to connect to CMD remote service.
     */
    private Callable<Boolean> connectToRemoteCmdWorker = new Callable<Boolean>() {

        @Override
        public Boolean call() throws Exception {
            try {
                return doWork();
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }

            return false;
        }

        private Boolean doWork() {
            logger.info("Try / Retry to connect to remote CMD service at:" + new Date());
            try {
                cmdRemoteService = new PidCmdServiceConsumer().getService();
                logger.info("Succeed to connect to remote CMD service at:" + new Date());

                return true;
            } catch (WebServiceException ex) {
                logger.info("Could not connect to remote CMD service at:" + new Date());
            }

            return false;
        }
    };

    private void tryToConnectToRemoteConsumer() {
        Future<Boolean> connectToRemoteCmdFuture = connectionManagerExecutor.submit(connectToRemoteCmdWorker);
        try {
            connectToRemoteCmdFuture.get(MAX_TIME_TO_WAIT, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            logger.error(ex.getMessage(), ex);

            Thread.currentThread().interrupt();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // CMD related - end
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Sensor related - begin
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Worker which tries to connect to Sensor remote service.
     */
    private Callable<Boolean> connectToRemoteSensorWorker = new Callable<Boolean>() {

        @Override
        public Boolean call() throws Exception {
            try {
                return doWork();
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }

            return false;
        }

        private Boolean doWork() {
            logger.info("Try / Retry to connect to remote Sensor service at:" + new Date());
            try {
                processValueService = new ProcessValueServiceConsumer().getService();
                logger.info("Succeed to connect to remote Sensor service at:" + new Date());

                return true;
            } catch (WebServiceException ex) {
                logger.info("Could not connect to remote Sensor service at:" + new Date());
            }

            return false;
        }
    };

    private double tryToReadProcessValueFromRemoteSensor(double appliedComputedCommand) {
        if (isRemoteSensorConnectionEstablished()) {

            ProcessValueQuery processValueQuery = new ProcessValueQuery();
            processValueQuery.setProcessId(1);
            processValueQuery.setLastComputedCommand(appliedComputedCommand);
            processValueQuery.setLastReceivedProcessValue(pidController.getLastInput());
            processValueQuery.setQueryDate(XMLCalendarToDate.XMLCalendarToDate());

            ProcessTransferFunction processTransferFunction = new ProcessTransferFunction();
            processTransferFunction.setDelayL(transferFunction.getDelayL());
            processTransferFunction.setGainK(transferFunction.getGainK());
            processTransferFunction.setTimeConstantT(transferFunction.getTimeConstantT());

            processValueQuery.setProcessTransferFunction(processTransferFunction);

            try {
                ProcessValueQueryFeedback feedback = processValueService.query(processValueQuery);
                return feedback.getProcessValue();
            } catch (WebServiceException | ProcessValueQueryException_Exception ex) {
                if (ex.getCause() instanceof ConnectException) {
                    logger.error("Connection lost to the Sensor remote service - closing existing service", ex);

                    processValueService = null;
                } else {
                    logger.error(ex.getMessage(), ex);
                }
            }
        } else {
            tryToConnectToRemoteSensor();
        }

        logger.debug("Remote sensor not connected - command SKIPPED");

        return 0.0;
    }

    public boolean isRemoteSensorConnectionEstablished() {
        return processValueService != null;
    }

    private void tryToConnectToRemoteSensor() {
        Future<Boolean> connectToRemoteSensorFuture = connectionManagerExecutor.submit(connectToRemoteSensorWorker);
        try {
            connectToRemoteSensorFuture.get(MAX_TIME_TO_WAIT, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            logger.error(ex.getMessage(), ex);

            Thread.currentThread().interrupt();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Sensor related - end
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Business methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Taken in GUI during polling.
     */
    public LineChartModel synchronizeWithGui() {
        lock.lock();
        List<SeriesSample> drainedSamples = new ArrayList<>();
        sampleQueue.drainTo(drainedSamples);
        this.chartsBacking.addSamples(drainedSamples);

        // truncate what is too much
        this.chartsBacking.removeRecordsTooOldFromSeriesIfNecessary();
        lock.unlock();

        return chartsBacking.getModel();
    }

    /**
     * Triggered by START button from the GUI
     */
    public void startService() {
        startPidExecutor();

        this.serviceStarted = true;
        Messages.addGlobalInfo("Service successfully started");
    }

    /**
     * Triggered by the STOP button from the GUI
     */
    public void stopService() {
        stopPidExecutor();
        this.serviceStarted = false;

        Messages.addGlobalInfo("Service successfully stopped");
    }

    /**
     * Starts the whole application with default settings.
     */
    public void startInstallation() {
        if (pids.size() == 0) {
            Messages.addGlobalError("Application cannot started - no PIDs defined.");

            return;
        } else if (transferFunctions.size() == 0) {
            Messages.addGlobalError("Application cannot started - no Transfer Functions defined");

            return;
        }

        // Take the first PID defined
        this.pid = pids.get(0);

        // Set default settings for DEMO
        this.pid.getPidParams().setKp(14.0);
        this.pid.getPidParams().setKi(15.0);
        this.pid.getPidParams().setKd(0.0);
        this.pid.getPidOutputRange().setMinRange(0);
        this.pid.getPidOutputRange().setMaxRange(255);

        // Take the first TF defined
        this.transferFunction = transferFunctions.get(0);

        // Set default settings for DEMO
        this.transferFunction.setDelayL(0.0);
        this.transferFunction.setGainK(0.6);
        this.transferFunction.setTimeConstantT(14.0);

        // Set a custom setPoint
        this.pidController.setPid(pid);
        this.pidController.setSetPoint(DEMO_SET_POINT);

        // We override these two options even the PID is differently defined.
        this.pidController.setPidDirection(PidDirection.DIRECT);
        this.pidController.setPidMode(PidMode.AUTO);

        this.chartsBacking.getSeriesOptions().setSamplesToShow(DEMO_SAMPLES_TO_SHOW);
        this.chartsBacking.setCommandSeriesRange(pid.getPidOutputRange().getMinRange(), pid.getPidOutputRange().getMaxRange());

        this.demoDateSinceSetPointWasChanged = new Date();

        // Mark installation as started
        this.installationStarted = true;
        this.serviceStarted = true;
        this.pollingStarted = true;

        startPidExecutor();

        // Start remote connection services
        cmdPvServiceStarter.startService(isRemoteConsumerConnectionEstablished() && isRemoteConsumerConnectionEstablished());
        Messages.addGlobalInfo("Application demo successfully started");
    }

    /**
     * Starts PID executor
     */
    private void startPidExecutor() {
        if (existingFutures == null || existingFutures.isDone()) {
            this.existingFutures = pidSchedulerExecutor
                .scheduleAtFixedRate(pidComputerWorker, DELAY, pidSampleSeconds, TimeUnit.SECONDS);
        }
    }

    /**
     * While in demo mode - generate new setpoint after few seconds.
     */
    private void generateNewSetPoint() {
        long MAX_DURATION = TimeUnit.MILLISECONDS.convert(20, TimeUnit.SECONDS);
        long duration = new Date().getTime() - demoDateSinceSetPointWasChanged.getTime();
        if (duration >= MAX_DURATION) {
            pidController.setSetPoint(new Random().nextInt(100));
            demoDateSinceSetPointWasChanged = new Date();
        }
    }

    /**
     * Stops the whole application.
     */
    public void stopInstallation() {
        this.pidController.setSetPoint(10);

        // We close connection to remote services too.
        this.cmdRemoteService = null;
        this.processValueService = null;

        this.installationStarted = false;
        this.serviceStarted = false;
        this.pollingStarted = false;

        stopPidExecutor();

        Messages.addGlobalInfo("Application demo successfully stopped");
    }

    /**
     * Stops PID executor
     */
    private void stopPidExecutor() {
        if (existingFutures != null) {
            this.existingFutures.cancel(MAY_INTERRUPT_IF_RUNNING);
        }
    }

    /**
     * Triggered by START polling button from the GUI
     */
    public void startPolling() {
        if (!pollingStarted) {
            this.pollingStarted = true;

            Messages.addGlobalInfo("Polling service successfully started");
        }
    }

    /**
     * Triggered by the STOP polling button from the GUI
     */
    public void stopPolling() {
        if (pollingStarted) {

            this.pollingStarted = false;

            Messages.addGlobalInfo("Polling service successfully stopped");
        }
    }

    /**
     * Triggered by the tab switch.
     */
    public void stopPollingDueToBrowserBlur() {
        if (pollingStoppedDueToTabSwitchStarted || !pollingStarted) {
            return;
        }

        this.pollingStoppedDueToTabSwitchStarted = true;
        this.pollingStarted = false;
        Messages.addGlobalInfo("Polling successfully stopped due to browser page unfocus");
    }

    /**
     * Triggered by the tab switch.
     */
    public void startPollingDueToTabSwitchFocus() {
        if (!pollingStoppedDueToTabSwitchStarted) {
            return;
        }

        if (!pollingStarted) {
            this.pollingStoppedDueToTabSwitchStarted = false;
            this.pollingStarted = true;
            Messages.addGlobalInfo("Polling successfully started due to browser page focus.");
        }
    }

    /**
     * Triggered whenever a {@code PidOutputRange} element is changed in the GUI (inside forms)
     */
    public void submitEditedPidSettings() {
        editPid();
        chartsBacking.setCommandSeriesRange(pid.getPidOutputRange().getMinRange(), pid.getPidOutputRange().getMaxRange());

        Messages.addGlobalInfo("Pid settings {0} successfully submitted", pid.getPidParams().toString());
    }

    /**
     * Submitted from DROP-DOWN
     */
    public void submitSelectedPid() {
        submitEditedPidSettings();

        Messages.addGlobalInfo("Pid {0} successfully selected", pid.getPidParams().toString());
    }

    /**
     * Submitted from DROP-DOWN
     */
    public void submitSelectedTransferFunction() {

        Messages.addGlobalInfo("Transfer function {0} successfully selected", transferFunction.toString());
    }

    /**
     * Triggered whenever a {@code TransferFunction} element is changed in the GUI (inside forms)
     */
    public void submitEditedTransferFunction() {

        Messages.addGlobalInfo("Transfer function {0} successfully submitted", transferFunction.toString());
    }

    public void submitSetPoint() {

        Messages.addGlobalInfo("SetPoint {0} successfully submitted", pidController.getSetPoint());
    }

    public Pid getPid() {
        return pid;
    }

    public void setPid(Pid pid) {
        this.pid = pid;
    }

    public TransferFunction getTransferFunction() {
        return transferFunction;
    }

    public void setTransferFunction(TransferFunction transferFunction) {
        this.transferFunction = transferFunction;
    }

    public boolean isServiceStarted() {
        return serviceStarted;
    }

    public void setServiceStarted(boolean serviceStarted) {
        this.serviceStarted = serviceStarted;
    }

    public boolean isPollingStarted() {
        return pollingStarted;
    }

    public void setPollingStarted(boolean pollingStarted) {
        this.pollingStarted = pollingStarted;
    }

    public long getPidSampleSeconds() {
        return pidSampleSeconds;
    }

    public void setPidSampleSeconds(long pidSampleSeconds) {
        this.pidSampleSeconds = pidSampleSeconds;
    }

    public PidControllerServiceImpl getPidController() {
        return pidController;
    }

    public void setPidController(PidControllerServiceImpl pidController) {
        this.pidController = pidController;
    }

    public List<Pid> getPids() {
        return pids;
    }

    public void setPids(List<Pid> pids) {
        this.pids = pids;
    }

    public List<TransferFunction> getTransferFunctions() {
        return transferFunctions;
    }

    public void setTransferFunctions(List<TransferFunction> transferFunctions) {
        this.transferFunctions = transferFunctions;
    }

    public boolean isInstallationStarted() {
        return installationStarted;
    }

    public void setInstallationStarted(boolean installationStarted) {
        this.installationStarted = installationStarted;
    }

    public boolean isPollingStoppedDueToTabSwitchStarted() {
        return pollingStoppedDueToTabSwitchStarted;
    }

    public void setPollingStoppedDueToTabSwitchStarted(boolean pollingStoppedDueToTabSwitchStarted) {
        this.pollingStoppedDueToTabSwitchStarted = pollingStoppedDueToTabSwitchStarted;
    }
}
