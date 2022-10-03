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
    protected Graphics2D g = null;
    protected FontMetrics fm = null;
    protected Font f;
    protected final String txt;
    protected final Rectangle rect = new Rectangle();
    protected final ArrayList<String> words = new ArrayList<>();
    protected final ArrayList<Trio<String, Rectangle, Point>> lines = new ArrayList<>();
    protected int fontHeight;
    protected int fontAscent;
    public static final int LEFT_JUSTIFY = 1;
    public static final int CENTER_JUSTIFY = 2;
    public static final int RIGHT_JUSTIFY = 4;
    private final int justification;
    private boolean pushToNextLine = false;
    private final boolean empty;
    protected boolean calculated = false;
    protected boolean locked = false; // only true for PlotLabel objects
    protected TextBounds(String text, Font font, int textJustification,
                         boolean pushLongWordToNextLine) { // ctor for PlotLabel objects
        if (text == null) {
            throw new NullPointerException();
        }
        if ((textJustification & textJustification - 1) != 0 && textJustification >> 3 != 0) {
            textJustification = CENTER_JUSTIFY;
        }
        f = font;
        txt = text;
        justification = textJustification;
        this.pushToNextLine = pushLongWordToNextLine;
        if (text.isEmpty()) {
            empty = true;
            return;
        }
        empty = false;
        locked = true;
    }
    public TextBounds(String text, Font font, int xPosition, int yPosition, int width, int textJustification, boolean pushLongWordToNextLine) {
        if (text == null) {
            throw new NullPointerException();
        }
        if ((textJustification & textJustification - 1) != 0 && textJustification >> 3 != 0) {
            textJustification = CENTER_JUSTIFY;
        }
        txt = text;
        f = font; // it is OK here if font is null
        rect.x = xPosition;
        rect.y = yPosition;
        rect.width = width;
        justification = textJustification;
        this.pushToNextLine = pushLongWordToNextLine;
        if (text.isEmpty()) {
            empty = true;
            return;
        }
        empty = false;
    }
    public TextBounds(Graphics2D graphics, String text, Font font, int xPosition, int yPosition, int width,
               int textJustification, boolean pushLongWordToNextLine) throws CharacterDoesNotFitException {
        if (graphics == null || text == null || font == null) {
            throw new NullPointerException();
        }
        if ((textJustification & textJustification - 1) != 0 && textJustification >> 3 != 0) {
            textJustification = CENTER_JUSTIFY;
        }
        Font originalFont = graphics.getFont();
        graphics.setFont(font);
        g = graphics;
        fm = graphics.getFontMetrics();
        f = font;
        txt = text;
        rect.width = width;
        fontHeight = fm.getHeight();
        fontAscent = fm.getAscent();
        justification = textJustification;
        rect.x = xPosition;
        rect.y = yPosition;
        this.pushToNextLine = pushLongWordToNextLine;
        if (text.length() == 0) {
            rect.height = 0;
            empty = true;
            return;
        }
        createWords();
        createLines();
        rect.height = fontHeight * lines.size();
        empty = false;
        graphics.setFont(originalFont);
        calculated = true;
    }
    public TextBounds(Graphics2D graphics, String text, int xPosition, int yPosition, int width,  // creates object with
               int textJustification, boolean pushLongWordToNextLine) throws CharacterDoesNotFitException { // graphics'
        this(graphics, text, graphics.getFont(), xPosition, yPosition, width, textJustification,            // font
                pushLongWordToNextLine);
    }
    public TextBounds(Graphics2D graphics, String text, int width, int textJustification,
                      boolean pushLongWordToNextLine) throws CharacterDoesNotFitException {
        this(graphics, text, 0, 0, width, textJustification, pushLongWordToNextLine); // will throw NPE if null graphics
    }
    public TextBounds(Figure fig, String text, Font font, int xPosition, int yPosition, int width,
               int textJustification, boolean pushLongWordToNextLine) throws CharacterDoesNotFitException {
        this(fig.fullImageGraphics, text, font, xPosition, yPosition, width, textJustification, pushLongWordToNextLine);
    }
    public TextBounds(Figure fig, String text, int xPosition, int yPosition, int width, int textJustification,
                      boolean pushLongWordToNextLine) throws CharacterDoesNotFitException {
        this(fig.fullImageGraphics, text, xPosition, yPosition, width, textJustification, pushLongWordToNextLine);
    }
    public TextBounds(TextBounds other) {
        g = other.g; // only choice
        fm = g.getFontMetrics();
        f = new Font(other.f.getFontName(), other.f.getStyle(), other.f.getSize());
        txt = other.txt; // Strings are immutable
        rect.x = other.rect.x;
        rect.y = other.rect.y;
        rect.width = other.rect.width;
        words.clear();
        lines.clear();
        words.addAll(other.words);
        lines.addAll(other.lines);
        fontHeight = fm.getHeight();
        fontAscent = fm.getAscent();
        justification = other.justification;
        this.pushToNextLine = other.pushToNextLine;
        empty = other.empty;
        this.calculated = other.calculated;
        this.locked = other.locked;
    }
    void unlock() { // package-private - only to be called within Plot objects
        locked = false;
    }
    // below method forces TextBounds obj. to recalculate everything for new Graphics2D and Font objects
    public boolean calculate(Graphics2D graphics, Font newFont) throws CharacterDoesNotFitException {
        if (empty || locked) {
            return false;
        }
        if (graphics != null) {
            g = graphics;
        }
        else {
            if (g == null) { // for a TextBounds object constructed without a Graphics2D object
                return false;
            }
        }
        Font originalFont = g.getFont();
        if (newFont != null) {
            f = newFont;
        }
        else {
            if (f == null) {
                f = originalFont;
            }
        }
        g.setFont(f);
        fm = g.getFontMetrics();
        fontHeight = fm.getHeight();
        fontAscent = fm.getAscent();
        createWords();
        createLines();
        rect.height = fontHeight*lines.size();
        g.setFont(originalFont);
        return calculated = true;
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
        words.clear();
        if (txt.indexOf(' ') == -1 && txt.indexOf('\n') == -1 && txt.indexOf('\t') == -1) {
            words.add(txt);
            return;
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
        lines.clear();
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
                    //if (!lines.isEmpty()) {
                    if (!line.isEmpty() && pushToNextLine) {
                        lines.add(new Trio<>(line.toString(), getProperRectangle(line.toString())));
                    }
                    if (!pushToNextLine && !line.isEmpty()) {
                        splitWord(line + " " + word);
                    }
                    else {
                        splitWord(word);
                    }
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
        String test = "The quickbrown fox jumped over the lazy dogs.";
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
        TextBounds tb = new TextBounds(g, test, xPos, yPos - fm.getAscent(), testWidth, TextBounds.RIGHT_JUSTIFY, true);
        System.out.println("Words: " + tb.getWords());
        System.out.println("Lines: " + tb.getLines());
        for (final Trio<String, Rectangle, Point> line : tb.getLines()) {
            g.setPaint(Color.red);
            g.fill(line.second);
            g.setPaint(Color.black);
            g.drawString(line.first, line.third.x, line.third.y);
            System.out.println(line.first);
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