package greg.graphing;

import java.util.Optional;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.FontMetrics;
import java.awt.Rectangle;

public final class Label implements Cloneable {
    public static final int LEFT_JUSTIFY = 0;
    public static final int CENTER = 1;
    public static final int RIGHT_JUSTIFY = 2;
    private Color color = Color.black;
    private Color originalColor = null;
    private Font font;
    private Font originalFont = null;
    private double rotation = 0d; // in radians
    String text = "";
    Graphics2D graphics = null;
    Rectangle rect = new Rectangle();
    private int textJust = CENTER;
    private boolean singleLine = true;
    private boolean bounded = false;
    private int checkJustification(int justification) {
        return justification == LEFT_JUSTIFY || justification == CENTER || justification == RIGHT_JUSTIFY ?
                justification : CENTER;
    }
    private void calculateBounds() { // calculates the bounds for the Label and sets the text wrapping

    }
    public boolean setText(String txt) {
        if (txt == null || txt.length() == 0) {
            return false;
        }
        if (!bounded) {
            singleLine = txt.indexOf('\n') == txt.length() - 1 || txt.indexOf('\n') == -1;
        }
        text = txt;
        return true;
    }
    public String getText() {
        return text;
    }
    public boolean setColor(Color col) {
        if (col == null) {
            return false;
        }
        color = col;
        return true;
    }
    public Color getColor() {
        return color;
    }
    public boolean setFontSize(int size) {
        if (size == 0) {
            return false;
        }
        font = new Font(font.getFontName(), font.getStyle(), size);
        return true;
    }
    public int getFontSize() {
        return font.getSize();
    }
    public boolean setFont(Font f) {
        if (f == null) {
            return false;
        }
        font = f;
        return true;
    }
    public Font getFont() {
        return font;
    }
    public void setRotation(double rot) {
        rotation = rot;
    }
    public double getRotation() {
        return rotation;
    }
    public boolean setXPosition(int xPosition) {
        if (xPosition < 0) {
            return false;
        }
        rect.x = xPosition;
        return true;
    }
    public boolean setYPosition(int yPosition) {
        if (yPosition < 0) {
            return false;
        }
        rect.y = yPosition;
        return true;
    }
    public boolean setPosition(int xPosition, int yPosition) { // sets BOTH if both can be set - if not, none
        if (xPosition < 0 || yPosition < 0) {
            return false;
        }
        rect.x = xPosition;
        rect.y = yPosition;
        return true;
    }
    public boolean movePosition(int xOffset, int yOffset) {
        if (rect.x + xOffset < 0 || rect.y + yOffset < 0) {
            return false;
        }
        rect.x += xOffset;
        rect.y += yOffset;
        return true;
    }
    public void setWidth(int width) {
        bounded = true;
        rect.width = width;
    }
    public int getWidth() {
        if (!bounded) {
            return 0;
        }
        return rect.width;
    }
    public boolean setGraphics2D(Graphics2D graphics2D) {
        if (graphics2D == null) {
            return false;
        }
        graphics = graphics2D;
        return true;
    }
    public Optional<Graphics2D> getGraphics2D() {
        return Optional.ofNullable(graphics);
    }
    public Rectangle getRectangle() { // returns the space occupied by the label on the Graphics2D object
        calculateBounds();
        return new Rectangle(this.rect);
    }
    // ----------
    // note: if width field is not included, Label object is single-lined if no newline is encountered, and multi-lined
    // if 1<= newline characters are present
    // -----
    // if width is included, then the Label object is multi-lined if the text width is greater than the width given, so
    // any newline characters are ignored
    // -----
    // multi-line Label objects can either be left-, center-, or right-justified
    // ----------
    public Label() {
        font = new Font("sansserif", Font.PLAIN, 10);
    }
    public Label(String labelText, int fontSize, int xPosition, int yPosition) {
        if (labelText == null) {
            throw new NullPointerException();
        }
        singleLine = labelText.indexOf('\n') == labelText.length() - 1 || labelText.indexOf('\n') == -1;
        font = new Font("sansserif", Font.PLAIN, fontSize);
        text = labelText;
        rect.x = xPosition;
        rect.y = yPosition;
    }
    public Label(String labelText, int fontSize, int xPosition, int yPosition, int width) {
        if (labelText == null) {
            throw new NullPointerException();
        }
        bounded = true;
        rect.width = width;
        font = new Font("sansserif", Font.PLAIN, fontSize);
        text = labelText;
        rect.x = xPosition;
        rect.y = yPosition;
    }
    public Label(String labelText, Font font, int xPosition, int yPosition) {
        if (labelText == null || font == null) {
            throw new NullPointerException();
        }
        singleLine = labelText.indexOf('\n') == labelText.length() - 1 || labelText.indexOf('\n') == -1;
        this.font = font;
        text = labelText;
        rect.x = xPosition;
        rect.y = yPosition;
    }
    public Label(String labelText, Font font, int xPosition, int yPosition, int width) {
        if (labelText == null || font == null) {
            throw new NullPointerException();
        }
        bounded = true;
        rect.width = width;
        this.font = font;
        text = labelText;
        rect.x = xPosition;
        rect.y = yPosition;
    }
    public Label(String labelText, Font font, Color color, int xPosition, int yPosition) {
        if (labelText == null || font == null || color == null) {
            throw new NullPointerException();
        }
        singleLine = labelText.indexOf('\n') == labelText.length() - 1 || labelText.indexOf('\n') == -1;
        this.color = color;
        this.font = font;
        text = labelText;
        rect.x = xPosition;
        rect.y = yPosition;
    }
    public Label(String labelText, Font font, Color color, int xPosition, int yPosition,
                 int width) {
        if (labelText == null || font == null || color == null) {
            throw new NullPointerException();
        }
        bounded = true;
        rect.width = width;
        this.color = color;
        this.font = font;
        text = labelText;
        rect.x = xPosition;
        rect.y = yPosition;
    }
    public Label(String labelText, Font font, Color color, int xPosition, int yPosition,
                 double rotationInRadians) {
        if (labelText == null || font == null || color == null) {
            throw new NullPointerException();
        }
        singleLine = labelText.indexOf('\n') == labelText.length() - 1 || labelText.indexOf('\n') == -1;
        this.color = color;
        this.font = font;
        text = labelText;
        rect.x = xPosition;
        rect.y = yPosition;
        rotation = rotationInRadians;
    }
    public Label(String labelText, Font font, Color color, int xPosition, int yPosition,
                 int width, double rotationInRadians) {
        if (labelText == null || font == null || color == null) {
            throw new NullPointerException();
        }
        bounded = true;
        rect.width = width;
        this.color = color;
        this.font = font;
        text = labelText;
        rect.x = xPosition;
        rect.y = yPosition;
        rotation = rotationInRadians;
    }
    public Label(String labelText, Font font, Color color, int xPosition, int yPosition,
                 double rotationInRadians, int textJustification) {
        if (labelText == null || font == null || color == null) {
            throw new NullPointerException();
        }
        singleLine = labelText.indexOf('\n') == labelText.length() - 1 || labelText.indexOf('\n') == -1;
        this.color = color;
        this.font = font;
        text = labelText;
        rect.x = xPosition;
        rect.y = yPosition;
        rotation = rotationInRadians;
        textJust = checkJustification(textJustification);
    }
    public Label(String labelText, Font font, Color color, int xPosition, int yPosition,
                 int width, double rotationInRadians, int textJustification) {
        if (labelText == null || font == null || color == null) {
            throw new NullPointerException();
        }
        bounded = true;
        rect.width = width;
        this.color = color;
        this.font = font;
        text = labelText;
        rect.x = xPosition;
        rect.y = yPosition;
        rotation = rotationInRadians;
        textJust = checkJustification(textJustification);
    }
    public Label(Graphics2D imageGraphics, String labelText, int fontSize, int xPosition, int yPosition) {
        if (imageGraphics == null || labelText == null) {
            throw new NullPointerException();
        }
        singleLine = labelText.indexOf('\n') == labelText.length() - 1 || labelText.indexOf('\n') == -1;
        font = new Font("sansserif", Font.PLAIN, fontSize);
        graphics = imageGraphics;
        text = labelText;
        rect.x = xPosition;
        rect.y = yPosition;
    }
    public Label(Graphics2D imageGraphics, String labelText, int fontSize, int xPosition, int yPosition, int width) {
        if (imageGraphics == null || labelText == null) {
            throw new NullPointerException();
        }
        bounded = true;
        rect.width = width;
        font = new Font("sansserif", Font.PLAIN, fontSize);
        graphics = imageGraphics;
        text = labelText;
        rect.x = xPosition;
        rect.y = yPosition;
    }
    public Label(Graphics2D imageGraphics, String labelText, Font font, int xPosition, int yPosition) {
        if (imageGraphics == null || labelText == null || font == null) {
            throw new NullPointerException();
        }
        singleLine = labelText.indexOf('\n') == labelText.length() - 1 || labelText.indexOf('\n') == -1;
        this.font = font;
        graphics = imageGraphics;
        text = labelText;
        rect.x = xPosition;
        rect.y = yPosition;
    }
    public Label(Graphics2D imageGraphics, String labelText, Font font, int xPosition, int yPosition, int width) {
        if (imageGraphics == null || labelText == null || font == null) {
            throw new NullPointerException();
        }
        bounded = true;
        rect.width = width;
        this.font = font;
        graphics = imageGraphics;
        text = labelText;
        rect.x = xPosition;
        rect.y = yPosition;
    }
    public Label(Graphics2D imageGraphics, String labelText, Font font, Color color, int xPosition, int yPosition) {
        if (imageGraphics == null || labelText == null || font == null || color == null) {
            throw new NullPointerException();
        }
        singleLine = labelText.indexOf('\n') == labelText.length() - 1 || labelText.indexOf('\n') == -1;
        this.color = color;
        this.font = font;
        graphics = imageGraphics;
        text = labelText;
        rect.x = xPosition;
        rect.y = yPosition;
    }
    public Label(Graphics2D imageGraphics, String labelText, Font font, Color color, int xPosition, int yPosition,
                 int width) {
        if (imageGraphics == null || labelText == null || font == null || color == null) {
            throw new NullPointerException();
        }
        bounded = true;
        rect.width = width;
        this.color = color;
        this.font = font;
        graphics = imageGraphics;
        text = labelText;
        rect.x = xPosition;
        rect.y = yPosition;
    }
    public Label(Graphics2D imageGraphics, String labelText, Font font, Color color, int xPosition, int yPosition,
                 double rotationInRadians) {
        if (imageGraphics == null || labelText == null || font == null || color == null) {
            throw new NullPointerException();
        }
        singleLine = labelText.indexOf('\n') == labelText.length() - 1 || labelText.indexOf('\n') == -1;
        this.color = color;
        this.font = font;
        graphics = imageGraphics;
        text = labelText;
        rect.x = xPosition;
        rect.y = yPosition;
        rotation = rotationInRadians;
    }
    public Label(Graphics2D imageGraphics, String labelText, Font font, Color color, int xPosition, int yPosition,
                 int width, double rotationInRadians) {
        if (imageGraphics == null || labelText == null || font == null || color == null) {
            throw new NullPointerException();
        }
        bounded = true;
        rect.width = width;
        this.color = color;
        this.font = font;
        graphics = imageGraphics;
        text = labelText;
        rect.x = xPosition;
        rect.y = yPosition;
        rotation = rotationInRadians;
    }
    public Label(Graphics2D imageGraphics, String labelText, Font font, Color color, int xPosition, int yPosition,
                 double rotationInRadians, int textJustification) {
        if (imageGraphics == null || labelText == null || font == null || color == null) {
            throw new NullPointerException();
        }
        singleLine = labelText.indexOf('\n') == labelText.length() - 1 || labelText.indexOf('\n') == -1;
        this.color = color;
        this.font = font;
        graphics = imageGraphics;
        text = labelText;
        rect.x = xPosition;
        rect.y = yPosition;
        rotation = rotationInRadians;
        textJust = checkJustification(textJustification);
    }
    public Label(Graphics2D imageGraphics, String labelText, Font font, Color color, int xPosition, int yPosition,
                 int width, double rotationInRadians, int textJustification) {
        if (imageGraphics == null || labelText == null || font == null || color == null) {
            throw new NullPointerException();
        }
        bounded = true;
        rect.width = width;
        this.color = color;
        this.font = font;
        graphics = imageGraphics;
        text = labelText;
        rect.x = xPosition;
        rect.y = yPosition;
        rotation = rotationInRadians;
        textJust = checkJustification(textJustification);
    }
    public boolean draw() throws NoGraphics2DObjectException {
        if (graphics == null) {
            throw new NoGraphics2DObjectException("No Graphics2D object has been set for this Label object.");
        }
        originalColor = graphics.getColor();
        originalFont = graphics.getFont();
        graphics.setPaint(color);
        graphics.setFont(font);
        BufferedImage img;
        FontMetrics fm = graphics.getFontMetrics();
        if (singleLine) {
            int w = fm.stringWidth(text);
            int asc = fm.getAscent();
            img = new BufferedImage(w, asc, BufferedImage.TYPE_3BYTE_BGR);
            if (rotation == 0d) {
                graphics.drawString(text, rect.x, rect.y);
            }
            else {

            }
        }
        graphics.setPaint(originalColor);
        graphics.setFont(originalFont);
        originalColor = null;
        originalFont = null;
        return true;
    }
    @Override
    public Label clone() {
        Label retLabel;
        try {
            retLabel = (Label) super.clone();
        } catch (CloneNotSupportedException e) {
            return new Label();
        }
        retLabel.color = new Color(this.color.getRGB());
        retLabel.font = new Font(this.font.getFontName(), this.font.getStyle(), this.font.getSize());
        // retLabel.graphics = this.graphics; // don't reassign: every Label object needs a Graphics2D obj. to draw on
        // retLabel.text = this.text; // no need to reassign, since a String is an immutable object
        return retLabel;
    }
}