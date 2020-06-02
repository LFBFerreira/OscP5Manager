package luis.ferreira.libraries.ui;

import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.*;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class OscP5Manager implements SchedulerInterface{

    public final static String VERSION = "1.1";
    private PApplet parent;
    private List<InputListennerInterface> listeners = new LinkedList<>();
    private OscP5 osc;
    private int portNumer = 0;
    private boolean printEvents = true;
    private List<InputTask> tasks = new LinkedList<>();
    private ExecutorService executor;
    private boolean filterRepeatedEvents = true;

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

    public void pre()
    {
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

    /**
     * @param inputTask
     */
    public void scheduleAction(InputTask inputTask) {
        tasks.add(inputTask);
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

        if (filterRepeatedEvents) {
            localTasks = filterRepeatedEvents(tasks);
            //System.out.println("Filtered " + (tasks.size() - localTasks.size()));
        } else {
            localTasks = new LinkedList<>(tasks);
        }

        //System.out.println("Executing " + localTasks.size() + " tasks");

        for (InputTask task : localTasks) {
            try {
                Future result = executor.submit(task);
                result.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        tasks.clear();
    }

    private List<InputTask> filterRepeatedEvents(List<InputTask> tasks) {
        List<InputTask> filteredTasks = new LinkedList<>();

        // invert the taks to give priority to the most recent events
        Collections.reverse(tasks);


        tasks.stream().forEach(t -> {
            AtomicBoolean valid = new AtomicBoolean(true);

            // add unique tasks to the list
            filteredTasks.forEach(ft -> valid.set(valid.get() && !ft.id.equals(t.id)));

            if (valid.get()) {
//                System.out.println(String.format("Adding %s, %s", t.id, t.values));
                filteredTasks.add(t);
            } else {
//                System.out.println(String.format("Not %s, %s", t.id, t.values));
            }
        });

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

