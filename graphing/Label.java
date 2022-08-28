package greg.graphing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class Label {
    public static final int SINGLE_LINE = 0;
    public static final int MULTI_LINE = 1;
    public static final int AUTO_WRAP = 2;
    public static final int LEFT_JUSTIFY = 4;
    public static final int CENTER = 8;
    public static final int RIGHT_JUSTIFY = 16;
    private Color color;
    private Font font;
    private double rotation; // in radians
    String text;
    Graphics2D graphics;
    private int xPos;
    private int yPos;
    public Label(Graphics2D imageGraphics, String labelText, int fontSize, int xPosition, int yPosition) {
        if (imageGraphics == null || labelText == null) {
            throw new NullPointerException();
        }
        color = Color.black;
        font = new Font("sansserif", Font.PLAIN, fontSize);
        rotation = 0d;
        graphics = imageGraphics;
        text = labelText;
        xPos = xPosition;
        yPos = yPosition;
    }
    public Label(Graphics2D imageGraphics, String labelText, Font font, int xPosition, int yPosition) {
        if (imageGraphics == null || labelText == null || font == null) {
            throw new NullPointerException();
        }
        color = Color.black;
        this.font = font;
        rotation = 0d;
        graphics = imageGraphics;
        text = labelText;
        xPos = xPosition;
        yPos = yPosition;
    }
    public Label(Graphics2D imageGraphics, String labelText, Font font, Color color, int xPosition, int yPosition) {
        if (imageGraphics == null || labelText == null || font == null || color == null) {
            throw new NullPointerException();
        }
        this.color = color;
        this.font = font;
        rotation = 0d;
        graphics = imageGraphics;
        text = labelText;
        xPos = xPosition;
        yPos = yPosition;
    }
    public Label(Graphics2D imageGraphics, String labelText, Font font, Color color, int xPosition, int yPosition,
                 double rotationInRadians) {
        if (imageGraphics == null || labelText == null || font == null || color == null) {
            throw new NullPointerException();
        }
        this.color = color;
        this.font = font;
        rotation = 0d;
        graphics = imageGraphics;
        text = labelText;
        xPos = xPosition;
        yPos = yPosition;
        rotation = rotationInRadians;
    }
}