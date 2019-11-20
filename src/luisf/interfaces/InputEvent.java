package luisf.interfaces;

import oscP5.OscMessage;
import processing.core.PVector;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class InputEvent {
    public final InputMethodEnum inputMethod;

    public String id = "";
    public String source = "";

    private List<Float> values = new LinkedList<>();
    private final String separatingChar = "/";

    // ================================================================

   public InputEvent(OscMessage message) {
        inputMethod = InputMethodEnum.OSC;
        id = message.addrPattern();
        source = message.address();
        values = parseOscValues(message);
    }


    // ================================================================

    // Public

    public float getAsFloat() {
        if (values.isEmpty()) {
            System.out.println("There are no values for this input");
            return 0;
        }

        return values.get(0).floatValue();
    }

    public float getAsFloat(int min, int max) {
        return map(getAsFloat(), 0, 1, min, max);
    }

    public int getAsInt() {
        return values.get(0).intValue();
    }

    public int getAsInt(int min, int max) {
        return Math.round(map(getAsFloat(), 0, 1, min, max));
    }

    public Boolean getAsBoolean() {
        if (values.isEmpty()) {
            System.out.println("There are no values for this input");
            return false;
        }

        return values.get(0) == 1;
    }

    public PVector getAsXY() {
        if (values.size() < 2) {
            System.out.println("There are not enough values for this input");
            return new PVector();
        }

        return new PVector(values.get(0).floatValue(), values.get(1).floatValue());
    }

    @Override
    public String toString() {
        return String.format("id = %s \tvalues = %s", id, values);
    }

    // ================================================================

    // Helpers

    private float map(float value, float start1, float stop1, float start2, float stop2) {
        return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
    }

    private List<Float> parseOscValues(OscMessage message) {
        List<Float> values = new LinkedList<>();

        String types = message.typetag();

        if (types.isEmpty())
        {
            return values;
        }

        float value = 0;
        for (int i = 0; i < types.length(); i++) {
            switch (types.charAt(i)) {
                case 'f':
                    try {
                        value = message.get(i).floatValue();
                    } catch (NumberFormatException e) {
                        System.out.println("Number format exception");
                        e.printStackTrace();
                    }
                    values.add(value);
                    break;

                default:
                    System.out.println("Could not parse the type of the OSC message");
            }
        }

        return values;
    }

    public boolean isPage(String page) {
        if (page == null) {
            return false;
        }
        return page.equals(getPage());
    }

    public boolean isName(String name) {
        if (name == null) {
            return false;
        }
        return name.equals(getName());
    }

    public boolean isGroup(String group) {
        if (group == null) {
            return false;
        }
        return group.equals(getGroup());
    }

    public boolean isPressed() {
        return getAsInt() == 1;
    }

    public boolean isReleased() {
        return getAsInt() == 0;
    }


    public String getPage() {
        if (id.isEmpty()) {
            return "";
        }

        String[] parts = id.split(separatingChar);
        if (parts.length > 1) {
            return parts[1];
        } else {
            return "";
        }
    }

    public String getName() {
        if (id.isEmpty()) {
            return "";
        }

        String[] parts = id.split(separatingChar);
        if (parts.length > 2) {
            return parts[2];
        } else {
            return "";
        }
    }

    public String getGroup() {
        if (id.isEmpty()) {
            return "";
        }

        String[] parts = id.split(separatingChar);
        if (parts.length > 3) {
            return parts[3];
        } else {
            return "";
        }
    }

}
