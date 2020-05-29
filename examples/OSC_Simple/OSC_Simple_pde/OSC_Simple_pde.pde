import luis.ferreira.libraries.ui.*;

OscP5Manager man;

int backgroundRed = 0;
int backgroundGreen = 0;
int backgroundBlue = 0;

void setup() {
    size(200, 200, PConstants.P2D);

    man = new OscP5Manager(8000, true, this);
    man.registerListener(oscListenner);

    PFont font = createFont("",22);
    textFont(font);
}

void draw() {
    int backgroundColor = (255 << 24) | (backgroundRed << 16) | (backgroundGreen << 8) | backgroundBlue;

    background(backgroundColor);
    fill(255);
    text("Running: " , 20, 40);
}

private InputListennerInterface oscListenner = new InputListennerInterface () {
    @Override
    public void newEvent(InputEvent input) {
        // example using TouchOSC's "Simple" Layout controls
        if (input.isPage("1") && input.isName("fader1")) {
            backgroundRed = (int) input.getAsFloat(0, 255);
        } else if (input.isPage("1") && input.isName("fader2")) {
            backgroundGreen = (int) input.getAsFloat(0, 255);
        } else if (input.isPage("1") && input.isName("fader3")) {
            backgroundBlue = (int) input.getAsFloat(0, 255);
        }
    }
};
