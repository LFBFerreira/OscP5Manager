package space.luisferreira.ui.test;

import static processing.core.PApplet.runSketch;

public class OscP5TestLauncher {
    public static void main(String[] args){

        OscP5Test test = new OscP5Test();

        int screenIndex = 1;    // 1 -> Main Screen, 2 -> Second Screen

        String[] localArgs = new String[]{"--display=" + screenIndex, test.getClass().getSimpleName()};

        runSketch(localArgs, test);
    }
}
