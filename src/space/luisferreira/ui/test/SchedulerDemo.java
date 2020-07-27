package space.luisferreira.ui.test;

import space.luisferreira.ui.*;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import space.luisferreira.ui.input.InputEvent;
import space.luisferreira.ui.input.InputListennerInterface;
import space.luisferreira.ui.input.InputTask;

/**
 * This demo shows how to use the scheduling feature of OSCAssistant.
 * OSC commands can be received at any time, however some Processing commands can only be used while the sketch is
 * in Draw. Also, sketches with low FPS sometimes create situations where the value of a variable in the begging
 * of Draw is not the same as in the end because it was changed in the "newEvent" method
 * To avoid these two issues, tasks can be scheduled to execute all just before the Draw method, keeping the state of
 * the sketch consistent
 * <p>
 * Another optional feature from the scheduler is that it can filter tasks from the same control, since most of the time
 * what matter is the most recent value assigned to a variable, not the changes it went through.
 * <p>
 * Try turning this option on and off with any keyboard key and see the difference in the ammount of prints in the Console,
 * while playing with the faders in the "Simple" TouchOSC layout for example.
 */
public class SchedulerDemo extends PApplet {
    OSCAssistant assistant;

    int backgroundRed = 0;
    int backgroundGreen = 0;
    int backgroundBlue = 0;

    boolean filterTasks = true;


    public void settings() {
        size(500, 300, PConstants.P2D);
    }

    public void setup() {
        // force the sketch to run at slow speed, while the OSC commands keep arriving at normal speed
        // for demo purposes
        frameRate(2);

        assistant = new OSCAssistant(this);

        assistant.registerListener(oscListenner);
        assistant.printEvents(false);

        // typical port number
        assistant.start(8000);

        PFont font = createFont("", 20);
        textFont(font);
    }

    public void draw() {
        // sets the filtering mode for the next frame
        assistant.setTasksFilter(filterTasks);

        int backgroundColor = (255 << 24) |
                (backgroundRed << 16) |
                (backgroundGreen << 8) |
                backgroundBlue;

        String text = assistant.isOn() ? "Running!\n" + assistant.getServerAddress() + "\n" + assistant.getServerPort() :
                "Not running";

        background(backgroundColor);
        fill(255);
        text(text, 20, 50);
    }

    // turn filtering on and off with any keyboard key
    public void keyPressed() {
        filterTasks = !filterTasks;
        println("Tasks filtering is " + (filterTasks ? "On" : "Off"));
    }

    InputListennerInterface oscListenner = new InputListennerInterface() {
        @Override
        public void newEvent(final InputEvent input) {
            if (input.isPrefix("toggle")) {
                // task for toggles
                assistant.addTask(new InputTask(input) {
                    public Void call() {
                        println(String.format("Toggle %s, page %s, is %s",
                                input.getName(),
                                input.getPage(),
                                input.asBoolean() ? "On" : "Off"));

                        backgroundRed = (int) random(255);

                        return null;
                    }
                });
            } else if (input.isPrefix("push")) {
                // task for push buttons
                assistant.addTask(new InputTask(input) {
                    public Void call() {
                        println(String.format("Push %s, page %s, is %s",
                                input.getName(),
                                input.getPage(),
                                input.asBoolean() ? "Down" : "Up"));

                        backgroundGreen = (int) random(255);

                        return null;
                    }
                });
            } else if (input.isPrefix("fader")) {
                // task for faders
                assistant.addTask(new InputTask(input) {
                    public Void call() {
                        println(String.format("Fader %s, page %s, is %s",
                                input.getName(),
                                input.getPage(),
                                input.asInt(0, 100)));

                        backgroundBlue = (int) random(255);

                        return null;
                    }
                });
            }
        }
    };
}
