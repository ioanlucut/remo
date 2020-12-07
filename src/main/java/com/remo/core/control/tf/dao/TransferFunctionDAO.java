package com.remo.core.control.tf.dao;

import com.remo.core.control.tf.model.TransferFunction;
import com.remo.core.shiro.models.User;

import java.util.List;

public interface TransferFunctionDAO {

  List<TransferFunction> getUserTransferFunctionOf(User user);

  public void saveTransferFunction(TransferFunction pid);

  TransferFunction findOver(Long pidId);
}