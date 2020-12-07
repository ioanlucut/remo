package com.remo.core.logging;

import org.apache.log4j.Logger;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;

public class LoggingProducer {

  @Produces
  public Logger createLogger(InjectionPoint injectionPoint) {

    return Logger.getLogger(injectionPoint.getMember().getDeclaringClass().getName());
  }
}