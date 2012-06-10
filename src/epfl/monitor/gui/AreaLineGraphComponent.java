/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package epfl.monitor.gui;

import java.awt.Component;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

/**
 * LinesGraphComponent.java (UTF-8)
 *
 * 23 avr. 2012
 * @author Loic
 */
public class AreaLineGraphComponent extends GraphComponent {
    private TimeSeriesCollection dataset;
    private Component panel;
    private JFreeChart lineChart;
    private boolean tooltips = false;
    
    public AreaLineGraphComponent(String name, String xTitle, String yTitle,
            String ... lineNames){
        super(name);
        //collection
        dataset = new TimeSeriesCollection();
        for (String lineName : lineNames) {
            TimeSeries series = new TimeSeries(lineName);
            series.add(new Second(), 0D);
            dataset.addSeries(series);
        }
        //dataset.setIntervalWidth(0.0D);
        //graph
        DateAxis xAxis = new DateAxis(xTitle);
        xAxis.setLowerMargin(0.02);
        xAxis.setUpperMargin(0.02);
        xAxis.setAutoRange(true);
        NumberAxis yAxis = new NumberAxis(yTitle);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, null);
        plot.setOrientation(PlotOrientation.VERTICAL);
        plot.setForegroundAlpha(0.5f);
        XYToolTipGenerator tipGenerator = null;
        if (tooltips) {
            tipGenerator = StandardXYToolTipGenerator.getTimeSeriesInstance();
        }

        XYURLGenerator urlGenerator = null;
//        if (urls) {
//            urlGenerator = new StandardXYURLGenerator();
//        }

        plot.setRenderer(
            new XYAreaRenderer(XYAreaRenderer.AREA, tipGenerator, urlGenerator)
        );
        lineChart = new JFreeChart(name, JFreeChart.DEFAULT_TITLE_FONT,
                plot, false);

//        lineChart = ChartFactory.createXYAreaChart(name, xTitle, yTitle,
//                dataset, PlotOrientation.VERTICAL, true, true, false);
        lineChart.getXYPlot().getDomainAxis().setFixedAutoRange(60000D);
    }

    @Override
    public Component getChart() {
        if (panel == null) panel = new ChartPanel(lineChart);
        return panel;
    }

    /**
     * val1 : line name, val2 : not used, value : y value<br>
     * The first value is deleted.
     * @param val1 -
     * @param val2 -
     * @param value -
     */
    @Override
    public void updateGraph(String val1, String val2, Number value) {
        TimeSeries ts = dataset.getSeries(val1);
        //xys.add(xys.getMaxX() + 1D, value);
        ts.addOrUpdate(new Second(), value);
    }
}
