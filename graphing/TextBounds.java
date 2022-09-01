package greg.graphing;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import javax.imageio.ImageIO;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.io.File;
import greg.misc.Pair;
import greg.misc.Trio;
import java.awt.Point;
import java.awt.Desktop;

public class TextBounds {
    private final Graphics2D g;
    private final FontMetrics fm;
    private final Font f;
    private final String txt;
    private final Rectangle rect = new Rectangle();
    private final ArrayList<String> words = new ArrayList<>();
    private final ArrayList<Trio<String, Rectangle, Point>> lines = new ArrayList<>();
    private final int fontHeight;
    private final int fontAscent;
    public static final int LEFT_JUSTIFY = 1;
    public static final int CENTER_JUSTIFY = 2;
    public static final int RIGHT_JUSTIFY = 4;
    private final int justification;
    private final boolean empty;
    TextBounds(Graphics2D graphics, String text, int xPosition, int yPosition, int width, int textJustification) throws
            CharacterDoesNotFitException {
        if (graphics == null || text == null) {
            throw new NullPointerException();
        }
        if ((textJustification & textJustification - 1) != 0 && textJustification >> 3 != 0) {
            textJustification = CENTER_JUSTIFY;
        }
        g = graphics;
        fm = graphics.getFontMetrics();
        f = fm.getFont();
        txt = text;
        rect.width = width;
        fontHeight = fm.getHeight();
        fontAscent = fm.getAscent();
        justification = textJustification;
        rect.x = xPosition;
        rect.y = yPosition;
        if (text.length() == 0) {
            rect.height = 0;
            empty = true;
            return;
        }
        createWords();
        createLines();
        rect.height = fontHeight * lines.size();
        empty = false;
    }
    TextBounds(Graphics2D graphics, String text, int width, int textJustification) throws CharacterDoesNotFitException {
        this(graphics, text, 0, 0, width, textJustification); // will throw NPE if graphics is null
    }
    public final int getNumberOfLines() {
        return lines.size();
    }
    public final boolean isEmpty() {
        return empty;
    }
    public final FontMetrics getFontMetrics() {
        return fm;
    }
    public final String getText() {
        return txt;
    }
    public final Rectangle getBounds() {
        return new Rectangle(this.rect);
    }
    public final int getJustificationType() {
        return justification;
    }
    private void createWords() {
        if (txt.indexOf(' ') == -1) {
            words.add(txt);
        }
        StringBuilder builder = new StringBuilder();
        int last = txt.length() - 1;
        int index = 0;
        boolean hasSpace;
        for (final char c : txt.toCharArray()) {
            if (c != ' ' && c != '\t' && c != '\n') {
                hasSpace = false;
                builder.append(c);
            }
            else {
                hasSpace = true;
            }
            if (!(index++ == last) && !hasSpace) {
                continue;
            }
            words.add(builder.toString());
            builder.setLength(0);
        }
    }
    public final ArrayList<String> getWords() {
        return new ArrayList<>(words);
    }
    private void splitWord(String word) throws CharacterDoesNotFitException {
        if (word == null || word.length() == 0) { // here for completeness, as this would never be true
            return;
        }
        StringBuilder characters = new StringBuilder();
        int cWidth;
        int totWidth = 0;
        for (final char c : word.toCharArray()) {
            cWidth = fm.stringWidth(Character.toString(c));
            if (cWidth > rect.width) {
                throw new CharacterDoesNotFitException("The character '" + c + "' with the current font " +
                        "settings exceeds the width specified for this TextBounds object.");
            }
            totWidth += cWidth;
            if (totWidth >= rect.width && characters.length() > 0) {
                lines.add(new Trio<>(characters.toString(), getProperRectangle(characters.toString())));
                characters.setLength(0);
                totWidth = cWidth;
            }
            characters.append(c);
        }
        lines.add(new Trio<>(characters.toString(), getProperRectangle(characters.toString())));
    }
    private void createLines() throws CharacterDoesNotFitException {
        StringBuilder line = new StringBuilder();
        int w = 0;
        int width;
        boolean hasSpace = false;
        for (final String word : words) {
            if (line.length() > 0) {
                if (getProperWidth(line + " ") > rect.width) {
                    lines.add(new Trio<>(line.toString(), getProperRectangle(line.toString())));
                    line.setLength(0);
                    w = getProperWidth(word);
                    if (w > rect.width) {
                        splitWord(word);
                        line.setLength(0);
                        line.append(lines.get(lines.size() - 1).first);
                        w = getProperWidth(line.toString());
                        lines.remove(lines.size() - 1);
                    }
                    else {
                        line.append(word);
                    }
                    continue;
                }
                else {
                    line.append(' ');
                    w = getProperWidth(line.toString());
                    hasSpace = true;
                }
            }
            w += (width = getProperWidth(word));
            if (w >= rect.width) {
                if (hasSpace) {
                    line.setLength(line.length() - 1);
                    hasSpace = false;
                }
                if (width > rect.width) {
                    if (!lines.isEmpty()) {
                        lines.add(new Trio<>(line.toString(), getProperRectangle(line.toString())));
                    }
                    splitWord(word);
                    line.setLength(0);
                    line.append(lines.get(lines.size() - 1).first);
                    lines.remove(lines.size() - 1);
                    w = getProperWidth(line.toString());
                    continue;
                }
                else {
                    lines.add(new Trio<>(line.toString(), getProperRectangle(line.toString())));
                }
                line.setLength(0);
                line.append(word);
                w = 0;
                continue;
            }
            line.append(word);
        }
        lines.add(new Trio<>(line.toString(), getProperRectangle(line.toString())));
    }
    public final ArrayList<Trio<String, Rectangle, Point>> getLines() {
        ArrayList<Trio<String, Rectangle, Point>> ret = new ArrayList<>();
        for (final Trio<String, Rectangle, Point> p : lines) {
            ret.add(new Trio<>(p.first, new Rectangle(p.second), new Point(p.third))); // no deep-copy of String because
        } // it is immutable
        return ret;
    }
    private Rectangle getProperBounds(String str) {
        if (str == null) {
            return new Rectangle();
        }
        return f.layoutGlyphVector(g.getFontRenderContext(), str.toCharArray(), 0, str.length(),
                Font.LAYOUT_LEFT_TO_RIGHT).getPixelBounds(null, rect.x, rect.y);
    }
    private int getProperWidth(String str) {
        if (str == null) { // for completeness
            return 0;
        }
        int lineWidth = fm.stringWidth(str);
        Rectangle bounds = getProperBounds(str);
        if (bounds.x < rect.x) {
            lineWidth += rect.x - bounds.x;
        }
        if (bounds.width > lineWidth) {
            lineWidth = bounds.width;
        }
        return lineWidth;
    }
    private Pair<Rectangle, Point> getProperRectangle(String str) {
        if (str == null) {
            return new Pair<>(new Rectangle(), new Point());
        }
        int lineWidth = getProperWidth(str);
        Rectangle bounds = getProperBounds(str);
        return new Pair<>(new Rectangle(switch (justification) {
            case LEFT_JUSTIFY -> rect.x;
            case CENTER_JUSTIFY -> rect.x + (rect.width - lineWidth)/2;
            case RIGHT_JUSTIFY -> rect.x + rect.width - lineWidth;
            default -> 0;
        }, rect.y + (lines.size())*fontHeight, lineWidth, fontHeight), new Point(switch (justification) {
            case LEFT_JUSTIFY -> bounds.x < rect.x ? 2*rect.x - bounds.x : rect.x;
            case CENTER_JUSTIFY -> rect.x + (rect.width - lineWidth)/2 + (bounds.x < rect.x ? rect.x - bounds.x : 0);
            case RIGHT_JUSTIFY -> rect.x + rect.width - lineWidth + (bounds.x < rect.x ? rect.x - bounds.x : 0);
            default -> 0; // repeated code in switch () doesn't matter because only one branch is executed
        }, rect.y + fontAscent + lines.size()*fontHeight));
    }
    public static void main(String [] args) throws IOException, CharacterDoesNotFitException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = img.createGraphics();
        String test = "The quick brown fox jumped over the lazy dogs.";
        g.setFont(new Font("sansserif", Font.PLAIN, 100));
        FontMetrics fm = g.getFontMetrics();
        int fontHeight = fm.getHeight();
        g.setColor(Color.white);
        g.fillRect(0, 0, width, height);
        g.setPaint(Color.blue);
        g.drawString(test, xPos, yPos);
        g.fillRect(xPos, yPos + height/10, g.getFontMetrics().stringWidth(test), fontHeight);
        g.setPaint(Color.green);
        g.fillRect(xPos, yPos + height/10, testWidth, g.getFontMetrics().getHeight());
        TextBounds tb = new TextBounds(g, test, xPos, yPos - fm.getAscent(), testWidth, TextBounds.RIGHT_JUSTIFY);
        System.out.println("Words: " + tb.getWords());
        System.out.println("Lines: " + tb.getLines());
        int offset = yPos - fm.getAscent();
        int factor = 0;
        for (final Trio<String, Rectangle, Point> line : tb.getLines()) {
            g.setPaint(Color.red);
            g.fill(line.second);
            g.setPaint(Color.black);
            g.drawString(line.first, line.third.x, line.third.y);
            System.out.println(line.first);
            ++factor;
        }
        g.setPaint(Color.GREEN);
        g.draw(tb.getBounds());
        ImageIO.write(img, "bmp", new File(path));
        g.dispose();
        img.flush();
        Desktop.getDesktop().open(new File(path));
    }
    private static final int width = 2000;
    private static final int height = 1500;
    private static final int testWidth = 600;
    private static final int xPos = width/4;
    private static final int yPos = height/4;
    private static final String path = "C:\\Users\\mario\\TextBounds2.bmp";
}