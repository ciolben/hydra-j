package epfl.monitor.gui;

import epfl.monitor.mbeans.BeanClient;
import epfl.monitor.mbeans.MRClientProxy;
import epfl.monitor.mbeans.OSInfo;
import epfl.monitor.mbeans.ServerUnreachableEventHandler;
import epfl.project.sense.OverviewList;
import java.awt.*;
import java.awt.event.*;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadInfo;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;


/**
 *
 * @author Loic
 */
public class MainGui extends javax.swing.JFrame {

    protected static final String CONF_SRC = "conf.dat";
    
    private final long PING_INTERVAL = 15000;
    
    protected MRClientProxy mxp;
    private HashMap<String, GraphComponent> treeOccurences = new HashMap<>();
    
    private MessageBoxFrame msgBox = new MessageBoxFrame();
    private boolean isConnected = false;
    private OverviewList overlist = null;
    private PopUpTab popup = new PopUpTab();
    
    protected int currentRound;
    
    private Timer lightConnectionTester;
    
    //need only one handler because only one client attribute
    private ServerUnreachableEventHandler serverEventHandler =
            new ServerUnreachableEventHandler() {

        @Override
        public void handleEvent(final BeanClient client) {
            //need to stop all the refresh tasks for loaded graphs.
            cancelAllRefresh();
//            if (!treeIsBuilt) return;
            if (!isConnected) {
                //will try to reconnect at the second attempt
                isConnected = false;
//                connectViaProxy(mxp.getAddress(), mxp.getPort());
                return;
            }
            //we don't lost time in waiting a possibly timeout before stopping refresh tasks.
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    client.stopClient();
//                }
//            }).start();
            //print a message
            wlog("Server unreachable. Click connect in Menu->File->Connect... to"
                    + " try to reconnect to the server.");
        }
    };
    
