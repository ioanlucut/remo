package com.remo.core.control.backing;

import com.remo.core.control.gui.Series;
import com.remo.core.control.gui.SeriesOptions;
import com.remo.core.control.gui.SeriesSample;
import org.omnifaces.util.Messages;
import org.primefaces.model.chart.*;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Named
@SessionScoped
public class ChartsBacking implements Serializable {

    private LineChartModel model;
    private Series commandSeries;
    private Series pvSeries;
    private Series spSeries;
    private SeriesOptions seriesOptions;

    private static final int THRESHOLD = 25;

    @PostConstruct
    public void initializeCharts() {
        // Line chart model
        this.model = new LineChartModel();
        this.model.setLegendPosition("e");
        this.model.getAxes().put(AxisType.X, new CategoryAxis("Time"));

        // Set point Y axis
        this.model.getAxes().put(AxisType.Y, new LinearAxis("SetPoint"));
        Axis yAxis = model.getAxis(AxisType.Y);
        yAxis.setMin(0 - THRESHOLD);
        yAxis.setMax(100 + THRESHOLD);
        this.spSeries = new Series();
        this.spSeries.setLabel("SetPoint");
        this.spSeries.setXaxis(AxisType.X);
        this.spSeries.setYaxis(AxisType.Y);
        this.spSeries.set(0, 0);

        // Process value Y axis
        this.model.getAxes().put(AxisType.Y2, new LinearAxis("Process Value"));
        Axis yAxis2 = model.getAxis(AxisType.Y2);
        yAxis2.setMin(0 - THRESHOLD);
        yAxis2.setMax(100 + THRESHOLD);
        this.pvSeries = new Series();
        this.pvSeries.setLabel("Process value");
        this.pvSeries.setXaxis(AxisType.X);
        this.pvSeries.setYaxis(AxisType.Y2);
        this.pvSeries.set(0, 0);

        // Command Y axis
        this.model.getAxes().put(AxisType.Y3, new LinearAxis("Command"));
        Axis yAxis3 = model.getAxis(AxisType.Y3);
        yAxis3.setMin(0 - THRESHOLD);
        yAxis3.setMax(250 + THRESHOLD);
        this.commandSeries = new Series();
        this.commandSeries.setLabel("Command");
        this.commandSeries.setXaxis(AxisType.X);
        this.commandSeries.setYaxis(AxisType.Y3);
        this.commandSeries.set(0, 0);

        this.model.addSeries(pvSeries);
        this.model.addSeries(spSeries);
        this.model.addSeries(commandSeries);

        this.seriesOptions = new SeriesOptions();
    }

    public void clearCharts() {
        this.pvSeries.clear();
        this.commandSeries.clear();
        this.spSeries.clear();

        this.pvSeries.set(0, 0);
        this.commandSeries.set(0, 0);
        this.spSeries.set(0, 0);
    }

    public void setCommandSeriesRange(double min, double max) {
        Axis yAxis3 = model.getAxis(AxisType.Y3);

        yAxis3.setMin(min - THRESHOLD);
        yAxis3.setMax(max + THRESHOLD);

    }

    public void addSamples(List<SeriesSample> samples) {
        for (SeriesSample sample : samples) {
            this.pvSeries.set(sample.getSampleTimeEntry(), sample.getReceivedProcessValue());
            this.spSeries.set(sample.getSampleTimeEntry(), sample.getSetPoint());
            this.commandSeries.set(sample.getSampleTimeEntry(), sample.getReceivedCommandOutput());
        }
    }

    public void removeRecordsTooOldFromSeriesIfNecessary() {
        removeRecordsTooOldFromSeriesIfNecessary(pvSeries.getData());
        removeRecordsTooOldFromSeriesIfNecessary(spSeries.getData());
        removeRecordsTooOldFromSeriesIfNecessary(commandSeries.getData());
    }

    private void removeRecordsTooOldFromSeriesIfNecessary(Map<Object, Number> seriesData) {
        int numberOfSeriesSoFar = seriesData.size();
        if (numberOfSeriesSoFar > seriesOptions.getSamplesToShow()) {
            long difference = numberOfSeriesSoFar - seriesOptions.getSamplesToShow();

            long counter = 0;

            for (Iterator<Map.Entry<Object, Number>> iterator = seriesData.entrySet().iterator(); iterator.hasNext(); ) {
                if (counter++ > difference) {
                    break;
                }
                iterator.next();
                iterator.remove();
            }
        }
    }

    /**
     * Triggered whenever a {@code SeriesOptions} element is changed in the GUI (inside forms)
     */
    public void submitSeriesOption() {
        Messages.addGlobalInfo("Samples {0} successfully submitted", seriesOptions.getSamplesToShow());
    }

    public LineChartModel getModel() {
        return model;
    }

    public SeriesOptions getSeriesOptions() {
        return seriesOptions;
    }

    public void setSeriesOptions(SeriesOptions seriesOptions) {
        this.seriesOptions = seriesOptions;
    }
}
