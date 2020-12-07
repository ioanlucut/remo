package com.remo.utils;

import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class JobManagerUtils {

  public static void closeScheduledThread(ExecutorService scheduler, String jobDescription, Logger LOGGER) {
    scheduler.shutdownNow();

    try {
      if (!scheduler.awaitTermination(3, TimeUnit.SECONDS)) {
        LOGGER.debug(String.format("Still waiting to shutdown threads %s", jobDescription));
      } else {
        LOGGER.debug(String.format("Finished to cleanup threads %s", jobDescription));
      }
    } catch (InterruptedException ex) {
      LOGGER.error(ex.getMessage(), ex);
    }
  }
}
