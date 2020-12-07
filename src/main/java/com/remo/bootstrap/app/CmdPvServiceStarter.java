package com.remo.bootstrap.app;

import com.remo.annotations.ThreadPool;
import com.remo.utils.JobManagerUtils;
import org.apache.log4j.Logger;

import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * User: Ionel
 * Date: 7/3/14
 * Time: 8:40 PM
 */
@ApplicationScoped
public class CmdPvServiceStarter implements Serializable {

  private static Logger logger = Logger.getLogger(CmdPvServiceStarter.class);

  @ThreadPool
  @Inject
  private transient ThreadPoolExecutor connectionManagerExecutor;

  private Process myProcess;

  public void startService(boolean alreadyStarted) {
    if (!alreadyStarted) {
      submit();
    }
  }

  private void submit() {
    Runnable connectToRemoteCmdWorker = new Runnable() {
      @Override
      public void run() {
        String file = CmdPvServiceStarter.class.getResource("start-clients.bat").getFile();
        try {
          ProcessBuilder processBuilder = new ProcessBuilder(file, "");
          myProcess = processBuilder.start();
        } catch (Exception ex) {
          logger.error(ex.getMessage(), ex);
        }

      }
    };

    connectionManagerExecutor.execute(connectToRemoteCmdWorker);
  }

  public void stopService() throws IOException, InterruptedException {
    myProcess.waitFor();

  }

  @PreDestroy
  public void destroy() {
    JobManagerUtils.closeScheduledThread(connectionManagerExecutor, "Connection manager executor", logger);
  }

}
