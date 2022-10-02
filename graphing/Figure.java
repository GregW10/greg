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
    private String titleText = null;
    private String xAxesLabelText = null;
    private String yAxesLabelText = null;
    private Color xAxesLabelColour = Color.black;
    private Color yAxesLabelColour = Color.black;
    private Color titleColour = Color.black;
    protected Color annotationColour = Color.black;
    private int maxTitleWidth;
    private boolean titleWidthSet = false;
    private Pair<Integer, Integer> titleCoords;
    private final ArrayList<Trio<Color, String, Pair<Integer, Integer>>> annotations = new ArrayList<>();
    private final ArrayList<Label> laterLabels = new ArrayList<>();
    protected int plotPercentage = 72; // percentage of Figure width and height that Plot will take up
    protected int plotPadding = 0;
    protected boolean cleared = true; // there is no image initially
    private boolean generated = false; // same
    protected void createImage() {
        fullImage = new BufferedImage(fullWidth, fullHeight, IMAGE_TYPE);
        fullImageGraphics = fullImage.createGraphics();
        fullImageGraphics.setPaint(figureBackground);
        fullImageGraphics.fillRect(0, 0, fullWidth, fullHeight);
        cleared = false;
        generated = false; // the image exists, but has nothing on it, so has NOT been generated yet
    }
    private void drawTitle() throws CharacterDoesNotFitException {
        if (titleText == null) { // no title if this is the case
            return;
        }
        int plotWidth = this.fullWidth*this.plotPercentage/100;
        int plotHeight = this.fullHeight*this.plotPercentage/100;
        int topOfPlot = (this.fullHeight - plotHeight)/2;
        int presentWidth = titleWidthSet ? maxTitleWidth : plotWidth;
        TextBounds tb = new TextBounds(this.fullImageGraphics, titleText, titleFont, 0, 0, presentWidth,
                TextBounds.CENTER_JUSTIFY);
        if (tb.lines.size() == 1) {
            int w = tb.lines.get(0).second.width;
            presentWidth = w + w/20;
        }
        (new Label(this.fullImageGraphics, titleText, titleFont, Color.blue, titleColour, this.fullWidth/2 -
                presentWidth/2, topOfPlot - tb.rect.height - this.plotPadding, presentWidth, 0d,
                Label.CENTER_JUSTIFY)).draw();
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
        xAxesLabelFont = yAxesLabelFont = new Font("SANSSERIF", Font.PLAIN, fullWidth >= fullHeight ? fullHeight / 40 :
                fullWidth / 40);
        titleFont = new Font("SANSSERIF", Font.PLAIN, fullWidth >= fullHeight ? fullHeight / 20 : fullWidth / 20);
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
        cleared = true;
        generated = false;
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
        if (color == null || cleared) {
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
    public final boolean setMaxTitleWidth(int pixels) {
        if (pixels <= 0 || pixels > this.fullWidth) {
            return false;
        }
        maxTitleWidth = pixels;
        titleWidthSet = true;
        return true;
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
    public final boolean addTitle(String text, Font f) {
        if (text == null) {
            return false;
        }
        titleText = text;
        if (f != null) {
            titleFont = f;
        }
        return true;
    }
    public final boolean addTitle(String text) {
        return addTitle(text, null);
    }
    public abstract boolean addPlotPadding(int padding);
    public final boolean addAnnotationNow(String text, int x_pos, int y_pos, Color color) { // always single-line
        if (text == null ||  color == null || x_pos < 0 || y_pos < 0 || x_pos >= fullWidth || y_pos >= fullHeight ||
                text.isEmpty() || cleared) {
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
    public final synchronized void generateFigure() throws CharacterDoesNotFitException {
        if (cleared) {
            createImage();
        }
        if (generated) { // resets the figure if it has been previously generated
            this.fullImageGraphics.setPaint(figureBackground);
            this.fullImageGraphics.fillRect(0, 0, this.fullWidth, this.fullHeight);
        }
        drawAnnotations();
        drawLabels();
        drawTitle();
        generated = true;
    }
    public final synchronized void write(boolean freeMemory) throws IOException, CharacterDoesNotFitException {
        if (cleared) {
            generateFigure();
        }
        ImageIO.write(fullImage, imageFormat, new File(path));
        if (freeMemory) {
            clearFigure();
        }
    }
    public final synchronized void generateFigureAndWrite(boolean freeMemory) throws IOException,
            CharacterDoesNotFitException {
        generateFigure();
        write(freeMemory);
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
    public final static class Colors {
        public final static Color black = new Color(0, 0, 0);
        public final static Color white = new Color(255, 255, 255);
        public final static Color blue = new Color(0, 0, 255);
        public final static Color green = new Color(0, 255, 0);
        public final static Color red = new Color(255, 0, 0);
        public final static Color pink = new Color(255, 105, 180);
        public final static Color cerise = new Color(222, 49, 99);
        public final static Color fuchsia = new Color(255, 0, 255);
        public final static Color neonPink = new Color(255, 16, 240);
        public final static Color pinkOrange = new Color(248, 152, 128);
        public final static Color purple = new Color(128, 0, 128);
        public final static Color salmon = new Color(250, 128, 114);
        public final static Color watermelonPink = new Color(227, 115, 131);
        public final static Color orange = new Color(255, 165, 0);
        public final static Color gold = new Color(255, 215, 0);
        public final static Color yellow = new Color(255, 255, 0);
        public final static Color lavender = new Color(230, 230, 250);
        public final static Color indigo = new Color(75, 0, 130);
        public final static Color violet = new Color(238, 130, 238);
        public final static Color limeGreen = new Color(50, 205, 50);
        public final static Color forestGreen = new Color(34, 139, 34);
        public final static Color darkGreen = new Color(0, 100, 0);
        public final static Color aqua = new Color(0, 255, 255);
        public final static Color skyBlue = new Color(135, 206, 235);
        public final static Color royalBlue = new Color(65, 105, 225);
        public final static Color navy = new Color(0, 0, 128);
        public final static Color wheat = new Color(245, 222, 179);
        public final static Color tan = new Color(210, 180, 140);
        public final static Color rosyBrown = new Color(188, 143, 143);
        public final static Color peru = new Color(205, 133, 63);
        public final static Color chocolate = new Color(210, 105, 30);
        public final static Color brown = new Color(165, 42, 42);
        public final static Color maroon = new Color(128, 0, 0);
        public final static Color snow = new Color(255, 250, 250);
        public final static Color honeyDew = new Color(240, 255, 240);
        public final static Color azure = new Color(240, 255, 255);
        public final static Color ghostWhite = new Color(248, 248, 255);
        public final static Color beige = new Color(245, 245, 220);
        public final static Color ivory = new Color(255, 255, 240);
        public final static Color gainsboro = new Color(220, 220, 220);
        public final static Color silver = new Color(192, 192, 192);
        public final static Color gray = new Color(128, 128, 128);
        public final static Color slateGray = new Color(112, 128, 144);
    }
}