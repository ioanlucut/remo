package com.remo.core.control.pid.backing;

import com.remo.core.control.pid.dao.PidDAO;
import com.remo.core.control.pid.model.Pid;
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
public class PidBacking implements Serializable {

  private static final long serialVersionUID = 1944693549953500760L;

  @Inject
  private transient Logger logger;

  @Inject
  private PidDAO pidDAO;

  @Inject
  private UserBacking userBacking;

  private Pid pid;
  private List<Pid> pids;

  public void initOrReload() {
    this.pid = new Pid();
    this.pid.setUser(userBacking.getUser());
    this.pids = pidDAO.getUserPidsOf(userBacking.getUser());
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
    pidDAO.savePid(pid);
  }

  public void showMessageSuccessful() {
    BeanUtils.addMessage(FacesMessage.SEVERITY_INFO, "Successful", "Changes have been successfully submitted");
  }

  public void showMessageNothingSaved() {
    BeanUtils.addMessage(FacesMessage.SEVERITY_WARN, "Error",
        "Changes were not performed (perhaps no change was made prior to submit).");
  }

  public void remove(Pid value) {
    pids.remove(value);

    BeanUtils.addMessage(FacesMessage.SEVERITY_INFO, "Successful", "Position removed from form");
  }

  public PidDAO getPidDAO() {
    return pidDAO;
  }

  public void setPidDAO(PidDAO pidDAO) {
    this.pidDAO = pidDAO;
  }

  public UserBacking getUserBacking() {
    return userBacking;
  }

  public void setUserBacking(UserBacking userBacking) {
    this.userBacking = userBacking;
  }

  public Pid getPid() {
    return pid;
  }

  public void setPid(Pid pid) {
    this.pid = pid;
  }

  public List<Pid> getPids() {
    return pids;
  }

  public void setPids(List<Pid> pids) {
    this.pids = pids;
  }
}