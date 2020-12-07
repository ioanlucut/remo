package org.ilu.pv.simulation;

import org.ilu.pv.model.ProcessTransferFunction;

public interface ProcessValueSimulator {

  public double getSimulatedInput(ProcessTransferFunction transferFunction, double pidOutput, double pidLastInput);
}