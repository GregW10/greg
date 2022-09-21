package greg.graphing;

import java.awt.Color;
import java.lang.Number;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;
import java.math.BigInteger;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.lang.Math;
import java.lang.Double;
import java.awt.BasicStroke;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.font.GlyphVector;
import java.awt.Rectangle;
import greg.misc.Pair;

public class Plot <T extends Number> extends Figure {
    private int width;
    private int height;
    private BufferedImage image;
    private Graphics2D image_graphics;
    private final ArrayList<ArrayList<Pair<BigDecimal, BigDecimal>>> plots = new ArrayList<>();
    private int axes_thickness;
    private int axes_thickness2;
    private Color axes_colour = Color.black;
    private Color axes_colour2 = Color.black;
    private int xtick_thickness;
    private int xtick_length;
    private int num_xticks = 10;
    private ArrayList<BigDecimal> xtick_positions = new ArrayList<>();
    private List<T> xTickTPositions;
    private int ytick_thickness;
    private int ytick_length;
    private int num_yticks = 8;
    private ArrayList<BigDecimal> ytick_positions = new ArrayList<>();
    private List<T> yTickTPositions;
    private int origin_x;
    private int origin_y;
    private int end_x;
    private int end_y;
    private BigDecimal plotmin_x;
    private BigDecimal plotmin_y;
    private BigDecimal plotmax_x;
    private BigDecimal plotmax_y;
    private BigDecimal min_x;
    private BigDecimal min_y;
    private BigDecimal max_x;
    private BigDecimal max_y;
    private final ArrayList<Pair<BigDecimal, Pair<Integer, Integer>>> xTickPixelPositions = new ArrayList<>();
    private final ArrayList<Pair<BigDecimal, Pair<Integer, Integer>>> yTickPixelPositions = new ArrayList<>();
    private Font xTickFont;
    private Font yTickFont;
    private Color xTickColour = Color.black;
    private Color yTickColour = Color.black;
    private boolean xTickThicknessSet = false;
    private boolean yTickThicknessSet = false;
    boolean secondaryAxes = false;
    boolean created = false;
    private boolean generated = false;
    private boolean isIntegral;
    private boolean isBigD;
    private boolean typeSet = false;
    private boolean outdated_xtick_pos;
    private boolean outdated_ytick_pos;
    private boolean xTickLabelExpNotation;
    private boolean xExpSet = false;
    private boolean yTickLabelExpNotation;
    private boolean yExpSet = false;
    private void fill_background() {
        if (!created) {
            image = new BufferedImage(width, height, super.IMAGE_TYPE);
            // image = super.fullImage.getSubimage((super.fullWidth-width)/2, (super.fullHeight-height)/2, width, height);
            image_graphics = image.createGraphics();
            created = true;
        }
        image_graphics.setPaint(super.figureBackground);
        image_graphics.fillRect(0, 0, width, height);
        generated = false;
    }
    private double gradient(double x1, double y1, double x2, double y2) {
        return (y2 - y1) / (x2 - x1);
    }
    private double intercept(double m, double x_point, double y_point) {
        return y_point - m*x_point;
    }
    private double thickness_x_bound(double x, double y, double thickness) {
        return thickness / (2.0*Math.cos(Math.atan(y/x)));
    }
    private double thickness_y_bound(double x, double y, double thickness) {
        return thickness / (2.0*Math.cos(Math.atan(x/y)));
    }
    private void create_axes() {
        image_graphics.setStroke(new BasicStroke(axes_thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        image_graphics.setPaint(axes_colour);
        image_graphics.drawLine(origin_x, origin_y, end_x, origin_y);
        image_graphics.drawLine(origin_x, origin_y, origin_x, end_y);
        image_graphics.fillRect(origin_x - (axes_thickness - 1)/2, origin_y - (axes_thickness - 1) / 2,
                (axes_thickness - 1) / 2, (axes_thickness - 1) / 2);
        if (secondaryAxes) {
            image_graphics.setStroke(new BasicStroke(axes_thickness2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            image_graphics.setPaint(axes_colour2);
            image_graphics.drawLine(origin_x + axes_thickness/2 + 1, end_y - axes_thickness2/2 - 1, end_x, end_y -
                    axes_thickness2/2 - 1);
            image_graphics.drawLine(end_x - axes_thickness2/2 - 1, origin_y + axes_thickness/2 + 1, end_x -
                    axes_thickness2/2 - 1, end_y);
            // image_graphics.drawLine(origin_x, end_y - 1, end_x, end_y - 1);
            // image_graphics.drawLine(end_x - 1, origin_y, end_x - 1, end_y);
            // image_graphics.fillRect(origin_x - (axes_thickness - 1)/2, end_y, (axes_thickness - 1) / 2,
            //         (axes_thickness2 - 1) / 2);
            // image_graphics.fillRect(end_x, origin_y - (axes_thickness - 1) / 2, (axes_thickness2 - 1) / 2,
            //         (axes_thickness - 1) / 2);
            // image_graphics.fillRect(end_x, end_y, (axes_thickness2 - 1) / 2, (axes_thickness2 - 1) / 2);
            // if (!axes_colour.equals(axes_colour2)) {
            //     image_graphics.fillRect(origin_x - (axes_thickness - 1)/2, end_y - (axes_thickness2 + 1)/2,
            //             (axes_thickness - 1) / 2, (axes_thickness2 + 1) / 2);
            //     image_graphics.fillRect(end_x - (axes_thickness2 + 1) / 2, origin_y - (axes_thickness - 1) / 2,
            //             (axes_thickness2 + 1) / 2, (axes_thickness - 1) / 2);
            // }
        }
        create_x_ticks();
        create_y_ticks();
    }
    // private void transformCoordinates() {
    //     for (final Pair<BigDecimal, Pair<Integer, Integer>> p : xTickPixelPositions) {
    //         // p.second.second *= -1;
    //         // p.second.second -= height;
    //     }
    //     for (final Pair<BigDecimal, Pair<Integer, Integer>> p : yTickPixelPositions) {
    //         // p.second.second *= -1;
    //         // p.second.second -= height;
    //     }
    // }
    private static String forceBigDecimalExponentNotation(BigDecimal bd) {
        if (bd == null) {
            return "";
        }
        boolean negative = bd.compareTo(BigDecimal.ZERO) < 0;
        if (negative) {
            bd = bd.abs();
        }
        bd = bd.stripTrailingZeros();
        int scale = bd.scale();
        int precision = bd.precision();
        int n;
        int lastIndex;
        StringBuilder retBuilder = new StringBuilder(bd.toPlainString());
        if (bd.compareTo(BigDecimal.ONE) >= 0) {
            n = precision - scale - 1;
            int dotIndex;
            if ((dotIndex = retBuilder.indexOf(".")) != -1) {
                retBuilder.deleteCharAt(dotIndex);
            }
            int nonZeroCount = 0;
            for (final char c : retBuilder.toString().toCharArray()) {
                if (c != '0') {
                    ++nonZeroCount;
                }
            }
            if (nonZeroCount > 1) {
                retBuilder.insert(1, '.');
            }
            while (retBuilder.charAt((lastIndex = retBuilder.length() - 1)) == '0') {
                retBuilder.deleteCharAt(lastIndex);
            }
            if (negative) {
                retBuilder.insert(0, "-");
            }
            return retBuilder.toString() + "E" + n;
        }
        else if (bd.compareTo(BigDecimal.ZERO) == 0) {
            return "0";
        }
        n = scale - precision + 1;
        retBuilder.delete(0, n + 1);
        if (precision > 1) {
            retBuilder.insert(1, ".");
        }
        // while (retBuilder.charAt((lastIndex = retBuilder.length() - 1)) == '0') {
        //     retBuilder.deleteCharAt(lastIndex);
        // }
        if (negative) {
            retBuilder.insert(0, "-");
        }
        return retBuilder.toString() + "E-" + n;
    }
    private void drawXTickLabels() {
        image_graphics.setPaint(xTickColour);
        image_graphics.setFont(xTickFont);
        FontMetrics fm = image_graphics.getFontMetrics();
        int totalWidth = 0;
        for (final BigDecimal b : xtick_positions) {
            totalWidth += fm.stringWidth(b.toPlainString());
        }
        boolean plain = xExpSet ? !xTickLabelExpNotation : totalWidth < end_x - origin_x - (end_x - origin_x)/10;
        int h = fm.getAscent();
        int w;
        String str;
        for (Pair<BigDecimal, Pair<Integer, Integer>> p : xTickPixelPositions) {
            if (plain) {
                str = p.first.toPlainString();
            }
            else {
                str = forceBigDecimalExponentNotation(p.first);
            }
            w = fm.stringWidth(str);
            image_graphics.drawString(str, p.second.first - w/2, p.second.second + h);
        }
    }
    private void drawYTickLabels() {
        image_graphics.setPaint(yTickColour);
        image_graphics.setFont(yTickFont);
        FontMetrics fm = image_graphics.getFontMetrics();
        int maxWidth = 0;
        int testWidth;
        for (final BigDecimal b : ytick_positions) {
            testWidth = fm.stringWidth(b.toPlainString());
            if (testWidth > maxWidth) {
                maxWidth = testWidth;
            }
        }
        boolean plain = yExpSet ? !yTickLabelExpNotation : maxWidth <= origin_x - axes_thickness/2 - ytick_length -
                width/200;
        GlyphVector gv = yTickFont.layoutGlyphVector(image_graphics.getFontRenderContext(), ("0").toCharArray(), 0, 1,
                Font.LAYOUT_LEFT_TO_RIGHT); // all numbers have the same ascent, so only need to use one (such as zero)
        Rectangle rect = gv.getPixelBounds(image_graphics.getFontRenderContext(), 0, 0);
        int h = rect.height;
        int w;
        String str;
        for (Pair<BigDecimal, Pair<Integer, Integer>> p : yTickPixelPositions) {
            if (plain) {
                str = p.first.toPlainString();
            }
            else {
                str = forceBigDecimalExponentNotation(p.first);
            }
            w = fm.stringWidth(str);
            image_graphics.drawString(str, p.second.first - w - width/200, p.second.second + h/2);
        }
    }
    private static ArrayList<BigDecimal> getTickPositionsBase(BigDecimal lower_bound, BigDecimal upper_bound) {
        if (lower_bound.equals(upper_bound)) {
            return new ArrayList<>();
        }
        if (lower_bound.compareTo(upper_bound) > 0) {
            BigDecimal temp = lower_bound;
            lower_bound = upper_bound;
            upper_bound = temp;
        }
        ArrayList<BigDecimal> retpoints = new ArrayList<>();
        BigDecimal view_range = upper_bound.subtract(lower_bound);
        BigDecimal num = new BigDecimal(lower_bound.toString());
        int n = (int) Math.floor(Math.log10(view_range.doubleValue()) - 1);
        BigDecimal interval = n < 0 ? BigDecimal.ONE.divide(BigDecimal.TEN.pow(-n), -n, RoundingMode.HALF_UP) :
                BigDecimal.TEN.pow(n);
        int num_ticks = 1;
        BigDecimal one;
        BigDecimal two;
        BigDecimal four;
        BigDecimal five;
        while (num.compareTo(upper_bound) <= 0) {
            if (n >= 0) {
                one = BigDecimal.TEN.pow(n);
                two = BigDecimal.TEN.pow(n).multiply(BigDecimal.valueOf(2L));
                four = BigDecimal.TEN.pow(n).multiply(BigDecimal.valueOf(4L));
                five = BigDecimal.TEN.pow(n).multiply(BigDecimal.valueOf(5L));
            }
            else {
                one = BigDecimal.ONE.divide(BigDecimal.TEN.pow(-n), -n, RoundingMode.HALF_UP);
                two = BigDecimal.ONE.divide(BigDecimal.TEN.pow(-n), -n, RoundingMode.HALF_UP).
                        multiply(BigDecimal.valueOf(2L));
                four = BigDecimal.ONE.divide(BigDecimal.TEN.pow(-n), -n, RoundingMode.HALF_UP).
                        multiply(BigDecimal.valueOf(4L));
                five = BigDecimal.ONE.divide(BigDecimal.TEN.pow(-n), -n, RoundingMode.HALF_UP).
                        multiply(BigDecimal.valueOf(5L));
            }
            num = num.add(interval);
            if (++num_ticks > 10) {
                if (interval.equals(one))
                    interval = two;
                else if (interval.equals(two))
                    interval = four;
                else if (interval.equals(four))
                    interval = five;
                else {
                    ++n;
                    interval = n < 0 ? BigDecimal.ONE.divide(BigDecimal.TEN.pow(-n), -n, RoundingMode.HALF_UP) :
                            BigDecimal.TEN.pow(n);
                }
                num = lower_bound;
                num_ticks = 1;
            }
        }
        BigDecimal first_val = BigDecimal.ZERO;
        BigDecimal prev_val = BigDecimal.ZERO;
        BigDecimal times = BigDecimal.ZERO;
        if (lower_bound.compareTo(BigDecimal.ZERO) < 0) {
            if (upper_bound.compareTo(interval.multiply(new BigDecimal("-2.0"))) < 0) {
                times = (upper_bound.divideToIntegralValue(interval)).add(BigDecimal.valueOf(2L));
            }
            while (first_val.compareTo(lower_bound) >= 0) {
                prev_val = first_val;
                first_val = interval.multiply(new BigDecimal(times.toString()));
                times = times.subtract(BigDecimal.ONE);
            }
            first_val = prev_val;
            times = times.add(BigDecimal.valueOf(3L));
        }
        else if (lower_bound.compareTo(BigDecimal.ZERO) > 0) {
            if (lower_bound.compareTo(interval.multiply(BigDecimal.valueOf(2L))) > 0) {
                times = (lower_bound.divideToIntegralValue(interval)).subtract(BigDecimal.valueOf(2L));
            }
            while (first_val.compareTo(lower_bound) < 0) {
                first_val = interval.multiply(new BigDecimal(times.toString()));
                times = times.add(BigDecimal.ONE);
            }
        }
        if (n < 0)
            retpoints.add(first_val.setScale(-n, RoundingMode.HALF_UP));
        else
            retpoints.add(first_val.setScale(0, RoundingMode.HALF_UP));
        BigDecimal val = first_val;
        times = BigDecimal.ONE;
        while (val.compareTo(upper_bound) <= 0) {
            val = first_val.add(times.multiply(interval)).setScale(-n, RoundingMode.HALF_UP);
            retpoints.add(val);
            times = times.add(BigDecimal.ONE);
        }
        retpoints.remove(retpoints.size() - 1); // like std::vector::pop_back()
        return retpoints;
    }
    private static ArrayList<BigDecimal> getTickPositions(BigDecimal Min, BigDecimal Max) {
        if (Min.equals(Max)) {
            return new ArrayList<>();
        }
        if (Min.compareTo(Max) > 0) {
            BigDecimal temp = Min;
            Min = Max;
            Max = temp;
        }
        BigDecimal data_range = Max.subtract(Min);
        BigDecimal pad = data_range.divide(BigDecimal.TEN, 2*data_range.scale(), RoundingMode.HALF_UP);
        BigDecimal lower_bound = Min.subtract(pad);
        BigDecimal upper_bound = Max.add(pad);
        return getTickPositionsBase(lower_bound, upper_bound);
    }
    private static <NumType extends Number & Comparable<? super NumType>> ArrayList<BigDecimal>
                    getTickPositions(List<NumType> data) {
        if (data.isEmpty() || data.size() == 1) {
            return null;
        }
        NumType dataMinNT = getMin(data).get();
        NumType dataMaxNT = getMax(data).get();
        BigDecimal Min = dataMinNT.getClass() == Byte.class || dataMinNT.getClass() == Short.class ||
                dataMinNT.getClass() == Integer.class || dataMinNT.getClass() == BigInteger.class ||
                dataMinNT.getClass() == Long.class ? BigDecimal.valueOf(dataMinNT.longValue()) :
                (dataMinNT.getClass() == BigDecimal.class ? (BigDecimal) dataMinNT :
                        BigDecimal.valueOf(dataMinNT.doubleValue()));
        BigDecimal Max = dataMaxNT.getClass() == Byte.class || dataMaxNT.getClass() == Short.class ||
                dataMaxNT.getClass() == Integer.class || dataMaxNT.getClass() == BigInteger.class ||
                dataMaxNT.getClass() == Long.class ? BigDecimal.valueOf(dataMaxNT.longValue()) :
                (dataMaxNT.getClass() == BigDecimal.class ? (BigDecimal) dataMaxNT :
                        BigDecimal.valueOf(dataMaxNT.doubleValue()));
        return getTickPositions(Min, Max);
    }
    private void create_x_ticks() {
        if (num_xticks == 0) {
            return;
        }
        BigDecimal orgX = BigDecimal.valueOf((long) origin_x);
        BigDecimal xAxesRange = BigDecimal.valueOf((long) end_x).subtract(BigDecimal.valueOf((long) origin_x)).add(BigDecimal.ONE);
        BigDecimal pixel_pos;
        BigDecimal range = plotmax_x.subtract(plotmin_x);
        BigDecimal ratio = xAxesRange.divide(range, (range.scale() + (int)
                Math.ceil(Math.log10(xAxesRange.doubleValue())))*10, RoundingMode.HALF_UP);
        image_graphics.setStroke(new BasicStroke(xtick_thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        image_graphics.setPaint(axes_colour);
        int x;
        int y1 = origin_y - axes_thickness/2;
        int y2 = y1 - xtick_length;
        if (xtick_positions.isEmpty() || outdated_xtick_pos) {
            xtick_positions = getTickPositionsBase(plotmin_x, plotmax_x);
        }
        Pair<BigDecimal, Pair<Integer, Integer>> p;
        for (BigDecimal pos : xtick_positions) {
            p = new Pair<>();
            pixel_pos = orgX.add(pos.subtract(plotmin_x).multiply(ratio));
            x = pixel_pos.setScale(0, RoundingMode.HALF_UP).intValue();
            image_graphics.drawLine(x, y1, x, y2);
            p.first = pos;
            p.second = new Pair<>(x, -y2 + height);
            xTickPixelPositions.add(p);
        }
    }
    private void create_y_ticks() {
        if (num_yticks == 0) {
            return;
        }
        BigDecimal orgY = BigDecimal.valueOf((long) origin_y);
        BigDecimal yAxesRange = BigDecimal.valueOf((long) end_y).subtract(BigDecimal.valueOf((long) origin_y)).add(BigDecimal.ONE);
        BigDecimal pixel_pos;
        BigDecimal range = plotmax_y.subtract(plotmin_y);
        BigDecimal ratio = yAxesRange.divide(range, (range.scale() + (int)
                Math.ceil(Math.log10(yAxesRange.doubleValue())))*10, RoundingMode.HALF_UP);
        image_graphics.setStroke(new BasicStroke(ytick_thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        image_graphics.setPaint(axes_colour);
        int y = 0;
        int x1 = origin_x - axes_thickness/2;
        int x2 = x1 - ytick_length;
        if (ytick_positions.isEmpty() || outdated_ytick_pos) {
            ytick_positions = getTickPositionsBase(plotmin_y, plotmax_y);
        }
        Pair<BigDecimal, Pair<Integer, Integer>> p;
        for (BigDecimal pos : ytick_positions) {
            p = new Pair<>();
            pixel_pos = orgY.add(pos.subtract(plotmin_y).multiply(ratio));
            y = pixel_pos.setScale(0, RoundingMode.HALF_UP).intValue();
            image_graphics.drawLine(x1, y, x2, y);
            p.first = pos;
            p.second = new Pair<>(x2, -y + height);
            yTickPixelPositions.add(p);
        }
    }
    private int get_max_points() {
        int retval = 0;
        int size;
        for (final ArrayList<Pair<BigDecimal, BigDecimal>> plot : plots) {
            size = plot.size(); // in case of multiple plots, it is faster to only call size() once per iteration
            if (size > retval) {
                retval = size;
            }
        }
        return retval;
    }
    private void set_mins() {
        min_x = plots.get(0).get(0).first;
        min_y = plots.get(0).get(0).second;
        for (final ArrayList<Pair<BigDecimal, BigDecimal>> plot : plots) {
            for (final Pair<BigDecimal, BigDecimal> pair : plot) {
                if (pair.first.compareTo(min_x) < 0) {
                    min_x = pair.first;
                }
                if (pair.second.compareTo(min_y) < 0) {
                    min_y = pair.second;
                }
            }
        }
    }
    private void set_maxes() {
        max_x = plots.get(0).get(0).first;
        max_y = plots.get(0).get(0).second;
        for (final ArrayList<Pair<BigDecimal, BigDecimal>> plot : plots) {
            for (final Pair<BigDecimal, BigDecimal> pair : plot) {
                if (pair.first.compareTo(max_x) > 0) {
                    max_x = pair.first;
                }
                if (pair.second.compareTo(max_y) > 0) {
                    max_y = pair.second;
                }
            }
        }
    }
    private void expand_xlims() {
        BigDecimal min_xtick = getMin(xtick_positions).orElse(BigDecimal.valueOf(Double.MIN_VALUE));
        BigDecimal max_xtick = getMax(xtick_positions).orElse(BigDecimal.valueOf(Double.MAX_VALUE));
        if (min_xtick.compareTo(plotmin_x) < 0) plotmin_x = min_xtick;
        if (max_xtick.compareTo(plotmax_x) > 0) plotmax_x = max_xtick;
    }
    private void expand_ylims() {
        BigDecimal min_ytick = getMin(ytick_positions).orElse(BigDecimal.valueOf(Double.MIN_VALUE));
        BigDecimal max_ytick = getMax(ytick_positions).orElse(BigDecimal.valueOf(Double.MAX_VALUE));
        if (min_ytick.compareTo(plotmin_y) < 0) plotmin_y = min_ytick;
        if (max_ytick.compareTo(plotmax_y) > 0) plotmax_y = max_ytick;
    }
    private void drawPlotToFigure(boolean free_mem) {
        super.fullImageGraphics.drawImage(image, (super.fullWidth - width)/2, (super.fullHeight - height)/2, null);
        //boolean retval = ImageIO.write(image, super.imageFormat, new File(super.path));
        if (free_mem) {
            clearPlot();
        }
    }
    void set_defaults() {
        width = super.fullWidth*8/10;
        height = super.fullHeight*8/10;
        axes_thickness = width >= height ? height / 200 : width / 200;
        axes_thickness += axes_thickness % 2 == 0 ? 1 : 0;
        axes_thickness2 = axes_thickness;
        // ytick_thickness = (xtick_thickness = ((axes_thickness / 2) % 2 == 0 ? (axes_thickness / 2) + 1 :
        //         axes_thickness / 2));
        xtick_thickness = ytick_thickness = axes_thickness;
        ytick_length = (xtick_length = axes_thickness*2);
        origin_x = width / 8;
        origin_y = height / 8;
        end_x = width - origin_x;
        end_y = height - origin_y;
        yTickFont = xTickFont = new Font("SANSSERIF", Font.PLAIN, width >= height ? height / 50 : width / 50);
    }
    private void setType(T val) {
        if (typeSet) {
            return;
        }
        isIntegral = val.getClass() == Byte.class || val.getClass() == Short.class || val.getClass() ==
                     Integer.class || val.getClass() == BigInteger.class || val.getClass() == Long.class;
        isBigD = val.getClass() == BigDecimal.class;
        typeSet = true;
    }

    private void flipImage() {
        // image_graphics.scale(1, -1);
        // image_graphics.translate(0, -height);
        AffineTransform at = AffineTransform.getScaleInstance(1, -1);
        at.translate(0, -height);
        image = (new AffineTransformOp(at, AffineTransformOp.TYPE_NEAREST_NEIGHBOR)).filter(image, null);
        image_graphics.dispose();
        image_graphics = image.createGraphics();
    }
    // ------------------------------------------ private above, public below ------------------------------------------
    // --------------------------------------------- start of constructors ---------------------------------------------
    public Plot() {
        set_defaults();
    }
    public Plot(String pathToFigure) {
        super(pathToFigure);
        set_defaults();
    }
    public Plot(String pathToFigure, byte R_background, byte G_background, byte B_background) {
        super(pathToFigure, R_background, G_background, B_background);
        set_defaults();
    }
    public Plot(String pathToFigure, byte R_background, byte G_background, byte B_background, int figureWidth,
                int figureHeight) {
        super(pathToFigure, R_background, G_background, B_background, figureWidth, figureHeight);
        set_defaults();
    }
    public Plot(String pathToFigure, Color backgroundColour) {
        super(pathToFigure, backgroundColour);
        set_defaults();
    }
    public Plot(String pathToFigure, Color backgroundColour, int figureWidth, int figureHeight) {
        super(pathToFigure, backgroundColour, figureWidth, figureHeight);
        set_defaults();
    }
    public Plot(String pathToFigure, Color backgroundColour, int figureWidth, int figureHeight, int imageType)
            throws InvalidImageTypeException {
        super(pathToFigure, backgroundColour, figureWidth, figureHeight, imageType);
        set_defaults();
    }
    // ---------------------------------------------- end of constructors ----------------------------------------------
    public final boolean addData(final List<T> x, final List<T> y) {
        if (x.size() == 0 || y.size() == 0) {
            return false;
        }
        boolean wasEmpty = plots.isEmpty();
        BigDecimal prevMinX = min_x;
        BigDecimal prevMaxX = max_x;
        BigDecimal prevMinY = min_y;
        BigDecimal prevMaxY = max_y;
        if (wasEmpty) {
            min_x = max_x = isIntegral ? BigDecimal.valueOf(x.get(0).longValue()) :
                    (isBigD ? (BigDecimal) x.get(0) : BigDecimal.valueOf(x.get(0).doubleValue()));
            min_y = max_y = isIntegral ? BigDecimal.valueOf(y.get(0).longValue()) :
                    (isBigD ? (BigDecimal) y.get(0) : BigDecimal.valueOf(y.get(0).doubleValue()));
        }
        int size = x.size() < y.size() ? x.size() : y.size();
        ArrayList<Pair<BigDecimal, BigDecimal>> plot = new ArrayList<>(size);
        BigDecimal x_val;
        BigDecimal y_val;
        for (int i = 0; i < size; ++i) {
            x_val = isIntegral ? BigDecimal.valueOf(x.get(i).longValue()) : BigDecimal.valueOf(x.get(i).doubleValue());
            y_val = isIntegral ? BigDecimal.valueOf(y.get(i).longValue()) : BigDecimal.valueOf(y.get(i).doubleValue());
            if (x_val.compareTo(min_x) < 0) {
                min_x = x_val;
            }
            if (x_val.compareTo(max_x) > 0) {
                max_x = x_val;
            }
            if (y_val.compareTo(min_y) < 0) {
                min_y = y_val;
            }
            if (y_val.compareTo(max_y) > 0) {
                max_y = y_val;
            }
            plot.add(new Pair<BigDecimal, BigDecimal>(x_val, y_val));
        }
        plots.add(plot);
        BigDecimal x_range = max_x.subtract(min_x);
        BigDecimal y_range = max_y.subtract(min_y);
        BigDecimal xpad = x_range.divide(BigDecimal.TEN, x_range.scale() + 1, RoundingMode.HALF_UP);
        BigDecimal ypad = y_range.divide(BigDecimal.TEN, y_range.scale() + 1, RoundingMode.HALF_UP);
        plotmin_x = min_x.subtract(xpad);
        plotmax_x = max_x.add(xpad);
        plotmin_y = min_y.subtract(ypad);
        plotmax_y = max_y.add(ypad);
        if (!wasEmpty) {
            if (!prevMinX.equals(min_x) || !prevMaxX.equals(max_x)) {
                outdated_xtick_pos = true;
            }
            if (!prevMinY.equals(min_y) || !prevMaxY.equals(max_y)) {
                outdated_ytick_pos = true;
            }
        }
        return true;
    }

    public final boolean addData(T [] xValues, T [] yValues) {
        return addData(List.of(xValues), List.of(yValues));
    }

    public final boolean addXPadding(int number_of_pixels) { // if number is negative, plot is stretched in x-direction
        if (number_of_pixels == 0) {
            return false;
        }
        if (number_of_pixels > 0) {
            if (end_x - origin_x - 2*number_of_pixels < width/10) {
                return false;
            }
        }
        else {
            if (origin_x + number_of_pixels < 0 || end_x - number_of_pixels > width) {
                return false;
            }
        }
        origin_x += number_of_pixels;
        end_x -= number_of_pixels;
        if (xTickTPositions != null) {
            if (!setXTickPositions(xTickTPositions)) {
                origin_x -= number_of_pixels;
                end_x += number_of_pixels;
                return false;
            }
        }
        return true;
    }

    public final boolean addYPadding(int number_of_pixels) { // if number is negative, plot is stretched in x-direction
        if (number_of_pixels == 0) {
            return false;
        }
        if (number_of_pixels > 0) {
            if (end_y - origin_y - 2*number_of_pixels < height/10) {
                return false;
            }
        }
        else {
            if (origin_y + number_of_pixels < 0 || end_y - number_of_pixels > height) {
                return false;
            }
        }
        origin_y += number_of_pixels;
        end_y -= number_of_pixels;
        if (yTickTPositions != null) {
            if (!setYTickPositions(yTickTPositions)) {
                origin_y -= number_of_pixels;
                end_y += number_of_pixels;
                return false;
            }
        }
        return true;
    }

    public final boolean displaceAxesHorizontally(int number_of_pixels) { // positive for moving right
        if (number_of_pixels > width - end_x) {
            return false;
        }
        origin_x += number_of_pixels;
        end_x += number_of_pixels;
        return true;
    }

    public final boolean displaceAxesVertically(int number_of_pixels) { // positive for moving up
        if (number_of_pixels > height - end_y) {
            return false;
        }
        origin_y += number_of_pixels;
        end_y += number_of_pixels;
        return true;
    }

    public final void displayData() {
        if (plots.isEmpty()) {
            System.out.println("No data!");
            return;
        }
        int num = 0;
        int count;
        for (final ArrayList<Pair<BigDecimal, BigDecimal>> plot : plots) {
            System.out.println("Dataset " + ++num);
            count = 0;
            for (final Pair<BigDecimal, BigDecimal> pair : plot) {
                System.out.println("Point " + ++count + " -> x: " + pair.first + ", y: " + pair.second);
            }
            System.out.println();
        }
        System.out.println();
    }

    public final void clearData() {
        plots.clear();
        outdated_xtick_pos = true;
        outdated_ytick_pos = true;
    }

    public final void clearPlot() {
        if (created) {
            image_graphics.dispose();
            image.flush();
            image = null;
            image_graphics = null;
            created = false;
            generated = false;
        }
    }

    public final void clearAll() {
        clearData();
        super.clearFigure();
        clearPlot();
        set_defaults();
        super.figureBackground = Color.white;
        axes_colour = Color.black;
        axes_colour2 = Color.black;
        super.path = System.getProperty("user.dir") + System.getProperty("file.separator") + "Figure1.bmp";
        super.IMAGE_TYPE = BufferedImage.TYPE_3BYTE_BGR;
    }
    // ----------------------------------------- start of getters and setters -----------------------------------------
    public int getImageType() {
        return super.IMAGE_TYPE;
    }

    public boolean setImageType(int image_type) {
        if (!(image_type > 0 && image_type <= 12)) {
            return false;
        }
        super.IMAGE_TYPE = image_type;
        return true;
    }

    public final int getAxesThickness() {
        return axes_thickness;
    }

    public final boolean setAxesThickness(int thickness_in_pixels) { // in terms of pixels
        if (thickness_in_pixels > height/20 || thickness_in_pixels > width/20) {
            return false;
        }
        axes_thickness = thickness_in_pixels % 2 == 0 ? thickness_in_pixels + 1 : thickness_in_pixels;
        if (!xTickThicknessSet) {
            xtick_thickness = axes_thickness;
        }
        if (!yTickThicknessSet) {
            ytick_thickness = axes_thickness;
        }
        return true;
    }

    public final boolean setAxesColor(Color col) {
        if (super.figureBackground.equals(col)) {
            return false;
        }
        axes_colour = col;
        return true;
    }

    public final Color getAxesColor() {
        return axes_colour;
    }

    public final int getSecondaryAxesThickness() {
        return axes_thickness2;
    }

    public final int getXTickLabelFontSize() {
        return xTickFont.getSize();
    }

    public final void setXTickLabelFontSize(int size) {
        xTickFont = new Font("SANSSERIF", Font.PLAIN, size);
    }

    public final boolean setXTickLabelFont(Font font) {
        if (font == null) {
            return false;
        }
        xTickFont = font;
        return true;
    }

    public final int getYTickLabelFontSize() {
        return yTickFont.getSize();
    }

    public final void setYTickLabelFontSize(int size) {
        yTickFont = new Font("SANSSERIF", Font.PLAIN, size);
    }

    public final boolean setYTickLabelFont(Font font) {
        if (font == null) {
            return false;
        }
        yTickFont = font;
        return true;
    }

    public final boolean setXTickLabelColor(Color color) {
        if (color == null) {
            return false;
        }
        xTickColour = color;
        return true;
    }

    public final boolean setYTickLabelColor(Color color) {
        if (color == null) {
            return false;
        }
        yTickColour = color;
        return true;
    }

    public final Color getXTickLabelColor() {
        return xTickColour;
    }

    public final Color getYTickLabelColor() {
        return yTickColour;
    }

    // public final boolean setNumberOfXTicks(int number_of_xticks) {
    //     if (number_of_xticks*xtick_thickness >= end_x - origin_x) {
    //         return false;
    //     }
    //     num_xticks = number_of_xticks;
    //     return true;
    // }
//
    // public final boolean setNumberOfYTicks(int number_of_yticks) {
    //     if (number_of_yticks*ytick_thickness >= end_y - origin_y) {
    //         return false;
    //     }
    //     num_yticks = number_of_yticks;
    //     return true;
    // }

    public final boolean setXTickThickness(int thickness) {
        if (thickness > axes_thickness) {
            return false;
        }
        if (thickness % 2 == 0) {
            ++thickness;
        }
        xtick_thickness = thickness;
        xTickThicknessSet = true;
        return true;
    }

    public final boolean setYTickThickness(int thickness) {
        if (thickness > axes_thickness) {
            return false;
        }
        if (thickness % 2 == 0) {
            ++thickness;
        }
        ytick_thickness = thickness;
        yTickThicknessSet = true;
        return true;
    }

    public final boolean setXTickLength(int length) {
        if (length > 6*axes_thickness) {
            return false;
        }
        xtick_length = length;
        return true;
    }

    public final boolean setYTickLength(int length) {
        if (length > 6*axes_thickness) {
            return false;
        }
        ytick_length = length;
        return true;
    }

    public final boolean setXTickPositions(final List<T> positions) { // all ticks must always be visible, so the
        if (plots.isEmpty()) {                                        // xlims are expanded if necessary
            return false;
        }
        int size = positions.size();
        if (size == 0 || size*xtick_thickness >= end_x - origin_x) {
            return false;
        }
        num_xticks = size;
        setType(positions.get(0));
        if (!xtick_positions.isEmpty()) xtick_positions.clear();
        if (isIntegral) {
            for (final T elem : positions) {
                xtick_positions.add(BigDecimal.valueOf(elem.longValue()));
            }
        }
        else if (isBigD) {
            for (final T elem : positions) {
                xtick_positions.add((BigDecimal) elem);
            }
        }
        else {
            for (final T elem : positions) {
                xtick_positions.add(BigDecimal.valueOf(elem.doubleValue()));
            }
        }
        expand_xlims();
        outdated_xtick_pos = false;
        outdated_ytick_pos = false;
        xTickTPositions = positions;
        return true;
    }

    public final boolean setXTickPositions(final T [] positions) {
        return setXTickPositions(List.of(positions));
    }

    public final boolean setYTickPositions(final List<T> positions) { // all ticks must always be visible, so the
        if (plots.isEmpty()) {                                        // ylims are expanded if necessary
            return false;
        }
        int size = positions.size();
        if (size == 0 || size*ytick_thickness >= end_y - origin_y) {
            return false;
        }
        num_yticks = size;
        setType(positions.get(0));
        if (!ytick_positions.isEmpty()) ytick_positions.clear();
        if (isIntegral) {
            for (final T elem : positions) {
                ytick_positions.add(BigDecimal.valueOf(elem.longValue()));
            }
        }
        else if (isBigD) {
            for (final T elem : positions) {
                xtick_positions.add((BigDecimal) elem);
            }
        }
        else {
            for (final T elem : positions) {
                ytick_positions.add(BigDecimal.valueOf(elem.doubleValue()));
            }
        }
        expand_ylims();
        outdated_xtick_pos = false;
        outdated_ytick_pos = false;
        yTickTPositions = positions;
        return true;
    }

    public final boolean setYTickPositions(final T [] positions) {
        return setYTickPositions(List.of(positions));
    }

    public final boolean setXLimits(T lower_bound, T upper_bound) { // in terms of T units // if T not Int., ass. to be FP
        BigDecimal tempMin;
        BigDecimal tempMax;
        setType(lower_bound);
        if (isIntegral) {
            tempMin = BigDecimal.valueOf(lower_bound.longValue());
            tempMax = BigDecimal.valueOf(upper_bound.longValue());
        }
        else if (isBigD) {
            tempMin = (BigDecimal) lower_bound;
            tempMax = (BigDecimal) upper_bound;
        }
        else {
            tempMin = BigDecimal.valueOf(lower_bound.doubleValue());
            tempMax = BigDecimal.valueOf(upper_bound.doubleValue());
        }
        if (tempMin.compareTo(tempMax) >= 0) {
            return false;
        }
        if (!xtick_positions.isEmpty()) {
            if (getMin(xtick_positions).get().compareTo(tempMin) > 0) {
                plotmin_x = tempMin;
            }
            if (getMax(xtick_positions).get().compareTo(tempMax) < 0) {
                plotmax_x = tempMax;
            }
        }
        return true;
    }

    public final boolean setYLimits(T lower_bound, T upper_bound) { // in terms of T units // if T not Int., ass. to be FP
        BigDecimal tempMin;
        BigDecimal tempMax;
        setType(lower_bound);
        if (isIntegral) {
            tempMin = BigDecimal.valueOf(lower_bound.longValue());
            tempMax = BigDecimal.valueOf(upper_bound.longValue());
        }
        else if (isBigD) {
            tempMin = (BigDecimal) lower_bound;
            tempMax = (BigDecimal) upper_bound;
        }
        else {
            tempMin = BigDecimal.valueOf(lower_bound.doubleValue());
            tempMax = BigDecimal.valueOf(upper_bound.doubleValue());
        }
        if (tempMin.compareTo(tempMax) >= 0) {
            return false;
        }
        if (!ytick_positions.isEmpty()) {
            if (getMin(ytick_positions).get().compareTo(tempMin) > 0) {
                plotmin_y = tempMin;
            }
            if (getMax(ytick_positions).get().compareTo(tempMax) < 0) {
                plotmax_y = tempMax;
            }
        }
        return true;
    }
    public final void setXTickLabelsExponentNotation(boolean exponent) {
        xTickLabelExpNotation = exponent;
        xExpSet = true;
    }
    public final void setYTickLabelsExponentNotation(boolean exponent) {
        yTickLabelExpNotation = exponent;
        yExpSet = true;
    }
    // ------------------------------------------ end of getters and setters ------------------------------------------
    public final void addSecondaryAxes() {
        secondaryAxes = true;
    }
    public final boolean addSecondaryAxes(int thickness_in_pixels) {
        if (thickness_in_pixels > height/20 || thickness_in_pixels > width/20) {
            return false;
        }
        axes_thickness2 = thickness_in_pixels % 2 == 0 ? thickness_in_pixels + 1 : thickness_in_pixels;
        secondaryAxes = true;
        return true;
    }
    public final boolean addSecondaryAxes(int thickness_in_pixels, Color color) {
        if (color.equals(super.figureBackground)) {
            return false;
        }
        axes_colour2 = color;
        return addSecondaryAxes(thickness_in_pixels);
    }
    public final void removeSecondaryAxes() {
        secondaryAxes = false;
    }
    public boolean generatePlot(boolean freePlot) {
        if (plots.isEmpty()) {
            return false;
        }
        super.check_dim(); // change this
        // checkPath();
        fill_background();
        create_axes();
        flipImage();
        drawXTickLabels();
        drawYTickLabels();
        // drawTitle();
        // drawAnnotations();
        // drawXAxesLabel();
        // drawYAxesLabel();
        drawPlotToFigure(freePlot);
        generated = true;
        return true;
    }
    public final boolean writeImage(boolean free_image) throws IOException {
        if (!generated) {
            return false;
        }
        drawPlotToFigure(image != null && free_image);
        super.writeFigure(free_image);
        return true;
    }
    public final boolean generatePlotAndWrite(boolean free_image) throws IOException {
        if (!generatePlot(image != null && free_image)) {
            return false;
        }
        return writeImage(free_image);
    }
    public static <type extends Number & Comparable<? super type>> Optional<type> getMin(final List<type> list) {
        if (list.isEmpty()) {
            return Optional.empty();
        }
        type min = list.get(0);
        for (final type elem : list) {
            if (elem.compareTo(min) < 0) {
                min = elem;
            }
        }
        return Optional.of(min);
    }
    public static <type extends Number & Comparable<? super type>> Optional<type> getMax(final List<type> list){
        if (list.isEmpty()) {
            return Optional.empty();
        }
        type max = list.get(0);
        for (final type elem : list){
            if (elem.compareTo(max) > 0){
                max = elem;
            }
        }
        return Optional.of(max);
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
};
