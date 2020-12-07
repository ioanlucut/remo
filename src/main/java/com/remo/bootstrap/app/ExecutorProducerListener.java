package com.remo.bootstrap.app;

import com.remo.annotations.ThreadPool;
import com.remo.core.control.pid.PidControllerService;
import com.remo.core.control.pid.PidControllerServiceImpl;
import com.remo.utils.JobManagerUtils;
import org.apache.log4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@ApplicationScoped
public class ExecutorProducerListener implements ServletContextListener {

  protected static final Logger LOGGER = Logger.getLogger(ExecutorProducerListener.class);
  public static final int CORE_POOL_SIZE = 2;
  public static final int THREADS = 2;

  private Lock lock = new ReentrantLock();

  private List<ExecutorService> schedulerExecutors = new ArrayList<>();

  @Produces
  public ScheduledExecutorService produceService() {
    lock.lock();
    ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
    schedulerExecutors.add(service);
    lock.unlock();

    return service;
  }

  @Produces
  @ThreadPool
  public ThreadPoolExecutor produceThreadPoolExecutor() {
    lock.lock();

    ThreadPoolExecutor service = (ThreadPoolExecutor) Executors.newFixedThreadPool(THREADS);
    schedulerExecutors.add(service);

    lock.unlock();
    return service;
  }

  @Produces
  public PidControllerService producePidService() {
    return new PidControllerServiceImpl();
  }

  public void destroy(@Disposes
                          ScheduledExecutorService schedulerExecutor) {
    LOGGER.info("Dispose produced scheduler %s");

    JobManagerUtils.closeScheduledThread(schedulerExecutor, "Produced thread " + schedulerExecutor.toString(), LOGGER);
  }

  @Override
  public void contextInitialized(ServletContextEvent event) {
    LOGGER.info(String.format("Job %s started", "EXECUTOR PRODUCER LISTENER"));
  }

  @Override
  public void contextDestroyed(ServletContextEvent event) {
    LOGGER.info(String.format("Context destroyed %s - cleanup threads if not cleaned up", "EXECUTOR PRODUCER LISTENER"));

    for (ExecutorService schedulerExecutor : schedulerExecutors) {
      JobManagerUtils.closeScheduledThread(schedulerExecutor, "Produced thread " + schedulerExecutor.toString(), LOGGER);
    }
  }
}
