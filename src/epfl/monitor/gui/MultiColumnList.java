package epfl.monitor.gui;

import java.awt.Component;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 *
 * Class : custom JList with two column per row.
 * 
 * @author Loic
 */
public class MultiColumnList extends JList {

    private MultiListModel model;
    
    public MultiColumnList() {
        super();
        //create renderer and display
        this.setOpaque(false); //try to paint what is behind
        setCellRenderer(new MultiColumnCellRenderer());
    }
    
    public MultiColumnList(String [][] listData) {
        MultiListModel model0 = new MultiListModel();
        this.setModel(model0);
        model0.setListData(listData);
        this.model = model0;
    }
    
    /**
     * Construct a dynamic multiListModel from listData.
     * @param listData an array of Object of the form <b>String [][]</b>.
     */
    @Override
    public void setListData(Object[] listData) {
        //use multilistmodel instead
        if (listData instanceof String[][]) {
            String [][] listData2D = (String[][]) listData;
            MultiListModel model0 = new MultiListModel();
            model0.setListData(listData2D);
            this.setModel(model0);
            this.model = model0;
        } else {
            this.setListData(listData);
        }
    }
    
    public String [][] getStaticModel() {
        return model.getStaticModel();
    }
    
    public void setProgressBarValue(String name, int value) {
        model.updateRowEntry(name, value);
    }
    
    public void setProgressBarValueExtra(String extra, int value) {
        model.updateRowEntryExtra(extra, value);
    }
    
    /**
     * Only two column so far. Customized to fit in the context (color, progress
     * bar).
     *
     * @author Loic
     */
    class MultiColumnCellRenderer extends JPanel implements ListCellRenderer {

        private JLabel left;
        private JProgressBar pbar;

        public MultiColumnCellRenderer() {
            GridLayout layout = new GridLayout();
            setLayout(layout);

            left = new JLabel();
            left.setOpaque(true);

            pbar = new JProgressBar();
            pbar.setMinimum(0);
            pbar.setMaximum(100);
            pbar.setValue(0);

            add(left);
            add(pbar);

            this.setBorder(new EmptyBorder(this.getInsets()));
            this.setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value,
                        int index, boolean isSelected, boolean cellHasFocus) {

            String text;
            //intercept the special parameters of this multi column list.
            if (value instanceof String[]) {
                text = ((String []) value)[0];
                pbar.setValue(Integer.valueOf(((String[]) value)[1]));
            //else: it is standard
            } else {
                text = value.toString();
            }
            
            left.setText(text);

             /* selection (maybe no need, esthetically)
                if (isSelected) {
                    left.setBackground(list.getSelectionBackground());
                    left.setForeground(list.getSelectionForeground());
                } else {
                    left.setBackground(list.getBackground());
                    left.setForeground(list.getForeground());
                }
             */

            setEnabled(list.isEnabled());
            setFont(list.getFont());
            return this;
        }
    }
    
    class MultiListModel extends DefaultListModel {

        /**
         * Array of tuples
         */
        private ArrayList<String[]> data = new ArrayList<>();
        
        @Override
        public int getSize() {
            return data.size();
        }

        @Override
        public Object getElementAt(int index) {
            return data.get(index);
        }
        
        /**
         * Add a value for a jlabel and its progress bar. If you want to
         * update, use <code>updateRowEntry</code>.
         * @param name the name to display.
         * @param value the value to set.
         * @param extra other data to link with this row
         */
        public void addRowEntry(String name, int value, String extra) {
            if (data.get(0).length != 3) {
                data.add(new String[]{name, Integer.toString(value)});
            } else {
                data.add(new String[]{name, Integer.toString(value), extra});
            }
            this.fireContentsChanged(this, 0, data.size());
        }
        
        /**
         * Update a row entry (the value of the progess bar). If the entry doesn't
         * exists, it does nothing.
         * @param name the name to display.
         * @param value the value to set.
         */
        public void updateRowEntry(String name, int value) {
            for (String [] tab : data) {
                if (tab[0].equals(name)) {
                    tab[1] = Integer.toString(value);
                    this.fireContentsChanged(this, 0, data.size() - 1);
                }
            }
        }
        
        /**
         * Update a row entry (the value of the progess bar). If the entry doesn't
         * exists, it does nothing.
         * @param extra the extra data to look for. The first occurence will be
         * updated.
         * @param value the value to set.
         */
        public void updateRowEntryExtra(String extra, int value) {
            if (data.get(0).length != 3) return;
            for (String [] tab : data) {
                if (tab[2].equals(extra)) {
                    tab[1] = Integer.toString(value);
                    this.fireContentsChanged(this, 0, data.size() - 1);
                }
            }
        }
        
        public void setListData(String [][] list) {
            data.addAll(Arrays.asList(list));
            this.fireContentsChanged(this, 0, data.size() - 1);
        }
        
        /**
         * Doesn't dump extra data.
         * @return a 2D array representation of this model.
         */
        public String[][] getStaticModel() {
            String [][] res = new String[data.size()][2];
            int i = 0;
            for (String[] tab : data) {
                res[i][0] = tab[0];
                res[i][1] = tab[1];
                i++;
            }
            return res;
        }
    }
}