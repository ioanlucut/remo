package com.remo.core.control.gui;

import org.primefaces.model.chart.LineChartSeries;

public class Series extends LineChartSeries {

  private static final long serialVersionUID = -3737341742721021983L;

  public void clear() {
    this.getData().clear();
  }
}
