package com.remo.core.control.tf.backing;

import com.remo.core.control.tf.dao.TransferFunctionDAO;
import com.remo.core.control.tf.model.TransferFunction;
import com.remo.core.shiro.backing.UserBacking;
import com.remo.utils.BeanUtils;
import org.apache.log4j.Logger;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named
@SessionScoped
public class TransferFunctionBacking implements Serializable {

  private static final long serialVersionUID = 1944693549953500760L;

  @Inject
  private transient Logger logger;

  @Inject
  private TransferFunctionDAO transferFunctionDAO;

  @Inject
  private UserBacking userBacking;

  private TransferFunction transferFunction;
  private List<TransferFunction> transferFunctions;

  public void initOrReload() {
    this.transferFunction = new TransferFunction();
    this.transferFunction.setUser(userBacking.getUser());
    this.transferFunctions = transferFunctionDAO.getUserTransferFunctionOf(userBacking.getUser());
  }

  public void submitForm() {
    try {
      tryToSubmit();
      showMessageSuccessful();
      initOrReload();
    } catch (Exception ex) {
      logger.error(ex.getMessage(), ex);
      showMessageNothingSaved();
    }
  }

  private void tryToSubmit() {
    transferFunctionDAO.saveTransferFunction(transferFunction);
  }

  public void showMessageSuccessful() {
    BeanUtils.addMessage(FacesMessage.SEVERITY_INFO, "Successful", "Changes have been successfully submitted");
  }

  public void showMessageNothingSaved() {
    BeanUtils.addMessage(FacesMessage.SEVERITY_WARN, "Error",
        "Changes were not performed (perhaps no change was made prior to submit).");
  }

  public void remove(TransferFunction value) {
    transferFunctions.remove(value);

    BeanUtils.addMessage(FacesMessage.SEVERITY_INFO, "Successful", "Position removed from form");
  }

  public TransferFunction getTransferFunction() {
    return transferFunction;
  }

  public void setTransferFunction(TransferFunction transferFunction) {
    this.transferFunction = transferFunction;
  }

  public List<TransferFunction> getTransferFunctions() {
    return transferFunctions;
  }

  public void setTransferFunctions(List<TransferFunction> transferFunctions) {
    this.transferFunctions = transferFunctions;
  }
}