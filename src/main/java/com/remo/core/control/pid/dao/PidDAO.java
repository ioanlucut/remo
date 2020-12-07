package com.remo.core.control.pid.dao;

import com.remo.core.control.pid.model.Pid;
import com.remo.core.shiro.models.User;

import java.util.List;

public interface PidDAO {

  List<Pid> getUserPidsOf(User user);

  public void savePid(Pid pid);

  Pid findOver(Long pidId);
}