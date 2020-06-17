package luis.ferreira.libraries.ui.test;

import luis.ferreira.libraries.ui.*;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;

public class TasksTest extends PApplet {
    private OscP5Manager man;
    private SchedulerInterface scheduler;
    private int backgroundRed = 0;
    private int backgroundGreen = 0;
    private int backgroundBlue = 0;

    // ================================================================

    /**
     *
     */
    public TasksTest() {
    }

    // ================================================================


    /**
     * Settings Method
     */
    public void settings() {
        size(300, 200, PConstants.P2D);
    }

    /**
     * Setup Method
     */
    public void setup() {
        // reduce framerate to increase the chance of multiple commands per frame
        //frameRate(2);

        man = new OscP5Manager(8000, this);
        man.registerListener(oscListenner);
        scheduler = man;

        PFont font = createFont("", 22);
        textFont(font);
    }

    public void draw() {
        //man.runTasks();

        int backgroundColor = (255 << 24) |
                (backgroundRed << 16) |
                (backgroundGreen << 8) |
                backgroundBlue;

        String text = man.isConnected() ? "Running!\n" + man.getServerAddress() + "\n" + man.getServerPort() :
                "Not running";

        background(backgroundColor);
        fill(255);
        text(text, 20, 50);
    }

    // ================================================================

    private InputListennerInterface oscListenner = new InputListennerInterface() {
        @Override
        public void newEvent(InputEvent input) {
            scheduler.add(new InputTask(input) {
                public Void call(){
                    backgroundRed = (int) random(255);
                    backgroundGreen = (int) random(255);
                    backgroundBlue = (int) random(255);
                    return null;
                }
            });

            if (input.isName("pan_pad")) {
                scheduler.add(new InputTask(input) {
                    public Void call() {
                        System.out.println(input.getAsOffsetXY());
                        return  null;
                    }
                });
            }
        }
    };
}
