package space.luisferreira.ui;

import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.*;
import space.luisferreira.ui.input.InputEvent;
import space.luisferreira.ui.input.InputListennerInterface;
import space.luisferreira.ui.input.InputTask;
import space.luisferreira.ui.input.SchedulerInterface;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 *
 */
public class OSCAssistant implements SchedulerInterface {

    public final static String VERSION = "1.1";

    private PApplet parent;
    private List<InputListennerInterface> listeners = new ArrayList<>();
    private OscP5 osc;
    private int portNumer = 0;
    private boolean printEvents = true;
    private List<InputTask> tasks = new ArrayList<>();
    private ExecutorService executor;
    private boolean filterRepeatedEvents = true;
    private boolean schedulerLocked = false;
    private String serverAddress = "";

    // ================================================================

    /**
     * Constructor
     *
     * @param parent
     */
    public OSCAssistant(PApplet parent) {
        this.parent = parent;

        parent.registerMethod("dispose", this);
        parent.registerMethod("pre", this);

        executor = Executors.newSingleThreadExecutor();
    }


    // ================================================================

    /**
     * Dispose function, called when program is exited
     */
    public void dispose() {
        if (osc != null) {
            osc.dispose();
        }
    }

    /**
     * Processing's Pre function, called just before draw
     */
    public void pre() {
        // execute all tasks received since the last Draw
        runTasks();
    }

    /**
     * Start the server in the given port
     *
     * @param oscPort port number
     */
    public void start(int oscPort) {
        System.out.println("Starting OSC server");
        this.portNumer = oscPort;
        osc = new OscP5(this, portNumer);

        serverAddress = getInterfacesAddresses(true);
    }

    /**
     * Show or hide console notifications when a new event is received
     *
     * @param print
     */
    public void printEvents(boolean print) {
        printEvents = print;
    }

    /**
     * Turn On or Off the option to filter events with the same name.
     * If On, only the last event from a given control's task is executed, the rest is discarderd
     * If Off, all tasks are executed
     *
     * @param filter
     */
    public void setTasksFilter(boolean filter) {
        filterRepeatedEvents = filter;
    }

    /**
     * Registers a listenner
     *
     * @param listener
     */
    public void registerListener(InputListennerInterface listener) {
        this.listeners.add(listener);
    }

    /**
     * Registers multiple listenners
     *
     * @param listeners
     */
    public void registerListeners(List<InputListennerInterface> listeners) {
        this.listeners.addAll(listeners);
    }

    /**
     *
     * @return
     */
    public String getServerAddress() {
        return  serverAddress;
    }

    /**
     * Get the OSC server address
     *
     * @return
     */
    private String getInterfacesAddresses(boolean ignoreVirtual) {
        Map<String, String> interfaces = getIPAddresses(ignoreVirtual);

        if (interfaces.size() > 1) {
            System.out.println("Network interfaces detected:");
            for (Map.Entry<String, String> entry : interfaces.entrySet()) {
                System.out.println(entry.getKey() + " - " + entry.getValue());
            }
        }

        // return the first interface's IP
        return interfaces.entrySet().iterator().next().getValue();

        //        return osc.ip();
    }

    /**
     * Gets the OSC server port
     *
     * @return
     */
    public int getServerPort() {
        return portNumer;
    }

    /**
     * Is the server on?
     *
     * @return
     */
    public boolean isOn() {
        return osc.ON && !osc.OFF;
    }


    // ================================================================


    // SchedulerInterface

    /**
     * Add a task to be executed later
     *
     * @param inputTask
     */
    @Override
    public void addTask(InputTask inputTask) {
        if (!isSchedulerLocked()) {
            tasks.add(inputTask);
        } else {
            System.out.println("Cannot add task to the scheduler right now");
        }
    }

    // ================================================================

    // Helpers

    /**
     * Runs all queued tasks
     */
    private void runTasks() {
        if (tasks.isEmpty()) {
            return;
        }

        List<InputTask> localTasks;

        // lock scheduler to avoid concurrency problems
        lockScheduler();

        localTasks = (filterRepeatedEvents) ? setTasksFilter(tasks) : new ArrayList<>(tasks);

        tasks.clear();
        releaseScheduler();

        //System.out.println("Executing " + localTasks.size() + " tasks");

        // execute all tasks
        Iterator<InputTask> iter = localTasks.iterator();
        while (iter.hasNext()) {
            InputTask task = iter.next();

            try {
                Future result = executor.submit(task);
                result.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Locks access to the scheduler
     */
    private void lockScheduler() {
        schedulerLocked = true;
    }

    /**
     * Releases access to the scheduler
     */
    private void releaseScheduler() {
        schedulerLocked = false;
    }

    /**
     * Returns true if the scheduler is locked, false if its not
     *
     * @return
     */
    private boolean isSchedulerLocked() {
        return schedulerLocked;
    }

    /**
     * Filters repeated events in a list and returns only unique entries
     *
     * @param tasks
     * @return
     */
    private List<InputTask> setTasksFilter(List<InputTask> tasks) {
        List<InputTask> filteredTasks = new LinkedList<>();

        // invert the taks to give priority to the most recent events
        Collections.reverse(tasks);

        Iterator<InputTask> iter = tasks.iterator();

        while (iter.hasNext()) {
            InputTask task = iter.next();

            AtomicBoolean isUnique = new AtomicBoolean(true);

            // only add unique tasks to the list
            filteredTasks.forEach(ft -> isUnique.set(isUnique.get() && !ft.id.equals(task.id)));

            if (isUnique.get()) {
                filteredTasks.add(task);
            }
        }

        return filteredTasks;
    }

    /**
     * OSCp5 callback method for events
     *
     * @param oscMessage
     */
    private void oscEvent(OscMessage oscMessage) {
        InputEvent event = new InputEvent(oscMessage);
        announceEvent(event);
    }


    /**
     * Announces an OSC event to all listenners
     *
     * @param event
     */
    private void announceEvent(InputEvent event) {
        if (listeners == null) {
            System.err.println("There are no listenners");
            return;
        }

        if (event == null) {
            System.err.println("The event is null!");
            return;
        }

        // print event info
        if (printEvents) {
            System.out.println(String.format("Event %s: %s - %s", event.inputMethod, event.id, event.getValues()));
        }

        // send event to all listenners
        listeners.forEach(l -> l.newEvent(event));
    }


    /**
     *
     */
    private Map<String, String> getIPAddresses(boolean ignoreVirtual) {
        Map<String, String> interfaceIpMap = new HashMap<>();

        Enumeration<NetworkInterface> net = null;
        try { // get all interfaces; ethernet, wifi, virtual... etc
            net = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

        if (net == null) {
            throw new RuntimeException("No network interfaces found.");
        }

        while (net.hasMoreElements()) {
            NetworkInterface element = net.nextElement();
            try {
                if (element.isVirtual() || element.isLoopback()) {
                    // discard virtual and loopback interface (127.0.0.1)
                    continue;
                }

                // rest are either Wifi or ethernet interfaces
                // loop through and print the IPs
                Enumeration<InetAddress> addresses = element.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress ip = addresses.nextElement();
                    if (ip instanceof Inet4Address) {
                        if (ip.isSiteLocalAddress()) {
                            if (!ignoreVirtual || (ignoreVirtual &&
                                    !Pattern.compile(Pattern.quote("virtual"), Pattern.CASE_INSENSITIVE).matcher(element.getDisplayName()).find())) {
                                interfaceIpMap.put(element.getDisplayName(), ip.getHostAddress());
                            }
                        }
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }

        return interfaceIpMap;
    } // listIPAddresses() ends
}

