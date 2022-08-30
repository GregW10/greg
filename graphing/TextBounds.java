package greg.graphing;

import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import javax.imageio.ImageIO;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

public class TextBounds {
    private final FontMetrics fm;
    private final String txt;
    private final Rectangle rect = new Rectangle();
    private final ArrayList<String> words = new ArrayList<>();
    private final ArrayList<String> lines = new ArrayList<>();
    private final int fontHeight;
    public static final int LEFT_JUSTIFY = 1;
    public static final int CENTER_JUSTIFY = 2;
    public static final int RIGHT_JUSTIFY = 4;
    private final int justification;
    TextBounds(FontMetrics metrics, String text, int width, int textJustification) throws CharacterDoesNotFitException {
        if (metrics == null || text == null || text.length() == 0) {
            throw new NullPointerException();
        }
        if ((textJustification & textJustification - 1) != 0 && textJustification >> 3 != 0) {
            textJustification = CENTER_JUSTIFY;
        }
        fm = metrics;
        txt = text;
        rect.width = width;
        fontHeight = fm.getHeight();
        justification = textJustification;
        createWords();
        createLines();
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
    private void createLines() throws CharacterDoesNotFitException {
        StringBuilder line = new StringBuilder();
        int w = 0;
        int wWord;
        int wWordWithSpace;
        boolean firstDoesNotFit;
        boolean firstDoesNotFitWithSpace;
        boolean didNotFit = false;
        for (final String word : words) {
            wWord = fm.stringWidth(word);
            wWordWithSpace = fm.stringWidth(word + " ");
            w += wWordWithSpace;
            if (w >= rect.width) {
                w -= wWordWithSpace;
                w += wWord;
            }
            firstDoesNotFit = wWord > rect.width;
            firstDoesNotFitWithSpace = wWordWithSpace > rect.width;
            if (w >= rect.width) {
                if (line.length() > 0 && didNotFit) {
                    System.out.println("Line: " + line);
                    line.deleteCharAt(line.length() - 1);
                    didNotFit = false;
                }
                if (firstDoesNotFit) {
                    didNotFit = true;
                    System.out.println("Non-fitting word: " + word);
                    int cWidth;
                    int totWidth = 0;
                    for (final char c : word.toCharArray()) {
                        cWidth = fm.stringWidth(Character.toString(c));
                        if (cWidth > rect.width) {
                            throw new CharacterDoesNotFitException("The character '" + c + "' with the current font " +
                                    "settings exceeds the width specified for this TextBounds object.");
                        }
                        totWidth += cWidth;
                        if (totWidth >= rect.width) {
                            totWidth = 0;
                            lines.add(line.toString());
                            line.setLength(0);
                            continue;
                        }
                        line.append(c);
                    }
                }
                w = 0;
                lines.add(line.toString());
                line.setLength(0);
                if (!firstDoesNotFit) {
                    line.append(firstDoesNotFitWithSpace ? word : word + " ");
                    w = firstDoesNotFitWithSpace ? wWord : wWordWithSpace;
                }
            }
            else {
                line.append(word + " ");
            }
        }
        lines.add(line.toString());
    }
    public final ArrayList<String> getLines() {
        return new ArrayList<>(lines);
    }
    public static void main(String [] args) throws IOException, CharacterDoesNotFitException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = img.createGraphics();
        String test = "The quick brown fox jumped over the lazy dogs.";
        g.setFont(new Font("sansserif", Font.PLAIN, 20));
        int fontHeight = g.getFontMetrics().getHeight();
        g.setColor(Color.white);
        g.fillRect(0, 0, width, height);
        g.setPaint(Color.blue);
        g.drawString(test, width/2, height/2);
        g.fillRect(width/2, height/2 + height/10, g.getFontMetrics().stringWidth(test), fontHeight);
        g.setPaint(Color.red);;
        g.fillRect(width/2, height/2 + height/10, testWidth, g.getFontMetrics().getHeight());
        TextBounds tb = new TextBounds(g.getFontMetrics(), test, testWidth, TextBounds.LEFT_JUSTIFY);
        System.out.println(tb.getWords());
        System.out.println(tb.getLines());
        int offset = 0;
        for (final String line : tb.getLines()) {
            g.drawString(line, width/2, height/2 + offset);
            offset += fontHeight;
        }
        ImageIO.write(img, "bmp", new File(path));
        g.dispose();
        img.flush();
    }
    private static final int width = 2000;
    private static final int height = 1500;
    private static final int testWidth = 50;
    private static final String path = "C:\\Users\\mario\\TextBounds.bmp";
}