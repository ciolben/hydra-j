/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package epfl.monitor.gui;

import java.awt.Component;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * HistogramGraphComponent.java (UTF-8)
 *
 * 23 avr. 2012
 * @author Loic
 */
public class HistogramGraphComponent extends GraphComponent {
    private JFreeChart histoChart;
    private ChartPanel panel;
    private DefaultCategoryDataset dataSet;
    
    public HistogramGraphComponent(String name, String xTitle, String yTitle) {
        super(name);
        dataSet = new DefaultCategoryDataset();
        histoChart = ChartFactory.createBarChart(name, xTitle, yTitle, dataSet,
                //                      legend ?, tooltips ?, urls ?
                PlotOrientation.VERTICAL, true, true, false);
    }
    
    @Override
    public Component getChart() {
        if (panel == null) panel = new ChartPanel(histoChart);
        return panel;
    }
    
    /**
     * Update the histogram.
     * @param serie the serie
     * @param histName the corresponding set of value
     * @param value the value
     */
    @Override
    public void updateGraph(String serie, String histName, Number value) {
        dataSet.setValue(value, histName, serie);
    }
}
