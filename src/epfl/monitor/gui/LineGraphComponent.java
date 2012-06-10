package epfl.monitor.gui;

import java.awt.Component;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * LineGraphComponent.java (UTF-8)
 *
 * 26 avr. 2012
 * @author Loic
 */
public class LineGraphComponent extends GraphComponent {
private XYSeriesCollection dataset;
    private Component panel;
    private JFreeChart lineChart;
    
    public LineGraphComponent(String name, String xTitle, String yTitle,
            String ... lineNames){
        super(name);
        //collection
        dataset = new XYSeriesCollection();
        for (String lineName : lineNames) {
            XYSeries series = new XYSeries(lineName);
            series.add(0D, 0D);
            dataset.addSeries(series);
        }
        dataset.setIntervalWidth(0.0D);
        //graph
        lineChart = ChartFactory.createXYLineChart(name, xTitle, yTitle,
                dataset, PlotOrientation.VERTICAL, true, true, false);
        lineChart.getXYPlot().getDomainAxis().setFixedAutoRange(60D);
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
        XYSeries xys = dataset.getSeries(val1);
        xys.add(xys.getMaxX() + 1D, value);
    }
}
