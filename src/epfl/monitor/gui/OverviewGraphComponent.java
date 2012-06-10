package epfl.monitor.gui;

import epfl.project.sense.OverviewList;
import java.awt.Component;
import java.util.ArrayList;

class OverviewGraphComponent extends GraphComponent {

    private OverviewPanel panel;
    private final OverviewList overlist;
    private int currentRound = 0;

    public OverviewGraphComponent(OverviewList overlist) {
        super("Overview");
        panel = new OverviewPanel();
        this.overlist = overlist;
        
        //create the list data from the Overview list
        if (overlist == null) return;
        int roundCount = 0;
        String [] node;
        String [] content;
        ArrayList<String []> list = new ArrayList<>(10);
        while ((node = overlist.getNextNode())!= null) {
            if (node[0].equals("0")) { //not isTask
                //round
                content = new String [] {node[1] == null
                        ? "round" + node[2]
                        : node[1], "0", /*extra*/"r"+node[2]};
                roundCount = Integer.valueOf(node[2]); //because of the linarity of the list.
            } else {
                //task
                content = new String [] {"|- " + (node[1] == null
                        ? "task" + node[2]
                        : node[1]), node[3], /*extra*/"t"+roundCount+node[2]};
            }
            list.add(content);
        }
        
        if (list.isEmpty()) {
           changeContent(new String[][]{{"error", "0"}}); 
        } else {
           changeContent(list.toArray(new String[1][1]));
        }
    }

    /**
     * Clear and render the content of the data parameter.
     * @param data 
     */
    public final void changeContent(String [][] data) {
        panel.lstList.setListData(data);
    } 
    
    /**
     * Update a progress bar value for a given row name.
     * @param name the name appearing in the left row.
     * @param value the value to set in the progress bar.
     */
    public void updateContent(String name, int value) {
        panel.lstList.setProgressBarValue(name, value);
    }
    
    /**
     * Update a progress bar value for a matching extra datum.
     * @param extra the extra data of this row.
     * @param value the value to set in the progress bar.
     */
    public void updateContentExtra(String extra, int value) {
        panel.lstList.setProgressBarValueExtra(extra, value);
    }
    
    public int getCurrentRound() {
        return currentRound;
    }
    
    public void setCurrentRound(int currentRound) {
        String [] node = overlist.getNextTaskFor(this, currentRound);
        while (node != null) {
            updateContentExtra("t"+currentRound+node[2], 100);
            node = overlist.getNextTaskFor(this, currentRound);
        }
        updateContentExtra("r"+Integer.toString(currentRound), 100);
        this.currentRound = currentRound;
    }
    
    public void signalAllRoundEnded() {
        currentRound = 0;
        String [][] model = this.panel.lstList.getStaticModel();
        for (String [] tab : model) {
            //update the values to 100
            tab[1] = "100";
        }
        this.panel.lstList.setListData(model);
        super.cancelRefresh();
    }
    
    public OverviewList getBackupList() {
        return overlist;
    }
    
    public void addNewEvent(String event) {
        panel.addNewEvent(event);
    }
    
    public void clearEvents() {
        currentRound = 0;
        panel.clearEvents();
        clearProgressions();
    }
    
    public void clearProgressions() {
        String [][] model = this.panel.lstList.getStaticModel();
        for (String [] tab : model) {
            //update the values to 0
            tab[1] = "0";
        }
        this.panel.lstList.setListData(model);
    }
    
    @Override
    public Component getChart() {
        return panel;
    }

    /**
     * Update the graph. (do nothing), override it.
     * @param val1 int : round id
     * @param val2 int : task id
     * @param value int : percentage
     */
    @Override
    public void updateGraph(String val1, String val2, Number value) {
//        int roundID = Integer.valueOf(val1);
//        int taskID = Integer.valueOf(val2);
//        int perc = (int) value;
//        
    }
}