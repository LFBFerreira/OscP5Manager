package luis.ferreira.libraries.ui;

import oscP5.OscMessage;
import processing.core.PVector;

import java.util.LinkedList;
import java.util.List;

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

    // Get Values As

    /**
     * Get the input value as Float
     * @return input value
     */
    public float getAsFloat() {
        if (values.isEmpty()) {
            System.out.println("There are no values for this input");
            return 0;
        }

        return values.get(0).floatValue();
    }

    /**
     * Map the input value to a Float range
     * @param min minimum value
     * @param max maximum value
     * @return mapped value
     */
    public float getAsFloat(float min, float max) {
        return map(getAsFloat(), 0, 1, min, max);
    }

    /**
     * Get the input value as Int
     * @return input value
     */
    public int getAsInt() {
        return values.get(0).intValue();
    }

    /**
     * Map the input value to a Int range
     * @param min minimum value
     * @param max maximum value
     * @return mapped value
     */
    public int getAsInt(int min, int max) {
        return Math.round(map(getAsFloat(), 0, 1, min, max));
    }

    /**
     * Get the input value as Boolean
     * @return input value
     */
    public Boolean getAsBoolean() {
        if (values.isEmpty()) {
            System.out.println("There are no values for this input");
            return false;
        }

        return values.get(0) == 1;
    }

    /**
     * Get the input value as a PVector
     * @return input value
     */
    public PVector getAsXY() {
        if (values.size() < 2) {
            System.out.println("There are not enough values for this input");
            return new PVector();
        }

        return new PVector(values.get(0), values.get(1));
    }


    public PVector getAsOffsetXY() {
        if (values.size() < 2) {
            System.out.println("There are not enough values for this input");
            return new PVector();
        }

        return new PVector(values.get(0) - 0.5f, values.get(1)- 0.5f);
    }

    @Override
    public String toString() {
        return String.format("id = %s \tvalues = %s", id, values.toString());
    }

    // ================================================================

    // Info getters

    /**
     * Compare the input's page name
     * @param page page name
     * @return true if names are the same
     */
    public boolean isPage(String page) {
        if (page == null) {
            return false;
        }
        return page.equals(getPage());
    }

    /**
     * Compare the input's name
     * @param name input's name
     * @return true if names are the same
     */
    public boolean isName(String name) {
        if (name == null) {
            return false;
        }
        return name.equals(getName());
    }

    /**
     * Compare the input's name
     * @param prefix input's prefix
     * @return true if prefix is part of the name
     */
    public boolean isPrefix(String prefix) {
        if (prefix == null) {
            return false;
        }
        return getName().contains(prefix);
    }

    /**
     * Compare the input's group
     * @param group input's group
     * @return true if group are the same
     */
    public boolean isGroup(int group) {
        return group == getGroup();
    }

    /**
     * Compare the input's sub-group
     * @param subgroup input's sub-group
     * @return true if sub-group are the same
     */
    public boolean isSubGroup(int subgroup) {
        return subgroup == getSubGroup();
    }

    /**
     * Checks if the input was pressed
     * @return true if the input is equal to 1
     */
    public boolean isPressed() {
        return getAsInt() == 1;
    }

    /**
     * Checks if the input was released
     * @return true if the input is equal to 0
     */
    public boolean isReleased() {
        return getAsInt() == 0;
    }

    /**
     * Get the input's page name
     * @return page name
     */
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

    /**
     * Get the input's name
     * @return name
     */
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

    /**
     * Get the input's group number
     * @return group number
     */
    public int getGroup() {
        if (id.isEmpty()) {
            return 0;
        }

        String[] parts = id.split(separatingChar);
        int groupNumber = 0;

        if (parts.length > 3) {
            try
            {
                groupNumber = Integer.parseInt(parts[3]);
            }
            catch(NumberFormatException e)
            {
                System.err.println("Exception converting group number");
                System.err.println(e.toString());
                return 0;
            }
        } else {
            System.err.println("Group number could not be found");
            return 0;
        }

        return groupNumber;
    }

    /**
     * Get the input's sub-group number
     * @return sub-group number
     */
    public int getSubGroup() {
        if (id.isEmpty()) {
            return 0;
        }

        String[] parts = id.split(separatingChar);
        int groupNumber = 0;

        if (parts.length > 4) {
            try
            {
                groupNumber = Integer.parseInt(parts[4]);
            }
            catch(NumberFormatException e)
            {
                System.err.println("Exception converting sub-group number");
                System.err.println(e.toString());
                return 0;
            }
        } else {
            System.err.println("Sub-group number could not be found");
            return 0;
        }

        return groupNumber;
    }

    /**
     * Get the input's values
     * @return group number
     */
    public List<Float> getValues()
    {
        return values;
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
}
