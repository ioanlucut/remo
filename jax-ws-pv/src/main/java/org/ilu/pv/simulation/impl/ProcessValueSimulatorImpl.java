package org.ilu.pv.simulation.impl;

import org.ilu.pv.model.ProcessTransferFunction;
import org.ilu.pv.simulation.ProcessValueSimulator;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

public class ProcessValueSimulatorImpl implements ProcessValueSimulator, Serializable {

  @Override
  public double getSimulatedInput(@NotNull ProcessTransferFunction processTransferFunction, double pidCommand,
                                  double pidLastProcessValue) {

    return pidCommand * computeB(processTransferFunction) - computeA(processTransferFunction) * pidLastProcessValue;
  }

  private double computeA(@NotNull ProcessTransferFunction processTransferFunction) {
    return -Math.exp(-1 / processTransferFunction.getTimeConstantT());
  }

  private double computeB(@NotNull ProcessTransferFunction processTransferFunction) {
    return processTransferFunction.getGainK() * (1 + computeA(processTransferFunction));
  }
}
