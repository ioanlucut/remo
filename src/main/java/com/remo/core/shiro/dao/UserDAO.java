package com.remo.core.shiro.dao;

import com.remo.core.shiro.models.User;

public interface UserDAO {

  public User findOverEmail(String userName);
}
