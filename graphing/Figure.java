package greg.graphing;

import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.awt.Desktop; // for opening the file with openFigure() - throws Exception or fails if fig. not generated
import greg.misc.*;

public abstract class Figure {
    private static final short DEF_WIDTH = 2272;
    private static final short DEF_HEIGHT = 1704;
    public static final short MIN_WIDTH = 300;
    public static final short MIN_HEIGHT = 200;
    protected String path = System.getProperty("user.home") + System.getProperty("file.separator") + "Figure1.bmp";
    protected String imageFormat = "bmp";
    protected Color figureBackground = Color.white;
    protected int fullWidth = DEF_WIDTH;
    protected int fullHeight = DEF_HEIGHT;
    protected int IMAGE_TYPE = BufferedImage.TYPE_3BYTE_BGR;
    protected BufferedImage fullImage;
    protected Graphics2D fullImageGraphics;
    private Font xAxesLabelFont;
    private Font yAxesLabelFont;
    private Font titleFont;
    protected Font annotationFont;
    private Color xAxesLabelColour = Color.black;
    private Color yAxesLabelColour = Color.black;
    private Color titleColour = Color.black;
    protected Color annotationColour = Color.black;
    private Pair<Integer, Integer> titleCoords;
    private final ArrayList<Trio<Color, String, Pair<Integer, Integer>>> annotations = new ArrayList<>();
    private final ArrayList<Label> laterLabels = new ArrayList<>();
    private void createImage() {
        fullImage = new BufferedImage(fullWidth, fullHeight, IMAGE_TYPE);
        fullImageGraphics = fullImage.createGraphics();
        fullImageGraphics.setPaint(figureBackground);
        fullImageGraphics.fillRect(0, 0, fullWidth, fullHeight);
    }
    private void drawAnnotations() {
        if (annotations.isEmpty()) {
            return;
        }
        fullImageGraphics.setFont(annotationFont);
        for (final Trio<Color, String, Pair<Integer, Integer>> t : annotations) {
            fullImageGraphics.setPaint(t.first);
            fullImageGraphics.drawString(t.second, t.third.first, t.third.second);
        }
    }
    private void drawLabels() throws CharacterDoesNotFitException {
        for (final Label l : laterLabels) {
            if (!l.calculated) {
                l.calculate(fullImageGraphics, null);
            }
            l.draw();
        }
    }
    private boolean checkPath() {
        int dotIndex = path.lastIndexOf('.');
        if (dotIndex == -1) {
            path += ".bmp";
            return false;
        }
        if (dotIndex == path.length() - 1) {
            path += "bmp";
            return false;
        }
        imageFormat = path.substring(dotIndex + 1);
        return true;
    }
    void check_dim() {
        if (fullWidth < Figure.MIN_WIDTH) {
            fullWidth = Figure.MIN_WIDTH;
        }
        if (fullHeight < Figure.MIN_HEIGHT) {
            fullHeight = Figure.MIN_HEIGHT;
        }
    }
    private void set_defaults() {
        annotationFont = new Font("SANSSERIF", Font.PLAIN, fullWidth >= fullHeight ? fullHeight / 50 :
                fullWidth / 50);
        xAxesLabelFont = yAxesLabelFont = new Font("SANSSERIF", Font.PLAIN, fullWidth >= fullHeight ? fullHeight / 80 :
                fullWidth / 80);
        titleFont = new Font("SANSSERIF", Font.PLAIN, fullWidth >= fullHeight ? fullHeight / 40 : fullWidth / 40);
    }
    // ------------------------------------------------- constructors -------------------------------------------------
    public Figure() {
        set_defaults();
        createImage();
    }

    public Figure(String pathToFigure) {
        path = pathToFigure;
        set_defaults();
        createImage();
    }

    public Figure(String pathToFigure, byte R_background, byte G_background, byte B_background) {
        if (pathToFigure == null) {
            throw new NullPointerException();
        }
        path = pathToFigure;
        figureBackground = new Color(R_background, G_background, B_background);
        set_defaults();
        createImage();
    }