    /**
     * Creates new form MainGui
     */
    public MainGui() {
        initComponents();
        this.setLocationRelativeTo(null);
        /*
         * "This" is ok here, because :
         * -MessageBoxFrame will use "this" when wlog is called
         * <=wlog is called from the MainGui
         * <=the MainGui maybe calls wlog after it has been created
         */
        msgBox.setLocationRelativeTo(this);
        
        //***LnF
        LookAndFeelInfo [] fs = UIManager.getInstalledLookAndFeels();
        for (final LookAndFeelInfo f : fs) {
            JMenuItem item = new JMenuItem(f.getName());
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setLook(f.getClassName());
                }
            });
            mnuChangeLook.add(item);
        }
        //***
        
        initConnection();
        
        lightConnectionTester = new Timer();
        lightConnectionTester.schedule(new TimerTask() {
            @Override
            public void run() {
                mxp.MRCtrl_connectionTest();
            }
        }, 0, PING_INTERVAL);
    }

    private void cancelAllRefresh() {
        Collection<GraphComponent> graphs = treeOccurences.values();
        for (GraphComponent graph : graphs) {
            graph.cancelRefresh();
        }
    }
    
    private void reactivateAllRefresh() {
        Collection<GraphComponent> graphs = treeOccurences.values();
        for (GraphComponent graph : graphs) {
            if (graph.isLoaded()) {
                graph.changeRefreshInterval(-1);
            }
        }
    }
    
    /**
     * Close all graphs but don't stop refreshing.
     */
    private void closeAllGraphs() {
        Collection<GraphComponent> graphs = treeOccurences.values();
        for (GraphComponent graph : graphs) {
            graph.setUnloaded();
        }
        tabPane.removeAll();
    }
    
    private void resetAllGraphs() {
        Collection<GraphComponent> graphs = treeOccurences.values();
        for (GraphComponent graph : graphs) {
            graph.setUnloaded();
            graph.cancelRefresh();
        }
        tabPane.removeAll();
        chkDisableLogging.setSelected(false);
        initTree();
        initTabs();
    }
    
    /**
     * Auto disable the tree if connection not available, else initiate a
     * (re)connection.
     * @param address ip
     * @param port number
     */
    private void connectViaProxy(String address, int port) {
        System.out.println("CONNECTION");
        if (isConnected) {
            resetAllGraphs();
            mxp.disconnect();
        }
        if (port != mxp.getPort() || !address.equals(mxp.getAddress())) {
            //create a new client proxy if address or port changes, or the proxy.
            mxp.disconnect();
            WeakReference wk = new WeakReference(mxp);
            wk.enqueue();
            mxp = new MRClientProxy(address, port);
        }
        if (!mxp.connect()) {
            wlog("Error : client cannot connect to distant JVM ("
                    + mxp.getAddress() + ":" + mxp.getPort() + ").");
//            serverEventHandler.handleEvent(mxp);
            //Server is not connected => cannot construct treeOccurences !
            trGraph.setEnabled(false);
        } else {
            //Implies to rebuild the tree occurences so that mxp is linked with
            //the graphs.
            initTree();
            //select the first tab
            initTabs();
            wlog("Connected to JVM @ " + mxp.getAddress() + ":" + mxp.getPort());
            isConnected = true;
            trGraph.setEnabled(true);
        }
    }
    
    private void initConnection() {
//        trGraph.setEnabled(false);
        mxp = new MRClientProxy();
        mxp.addServerUnreachableEventHandler(serverEventHandler);
        //we want to receive notifications
        mxp.addNotificationListener("type=mrstats",
                new GlobalNotificationListener(treeOccurences, this));
        connectViaProxy("localhost", 9876);
    }
    
    private void initTabs() {
        //create Status tab
        trGraph.setSelectionRow(0);
        trGraphMouseClicked(null);
    }
    
    /**
     * BUILD ALL THE GRAPH MODELS
     */
    private void initTree() {
        //build index : tree paths <-> graphComponents <-> tabs rendering
        treeOccurences.clear();
        GraphComponent graphC;
        RefreshTask task;
        trGraph.setEnabled(false);
        //-General**************************************************************
        graphC = new GraphComponent(TreeNames.General.name) {
            
            @Override
            public Component getChart() {
                GeneralPanel info = new GeneralPanel();
                try {
//                StringBuilder b = new StringBuilder();
//                String br = "<br>";
                java.lang.management.RuntimeMXBean runtime = mxp.getRuntimeInfo();
                OSInfo os = mxp.getOSInfo();
//                b.append("<html><b>Connected JVM and OS info<b><br>");
//                b.append("Connection addres : ").append(mxp.getAddress())
//                    .append(" on port ").append(mxp.getPort()).append(br);
//                b.append("OS name : ").append(os.getName()).append(br);
//                b.append("OS version : ").append(os.getVersion()).append(br);
//                b.append("OS arch : ").append(os.getArch()).append(br);
//                b.append("Number of available processors : ").append(os.getAvailableProcessors())
//                        .append("<br><br>");
//                
//                b.append("JVM name : ").append(runtime.getVmName()).append(br);
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(runtime.getStartTime());
//                b.append("JVM started at ").append(cal.get(Calendar.DAY_OF_MONTH)
//                        + "." + cal.get(Calendar.MONTH) + " @ "
//                        + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE)
//                        + ":" + cal.get(Calendar.SECOND))
//                        .append(br).append("</html>");
//                lastInfo = b.toString();
//                info.setText(lastInfo);
                  info.setConnectionTitle(mxp.getAddress() + " on port " + mxp.getPort());
                  info.setOsName(os.getName());
                  info.setOsVersion(os.getVersion());
                  info.setOsArch(os.getArch());
                  info.setOsCores(Integer.toString(os.getAvailableProcessors()));
                  info.setJvmName(runtime.getVmName());
                  info.setJvmDate(cal.get(Calendar.DAY_OF_MONTH) + "/"
                          + cal.get(Calendar.MONTH) + " @ "
                          + cal.get(Calendar.HOUR_OF_DAY) + ":"
                          + cal.get(Calendar.MINUTE) + ":"
                          + cal.get(Calendar.SECOND));
            } catch (java.lang.reflect.UndeclaredThrowableException ex) {
                info.fillWithDefault();
            } finally {
                return info;
            }
            }

            @Override
            public void updateGraph(String val1, String val2, Number value) {
                return;
            }
        };
        treeOccurences.put(TreeNames.General.t_name, graphC);
        //-Memory***************************************************************
        graphC = new AreaLineGraphComponent(TreeNames.JVMMemory.name,
                "Time", "KB", "used", "free");
        task = new RefreshTask() {
            MemoryUsage mem;
            
            @Override
            public void refresh(GraphComponent graph) {
                mem = mxp.getMemoryInfo(true);
                if (mem != null) {
                    graph.updateGraph("used", "", (mem.getUsed() >> 10));
                    graph.updateGraph("free", "", (mem.getCommitted() >> 10));
                }
            }
        
        };
        graphC.setRefreshTask(task);
        treeOccurences.put(TreeNames.JVMMemory.t_name, graphC);
        //-Thread***************************************************************
        graphC = new LineGraphComponent(TreeNames.JVMThread.name,
                "Time", "Number", "number");
        task = new RefreshTask() {
            ThreadInfo[] threads = mxp.getThreadInfo();
            
            @Override
            public void refresh(GraphComponent graph) {
                graph.updateGraph("number", "", threads.length);
            }
        
        };
        graphC.setRefreshTask(task);
        treeOccurences.put(TreeNames.JVMThread.t_name, graphC);
        //-CPU******************************************************************
        graphC = new LineGraphComponent(TreeNames.JVMCPU.name, "Time", "Activity", "%");
        task = new RefreshTask() {
            @Override
            public void refresh(GraphComponent graph) {
                graph.updateGraph("%", null, mxp.getMRStats_CpuLoad());
            }
        };
        
        graphC.setRefreshTask(task);
        treeOccurences.put(TreeNames.JVMCPU.t_name, graphC);
        //-WorkerCount**********************************************************
        graphC = new LineGraphComponent(TreeNames.WorkerCount.name, "Time",
                "Number", "mappers", "reducers");
        task = new RefreshTask() {

            @Override
            public void refresh(GraphComponent graph) {
                int n1 = mxp.getMRMemory_MapperCount();
                int n2 = mxp.getMRMemory_ReducerCount();
                graph.updateGraph("mappers", null, n1);
                graph.updateGraph("reducers", null, n2);
            }
            
        };
        graphC.setRefreshTask(task);
        treeOccurences.put(TreeNames.WorkerCount.t_name, graphC);
        //-AverageMapTimes******************************************************
        //histograms per round
        graphC = new HistogramGraphComponent(TreeNames.WorkerGlobal.name,
                "", "time (ms)");
        
        graphC.setRefreshTask(new RefreshTask() {

            @Override
            public void refresh(GraphComponent graph) {
                graph.updateGraph(Integer.toString(currentRound),
                        "map",
                        mxp.getMRStats_AverageMapTime());
                graph.updateGraph(Integer.toString(currentRound),
                        "red",
                        mxp.getMRStats_AverageReduceTime());
            }
            
        });
        treeOccurences.put(TreeNames.WorkerGlobal.t_name, graphC);
        //-Overview*************************************************************
        overlist = mxp.getMRStats_OverviewList();
        graphC = new OverviewGraphComponent(overlist);
        
        task = new RefreshTask() {
            
            @Override
            public void refresh(GraphComponent graph) {
                OverviewGraphComponent overv = (OverviewGraphComponent) graph;
                if (overv == null) { return; }
                //ask mxp for the current round only.
                int currentRound = overv.getCurrentRound();
//                OverviewList overlist = overv.getBackupList();
                
                int progressValue;
                int sum     = 0;
                int count   = 0;
                String [] node = overlist.getNextTaskFor(this, currentRound);
                while (node != null) {
                    progressValue = mxp.getMRStats_Progress(currentRound,
                            Integer.valueOf(node[2]));
                    overv.updateContentExtra("t"+currentRound+node[2], progressValue);
                    //optional (if notification system is disabled)
//                    if (progressValue == 100) { 
//                        overv.setCurrentRound(overv.getCurrentRound() + 1);
//                    }
                    sum += progressValue;
                    count++;
//                    System.out.println("finding value for : " + currentRound
//                            + "|" + node[2] + " get : " + progressValue + "\n"
//                            + "sum is : " + sum + " and count is " + count);
                    node = overlist.getNextTaskFor(this, currentRound);
                }
                //update round bar
                overv.updateContentExtra("r"+Integer.toString(currentRound),
                        count == 0 ? 0 : sum / count);
            }
            
        };
        graphC.setRefreshTask(task);
        treeOccurences.put(TreeNames.Overview.t_name, graphC);
//      graphC = new GraphComponent("CPU/Lines");
//      treeOccurences.put(graphC.getName(), graphC);
    }
    
    /**
     * Add a new tab at index, inserting it if necessary. A too big index will
     * be downscale to the next available index. A negative index add a tab to
     * the end of the list.
     * @param title the title to display in the tab.
     * @param tabName the name of the tab
     * @param index where to add a tab.
     * @return the index of this tab
     */
    private int addTab(String title, String tabName, int index) {
        if (index < 0) index = tabPane.getTabCount();
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.add(new JLabel(title));
        panel.add(new TabButton(tabPane, panel, treeOccurences));
        //***name is also used to retrieve the graphmodel***
        panel.setName(tabName);
        //**************************************************
        if (index >= tabPane.getTabCount()) {
            tabPane.addTab("newtab", null);
        } else {
            tabPane.insertTab("newtab", null, null, null, index);
        }
        tabPane.setTabComponentAt(index, panel);
        return index;
    }
    
    /**
     * Add a component to the content of a tab at the specified index.
     * @param index the tab index.
     * @param component the component to add (GridLayout layout)
     */
    private void addTabContent(int index, Component component) {
        if (index < 0 || index >= tabPane.getTabCount() || component == null)
            return;
        JPanel panel;
        if(tabPane.getComponentAt(index) == null) {
            panel = new JPanel();
            panel.setLayout(new GridLayout(1,0)); //choisir correctement r,c
            tabPane.setComponentAt(index, panel);
        } else {
            panel = (JPanel) tabPane.getComponentAt(index);
        }
        panel.add(component);
    }
    
    private void wlog(String msg) {
        lblStatus.setText(msg);
        //save message in the msgBox
        msgBox.txtMsgBox.append(msg.concat("\n"));
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        panStatus = new javax.swing.JPanel();
        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        trGraph = new javax.swing.JTree();
        jPanel2 = new javax.swing.JPanel();
        chkDisableLogging = new javax.swing.JCheckBox();
        btCloseAll = new javax.swing.JButton();
        tabPane = new javax.swing.JTabbedPane();
        lblStatus = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        lblMsgBox = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        mnuFile = new javax.swing.JMenu();
        mnuConnect = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuItem2 = new javax.swing.JMenuItem();
        mnuTask = new javax.swing.JMenu();
        mnuStartMR = new javax.swing.JMenuItem();
        mnuStopMR = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        mnuTestMR = new javax.swing.JMenuItem();
        mnuLog = new javax.swing.JMenu();
        mnuSend = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        mnuReset = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        mnuChangeLook = new javax.swing.JMenu();
        mnuHelp = new javax.swing.JMenu();
        mnuAbout = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        panStatus.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(109, 109, 109)));
        panStatus.setName("");

        javax.swing.GroupLayout panStatusLayout = new javax.swing.GroupLayout(panStatus);
        panStatus.setLayout(panStatusLayout);
        panStatusLayout.setHorizontalGroup(
            panStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        panStatusLayout.setVerticalGroup(
            panStatusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerLocation(180);
        jSplitPane1.setAutoscrolls(true);

        jSplitPane2.setBorder(null);
        jSplitPane2.setDividerLocation(350);
        jSplitPane2.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        javax.swing.tree.DefaultMutableTreeNode treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("General");
        treeNode1.add(treeNode2);
        treeNode2 = new javax.swing.tree.DefaultMutableTreeNode("Graphs");
        javax.swing.tree.DefaultMutableTreeNode treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("MapReduce");
        javax.swing.tree.DefaultMutableTreeNode treeNode4 = new javax.swing.tree.DefaultMutableTreeNode("Overview");
        treeNode3.add(treeNode4);
        treeNode4 = new javax.swing.tree.DefaultMutableTreeNode("Times");
        treeNode3.add(treeNode4);
        treeNode4 = new javax.swing.tree.DefaultMutableTreeNode("Worker Numbers");
        treeNode3.add(treeNode4);
        treeNode2.add(treeNode3);
        treeNode3 = new javax.swing.tree.DefaultMutableTreeNode("JVM");
        treeNode4 = new javax.swing.tree.DefaultMutableTreeNode("Memory");
        treeNode3.add(treeNode4);
        treeNode4 = new javax.swing.tree.DefaultMutableTreeNode("Threads");
        treeNode3.add(treeNode4);
        treeNode4 = new javax.swing.tree.DefaultMutableTreeNode("CPU");
        treeNode3.add(treeNode4);
        treeNode2.add(treeNode3);
        treeNode1.add(treeNode2);
        trGraph.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        trGraph.setRootVisible(false);
        trGraph.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                trGraphMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(trGraph);

        jSplitPane2.setLeftComponent(jScrollPane1);

        jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(160, 160, 160)));

        chkDisableLogging.setText("Disable graph logging");
        chkDisableLogging.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chkDisableLoggingActionPerformed(evt);
            }
        });

        btCloseAll.setText("Close all the graphs");
        btCloseAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btCloseAllActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btCloseAll)
                    .addComponent(chkDisableLogging))
                .addContainerGap(41, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(chkDisableLogging)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btCloseAll)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane2.setRightComponent(jPanel2);

        jSplitPane1.setLeftComponent(jSplitPane2);

        tabPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabPaneMouseClicked(evt);
            }
        });
        jSplitPane1.setRightComponent(tabPane);

        lblStatus.setText("succesfully loaded");
        lblStatus.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        lblStatus.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        jLabel3.setText("Last message :");
        jLabel3.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jLabel3.setVerticalTextPosition(javax.swing.SwingConstants.TOP);

        lblMsgBox.setFont(new java.awt.Font("Tahoma", 0, 5)); // NOI18N
        lblMsgBox.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblMsgBox.setText("<html>v<br>v</html>");
        lblMsgBox.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(153, 153, 255)));
        lblMsgBox.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        lblMsgBox.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblMsgBoxMouseClicked(evt);
            }
        });

        mnuFile.setText("File");

        mnuConnect.setText("Connect...");
        mnuConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuConnectActionPerformed(evt);
            }
        });
        mnuFile.add(mnuConnect);
        mnuFile.add(jSeparator2);

        jMenuItem2.setText("Exit");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        mnuFile.add(jMenuItem2);

        jMenuBar1.add(mnuFile);

        mnuTask.setText("Task");

        mnuStartMR.setText("Start MapReduce...");
        mnuStartMR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuStartMRActionPerformed(evt);
            }
        });
        mnuTask.add(mnuStartMR);

        mnuStopMR.setText("Stop MapReduce");
        mnuStopMR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuStopMRActionPerformed(evt);
            }
        });
        mnuTask.add(mnuStopMR);
        mnuTask.add(jSeparator1);

        mnuTestMR.setText("Test configuration...");
        mnuTestMR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuTestMRActionPerformed(evt);
            }
        });
        mnuTask.add(mnuTestMR);

        jMenuBar1.add(mnuTask);

        mnuLog.setText("Actions");

        mnuSend.setText("Send command");
        mnuSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuSendActionPerformed(evt);
            }
        });
        mnuLog.add(mnuSend);
        mnuLog.add(jSeparator3);

        mnuReset.setText("Reset all graphs");
        mnuReset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuResetActionPerformed(evt);
            }
        });
        mnuLog.add(mnuReset);

        jMenuBar1.add(mnuLog);

        jMenu1.setText("Window");

        mnuChangeLook.setText("Change Look");
        jMenu1.add(mnuChangeLook);

        jMenuBar1.add(jMenu1);

        mnuHelp.setText("Help");

        mnuAbout.setText("About...");
        mnuAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuAboutActionPerformed(evt);
            }
        });
        mnuHelp.add(mnuAbout);

        jMenuBar1.add(mnuHelp);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1)
            .addComponent(panStatus, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblMsgBox, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 625, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMsgBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        jSplitPane1.getAccessibleContext().setAccessibleDescription("");
        jSplitPane1.getAccessibleContext().setAccessibleParent(this);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        formWindowClosed(null);
        System.exit(0);
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        if (mxp != null) {
            mxp.disconnect();
        }
    }//GEN-LAST:event_formWindowClosed

    private void trGraphMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_trGraphMouseClicked
        if (!trGraph.isEnabled()) return;
        String path;
        try {
        path = trGraph.getSelectionPath().getParentPath().getLastPathComponent()
                + "/" + trGraph.getSelectionPath().getLastPathComponent();
        } catch (NullPointerException exNull) {
            //means that the selection is not a child or too close to the root.
            return;
        }
        GraphComponent graph = treeOccurences.get(path);
        if (graph == null) {
            //wlog("No graph registered for " + path);
            return;
        }
        if (graph.isLoaded()) {
            //open the right tab
            for (int i = 0; i < tabPane.getTabCount(); i++) {
                if (tabPane.getTabComponentAt(i) == graph.getTabRef()) {
                    tabPane.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            //create the new graph in a new tab
            //-create new tab
            int index = addTab(graph.getName(), path, -1);
            //-set the ref to the tab panel in the graphComponent
            graph.setTabRef(tabPane.getTabComponentAt(index));
            //-add the graph to the tab pane
            addTabContent(index, graph.getChart());
            //-select the tab
            tabPane.setSelectedIndex(index);
            //schedule a default refresh IF there exists a connection
            if (mxp.isConnected()) graph.scheduleRefresh(-1);
        }
        if (evt != null && evt.getButton() == 3) {
            popup.setActionSource(graph);
            popup.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }//GEN-LAST:event_trGraphMouseClicked

    private void mnuConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuConnectActionPerformed
        //load the dialog
        ConnectDialog dialog = new ConnectDialog(this, true);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        //-----------------------wait------------------------
        if (dialog.isCanceled) return;
        connectViaProxy(dialog.address, dialog.port); //the fields are guaranteed to be valid
    }//GEN-LAST:event_mnuConnectActionPerformed

    private void lblMsgBoxMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblMsgBoxMouseClicked
        msgBox.setVisible(true);
    }//GEN-LAST:event_lblMsgBoxMouseClicked

    private void btCloseAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btCloseAllActionPerformed
        closeAllGraphs();
        if (!isConnected) {
            trGraph.setEnabled(false);
        }
    }//GEN-LAST:event_btCloseAllActionPerformed

    private void mnuStartMRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuStartMRActionPerformed
        StartDialog dialog = new StartDialog(this, true, mxp, overlist);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }//GEN-LAST:event_mnuStartMRActionPerformed

    private void mnuTestMRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuTestMRActionPerformed
        TestDialog dialog = new TestDialog(this, true, mxp);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }//GEN-LAST:event_mnuTestMRActionPerformed

    private void mnuStopMRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuStopMRActionPerformed
        mxp.MRCtrl_sendCommand("stop");
        wlog("Job stopped.");
    }//GEN-LAST:event_mnuStopMRActionPerformed

    private void chkDisableLoggingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkDisableLoggingActionPerformed
        if (chkDisableLogging.isSelected()) {
            cancelAllRefresh();
        } else {
            reactivateAllRefresh();
        }
    }//GEN-LAST:event_chkDisableLoggingActionPerformed

    private void mnuSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuSendActionPerformed
        String res = (String) JOptionPane.showInputDialog(this, "Type in the command",
                "Send command", JOptionPane.PLAIN_MESSAGE, null,
                null, null);
        if (res == null || res.isEmpty()) return;
        mxp.MRCtrl_sendCommand(res);
    }//GEN-LAST:event_mnuSendActionPerformed

    private void tabPaneMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabPaneMouseClicked
        
    }//GEN-LAST:event_tabPaneMouseClicked

    private void mnuResetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuResetActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_mnuResetActionPerformed

    private void mnuAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuAboutActionPerformed
        new AboutDialog(this, true).setVisible(true);
    }//GEN-LAST:event_mnuAboutActionPerformed

    private void setLook(String name) {
        try {
            UIManager.setLookAndFeel(name);
            SwingUtilities.updateComponentTreeUI(this);
            this.pack();
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            wlog("Cannot change to this theme");
        }
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MainGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainGui.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new MainGui().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btCloseAll;
    private javax.swing.JCheckBox chkDisableLogging;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JLabel lblMsgBox;
    private javax.swing.JLabel lblStatus;
    private javax.swing.JMenuItem mnuAbout;
    private javax.swing.JMenu mnuChangeLook;
    private javax.swing.JMenuItem mnuConnect;
    private javax.swing.JMenu mnuFile;
    private javax.swing.JMenu mnuHelp;
    private javax.swing.JMenu mnuLog;
    private javax.swing.JMenuItem mnuReset;
    private javax.swing.JMenuItem mnuSend;
    private javax.swing.JMenuItem mnuStartMR;
    private javax.swing.JMenuItem mnuStopMR;
    private javax.swing.JMenu mnuTask;
    private javax.swing.JMenuItem mnuTestMR;
    private javax.swing.JPanel panStatus;
    private javax.swing.JTabbedPane tabPane;
    private javax.swing.JTree trGraph;
    // End of variables declaration//GEN-END:variables
}
/**
 * @see http://docs.oracle.com/javase/tutorial/displayCode.html?code=http://docs.oracle.com/javase/tutorial/uiswing/examples/components/TabComponentsDemoProject/src/components/ButtonTabComponent.java
 */
