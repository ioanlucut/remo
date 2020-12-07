package com.remo.core.shiro.backing;

import com.remo.core.shiro.dao.UserDAO;
import com.remo.core.shiro.models.User;
import org.apache.log4j.Logger;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;

@Named
@SessionScoped
public class UserBacking implements Serializable {

  private static final long serialVersionUID = 8927417184031364474L;

  @Inject
  private transient Logger logger;

  @Inject
  private UserDAO userDAO;

  private User user;

  public void loadUser(String username) {
    this.user = userDAO.findOverEmail(username);
  }

  /**
   * Returns the {@code user}
   *
   * @return
   */
  public User getUser() {
    return user;
  }

  /**
   * Sets the {@code user}
   *
   * @param user
   */
  public void setUser(User user) {
    this.user = user;
  }

}