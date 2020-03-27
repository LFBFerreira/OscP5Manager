package luisf.interfaces.test;

import luisf.interfaces.InputEvent;
import luisf.interfaces.InputListennerInterface;
import luisf.interfaces.OscP5Manager;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;

public class OscP5Test extends PApplet {
    private OscP5Manager man;

    private int backgroundRed = 0;
    private int backgroundGreen = 0;
    private int backgroundBlue = 0;

    public OscP5Test() {
    }

    // ================================================================

    /**
     * Dispose Method
     */
    public void dispose() {
        man.dispose();
    }

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
        man = new OscP5Manager(8000, true, this);

        man.registerListener(oscListenner);

        PFont font = createFont("", 22);
        textFont(font);
    }

    public void draw() {
        int backgroundColor = (255 << 24) |
                (backgroundRed << 16) |
                (backgroundGreen << 8) |
                backgroundBlue;

        String text = man.isConnected() ? "Running!\n" + man.getServerAddress() + "\n" + man.getServerPort():
                "Not running";

        background(backgroundColor);
        fill(255);
        text(text, 20, 50);
    }

    // ================================================================

    private InputListennerInterface oscListenner = new InputListennerInterface() {
        @Override
        public void newEvent(InputEvent input) {
            // example using TouchOSC's "Simple" Layout controls
            if (input.isPage("1") && input.isName("fader1")) {
                backgroundRed = input.getAsInt(0, 255);

            } else if (input.isPage("1") && input.isName("fader2")) {
                backgroundGreen = input.getAsInt(0, 255);

            } else if (input.isPage("1") && input.isName("fader3")) {
                backgroundBlue = input.getAsInt(0, 255);

            } else if (input.isPage("2") && input.isName("push1") && input.isPressed()) {
                backgroundRed = (int) random(0,255);
                backgroundGreen = (int) random(0,255);
                backgroundBlue = (int) random(0,255);
            }

            else if (input.isPage("2") && input.isName("push2") && input.isReleased()) {
                backgroundRed = (int) random(0,255);
                backgroundGreen = (int) random(0,255);
                backgroundBlue = (int) random(0,255);
            }
        }
    };
}