class TabButton extends JButton implements ActionListener {
    
    private JTabbedPane pane;
    private JPanel container;
    private final HashMap<String, GraphComponent> treeOccurences;
    private final MouseListener buttonMouseListener = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(true);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            Component component = e.getComponent();
            if (component instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) component;
                button.setBorderPainted(false);
            }
        }
    };

    public TabButton(JTabbedPane pane, JPanel container, HashMap<String
            , GraphComponent> treeOccurences) {
        this.pane = pane;
        this.container = container;
        this.treeOccurences = treeOccurences;
        int size = 17;
        setPreferredSize(new Dimension(size, size));
        setToolTipText("close this tab");
        //Make the button looks the same for all Laf's
        setUI(new BasicButtonUI());
        //Make it transparent
        setContentAreaFilled(false);
        //No need to be focusable
        setFocusable(false);
        setBorder(BorderFactory.createEtchedBorder());
        setBorderPainted(false);
        //Making nice rollover effect
        //we use the same listener for all buttons
        addMouseListener(buttonMouseListener);
        setRolloverEnabled(true);
        //Close the proper tab by clicking the button
        addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int i = pane.indexOfTabComponent(container);
        if (i != -1) {
            //the name store the path in the tree for the graph
            GraphComponent graph = treeOccurences.get(container.getName());
            graph.cancelRefresh();
            graph.setUnloaded();
            pane.remove(i);
        }
    }
    //we don't want to update UI for this button
    @Override
    public void updateUI() {
    }

    //paint the cross
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        //shift the image for pressed buttons
        if (getModel().isPressed()) {
            g2.translate(1, 1);
        }
        g2.setStroke(new BasicStroke(2));
        g2.setColor(Color.BLACK);
        if (getModel().isRollover()) {
            g2.setColor(Color.MAGENTA);
        }
        int delta = 6;
        g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
        g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
        g2.dispose();
    }
}

