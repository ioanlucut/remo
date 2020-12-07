package com.remo.core.persistence;

import org.apache.log4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class EntityManagerProducer {

  @Inject
  private transient Logger logger;

  @Inject
  private EntityManagerFactory emf;

  @Produces
  @RequestScoped
  public EntityManager create() {
    return emf.createEntityManager();
  }

  public void destroy(@Disposes
                          EntityManager em) {
    em.close();
    logger.debug(String.format("%s Entity manager was closed", em));
  }
}