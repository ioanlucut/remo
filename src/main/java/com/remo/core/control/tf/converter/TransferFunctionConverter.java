package com.remo.core.control.tf.converter;

import com.remo.core.control.tf.dao.TransferFunctionDAO;
import com.remo.core.control.tf.model.TransferFunction;
import org.apache.log4j.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.inject.Inject;
import javax.inject.Named;

@Named
public class TransferFunctionConverter implements Converter {

  @Inject
  private TransferFunctionDAO transferFunctionDAO;

  @Inject
  private transient Logger logger;

  public Object getAsObject(FacesContext facesContext, UIComponent component, String newValue) {
    if (newValue == null || newValue.isEmpty()) {
      return null;
    }
    try {
      return transferFunctionDAO.findOver(Long.valueOf(newValue));
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
    }
    return null;
  }

  public String getAsString(FacesContext facesContext, UIComponent component, Object value) {
    if (value == null || value.equals("")) {
      return "";
    }
    if (!(value instanceof TransferFunction)) {
      return null;
    } else {
      TransferFunction transferFunction = (TransferFunction) value;
      return String.valueOf(transferFunction.getId());
    }
  }
}