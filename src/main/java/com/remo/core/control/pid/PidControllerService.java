package com.remo.core.control.pid;

public interface PidControllerService {

  /*
   * compute() **********************************************************************
   * This, as they say, is where the magic happens. this function should be called every
   * time "void loop()" executes. the function will decide for itself whether a new pid
   * Output needs to be computed. returns true when the output is computed, false when
   * nothing has been done. *****************
   * ***************************************************************
   */
  public boolean compute();
}
