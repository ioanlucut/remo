package com.remo.core.control.pid.converter;

import com.remo.core.control.pid.dao.PidDAO;
import com.remo.core.control.pid.model.Pid;
import org.apache.log4j.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.inject.Named;

@Named
public class PidConverter implements Converter {

  @Inject
  private PidDAO pidDAO;

  @Inject
  private transient Logger logger;

  public Object getAsObject(FacesContext facesContext, UIComponent component, String newValue) {
    if (newValue == null || newValue.isEmpty()) {
      return null;
    }
    try {
      return pidDAO.findOver(Long.valueOf(newValue));
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
    return null;
  }

  public String getAsString(FacesContext facesContext, UIComponent component, Object value) {
    if (value == null || value.equals("")) {
      return "";
    }
    if (!(value instanceof Pid)) {
      return null;
    } else {
      Pid pid = (Pid) value;
      return String.valueOf(pid.getId());
    }
  }
}