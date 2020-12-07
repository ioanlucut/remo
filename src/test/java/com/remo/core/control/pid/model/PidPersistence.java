package com.remo.core.control.pid.model;


import com.remo.core.roles.model.Role;
import com.remo.core.shiro.models.User;
import org.apache.shiro.crypto.hash.Sha256Hash;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;


public class PidPersistence {

  private static final EntityManagerFactory factory = Persistence.createEntityManagerFactory("remo-unit");
  private EntityManager entityManager;

  public void addUserWithEmail(String string) throws Exception {
    entityManager = factory.createEntityManager();

    Pid pid = new Pid();
    pid.setPidDirection(PidDirection.DIRECT);
    pid.setPidMode(PidMode.AUTO);
    PidOutputRange range = new PidOutputRange();
    range.setMaxRange(0);
    range.setMaxRange(250);
    pid.setPidOutputRange(range);

    PidParams pidParams = new PidParams();
    pidParams.setKd(0.6);
    pidParams.setKi(2.6);
    pidParams.setKp(6.6);
    pid.setPidParams(pidParams);

    User user = new User();
    user.setFirstName("Ioan");
    user.setLastName("Lucut");
    user.setEmail(string);
    user.setPassword(new Sha256Hash("ilu").toHex());
    user.setEnabled(true);
    user.setRoles(Arrays.asList(Role.values()));

    entityManager.getTransaction().begin();
    User found = entityManager.find(User.class, 600000L);
    pid.setUser(found != null ? found : entityManager.merge(user));
    entityManager.merge(pid);
    entityManager.getTransaction().commit();
  }

  public void getUser() throws Exception {
    entityManager = factory.createEntityManager();

    TypedQuery<Pid> pids = entityManager.createNamedQuery(Pid.PIDS_LIST_OF_USER, Pid.class);
    pids.setParameter("user", entityManager.find(User.class, 600000L));
    List<Pid> as = pids.getResultList();
  }

  public static void main(String[] args) {
    try {
      new PidPersistence().addUserWithEmail("ilu");
      new PidPersistence().getUser();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }
}