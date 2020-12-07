package com.remo.core.roles.backing;

import com.remo.core.roles.model.Role;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

@Named
@ApplicationScoped
public class RolesBacking {

  public Role[] getRoles() {

    return Role.values();
  }

}