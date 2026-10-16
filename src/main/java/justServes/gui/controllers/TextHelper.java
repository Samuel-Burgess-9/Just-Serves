package justServes.gui.controllers;

public class TextHelper {

    static String pretty(Enum<?> value) {
        String text = value.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}
