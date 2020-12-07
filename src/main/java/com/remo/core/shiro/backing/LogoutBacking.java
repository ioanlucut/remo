package com.remo.core.shiro.backing;

import org.apache.shiro.SecurityUtils;
import org.omnifaces.util.Faces;
import org.omnifaces.util.Messages;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import java.io.IOException;

@Named
@RequestScoped
public class LogoutBacking {

  private static final String HOME_URL = "login.xhtml";

  public void submit() throws IOException {
    SecurityUtils.getSubject().logout();
    Faces.invalidateSession();
    Messages.addFlashGlobalInfo("Successfully logged out");
    Faces.redirect(HOME_URL);
  }

}