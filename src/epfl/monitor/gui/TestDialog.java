package epfl.monitor.gui;

import epfl.monitor.mbeans.MRClientProxy;
import java.io.*;
import java.util.ArrayList;
import javax.management.Notification;
import javax.management.NotificationListener;

/**
 *
 * @author Loic
 */
public class TestDialog extends javax.swing.JDialog {

    private ArrayList<Configuration> helper;
    private MRClientProxy mxp;
    /**
     * Creates new form TestDialog
     */
    public TestDialog(java.awt.Frame parent, boolean modal, MRClientProxy proxy) {
        super(parent, modal);
        initComponents();
        
        mxp = proxy;
        
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
            cbConfig.addItem("No config");
            helper = new ArrayList<>();
        } else {
            helper = tests.getConfigurations();
            for (Configuration c : helper) {
                cbConfig.addItem(c);
            }
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
        jLabel2 = new javax.swing.JLabel();
        txtLimit1 = new javax.swing.JTextField();
        txtSize1 = new javax.swing.JTextField();
        cbConfig = new javax.swing.JComboBox();
        btSave1 = new javax.swing.JButton();
        btDel1 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        btTest = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtOutput = new javax.swing.JTextArea();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtSize2 = new javax.swing.JTextField();
        txtLimit2 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtName = new javax.swing.JTextField();
        btSave2 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtTime = new javax.swing.JTextField();
        txtPerc = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Test configuration");
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 255)), "Edit a configuration", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(153, 153, 255)));

        jLabel1.setText("Size of the pool of partition : ");

        jLabel2.setText("Memory threshold :");

        cbConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbConfigActionPerformed(evt);
            }
        });

        btSave1.setText("Save");
        btSave1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSave1ActionPerformed(evt);
            }
        });

        btDel1.setText("Delete");
        btDel1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btDel1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(cbConfig, javax.swing.GroupLayout.PREFERRED_SIZE, 240, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btDel1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel1)
                                .addComponent(jLabel2)))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtSize1)
                                    .addComponent(txtLimit1, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(44, 44, 44)
                                .addComponent(btSave1)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbConfig, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtSize1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtLimit1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btSave1)
                    .addComponent(btDel1)))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(new javax.swing.border.LineBorder(new java.awt.Color(153, 153, 255), 1, true), "Run a warmup phase", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, new java.awt.Color(153, 153, 255)));

        btTest.setText("Start the warmup phase");
        btTest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btTestActionPerformed(evt);
            }
        });

        txtOutput.setColumns(20);
        txtOutput.setEditable(false);
        txtOutput.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        txtOutput.setLineWrap(true);
        txtOutput.setRows(5);
        txtOutput.setText("output");
        txtOutput.setOpaque(false);
        jScrollPane1.setViewportView(txtOutput);

        jLabel3.setText("Size of the pool of partition : ");

        jLabel4.setText("Memory threshold :");

        jLabel5.setText("Name : ");

        txtName.setText("no name");

        btSave2.setText("Save");
        btSave2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btSave2ActionPerformed(evt);
            }
        });

        jLabel6.setText("exec time < ");

        jLabel7.setText("exec progression <");

        txtTime.setText("10");

        txtPerc.setText("15");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(txtSize2)
                            .addComponent(txtLimit2)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtName))
                    .addComponent(jScrollPane1)
                    .addComponent(btTest, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btSave2))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(txtTime, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtPerc, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btTest)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7)
                    .addComponent(txtTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPerc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtSize2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtLimit2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(txtName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btSave2))
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btSave1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSave1ActionPerformed
        if (cbConfig.getSelectedItem() instanceof String) return;
        int size = -1;
        long limit = -1;
        try {
            size = Integer.parseInt(txtSize1.getText());
            limit = Long.parseLong(txtLimit1.getText());
        } catch (NumberFormatException ex) {
        }
        if (size > 0 && limit > 0) {
            ((Configuration)cbConfig.getSelectedItem()).setMaxPartitionIndex(size);
            ((Configuration)cbConfig.getSelectedItem()).setMemoryThreshold(limit);
        } else {
            txtSize1.setText(Integer.toString(((Configuration)cbConfig.getSelectedItem()).getMaxPartitionIndex()));
            txtLimit1.setText(Long.toString(((Configuration)cbConfig.getSelectedItem()).getMemoryThreshold()));
        }
    }//GEN-LAST:event_btSave1ActionPerformed

    private void btDel1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btDel1ActionPerformed
        if (cbConfig.getSelectedItem() instanceof String) return;
        helper.remove((Configuration)cbConfig.getSelectedItem());
        cbConfig.removeItem((Configuration)cbConfig.getSelectedItem());
        if (cbConfig.getItemCount() == 0) cbConfig.addItem("no config");
        cbConfigActionPerformed(null);
    }//GEN-LAST:event_btDel1ActionPerformed

    private void cbConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbConfigActionPerformed
        Configuration config = cbConfig.getSelectedItem() instanceof Configuration
                ? (Configuration) cbConfig.getSelectedItem() : null;
        if (config == null) {
            txtSize1.setText("");
            txtLimit1.setText("");
            return;
        }
        txtSize1.setText(Integer.toString(config.getMaxPartitionIndex()));
        txtLimit1.setText(Long.toString(config.getMemoryThreshold()));
    }//GEN-LAST:event_cbConfigActionPerformed

    private void btSave2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btSave2ActionPerformed
        int size = -1;
        long limit = -1;
        try {
            size = Integer.parseInt(txtSize2.getText());
            limit = Long.parseLong(txtLimit2.getText());
        } catch (NumberFormatException ex) {
        }
        if (size > 0 && limit > 0) {
            String name = txtName.getText();
            if (name.isEmpty()) return;
            Configuration config = new Configuration(name, size, limit);
            helper.add(config);
            if (cbConfig.getItemCount() == 1 && cbConfig.getItemAt(0) instanceof String) {
                cbConfig.removeItemAt(0);
            }
            cbConfig.addItem(config);
            cbConfig.setSelectedIndex(cbConfig.getItemCount() - 1);
        } else {
            txtSize1.setText("nan");
            txtLimit1.setText("nan");
        }
    }//GEN-LAST:event_btSave2ActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        
            try (ObjectOutputStream fos = new ObjectOutputStream(
                         new FileOutputStream(new File(MainGui.CONF_SRC)))) {
                TestConfigurations configs = new TestConfigurations();
                for (Configuration c : helper) {
                    configs.addConfiguration(c.getConfigName(),
                            c.getMaxPartitionIndex(), c.getMemoryThreshold());
                }
                fos.writeObject(configs);
            } catch (IOException ex) {
                System.err.println("Cannot write the config.dat");
            }
            
    }//GEN-LAST:event_formWindowClosed

    private void btTestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btTestActionPerformed
        final int MAXTIME = 20;
        final int MAXPERC = 15;
        final Object lock = new Object();
        
        NotificationListener listener = new NotificationListener() {

            @Override
            public void handleNotification(Notification notification, Object handback) {
                if (notification.getType().equals("Prediction.results")) {
                    String [] message = notification.getMessage().split(":");
                    String output = "";
                    output += "average k/v : " + message[0] + "\n";
                    output += "estimated partition pool size : " + message[1] + "\n";
                    txtSize2.setText(message[1]);
                    output += "estimated memory consumption : " + message[2] + "\n";
                    txtLimit2.setText(message[2]);
                    output += "estimated time to finish : " + message[3];
                    txtOutput.setText(output);
                    synchronized (lock) {
                        lock.notify();
                    }
                }
            }

        };
        mxp.addNotificationListener("type=mrcontrol", listener);
        int time, perc;
        try {
            time = Integer.parseInt(txtTime.getText());
            perc = Integer.parseInt(txtPerc.getText());
        } catch (Exception ex) {
            txtTime.setText(Integer.toString(MAXTIME));
            txtPerc.setText(Integer.toString(MAXPERC));
            time = MAXTIME;
            perc = MAXPERC;
        }
        txtOutput.setText("...");
        mxp.MRCtrl_sendCommand("warm " + perc + " " + time);
        synchronized (lock) {
            try {
                lock.wait(time + 15); //15 seconds jitter allowed
            } catch (InterruptedException ex) {
            }
        }
        mxp.removeNotificationListener("type=mrcontrol", listener);
    }//GEN-LAST:event_btTestActionPerformed

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
    //            java.util.logging.Logger.getLogger(TestDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    //        } catch (InstantiationException ex) {
    //            java.util.logging.Logger.getLogger(TestDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    //        } catch (IllegalAccessException ex) {
    //            java.util.logging.Logger.getLogger(TestDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    //        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
    //            java.util.logging.Logger.getLogger(TestDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    //        }
    //        //</editor-fold>
    //
    //        /*
    //         * Create and display the dialog
    //         */
    //        java.awt.EventQueue.invokeLater(new Runnable() {
    //
    //            public void run() {
    //                TestDialog dialog = new TestDialog(new javax.swing.JFrame(), true);
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
    private javax.swing.JButton btDel1;
    private javax.swing.JButton btSave1;
    private javax.swing.JButton btSave2;
    private javax.swing.JButton btTest;
    private javax.swing.JComboBox cbConfig;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField txtLimit1;
    private javax.swing.JTextField txtLimit2;
    private javax.swing.JTextField txtName;
    private javax.swing.JTextArea txtOutput;
    private javax.swing.JTextField txtPerc;
    private javax.swing.JTextField txtSize1;
    private javax.swing.JTextField txtSize2;
    private javax.swing.JTextField txtTime;
    // End of variables declaration//GEN-END:variables
}
