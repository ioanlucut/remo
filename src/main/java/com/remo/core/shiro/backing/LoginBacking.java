package com.remo.core.shiro.backing;

import com.remo.core.shiro.models.User;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.omnifaces.util.Faces;
import org.omnifaces.util.Messages;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;

@Named
@RequestScoped
public class LoginBacking {

  private static final String HOME_URL = "control/service.xhtml";

  private static final Logger LOGGER = Logger.getLogger(LoginBacking.class);

  @Inject
  private UserBacking userBacking;

  /**
   * Given credentials and remember option
   */
  private String email;
  private String password;
  private boolean remember;

  /**
   * User model
   */
  private User user;

  public void submit() {
    try {
      // Proof over username and password (using shiro)
      UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(email, password, remember);
      SecurityUtils.getSubject().login(usernamePasswordToken);

      // User backing - load user
      userBacking.loadUser(email);

      // Forward to desired page
      SavedRequest savedRequest = WebUtils.getAndClearSavedRequest(Faces.getRequest());
      Messages.addFlashGlobalInfo("Welcome {0}", email);
      Faces.redirect(savedRequest != null ? savedRequest.getRequestUrl() : HOME_URL);
    } catch (AuthenticationException | IOException ex) {
      LOGGER.error(ex.getMessage(), ex);
      Messages.addGlobalError("Unknown user {0}, please try again.", email);
    }
  }

  /**
   * Returns the {@code email}
   *
   * @return
   */
  public String getEmail() {
    return email;
  }

  /**
   * Sets the {@code email}
   *
   * @param email
   */
  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * Returns the {@code password}
   *
   * @return
   */
  public String getPassword() {
    return password;
  }

  /**
   * Sets the {@code password}
   *
   * @param password
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Returns the {@code remember}
   *
   * @return
   */
  public boolean isRemember() {
    return remember;
  }

  /**
   * Sets the {@code remember}
   *
   * @param remember
   */
  public void setRemember(boolean remember) {
    this.remember = remember;
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