class PopUpTab extends JPopupMenu {
    ActionListener listener = new ActionListener() {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedGraph == null) return;
            String src = ((JMenuItem)e.getSource()).getText();
            switch (src) {
                case "stop":
                    selectedGraph.cancelRefresh();
                    break;
                case "250ms":
                    selectedGraph.changeRefreshInterval(250);
                    break;
                case "500ms":
                    selectedGraph.changeRefreshInterval(500);
                    break;
                case "1sec":
                    selectedGraph.changeRefreshInterval(1000);
                    break;
                case "2sec":
                    selectedGraph.changeRefreshInterval(2000);
                    break;
                case "4sec":
                    selectedGraph.changeRefreshInterval(4000);
                    break;
                case "6sec":
                    selectedGraph.changeRefreshInterval(6000);
                    break;
                case "10sec":
                    selectedGraph.changeRefreshInterval(10000);
                    break;
            }
            selectedGraph = null;
        }
    };
    private GraphComponent selectedGraph;
    
    public PopUpTab() {
        JMenuItem item;
        item = new JMenuItem("250ms");
        item.addActionListener(listener);
        add(item);
        item = new JMenuItem("500ms");
        item.addActionListener(listener);
        add(item);
        item = new JMenuItem("1sec");
        item.addActionListener(listener);
        add(item);
        item = new JMenuItem("2sec");
        item.addActionListener(listener);
        add(item);
        item = new JMenuItem("4sec");
        item.addActionListener(listener);
        add(item);
        item = new JMenuItem("6sec");
        item.addActionListener(listener);
        add(item);
        item = new JMenuItem("10sec");
        item.addActionListener(listener);
        add(item);
        item = new JMenuItem("stop");
        item.addActionListener(listener);
        add(item);
    }
    
    public void setActionSource(GraphComponent graph) {
        selectedGraph = graph;
    }
}