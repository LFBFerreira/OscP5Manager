package space.luisferreira.ui.test;

import space.luisferreira.ui.*;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import space.luisferreira.ui.input.InputEvent;
import space.luisferreira.ui.input.InputListennerInterface;

/**
 * Simple example of how to setup OSCAssistant.
 * Use any layout in the TouchOSC app to trigger the sketch, after configuring it to the right server and port.
 * When a new event is triggered the background color changes randomly and the event description is sent to the console
 */
public class HelloWorld extends PApplet {
    OSCAssistant assistant;

    int backgroundRed = 0;
    int backgroundGreen = 0;
    int backgroundBlue = 0;

    public void settings() {
        size(500, 300, PConstants.P2D);
    }

    public void setup() {
        assistant = new OSCAssistant(this);

        // register the method that will be triggered when new OSC commands are received
        assistant.registerListener(oscListenner);
        assistant.printEvents(true);

        // typical port number
        assistant.start(8000);

        PFont font = createFont("", 20);
        textFont(font);
    }

    public void draw() {
        // mix rgb channels
        int backgroundColor = (255 << 24) |
                (backgroundRed << 16) |
                (backgroundGreen << 8) |
                backgroundBlue;

        background(backgroundColor);

        String text = String.format("%s\naddress %s @ %s",
                assistant.isOn() ? "Server active" : "Server innactive",
                assistant.getServerAddress(),
                assistant.getServerPort());

        fill(255);
        text(text, 20, 50);
    }


    InputListennerInterface oscListenner = new InputListennerInterface() {
        @Override
        public void newEvent(final InputEvent input) {
            println(input.isPressed() ? "Button pressed" : "Button released");

            // any time an event is triggered, randomize the color
            backgroundRed = (int) random(0, 255);
            backgroundGreen = (int) random(0, 255);
            backgroundBlue = (int) random(0, 255);
        }
    };
}
