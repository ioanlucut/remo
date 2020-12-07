package com.remo.core.control.tf.dao;

import com.remo.core.control.tf.model.TransferFunction;
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
public class TransferFunctionDAOImpl implements TransferFunctionDAO, Serializable {

  @Inject
  private transient Logger logger;

  @Inject
  private EntityManager entityManager;

  @Override
  public List<TransferFunction> getUserTransferFunctionOf(User user) {
    TypedQuery<TransferFunction> transferFunctionTypedQuery = entityManager.createNamedQuery(
        TransferFunction.TF_LIST_OF_USER, TransferFunction.class);
    transferFunctionTypedQuery.setParameter("user", user);

    return transferFunctionTypedQuery.getResultList();
  }

  @Override
  public void saveTransferFunction(TransferFunction transferFunction) {
    logger.debug(String.format("%s Entity manager save Pid", entityManager));

    entityManager.getTransaction().begin();
    entityManager.merge(transferFunction);
    entityManager.getTransaction().commit();
  }

  @Override
  public TransferFunction findOver(Long pidId) {

    return entityManager.find(TransferFunction.class, pidId);
  }
}
