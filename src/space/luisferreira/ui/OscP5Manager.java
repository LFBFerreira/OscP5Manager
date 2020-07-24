package space.luisferreira.ui;

import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.*;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class OscP5Manager implements SchedulerInterface {

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

    // ================================================================

    /**
     * @param oscPort
     * @param parent
     */
    public OscP5Manager(int oscPort, PApplet parent) {
        this.parent = parent;
        this.portNumer = oscPort;
//        this.printEvents = printEvents;

        parent.registerMethod("dispose", this);
        parent.registerMethod("pre", this);

        osc = new OscP5(this, oscPort);

        executor = Executors.newSingleThreadExecutor();
    }

    // ================================================================

    /**
     * Processing's dispose function, frees all resources
     */
    public void dispose() {
        osc.dispose();
    }

    public void pre() {
        runTasks();
    }

    /**
     * @param print
     */
    public void setVerbosity(boolean print) {
        printEvents = print;
    }

    /**
     * @param filter
     */
    public void setFilterEvents(boolean filter) {
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
     * @return
     */
    public String getServerAddress() {
        return osc.ip();
    }

    /**
     * @return
     */
    public int getServerPort() {
        return portNumer;
    }

    /**
     * @return
     */
    public boolean isConnected() {
        return OscP5.ON && !OscP5.OFF;
    }

    // ================================================================


    // SchedulerInterface

    /**
     * @param inputTask
     */
    @Override
    public void add(InputTask inputTask) {
        if (!isSchedulerLocked()) {
            tasks.add(inputTask);
        } else {
            System.out.println("Cannot add task to the scheduler right now");
        }
    }

    // ================================================================

    // Helpers

    /**
     *
     */
    private void runTasks() {
        if (tasks.isEmpty()) {
            return;
        }

        List<InputTask> localTasks;


        lockScheduler();

        if (filterRepeatedEvents) {
            localTasks = filterRepeatedEvents(tasks);
            //System.out.println("Filtered " + (tasks.size() - localTasks.size()));
        } else {
            // copy the list
            localTasks = new ArrayList<>(tasks);
        }

        tasks.clear();
        releaseScheduler();

        //System.out.println("Executing " + localTasks.size() + " tasks");
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
     *
     */
    private void lockScheduler() {
        schedulerLocked = true;
    }

    /**
     *
     */
    private void releaseScheduler() {
        schedulerLocked = false;
    }

    /**
     * @return
     */
    private boolean isSchedulerLocked() {
        return schedulerLocked;
    }

    /**
     * @param tasks
     * @return
     */
    private List<InputTask> filterRepeatedEvents(List<InputTask> tasks) {
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


}

