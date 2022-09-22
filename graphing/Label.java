package greg.graphing;

import greg.misc.Trio;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Point;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.awt.Desktop;

public final class Label extends TextBounds {
    private Color textColor = Color.black;
    private Color backgroundColor = null;
    private double rotation = 0d; // in radians
    boolean calculated = true;
    public static Rectangle getRotatedRectangle(Rectangle r, double radians) {
        if (r == null) {
            return new Rectangle();
        }
        if (radians == 0d) { // saves the following overhead if this is the case
            return new Rectangle(r);
        }
        return new Rectangle((int) (r.x*Math.cos(radians) - r.y*Math.sin(radians)),
                             (int) (r.y*Math.cos(radians) + r.x*Math.sin(radians)), r.width, r.height);
    }
    public static Point getRotatedPoint(Point p, double radians) {
        if (p == null) {
            return new Point();
        }
        if (radians == 0d) { // saves the following overhead if this is the case
            return new Point(p);
        }
        return new Point((int) (p.x*Math.cos(radians) - p.y*Math.sin(radians)),
                         (int) (p.y*Math.cos(radians) + p.x*Math.sin(radians)));
    }
    public Label(Label other) {
        super(other);
        textColor = new Color(other.textColor.getRGB());
        backgroundColor = new Color(other.backgroundColor.getRGB());
        rotation = other.rotation;
        calculated = other.calculated;
    }
    public Label(Figure fig, String text, int xPos, int yPos, int width, int justification) throws
            CharacterDoesNotFitException {
        super(fig, text, xPos, yPos, width, justification);
    }
    public Label(Figure fig, String text, Font font, int xPos, int yPos, int width, int justification) throws
            CharacterDoesNotFitException {
        super(fig, text, font, xPos, yPos, width, justification);
    }
    public Label(Graphics2D graphics, String text, int xPos, int yPos, int width, int justification) throws
            CharacterDoesNotFitException {
        super(graphics, text, xPos, yPos, width, justification);
    }
    public Label(Graphics2D graphics, String text, Font font, int xPos, int yPos, int width, int justification) throws
            CharacterDoesNotFitException {
        super(graphics, text, font, xPos, yPos, width, justification);
    }
    public Label(String text, int xPos, int yPos, int width, int justification) throws
            CharacterDoesNotFitException {
        super(text, null, xPos, yPos, width, justification);
        calculated = false;
    }
    public Label(String text, Font font, int xPos, int yPos, int width, int justification) throws
            CharacterDoesNotFitException {
        super(text, font, xPos, yPos, width, justification);
        calculated = false;
    }
    public Label(Figure fig, String text, Color bgColor, Color txtColor, int xPos, int yPos, int width,
                 double rotationInRadians, int justification) throws CharacterDoesNotFitException {
        super(fig, text, xPos, yPos, width, justification);
        this.textColor = txtColor == null ? Color.black : txtColor;
        this.backgroundColor = bgColor == null ? fig.fullImageGraphics.getColor() : bgColor;
        this.rotation = rotationInRadians;
    }
    public Label(Figure fig, String text, Font font, Color bgColor, Color txtColor, int xPos, int yPos, int width,
                 double rotationInRadians, int justification) throws CharacterDoesNotFitException {
        super(fig, text, font, xPos, yPos, width, justification);
        this.textColor = txtColor == null ? Color.black : txtColor;
        this.backgroundColor = bgColor == null ? fig.fullImageGraphics.getColor() : bgColor;
        this.rotation = rotationInRadians;
    }
    public Label(Graphics2D graphics, String text, Color bgColor, Color txtColor, int xPos, int yPos, int width,
                 double rotationInRadians, int justification) throws CharacterDoesNotFitException {
        super(graphics, text, xPos, yPos, width, justification);
        this.textColor = txtColor == null ? Color.black : txtColor;
        this.backgroundColor = bgColor == null ? graphics.getColor() : bgColor;
        this.rotation = rotationInRadians;
    }
    public Label(Graphics2D graphics, String text, Font font, Color bgColor, Color txtColor, int xPos, int yPos,
                 int width, double rotationInRadians, int justification) throws CharacterDoesNotFitException {
        super(graphics, text, font, xPos, yPos, width, justification);
        this.textColor = txtColor == null ? Color.black : txtColor;
        this.backgroundColor = bgColor == null ? graphics.getColor() : bgColor;
        this.rotation = rotationInRadians;
    }
    public Label(String text, Color bgColor, Color txtColor, int xPos, int yPos, int width,
                 double rotationInRadians, int justification) {
        super(text, null, xPos, yPos, width, justification);
        this.textColor = txtColor == null ? Color.black : txtColor;
        this.backgroundColor = bgColor == null ? Color.white : bgColor;
        this.rotation = rotationInRadians;
        calculated = false;
    }
    public Label(String text, Font font, Color bgColor, Color txtColor, int xPos, int yPos,
                 int width, double rotationInRadians, int justification) {
        super(text, font, xPos, yPos, width, justification);
        this.textColor = txtColor == null ? Color.black : txtColor;
        this.backgroundColor = bgColor == null ? Color.white : bgColor;
        this.rotation = rotationInRadians;
        calculated = false;
    }
    public boolean setTextColor(Color col) {
        if (col == null) {
            return false;
        }
        textColor = col;
        return true;
    }
    public Color getTextColor() {
        return textColor;
    }
    public boolean setBackgroundColor(Color col) {
        if (col == null) {
            return false;
        }
        backgroundColor = col;
        return true;
    }
    public Color getBackgroundColor() {
        return backgroundColor;
    }
    public void setRotation(double rot) {
        rotation = rot;
    }
    public double getRotation() {
        return rotation;
    }
    public boolean draw() {
        if (super.isEmpty()) {
            return false;
        }
        Color originalColor = null;
        if (backgroundColor != null) {
            originalColor = super.g.getColor();
            super.g.setPaint(backgroundColor);
        }
        Font originalFont = super.g.getFont();
        super.g.setFont(super.f);
        super.g.rotate(rotation, super.rect.x, super.rect.y);
        super.g.fill(super.getBounds());
        super.g.setPaint(textColor);
        for (final Trio<String, Rectangle, Point> t : super.getLines()) {
            g.drawString(t.first, t.third.x, t.third.y);
        }
        super.g.rotate(-rotation, super.rect.x, super.rect.y);
        super.g.setFont(originalFont);
        if (backgroundColor != null) {
            super.g.setPaint(originalColor);
        }
        return true;
    }
    public static void main(String [] args) throws IOException, CharacterDoesNotFitException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D gr = img.createGraphics();
        gr.setPaint(Color.white);
        gr.fillRect(0, 0, width, height);
        Label lbl2 = new Label(gr, "The man went on a very long walk through the woods. He was a long way from home. He would have preferred Chinese food.",
                new Font("sansserif", Font.PLAIN, height/40), Color.blue, Color.red, width/2, height/2, 500, 0, 2);
        lbl2.draw();
        Label lbl = new Label(gr, "The man went on a very long walk through the woods. He was a long way from home. He would have preferred Chinese food.",
                    new Font("sansserif", Font.PLAIN, height/40), Color.pink, Color.yellow, width/2, height/2, 500, -2*Math.PI/1.5, 2);
        lbl.draw();
        ImageIO.write(img, "bmp", new File(path));
        Desktop.getDesktop().open(new File(path));
    }
    // @Override
    // public Label clone() {
    //     Label retLabel;
    //     try {
    //         retLabel = (Label) super.clone();
    //     } catch (CloneNotSupportedException e) {
    //         return new Label();
    //     }
    //     retLabel.color = new Color(this.color.getRGB());
    //     retLabel.font = new Font(this.font.getFontName(), this.font.getStyle(), this.font.getSize());
    //     // retLabel.graphics = this.graphics; // don't reassign: every Label object needs a Graphics2D obj. to draw on
    //     // retLabel.text = this.text; // no need to reassign, since a String is an immutable object
    //     return retLabel;
    // }
    public static final int width = 2000;
    public static final int height = 1500;
    public static final String path = System.getProperty("user.home") + System.getProperty("file.separator") +
            "LabelTest.bmp";
}