package com.remo.core.persistence;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class EntityManagerFactoryProducer {

  public static final String Remo = "remo-unit";

  @Produces
  @ApplicationScoped
  public EntityManagerFactory create() {
    return Persistence.createEntityManagerFactory(Remo);

  }

  public void destroy(@Disposes
                          EntityManagerFactory factory) {
    factory.close();
  }
}