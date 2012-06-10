package epfl.monitor.gui;

import epfl.monitor.mbeans.MRClientProxy;
import epfl.project.sense.OverviewList;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

/**
 *
 * @author Loic
 */
public class StartDialog extends javax.swing.JDialog {

    private final String VU_VALID = "<html><font color=green>&#x2713</font></html>"; //✓
    private final String VU_ERROR = "<html><font color=red>&#x2717</font></html>"; //✗
    private final String VU_UNDEF = "-"; //-
    private final String LBL_ARROW = "<html><font size=\"15\">&#x2192;</font></html>"; //→
    
    private MRClientProxy mxp;
    
    /**
     * Creates new form StartDialog
     */
    public StartDialog(java.awt.Frame parent, boolean modal, MRClientProxy proxy,
            OverviewList overlist) {
        super(parent, modal);
        initComponents();
        mxp = proxy;
        //format the presentation
        //-clear labels vu
        lblVu1.setText(VU_UNDEF);
        lblVu2.setText(VU_UNDEF);
        //-loading
        boolean error = false;
        //--mr config
        if (overlist == null || overlist.size() == 0) {
            lblVu1.setText(VU_ERROR);
            error = true;
        } else {
            lblVu1.setText(VU_VALID);
            String [] node;
            ArrayList<String> model = new ArrayList<>();
            while ((node =  overlist.getNextNode()) != null) {
                if (node[0].equals("1")) {
                   model.add(node[1]);
                }
            }
            lstDescription.setListData(model.toArray());
        }
        //--mr tests
        TestConfigurations tests = null;
        File file = new File(MainGui.CONF_SRC);
        if (file.canRead()) {
             ObjectInputStream ois = null;
            try {
                ois = new ObjectInputStream(new FileInputStream(file));
                try {
                    tests = (TestConfigurations) ois.readObject();
                } catch (ClassNotFoundException ex) {
                }
            } catch (IOException ex) {
            } finally {
                try {
                    if (ois  != null) ois.close();
                } catch (IOException ex) {
                }
            }
        }
        if (tests == null) {
            lblVu2.setText(VU_ERROR);
            error = true;
        } else {
            lblVu2.setText(VU_VALID);
            //-shows descr for tests
            ArrayList<Configuration> list = tests.getConfigurations();
            for (Configuration c : list) {
                cbPreConf.addItem(c);
            }
        }
        Configuration noconf = new Configuration("no configuration", 0, 0);
        cbPreConf.addItem(noconf);
        if (error) {
           btStart.setEnabled(false);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        lblVu1 = new javax.swing.JLabel();
        lblVu2 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        btStart = new javax.swing.JButton();
        cbPreConf = new javax.swing.JComboBox();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtPreConf = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        lblName = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstDescription = new javax.swing.JList();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Start MapReduce");
        setResizable(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 255)));

        jLabel1.setText("loading mapreduce configuration");

        lblVu1.setText("<html>&#x2713</html>");

        lblVu2.setText("<html>&#x2713</html>");

        jLabel4.setText("finding preconfigurations");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(lblVu2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(lblVu1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel1))))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lblVu1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(lblVu2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 255)));

        btStart.setText("Start MapReduce");
        btStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btStartActionPerformed(evt);
            }
        });

        cbPreConf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbPreConfActionPerformed(evt);
            }
        });

        jScrollPane2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 255), 1, true));

        txtPreConf.setColumns(20);
        txtPreConf.setEditable(false);
        txtPreConf.setFont(new java.awt.Font("Segoe UI Symbol", 0, 12)); // NOI18N
        txtPreConf.setLineWrap(true);
        txtPreConf.setRows(5);
        txtPreConf.setTabSize(4);
        txtPreConf.setToolTipText("");
        txtPreConf.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        txtPreConf.setOpaque(false);
        jScrollPane2.setViewportView(txtPreConf);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbPreConf, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(btStart, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbPreConf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 135, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btStart)
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 255)));

        jLabel3.setText("MapReduce job name : ");

        lblName.setText("noname");

        jScrollPane1.setBorder(null);
        jScrollPane1.setOpaque(false);

        lstDescription.setBackground(getBackground());
        lstDescription.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 255), 1, true));
        lstDescription.setOpaque(false);
        jScrollPane1.setViewportView(lstDescription);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblName))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 275, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(lblName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btStartActionPerformed
        btStart.setEnabled(false);
        btStart.setText("Processing");
        //configure mapreduce
        Configuration choice = (Configuration) cbPreConf.getSelectedItem();
        synchronized (this) {
            try {
                if (choice.getMaxPartitionIndex() > 0) {
                    mxp.MRCtrl_sendCommand("redef partition " + choice.getMaxPartitionIndex());
                    wait(1000);
                }
                if (choice.getMemoryThreshold() > 0) {
                    mxp.MRCtrl_sendCommand("redef memory " + choice.getMemoryThreshold());
                    wait(1000);
                }
                btStart.setText("Ok");
                wait(450);
            } catch (InterruptedException ex) {
                System.err.println(ex.getMessage());
            }
        }
        //start mapreduce
        mxp.MRCtrl_sendCommand("start enableprobes");
        this.dispose();
    }//GEN-LAST:event_btStartActionPerformed

    private void cbPreConfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbPreConfActionPerformed
        Configuration c = (Configuration) cbPreConf.getSelectedItem();
        String descr = c.getConfigName() + "\n";
        if (c.getConfigName().equals("no configuration")
                && c.getMaxPartitionIndex() == 0 && c.getMemoryThreshold() == 0) {
            descr += "No predefined configuration.";
        } else {
            descr += "maxIndex : " + c.getMaxPartitionIndex() + "\n";
            descr += "mem limit : " + c.getMemoryThreshold();
        }
        txtPreConf.setText(descr);
    }//GEN-LAST:event_cbPreConfActionPerformed

    /**
     * @param args the command line arguments
     */
//    public static void main(String args[]) {
//        /*
//         * Set the Nimbus look and feel
//         */
//        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
//        /*
//         * If Nimbus (introduced in Java SE 6) is not available, stay with the
//         * default look and feel. For details see
//         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
//         */
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException ex) {
//            java.util.logging.Logger.getLogger(StartDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (InstantiationException ex) {
//            java.util.logging.Logger.getLogger(StartDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (IllegalAccessException ex) {
//            java.util.logging.Logger.getLogger(StartDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(StartDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
//        //</editor-fold>
//
//        /*
//         * Create and display the dialog
//         */
//        java.awt.EventQueue.invokeLater(new Runnable() {
//
//            public void run() {
//                StartDialog dialog = new StartDialog(new javax.swing.JFrame(), true);
//                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
//
//                    @Override
//                    public void windowClosing(java.awt.event.WindowEvent e) {
//                        System.exit(0);
//                    }
//                });
//                dialog.setVisible(true);
//            }
//        });
//    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btStart;
    private javax.swing.JComboBox cbPreConf;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblName;
    private javax.swing.JLabel lblVu1;
    private javax.swing.JLabel lblVu2;
    private javax.swing.JList lstDescription;
    private javax.swing.JTextArea txtPreConf;
    // End of variables declaration//GEN-END:variables
}
