package luisf.interfaces;

import oscP5.OscMessage;
import oscP5.OscP5;
import processing.core.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class OscP5Manager {

	public final static String VERSION = "0.9";

	private PApplet parent;

    private List<InputListennerInterface> listeners = new LinkedList<>();

    private OscP5 osc;

    private int portNumer = 0;

    /**
     * a Constructor, usually called in the setup() method in your sketch to
     * initialize and start the Library.
     *
     * @param parent the parent PApplet
     */
    public OscP5Manager(int oscPort, PApplet parent) {
        this.parent = parent;
        this.portNumer = oscPort;

        parent.registerMethod("dispose", this);

        osc = new OscP5(this, oscPort);
    }

    // ================================================================

    /**
     * Processing's dispose function, frees all resources
     */
    public void dispose() {
        osc.dispose();
    }

    // ================================================================

    /**
     * Registers a listenner
     *
     * @param listener
     */
    public void registerListeners(InputListennerInterface listener) {
        this.listeners.add(listener);
    }

    /**
     * Registers multiple listenners
     *
     * @param listeners
     */
    public void registerListeners(InputListennerInterface[] listeners) {
        this.listeners = Arrays.asList(listeners);
    }

    public String getServerAddress()
    {
        return osc.ip();
    }

    public int getServerPort()
    {
        return portNumer;
    }

    public boolean isConnected()
    {
        return osc.ON == true && OscP5.OFF == false;
    }

    // ================================================================

    // Helpers

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
            System.out.println("There are no listenners");
            return;
        }

        if (event == null) {
            System.out.println("The event is null!");
            return;
        }

        System.out.println(String.format("Event %s: %s - %f", event.inputMethod, event.id, event.getAsFloat()));

        listeners.forEach(l -> l.newEvent(event));
    }
}

