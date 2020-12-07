package com.remo.utils;

import org.apache.log4j.Logger;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.omnifaces.util.Faces;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.context.FacesContext;
import java.io.IOException;

public class BeanUtils {

  private static final Logger logger = Logger.getLogger(BeanUtils.class);

  private static final String INDEX_PAGE = "bid/tfs.xhtml";

  public static void redirectWithMessage(String destination, Severity severity, String summary, String detail) {
    try {
      FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
      BeanUtils.addMessage(severity, summary, detail);
      FacesContext.getCurrentInstance().getExternalContext().redirect(destination);
    } catch (IOException ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

  public static void redirectHomeWithTheMessage(Severity severity, String summary, String detail) {
    BeanUtils.redirectWithMessage(INDEX_PAGE, severity, summary, detail);
  }

  public static void redirectToTheLastLocationOr404() {
    try {
      tryToRedirectToTheLastLocationOr404();
    } catch (IOException ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

  private static void tryToRedirectToTheLastLocationOr404() throws IOException {
    SavedRequest savedRequest = WebUtils.getAndClearSavedRequest(Faces.getRequest());

    if (savedRequest != null) {
      Faces.redirect(savedRequest.getRequestUrl());
    } else {
      Faces.responseSendError(404, "URL incomplete or invalid!");
    }
  }

  public static void tryToRedirectTo(String destination) {
    try {
      FacesContext.getCurrentInstance().getExternalContext().redirect(destination);
    } catch (IOException ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

  public static void sendResponseError() {
    try {
      Faces.responseSendError(404, "URL incomplete or invalid!");
    } catch (IOException ex) {
      logger.error(ex.getMessage(), ex);
    }
  }

  public static void addMessage(Severity severity, String summary, String detail) {
    FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(severity, summary, detail));
  }

}