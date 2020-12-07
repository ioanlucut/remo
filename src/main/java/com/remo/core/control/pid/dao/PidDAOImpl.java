package com.remo.core.control.pid.dao;

import com.remo.core.control.pid.model.Pid;
import com.remo.core.shiro.models.User;
import org.apache.log4j.Logger;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.Serializable;
import java.util.List;

/**
 * User: Ionel
 * Date: 6/30/14
 * Time: 7:38 PM
 */
public class PidDAOImpl implements PidDAO, Serializable {

  @Inject
  private transient Logger logger;

  @Inject
  private EntityManager entityManager;

  @Override
  public List<Pid> getUserPidsOf(User user) {
    TypedQuery<Pid> pids = entityManager.createNamedQuery(Pid.PIDS_LIST_OF_USER, Pid.class);
    pids.setParameter("user", user);

    return pids.getResultList();
  }

  @Override
  public void savePid(Pid pid) {
    logger.debug(String.format("%s Entity manager save Pid", entityManager));

    entityManager.getTransaction().begin();
    entityManager.merge(pid);
    entityManager.getTransaction().commit();
  }

  @Override
  public Pid findOver(Long pidId) {

    return entityManager.find(Pid.class, pidId);
  }
}
