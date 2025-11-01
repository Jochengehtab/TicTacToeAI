package src.GUI;

import javax.swing.*;

public class CustomButton extends JButton {
    private final int i;
    private final int j;

    public CustomButton(String text, int i, int j) {
        setText(text);
        this.i = i;
        this.j = j;
    }

    public int getI() {
        return i;
    }

    public int getJ() {
        return j;
    }

}