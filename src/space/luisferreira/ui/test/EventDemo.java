package space.luisferreira.ui.test;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PVector;
import space.luisferreira.ui.OSCAssistant;
import space.luisferreira.ui.input.*;

/**
 * This demo shows how to use the values inside the events.
 * Use the "Simple" layout in the TouchOSC app to trigger the sketch, after configuring it to the right server and port.
 * Play with the faders, buttons and XY pad to trigger different actions, and keep looking at the console to see the event's description
 */
public class EventDemo extends PApplet {
    OSCAssistant assistant;

    int rectangleWidth = 40;
    int rectangleHeight = 100;

    int circleColor = 0;
    int squareColor = 0;
    int opacity = 255;

    public void settings() {
        size(500, 300, PConstants.P2D);
    }

    public void setup() {
        assistant = new OSCAssistant(this);

        assistant.registerListener(oscListenner);
        assistant.printEvents(true);

        // typical port number
        assistant.start(8000);

        PFont font = createFont("", 20);
        textFont(font);
    }

    public void draw() {
        background(255);

        String text = String.format("%s\naddress %s @ %s",
                assistant.isOn() ? "Server active" : "Server innactive",
                assistant.getServerAddress(),
                assistant.getServerPort());

        fill(0);
        text(text, 20, 50);

        fill(circleColor, opacity);
        circle(width / 4, height / 2, 100);

        fill(squareColor, opacity);
        rectMode(PConstants.CENTER);
        rect((3 * width / 4), height / 2, 100, 100);

        fill(0);
        rectMode(PConstants.CENTER);
        rect(width / 2, height / 2, rectangleWidth, rectangleHeight);
    }


    // create a new listenner for incoming events
    InputListennerInterface oscListenner = new InputListennerInterface() {
        @Override
        public void newEvent(final InputEvent input) {
            // match the name "fader" so it reacts to all controls starting with that word
            if (input.isPrefix("fader")) {
                // match the entire name
                if (input.isName("fader5")) {
                    // get the value as an Int, mapped to [0, 255]
                    opacity = input.asInt(0, 255);
                } else {
                    // or get the value as a float [0, 1]
                    circleColor = color(random(255), random(255), input.asFloat() * 255);
                }
            }

            // react ao all push and toggle buttons
            if (input.isPrefix("push") || input.isPrefix("toggle")) {
                if (input.isPressed()) {
                    squareColor = 0;
                } else {
                    squareColor = color(random(255), random(255), input.asInt(0, 255));
                }
            }

            // react to the XY pad
            if (input.isPrefix("xy")) {
                // get the coordinates from the touch input, [0-1 , 0-1]
                PVector coordinate = input.asXY();

                // change the geometry of the middle rectangle according to the input
                rectangleWidth = (int) map(coordinate.x, 0, 1, 1, width);
                rectangleHeight = (int) map(coordinate.y, 0, 1, 1, height);
            }


            // this is how you can get the value from an input
            // all methods can also map the received value from a MIN to a MAX
//            input.asFloat();
//            input.asInt();
//            input.asXY();
//            input.asXYCentered();
//            input.asBoolean();
        }
    };
}
