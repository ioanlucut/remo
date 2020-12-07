package com.remo.core.logging;

import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.interceptor.AroundConstruct;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public class LoggingInterceptor implements Serializable {

  private static final long serialVersionUID = -6839437616142999589L;

  @Inject
  private transient Logger logger;

  @AroundConstruct
  public void init(InvocationContext ic) throws Exception {
    logger.info("Entering constructor");
    try {
      ic.proceed();
    } finally {
      logger.info("Exiting constructor");
    }
  }

  public Object logMethod(InvocationContext ic) throws Exception {
    logger.info(ic.getTarget().toString() + ic.getMethod().getName());
    try {
      return ic.proceed();
    } finally {
      logger.info(ic.getTarget().toString() + ic.getMethod().getName());
    }
  }

  @AroundInvoke
  public Object profile(InvocationContext ic) throws Exception {
    long initTime = System.currentTimeMillis();
    try {
      return ic.proceed();
    } finally {
      long diffTime = System.currentTimeMillis() - initTime;
      logger.error(ic.getMethod() + " took " + TimeUnit.MILLISECONDS.toSeconds(diffTime) + " sec OR" + diffTime
          + "millis");
    }
  }
}