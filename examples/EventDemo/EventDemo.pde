/**
 * This demo shows how to use the values inside the events.
 * Use the "Simple" layout in the TouchOSC app to trigger the sketch, after configuring it to the right server and port.
 * Play with the faders, buttons and XY pad to trigger different actions, and keep looking at the console to see the event's description
 */

import space.luisferreira.ui.*;
import oscP5.OscP5;

OSCAssistant assistant;

int rectangleWidth = 40;
int rectangleHeight = 100;

int circleColor = 0;
int squareColor = 0;
int opacity = 255;

void settings() {
  size(500, 300, PConstants.P2D);
}

void setup() {
  assistant = new OSCAssistant(8000, this);

  assistant.registerListener(oscListenner);
  assistant.printEvents(true);

  PFont font = createFont("", 20);
  textFont(font);
}

void draw() {
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


InputListennerInterface oscListenner = new InputListennerInterface() {
  @Override
    public void newEvent(InputEvent input) {

    // partially match the name
    if (input.isPrefix("fader")) {
      // match the entire name
      if (input.isName("fader5")) {
        // get the value as an Int, mapped from  [0, 1] to [0, 255] directly
        opacity = input.getAsInt(0, 255);
      } else {
        // or get the value as a float between 0 and 1
        circleColor = color(random(255), random(255), input.getAsFloat()*255 );
      }
    }

    if (input.isPrefix("push")) {
      // push and toggle buttons can easily be tested with isPressed
      if (input.isPressed()) {
        squareColor = 0;
      } else {
        squareColor = color(random(255), random(255), input.getAsInt(0, 255));
      }
    }

    if (input.isPrefix("xy")) {
      // the XY pad event can be requested as am X,Y coordinate
      PVector coordinate = input.getAsXY();

      rectangleWidth = (int)map(coordinate.x, 0, 1, 1, width);
      rectangleHeight = (int)map(coordinate.y, 0, 1, 1, height);
    }
  }
};
