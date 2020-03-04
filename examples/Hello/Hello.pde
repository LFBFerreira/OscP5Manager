import luisf.interfaces.*;
import oscP5.OscP5;

OscP5Manager man;

void setup() {
    size(200, 200, PConstants.P2D);

    man = new OscP5Manager(8000, true, this);
    man.registerListeners(oscListenner);

    PFont font = createFont("",22);
    textFont(font);
}

void draw() {
    background(100);
    fill(255);
    text("Running: " , 20, 40);
}

private InputListennerInterface oscListenner = new InputListennerInterface () {
    @Override
    public void newEvent(InputEvent input) {
        System.out.println("got it!");
    }
};