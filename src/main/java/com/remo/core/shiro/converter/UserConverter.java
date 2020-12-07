package com.remo.core.shiro.converter;

import com.remo.core.shiro.dao.UserDAO;
import com.remo.core.shiro.models.User;
import org.apache.log4j.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.inject.Named;

@Named
public class UserConverter implements Converter {

  @Inject
  private UserDAO userDAO;

  @Inject
  private transient Logger logger;

  public Object getAsObject(FacesContext facesContext, UIComponent component, String newValue) {
    if (newValue == null || newValue.isEmpty()) {
      return null;
    }
    try {
      return userDAO.findOverEmail(newValue);
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
    return null;
  }

  public String getAsString(FacesContext facesContext, UIComponent component, Object value) {
    if (value == null || value.equals("")) {
      return "";
    }
    if (!(value instanceof User)) {
      return null;
    } else {
      User user = (User) value;
      return String.valueOf(user.getEmail());
    }
  }
}