    public Figure(String pathToFigure, byte R_background, byte G_background, byte B_background, int figureWidth,
                  int figureHeight) {
        if (pathToFigure == null) {
            throw new NullPointerException();
        }
        path = pathToFigure;
        figureBackground = new Color(R_background, G_background, B_background);
        fullWidth = figureWidth;
        fullHeight = figureHeight;
        check_dim();
        set_defaults();
        createImage();
    }

    public Figure(String pathToFigure, Color backgroundColour) {
        if (pathToFigure == null || backgroundColour == null) {
            throw new NullPointerException();
        }
        path = pathToFigure;
        figureBackground = backgroundColour;
        set_defaults();
        createImage();
    }
    public Figure(String pathToFigure, Color backgroundColour, int figureWidth, int figureHeight) {
        if (pathToFigure == null || backgroundColour == null) {
            throw new NullPointerException();
        }
        path = pathToFigure;
        figureBackground = backgroundColour;
        fullWidth = figureWidth;
        fullHeight = figureHeight;
        check_dim();
        set_defaults();
        createImage();
    }
    public Figure(String pathToFigure, Color backgroundColour, int figureWidth, int figureHeight, int imageType)
            throws InvalidImageTypeException {
        if (pathToFigure == null || backgroundColour == null) {
            throw new NullPointerException();
        }
        if (!(imageType > 0 && imageType <= 12)) {
            throw new InvalidImageTypeException();
        }
        path = pathToFigure;
        figureBackground = backgroundColour;
        fullWidth = figureWidth;
        fullHeight = figureHeight;
        check_dim();
        set_defaults();
        createImage();
    }
    // -----------------------------------------------------------------------------------------------------------------
    public void clearFigure() {
        fullImageGraphics.dispose();
        fullImage.flush();
        fullImage = null;
        fullImageGraphics = null;
    }
    // ---------------------------------------------- setters and getters ----------------------------------------------
    public String getPath() {
        return path;
    }

    public boolean setPath(String pathToImage) {
        path = pathToImage;
        return checkPath();
    }
    public Color getFigureBackgroundColor() {
        return figureBackground;
    }
    public boolean setFigureBackgroundColor(Color color) {
        if (color == null) {
            return false;
        }
        fullImageGraphics.setPaint(color);
        fullImageGraphics.fillRect(0, 0, fullWidth, fullHeight);
        figureBackground = color;
        return true;
    }
    public boolean setXAxesLabelFont(Font font) {
        if (font == null) {
            return false;
        }
        xAxesLabelFont = font;
        return true;
    }

    public final Font getXAxesLabelFont() {
        return xAxesLabelFont;
    }

    public final boolean setXAxesLabelFontSize(int size) {
        if (size <= 0) {
            return false;
        }
        xAxesLabelFont = new Font("sansserif", Font.PLAIN, size);
        return true;
    }

    public final boolean setXAxesLabelColor(Color color) {
        if (color == null) {
            return false;
        }
        xAxesLabelColour = color;
        return true;
    }

    public boolean setYAxesLabelFont(Font font) {
        if (font == null) {
            return false;
        }
        yAxesLabelFont = font;
        return true;
    }

    public final Font getYAxesLabelFont() {
        return yAxesLabelFont;
    }

    public final boolean setYAxesLabelFontSize(int size) {
        if (size <= 0) {
            return false;
        }
        yAxesLabelFont = new Font("sansserif", Font.PLAIN, size);
        return true;
    }

    public final boolean setYAxesLabelColor(Color color) {
        if (color == null) {
            return false;
        }
        yAxesLabelColour = color;
        return true;
    }

    public final void setTitleFontSize(int size) {
        titleFont = new Font("SANSSERIF", Font.PLAIN, size);
    }

    public final boolean setTitleFont(Font font) {
        if (font == null) {
            return false;
        }
        titleFont = font;
        return true;
    }

