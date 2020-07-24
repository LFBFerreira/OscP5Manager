package space.luisferreira.ui.test;

import space.luisferreira.ui.*;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;

public class TasksTest extends PApplet {
    OSCAssistant man;
    SchedulerInterface scheduler;

    int backgroundRed = 0;
    int backgroundGreen = 0;
    int backgroundBlue = 0;


    public void settings() {
        size(300, 200, PConstants.P2D);
    }

    public void setup() {
        man = new OSCAssistant(8000, this);
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

        String text = man.isOn() ? "Running!\n" + man.getServerAddress() + "\n" + man.getServerPort() :
                "Not running";

        background(backgroundColor);
        fill(255);
        text(text, 20, 50);
    }

    // ================================================================

    private InputListennerInterface oscListenner = new InputListennerInterface() {
        @Override
        public void newEvent(InputEvent input) {
            scheduler.addTask(new InputTask(input) {
                public Void call(){
                    backgroundRed = (int) random(255);
                    backgroundGreen = (int) random(255);
                    backgroundBlue = (int) random(255);
                    return null;
                }
            });

            if (input.isName("pan_pad")) {
                scheduler.addTask(new InputTask(input) {
                    public Void call() {
                        System.out.println(input.getAsOffsetXY());
                        return  null;
                    }
                });
            }
        }
    };
}
