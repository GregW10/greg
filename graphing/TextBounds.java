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
import greg.misc.Pair;

public class TextBounds {
    private final FontMetrics fm;
    private final String txt;
    private final Rectangle rect = new Rectangle();
    private final ArrayList<String> words = new ArrayList<>();
    private final ArrayList<Pair<String, Rectangle>> lines = new ArrayList<>();
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
        rect.height = fontHeight * lines.size();
    }
    TextBounds(Graphics2D graphics, String text, int width, int textJustification) throws CharacterDoesNotFitException {
        if (graphics == null || text == null || text.length() == 0) {
            throw new NullPointerException();
        }
        if ((textJustification & textJustification - 1) != 0 && textJustification >> 3 != 0) {
            textJustification = CENTER_JUSTIFY;
        }
        fm = graphics.getFontMetrics();
        txt = text;
        rect.width = width;
        fontHeight = fm.getHeight();
        justification = textJustification;
        createWords();
        createLines();
        rect.height = fontHeight * lines.size();
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
        int lineWidth;
        for (final char c : word.toCharArray()) {
            cWidth = fm.stringWidth(Character.toString(c));
            if (cWidth > rect.width) {
                throw new CharacterDoesNotFitException("The character '" + c + "' with the current font " +
                        "settings exceeds the width specified for this TextBounds object.");
            }
            totWidth += cWidth;
            if (totWidth >= rect.width && characters.length() > 0) {
                lineWidth = fm.stringWidth(characters.toString());
                lines.add(new Pair<>(characters.toString(), new Rectangle(switch (justification) {
                    case LEFT_JUSTIFY -> 0;
                    case CENTER_JUSTIFY -> (rect.width - lineWidth)/2; // if rect.width - lineWidth is odd, then the
                    case RIGHT_JUSTIFY -> rect.width - lineWidth; // text is offset by 1 pix. to the right of centre
                    default -> 0;
                }, lines.size()*fontHeight - fontHeight, lineWidth, fontHeight)));
                characters.setLength(0);
                totWidth = cWidth;
            }
            characters.append(c);
        }
        lineWidth = fm.stringWidth(characters.toString());
        lines.add(new Pair<>(characters.toString(), new Rectangle(switch (justification) {
            case LEFT_JUSTIFY -> 0;
            case CENTER_JUSTIFY -> (rect.width - lineWidth)/2; // if rect.width - lineWidth is odd, then the
            case RIGHT_JUSTIFY -> rect.width - lineWidth; // text is offset by 1 pix. to the right of centre
            default -> 0;
        }, lines.size()*fontHeight - fontHeight, lineWidth, fontHeight)));
    }
    private void createLines() throws CharacterDoesNotFitException {
        StringBuilder line = new StringBuilder();
        int w = 0;
        int width;
        boolean hasSpace = false;
        int lineWidth;
        for (final String word : words) {
            if (line.length() > 0) {
                if (fm.stringWidth(line + " ") > rect.width) {
                    lineWidth = fm.stringWidth(line.toString());
                    lines.add(new Pair<>(line.toString(), new Rectangle(switch (justification) {
                        case LEFT_JUSTIFY -> 0;
                        case CENTER_JUSTIFY -> (rect.width - lineWidth)/2; // if rect.width - lineWidth is odd, then the
                        case RIGHT_JUSTIFY -> rect.width - lineWidth; // text is offset by 1 pix. to the right of centre
                        default -> 0;
                    }, (lines.size())*fontHeight - fontHeight, lineWidth, fontHeight)));
                    line.setLength(0);
                    w = fm.stringWidth(word);
                    if (w > rect.width) {
                        splitWord(word);
                        line.setLength(0);
                        line.append(lines.get(lines.size() - 1).first);
                        w = fm.stringWidth(line.toString());
                        lines.remove(lines.size() - 1);
                    }
                    else {
                        line.append(word);
                    }
                    continue;
                }
                else {
                    line.append(' ');
                    w = fm.stringWidth(line.toString());
                    hasSpace = true;
                }
            }
            w += (width = fm.stringWidth(word));
            if (w >= rect.width) {
                if (hasSpace) {
                    line.setLength(line.length() - 1);
                    hasSpace = false;
                }
                if (width > rect.width) {
                    if (!lines.isEmpty()) {
                        lineWidth = fm.stringWidth(line.toString());
                        lines.add(new Pair<>(line.toString(), new Rectangle(switch (justification) {
                            case LEFT_JUSTIFY -> 0;
                            case CENTER_JUSTIFY -> (rect.width - lineWidth)/2;
                            case RIGHT_JUSTIFY -> rect.width - lineWidth;
                            default -> 0;
                        }, (lines.size())*fontHeight - fontHeight, lineWidth, fontHeight)));
                    }
                    splitWord(word);
                    line.setLength(0);
                    line.append(lines.get(lines.size() - 1).first);
                    lines.remove(lines.size() - 1);
                    w = fm.stringWidth(line.toString());
                    continue;
                }
                else {
                    lineWidth = fm.stringWidth(line.toString());
                    lines.add(new Pair<>(line.toString(), new Rectangle(switch (justification) {
                        case LEFT_JUSTIFY -> 0;
                        case CENTER_JUSTIFY -> (rect.width - lineWidth)/2;
                        case RIGHT_JUSTIFY -> rect.width - lineWidth;
                        default -> 0;
                    }, (lines.size())*fontHeight - fontHeight, lineWidth, fontHeight)));
                }
                line.setLength(0);
                line.append(word);
                w = 0;
                continue;
            }
            line.append(word);
        }
        lineWidth = fm.stringWidth(line.toString());
        lines.add(new Pair<>(line.toString(), new Rectangle(switch (justification) {
            case LEFT_JUSTIFY -> 0;
            case CENTER_JUSTIFY -> (rect.width - lineWidth)/2;
            case RIGHT_JUSTIFY -> rect.width - lineWidth;
            default -> 0;
        }, (lines.size() + 1)*fontHeight, lineWidth, fontHeight)));
    }
    public final ArrayList<Pair<String, Rectangle>> getLines() {
        ArrayList<Pair<String, Rectangle>> ret = new ArrayList<>();
        for (final Pair<String, Rectangle> p : lines) {
            ret.add(new Pair<>(p.first, new Rectangle(p.second))); // no deep-copy of String because it is immutable
        }
        return ret;
    }
    public static void main(String [] args) throws IOException, CharacterDoesNotFitException {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = img.createGraphics();
        String test = "The quick brown fox jumped over the lazy dogs.";
        g.setFont(new Font("sansserif", Font.PLAIN, 50));
        int fontHeight = g.getFontMetrics().getHeight();
        g.setColor(Color.white);
        g.fillRect(0, 0, width, height);
        g.setPaint(Color.blue);
        g.drawString(test, width/4, height/4);
        g.fillRect(width/4, height/4 + height/10, g.getFontMetrics().stringWidth(test), fontHeight);
        g.setPaint(Color.red);
        g.fillRect(width/4, height/4 + height/10, testWidth, g.getFontMetrics().getHeight());
        TextBounds tb = new TextBounds(g, test, testWidth, TextBounds.LEFT_JUSTIFY);
        System.out.println("Words: " + tb.getWords());
        System.out.println("Lines: " + tb.getLines());
        int offset = 0;
        for (final Pair<String, Rectangle> line : tb.getLines()) {
            g.setPaint(Color.red);
            line.second.x = width/4;
            line.second.y += height/4;
            g.fill(line.second);
            g.setPaint(Color.black);
            g.drawString(line.first, width/4, height/4 + offset);
            System.out.println(line.first);
            offset += fontHeight;
        }
        ImageIO.write(img, "bmp", new File(path));
        g.dispose();
        img.flush();
    }
    private static final int width = 2000;
    private static final int height = 1500;
    private static final int testWidth = 100;
    private static final String path = "C:\\Users\\mario\\TextBounds2.bmp";
}