    public final int getTitleFontSize() {
        return titleFont.getSize();
    }

    public final void setAnnotationFontSize(int size) {
        annotationFont = new Font("SANSSERIF", Font.PLAIN, size);
    }

    public final boolean setAnnotationFont(Font font) {
        if (font == null) {
            return false;
        }
        annotationFont = font;
        return true;
    }

    public final int getAnnotationFontSize() {
        return annotationFont.getSize();
    }

    public final boolean setTitleColor(Color color) {
        if (color == null) {
            return false;
        }
        titleColour = color;
        return true;
    }

    public final Color getTitleColor() {
        return titleColour;
    }

    public final boolean setAnnotationColor(Color color) {
        if (color == null) {
            return false;
        }
        annotationColour = color;
        return true;
    }

    public final Color getAnnotationColor() {
        return annotationColour;
    }
    public final int getFigureWidth() {
        return fullWidth;
    }
    public final int getFigureHeight() {
        return fullHeight;
    }
    // public abstract boolean setFigureDimensions(int newWidth, int newHeight);
    // ------------------------------------------------ text additions ------------------------------------------------
    public final boolean addAnnotationNow(String text, int x_pos, int y_pos, Color color) { // always single-line
        if (text == null ||  color == null || x_pos < 0 || y_pos < 0 || x_pos >= fullWidth || y_pos >= fullHeight ||
                text.isEmpty()) {
            return false;
        } // no need to create a Label object below for a single line of text
        Font currentFont = fullImageGraphics.getFont();
        Color currentColor = fullImageGraphics.getColor();
        fullImageGraphics.setPaint(color);
        fullImageGraphics.setFont(annotationFont);
        fullImageGraphics.drawString(text, x_pos, y_pos);
        fullImageGraphics.setFont(currentFont);
        fullImageGraphics.setPaint(currentColor);
        return true;
    }
    public final boolean addAnnotationNow(String text, int x_pos, int y_pos) {
        return addAnnotationNow(text, x_pos, y_pos, annotationColour);
    }
    public final boolean addAnnotationLater(String text, int x_pos, int y_pos, Color color) {
        if (text == null || color == null || x_pos < 0 || y_pos < 0 || x_pos >= fullWidth || y_pos >= fullHeight ||
                text.isEmpty()) {
            return false;
        }
        Trio<Color, String, Pair<Integer, Integer>> t = new Trio<>();
        t.first = color;
        t.second = text;
        t.third = new Pair<>();
        t.third.first = x_pos;
        t.third.second = y_pos;
        annotations.add(t);
        return true;
    }
    public final boolean addAnnotationLater(String text, int x_pos, int y_pos) {
        return addAnnotationLater(text, x_pos, y_pos, annotationColour);
    } // draws Label obj. immediately onto Figure - should therefore not be used for labels 'behind' Plot image, since
    public final boolean addLabelNow(Label label) throws CharacterDoesNotFitException { // it is later superimposed
        if (label == null) {
            return false;
        }
        if (!label.calculated) {
            label.calculate(fullImageGraphics, null);
        }
        label.draw();
        return true;
    }
    public final boolean addLabelLater(Label label) {
        if (label == null) {
            return false;
        }
        laterLabels.add(label);
        return true;
    }
    // -----------------------------------------------------------------------------------------------------------------
    public final void writeFigure(boolean freeMemory) throws IOException, CharacterDoesNotFitException {
        drawAnnotations();
        drawLabels();
        ImageIO.write(fullImage, imageFormat, new File(path));
        if (freeMemory) {
            clearFigure();
        }
    }
    public final boolean openFigure() { // if file exists and figure hasn't been generated, will open file anyway
        File f = new File(path);
        if (f.exists()) {
            try {
                Desktop.getDesktop().open(f);
            } catch(Exception e) { // there are various different exceptions that open() could throw
                return false;
            }
            return true;
        }
        return false;
    }
}