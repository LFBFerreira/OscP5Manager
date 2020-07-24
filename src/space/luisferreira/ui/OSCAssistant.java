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

    // ================================================================

    /**
     * Constructor
     * @param oscPort
     * @param parent
     */
    public OSCAssistant(int oscPort, PApplet parent) {
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
     * Processing's dispose function, called when program is exited
     */
    public void dispose() {
        osc.dispose();
    }

    /**
     * Processing's Pre function, called just before draw
     */
    public void pre() {
        // execute all tasks received since the last Draw
        runTasks();
    }

    /**
     * Show or hide console notifications when a new event is triggered
     * @param print
     */
    public void printEvents(boolean print) {
        printEvents = print;
    }

    /**
     * Turn On or Off the option to filter events with the same name. If On, queued events with the same name as a recent one will be ignored
     * This filters multiple events from the same control, using only the most recent
     * @param filter
     */
    public void filterRepeatedEvents(boolean filter) {
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
     * Gets the OSC server address
     * @return
     */
    public String getServerAddress() {
        return osc.ip();
    }

    /**
     * Gets the OSC server port
     * @return
     */
    public int getServerPort() {
        return portNumer;
    }

    /**
     * Is the server on?
     * @return
     */
    public boolean isOn() {
        return OscP5.ON && !OscP5.OFF;
    }

    // ================================================================


    // SchedulerInterface

    /**
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

        localTasks = (filterRepeatedEvents) ? filterRepeatedEvents(tasks) : new ArrayList<>(tasks);

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
     *  Locks access to the scheduler
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
     * @return
     */
    private boolean isSchedulerLocked() {
        return schedulerLocked;
    }

    /**
     * Filters repeated events in a list and returns only unique entries
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

