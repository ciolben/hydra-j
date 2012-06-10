package epfl.project.controlinterface.console;

/**
 *
 * @author Loic
 */
public class StopCommand extends Command {

    private final String name = "stop";
    private final String description = "stop the mapreduce task "
            + "(can take some time)";

    @Override
    public boolean executeCommand(final String[] args, final Console console) {
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        if (!isName(args[0])) {
            return false;
        }
        boolean tryClose = console.tryClose;
//        if (!tryClose && console.master.getState() 
//                                != Thread.State.TERMINATED) {
        if (!tryClose) {
            tryClose = true;
            Thread stopT = new Thread(new Runnable() {

                @Override
                public void run() {
                    console.directPrint("closing master...");
                    console.master.close();
                    boolean hasQuit = false;
                    while (console.master.getState()
                             != Thread.State.TERMINATED && console.master.getState() != Thread.State.NEW  ) {
                        try {
                            //lower the load of the cpu
                            try {
                                synchronized (this) {
                                    wait(100);
                                }
                            } catch (IllegalMonitorStateException e) {
                                e.printStackTrace();
                                hasQuit = true;
                            } finally {
                                if (hasQuit) {
                                    break;
                                }
                            }
                        } catch (InterruptedException e) {
                        }
                    }
                    console.print("master closed.");
                    console.hasStarted = false;
                    out:
                    {
                        if (args.length >= 2) {
                            if (args[1].equals("notclone")) {
                                break out;
                            }
                        }
                        console.master = console.master.clone(); //new reference
                    }
                }
            });
            stopT.setPriority(Thread.MAX_PRIORITY);
            stopT.start();
            if (args.length > 2 && args[2].equals("join")) {
                try {
                    stopT.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            console.directPrint("nodes : nothing to close.");
        }
        return true;
    }

    @Override
    public boolean isName(String name) {
        return this.name.equals(name);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescriptionLine() {
        return description;
    }
}
