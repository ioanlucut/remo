package com.remo.core.shiro.dao;

import com.remo.core.shiro.models.User;
import org.apache.log4j.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.Serializable;

@RequestScoped
public class UserDAOImpl implements UserDAO, Serializable {

  private static final long serialVersionUID = 4806788420578024259L;

  @Inject
  private transient Logger logger;

  @Inject
  private EntityManager entityManager;

  @Override
  public User findOverEmail(String email) {
    TypedQuery<User> q = entityManager.createQuery("SELECT p FROM User p WHERE p.email = ?1", User.class);
    q.setParameter(1, email);

    return q.getSingleResult();
  }

}
