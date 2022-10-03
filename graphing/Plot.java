package greg.graphing;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.math.BigInteger;
import java.util.List;
import java.util.ArrayList;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.awt.BasicStroke;
import java.io.IOException;
import java.util.Optional;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.font.GlyphVector;
import java.awt.Rectangle;
import javax.swing.JFrame; // for displaying the Figure without writing it to a file
import javax.swing.JPanel;
import java.awt.event.WindowAdapter;
import greg.misc.Pair;
import greg.misc.Trio;

public class Plot <T extends Number> extends Figure {
    private int width;
    private int height;
    protected BufferedImage image;
    protected Graphics2D image_graphics;
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
    private int gridlineThickness;
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
    private Color plotBackground = null;
    private Color gridlineColour = new Color(220, 220, 220);
    private boolean xMinLimitSet = false;
    private boolean xMaxLimitSet = false;
    private boolean yMinLimitSet = false;
    private boolean yMaxLimitSet = false;
    private boolean xTickThicknessSet = false;
    private boolean yTickThicknessSet = false;
    private boolean secondaryAxes = false;
    private boolean secondaryAxesThicknessSet = false;
    private boolean gridlines = false;
    private boolean gridlineThicknessSet = false;
    private boolean firstXGridlinePopped = false;
    private boolean firstYGridlinePopped = false;
    private boolean lastXGridlinePopped = false;
    private boolean lastYGridlinePopped = false;
    private boolean somethingPopped = false;
    private boolean created = false;
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
    private final ArrayList<Trio<Color, String, Pair<Integer, Integer>>> annotations = new ArrayList<>();
    private final ArrayList<Label> labels = new ArrayList<>();
    private void fill_background() {
        if (!created) {
            image = new BufferedImage(width, height, super.IMAGE_TYPE);
            image_graphics = image.createGraphics();
            created = true;
        }
        image_graphics.setPaint(plotBackground == null ? super.figureBackground : plotBackground);
        image_graphics.fillRect(0, 0, width, height);
        generated = false;
    }
    private void drawAnnotations() {
        if (annotations.isEmpty()) {
            return;
        }
        image_graphics.setFont(super.annotationFont);
        for (final Trio<Color, String, Pair<Integer, Integer>> t : annotations) {
            image_graphics.setPaint(t.first);
            image_graphics.drawString(t.second, t.third.first, t.third.second);
        }
    }
    private void drawLabels() {
        for (final Label l : labels) {
            if (l instanceof PlotLabel<? extends Number> pLabel) {
                BigDecimal xPosition;
                BigDecimal yPosition;
                BigDecimal w;
                if (pLabel.xPos instanceof BigDecimal) {
                    xPosition = (BigDecimal) pLabel.xPos;
                    yPosition = (BigDecimal) pLabel.yPos;
                    w = (BigDecimal) pLabel.w;
                }
                else if (pLabel.xPos instanceof Float || pLabel.xPos instanceof Double) {
                    xPosition = BigDecimal.valueOf(pLabel.xPos.doubleValue());
                    yPosition = BigDecimal.valueOf(pLabel.yPos.doubleValue());
                    w = BigDecimal.valueOf(pLabel.w.doubleValue());
                }
                else {
                    xPosition = BigDecimal.valueOf(pLabel.xPos.longValue());
                    yPosition = BigDecimal.valueOf(pLabel.yPos.longValue());
                    w = BigDecimal.valueOf(pLabel.w.longValue());
                }
                BigDecimal XRatio = getXRatio();
                BigDecimal YRatio = getYRatio();
                l.rect.x = getXPixelPos(xPosition, XRatio);
                l.rect.y = -getYPixelPos(yPosition, YRatio) + height;
                l.rect.width = w.multiply(BigDecimal.valueOf(Math.sqrt(Math.pow(Math.cos(l.rotation)*XRatio.
                        doubleValue(), 2d) + Math.pow(Math.sin(l.rotation)*YRatio.doubleValue(), 2d)))).intValue();
                l.unlock();
            }
            if (!l.calculated) {
                l.calculate(this.image_graphics, null);
            }
            l.draw();
        }
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
        Pair<Integer, Integer> diff = create_ticks();
        image_graphics.setStroke(new BasicStroke(axes_thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        image_graphics.setPaint(axes_colour);
        image_graphics.drawLine(origin_x, origin_y, end_x, origin_y);
        image_graphics.drawLine(origin_x, origin_y, origin_x, end_y);
        image_graphics.fillRect(origin_x - (axes_thickness - 1)/2, origin_y - (axes_thickness - 1) / 2,
                (axes_thickness - 1) / 2, (axes_thickness - 1) / 2);
        if (secondaryAxes) {
            int realThickness = secondaryAxesThicknessSet ? axes_thickness2 : axes_thickness;
            int onePixelCorrection = realThickness == 1 ? 1 : 0;
            image_graphics.setStroke(new BasicStroke(realThickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            image_graphics.setPaint(axes_colour2);
            image_graphics.drawLine(origin_x + axes_thickness/2 + 1, end_y - realThickness/2 - 1 + diff.second, end_x +
                    diff.first - onePixelCorrection, end_y - realThickness/2 - 1 + diff.second);
            image_graphics.drawLine(end_x - realThickness/2 - 1 + diff.first, origin_y + axes_thickness/2 + 1, end_x -
                    realThickness/2 - 1 + diff.first, end_y + diff.second - onePixelCorrection);
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
    private BigDecimal getXRatio() {
        BigDecimal xAxesRange = BigDecimal.valueOf(end_x).subtract(BigDecimal.valueOf(origin_x));//.add(BigDecimal.ONE);
        BigDecimal range = plotmax_x.subtract(plotmin_x);
        return xAxesRange.divide(range, (range.scale() + (int)
                Math.ceil(Math.log10(xAxesRange.doubleValue())))*10, RoundingMode.HALF_UP);
    }
    private int getXPixelPos(BigDecimal xPosition) { // convert x-axis coordinate to a real pixel x-coordinate
        if (xPosition == null) { // overkill, but I can't help myself
            return 0;
        }
        return BigDecimal.valueOf(origin_x).add(xPosition.subtract(plotmin_x).multiply(getXRatio())).
                setScale(0, RoundingMode.HALF_UP).intValue();
    }
    private int getXPixelPos(BigDecimal xPosition, BigDecimal XRatio) { // method allows for XRatio to be calc. once
        if (xPosition == null) {
            return 0;
        }
        return BigDecimal.valueOf(origin_x).add(xPosition.subtract(plotmin_x).multiply(XRatio)).
                setScale(0, RoundingMode.HALF_UP).intValue();
    }
    private BigDecimal getYRatio() {
        BigDecimal yAxesRange = BigDecimal.valueOf(end_y).subtract(BigDecimal.valueOf(origin_y));//.add(BigDecimal.ONE);
        BigDecimal range = plotmax_y.subtract(plotmin_y);
        return yAxesRange.divide(range, (range.scale() + (int)
                Math.ceil(Math.log10(yAxesRange.doubleValue())))*10, RoundingMode.HALF_UP);
    }
    private int getYPixelPos(BigDecimal yPosition) { // convert y-axis coordinate to a real pixel y-coordinate
        if (yPosition == null) {
            return 0;
        }
        return BigDecimal.valueOf(origin_y).add(yPosition.subtract(plotmin_y).multiply(getYRatio())).
                setScale(0, RoundingMode.HALF_UP).intValue();
    }
    private int getYPixelPos(BigDecimal yPosition, BigDecimal YRatio) { // method allows for YRatio to be calc. once
        if (yPosition == null) {
            return 0;
        }
        return BigDecimal.valueOf(origin_y).add(yPosition.subtract(plotmin_y).multiply(YRatio)).
                setScale(0, RoundingMode.HALF_UP).intValue();
    }
    private Pair<Integer, Integer> create_ticks() {
        if (plots.isEmpty() || num_xticks == 0 || num_yticks == 0) {
            return new Pair<>(0, 0);
        }
        BigDecimal xRatio = getXRatio();
        BigDecimal yRatio = getYRatio();
        if (xtick_positions.isEmpty() || outdated_xtick_pos) {
            xtick_positions = getTickPositionsBase(plotmin_x, plotmax_x);
        }
        if (ytick_positions.isEmpty() || outdated_ytick_pos) {
            ytick_positions = getTickPositionsBase(plotmin_y, plotmax_y);
        }
        int diff_x = getXPixelPos(xtick_positions.get(xtick_positions.size() - 1), xRatio) + axes_thickness/2 + 1-end_x;
        if (diff_x > 0) {
            origin_x -= diff_x/2;
            end_x -= diff_x/2;
        }
        int diff_y = getYPixelPos(ytick_positions.get(ytick_positions.size() - 1), yRatio) + axes_thickness/2 + 1-end_y;
        if (diff_y > 0) {
            origin_y -= diff_y/2;
            end_y -= diff_y/2;
        }
        int x;
        int y1 = origin_y + axes_thickness/2 + 1;
        int y2 = y1 - axes_thickness - xtick_length;
        int y;
        int x1 = origin_x + axes_thickness/2 + 1;
        int x2 = x1 - axes_thickness - ytick_length;
        image_graphics.setPaint(axes_colour);
        image_graphics.setStroke(new BasicStroke(xtick_thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        Pair<BigDecimal, Pair<Integer, Integer>> p_x;
        for (BigDecimal pos : xtick_positions) {
            p_x = new Pair<>();
            x = getXPixelPos(pos, xRatio);
            image_graphics.drawLine(x, y1, x, y2);
            p_x.first = pos;
            p_x.second = new Pair<>(x, -y2 + height);
            xTickPixelPositions.add(p_x);
        }
        image_graphics.setStroke(new BasicStroke(ytick_thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        Pair<BigDecimal, Pair<Integer, Integer>> p_y;
        for (BigDecimal pos : ytick_positions) {
            p_y = new Pair<>();
            y = getYPixelPos(pos, yRatio);
            image_graphics.drawLine(x1, y, x2, y);
            p_y.first = pos;
            p_y.second = new Pair<>(x2, -y + height);
            yTickPixelPositions.add(p_y);
        }
        if (diff_x < 0) {
            diff_x = 0;
        }
        if (diff_y < 0) {
            diff_y = 0;
        }
        if (gridlines) {
            if (!gridlineThicknessSet) {
                gridlineThickness = xtick_thickness >= ytick_thickness ? ytick_thickness : xtick_thickness;
            }
            if (gridlineThickness > axes_thickness) { // if not, ugly
                gridlineThickness = axes_thickness;
            }
            int secondAxesOffset = (secondaryAxes ? axes_thickness2 : 0);
            int y_offset = origin_y + axes_thickness/2 + 1;
            int upto_y = end_y - secondAxesOffset + diff_y;
            int x_offset = origin_x + axes_thickness/2 + 1;
            int upto_x = end_x - secondAxesOffset + diff_x;
            image_graphics.setStroke(new BasicStroke(gridlineThickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            image_graphics.setPaint(gridlineColour);
            if (!somethingPopped) {
                for (final Pair<BigDecimal, Pair<Integer, Integer>> pair_x : xTickPixelPositions) {
                    image_graphics.drawLine(pair_x.second.first, y_offset, pair_x.second.first, upto_y);
                }
                for (final Pair<BigDecimal, Pair<Integer, Integer>> pair_y : yTickPixelPositions) {
                    y = height - pair_y.second.second;
                    image_graphics.drawLine(x_offset, y, upto_x, y);
                }
            }
            else {
                Pair<BigDecimal, Pair<Integer, Integer>> pair_x;
                int xStartCount = firstXGridlinePopped ? 1 : 0;
                int xEndCount = lastXGridlinePopped ? xTickPixelPositions.size() - 1 : xTickPixelPositions.size();
                for (; xStartCount < xEndCount; ++xStartCount) {
                    pair_x = xTickPixelPositions.get(xStartCount);
                    image_graphics.drawLine(pair_x.second.first, y_offset, pair_x.second.first, upto_y);
                }
                int yStartCount = firstYGridlinePopped ? 1 : 0;
                int yEndCount = lastYGridlinePopped ? yTickPixelPositions.size() - 1 : yTickPixelPositions.size();
                for (; yStartCount < yEndCount; ++yStartCount) {
                    y = height - yTickPixelPositions.get(yStartCount).second.second;
                    image_graphics.drawLine(x_offset, y, upto_x, y);
                }
            }
        }
        return new Pair<>(diff_x, diff_y);
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
        if (!created) {
            return;
        }
        if (super.cleared) {
            super.createImage();
        }
        super.fullImageGraphics.drawImage(image, (super.fullWidth - width)/2, (super.fullHeight - height)/2, null);
        if (free_mem) {
            clearPlot();
        }
    }
    void set_defaults() {
        width = super.fullWidth*super.plotPercentage/100;
        height = super.fullHeight*super.plotPercentage/100;
        axes_thickness = width >= height ? height / 200 : width / 200;
        axes_thickness += axes_thickness % 2 == 0 ? 1 : 0;
        axes_thickness2 = axes_thickness;
        // ytick_thickness = (xtick_thickness = ((axes_thickness / 2) % 2 == 0 ? (axes_thickness / 2) + 1 :
        //         axes_thickness / 2));
        xtick_thickness = ytick_thickness = axes_thickness;
        ytick_length = (xtick_length = axes_thickness*2);
        origin_x = width / 12;
        origin_y = height / 12;
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
            plot.add(new Pair<>(x_val, y_val));
        }
        plots.add(plot);
        BigDecimal x_range = max_x.subtract(min_x);
        BigDecimal y_range = max_y.subtract(min_y);
        BigDecimal xpad = x_range.divide(BigDecimal.TEN, x_range.scale() + 1, RoundingMode.HALF_UP);
        BigDecimal ypad = y_range.divide(BigDecimal.TEN, y_range.scale() + 1, RoundingMode.HALF_UP);
        if (!xMinLimitSet) { // if limits are set, the only way to forcibly undo them is by setting tick locations
            plotmin_x = min_x.subtract(xpad);
        }
        if (!xMaxLimitSet) {
            plotmax_x = max_x.add(xpad);
        }
        if (!yMinLimitSet) {
            plotmin_y = min_y.subtract(ypad);
        }
        if (!yMaxLimitSet) {
            plotmax_y = max_y.add(ypad);
        }
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
    public final boolean addYPadding(int number_of_pixels) { // if number is negative, plot is stretched in y-direction
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
    @Override
    public final boolean addPlotPadding(int pixels) {
        super.titleOffset += pixels; // prefer to avoid the 3 func. calls to super.move...()
        super.xAxisLabelOffset += pixels;
        super.yAxisLabelOffset += pixels;
        if (super.titleOffset < 0) {
            super.titleOffset = 0;
        }
        if (super.xAxisLabelOffset < 0) {
            super.xAxisLabelOffset = 0;
        }
        if (super.yAxisLabelOffset < 0) {
            super.yAxisLabelOffset = 0;
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
        String dataset;
        int len;
        for (final ArrayList<Pair<BigDecimal, BigDecimal>> plot : plots) {
            dataset = "Dataset " + ++num;
            System.out.println(dataset);
            System.out.print("--------");
            len = dataset.length() - 8;
            for (int i = 0; i < len; ++i)
                System.out.print('-');
            System.out.print('\n');
            count = 0;
            for (final Pair<BigDecimal, BigDecimal> pair : plot) {
                System.out.println("Point " + ++count + " -> x: " + pair.first + ", y: " + pair.second);
            }
            System.out.println();
        }
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
    public final void setPlotBackgroundColor(Color col) {
        plotBackground = col; // OK if null
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
        if (thickness > axes_thickness || thickness <= 0) {
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
        if (thickness > axes_thickness || thickness <= 0) {
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
    public final boolean setGridlineThickness(int thickness_in_pixels) {
        if (thickness_in_pixels <= 0) {
            return false;
        }
        gridlineThickness = thickness_in_pixels % 2 == 0 ? thickness_in_pixels + 1 : thickness_in_pixels;
        gridlineThicknessSet = true;
        return true;
    }
    public final boolean setGridlineColor(Color col) {
        if (col == null) {
            return false;
        }
        gridlineColour = col;
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
        xTickTPositions.addAll(positions);
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
                ytick_positions.add((BigDecimal) elem);
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
        yTickTPositions.addAll(positions);
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
                xMinLimitSet = true;
            }
            if (getMax(xtick_positions).get().compareTo(tempMax) < 0) {
                plotmax_x = tempMax;
                xMaxLimitSet = true;
            }
        }
        else {
            plotmin_x = tempMin;
            plotmax_x = tempMax;
            xMinLimitSet = true;
            xMaxLimitSet = true;
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
                yMinLimitSet = true;
            }
            if (getMax(ytick_positions).get().compareTo(tempMax) < 0) {
                plotmax_y = tempMax;
                yMaxLimitSet = true;
            }
        }
        else {
            plotmin_y = tempMin;
            plotmax_y = tempMax;
            yMinLimitSet = true;
            yMaxLimitSet = true;
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
    public final boolean drawLine(int xPos1, int yPos1, int xPos2, int yPos2, int thickness, Color col) {
        if (thickness <= 0) {
            return false;
        }
        java.awt.Stroke currentStroke = image_graphics.getStroke();
        Color currentColor = image_graphics.getColor();
        image_graphics.setStroke(new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
        if (col != null) {
            image_graphics.setPaint(col);
        }
        image_graphics.drawLine(xPos1, yPos1, xPos2, yPos2);
        image_graphics.setStroke(currentStroke);
        image_graphics.setPaint(currentColor);
        return true;
    }
    public final void addSecondaryAxes() {
        secondaryAxes = true;
    }
    public final boolean addSecondaryAxes(int thickness_in_pixels) {
        if (thickness_in_pixels > height/20 || thickness_in_pixels > width/20 || thickness_in_pixels <= 0) {
            return false;
        }
        axes_thickness2 = thickness_in_pixels % 2 == 0 ? thickness_in_pixels + 1 : thickness_in_pixels;
        secondaryAxes = true;
        secondaryAxesThicknessSet = true;
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
    public final void addGridlines() {
        gridlines = true;
    }
    public final void popFirstXGridline() {
        firstXGridlinePopped = true;
        somethingPopped = true;
    }
    public final void popFirstYGridline() {
        firstYGridlinePopped = true;
        somethingPopped = true;
    }
    public final void popLastXGridline() {
        lastXGridlinePopped = true;
        somethingPopped = true;
    }
    public final void popLastYGridline() {
        lastYGridlinePopped = true;
        somethingPopped = true;
    }
    public boolean addAnnotation(String text, T x_pos, T y_pos, Color color) {
        if (text == null || color == null || x_pos == null || y_pos == null || text.isEmpty()) {
            return false;
        }
        if (!typeSet) {
            setType(x_pos);
        }
        Trio<Color, String, Pair<Integer, Integer>> t = new Trio<>();
        t.first = color;
        t.second = text;
        t.third = new Pair<>();
        t.third.first = getXPixelPos(isBigD ? (BigDecimal) x_pos : (isIntegral ? BigDecimal.valueOf(x_pos.longValue()) :
                BigDecimal.valueOf(x_pos.doubleValue())));
        t.third.second = -getYPixelPos(isBigD ? (BigDecimal) y_pos : (isIntegral ? BigDecimal.valueOf(y_pos.longValue())
                : BigDecimal.valueOf(y_pos.doubleValue()))) + height;
        annotations.add(t);
        return true;
    }
    public final boolean addAnnotation(String text, T x_pos, T y_pos) {
        return addAnnotation(text, x_pos, y_pos, annotationColour);
    }
    public final boolean addLabel(Label label) {
        if (label == null) {
            return false;
        }
        labels.add(label);
        return true;
    }
    public final synchronized boolean generatePlot(boolean freePlot) throws CharacterDoesNotFitException,
            OutOfImageBoundsException {
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
        drawAnnotations();
        drawLabels(); // Labels will be drawn on top of annotations, in case of overlap
        drawPlotToFigure(freePlot);
        generated = true;
        super.generateFigure();
        return true;
    } // the below method writes JUST the image within Plot to the file
    public final synchronized boolean writePlot(boolean free_plot) throws IOException, CharacterDoesNotFitException {
        if (!generated) {
            return false;
        }
        javax.imageio.ImageIO.write(this.image, super.imageFormat, new java.io.File(super.path));
        if (free_plot)
            clearPlot();
        return true;
    } // the below method generates the Plot image, copies it to the Figure image, and writes the Figure image to file
    public final synchronized boolean generatePlotAndWrite(boolean free_images) throws IOException,
            CharacterDoesNotFitException, OutOfImageBoundsException {
        if (!generated) {
            if (!generatePlot(free_images)) {
                return false;
            }
        }
        super.write(free_images);
        return true;
    }
    public final synchronized boolean generatePlotAndWritePlot(boolean free_plot) throws IOException,
            CharacterDoesNotFitException {
        if (!generatePlot(free_plot)) {
            return false;
        }
        return writePlot(free_plot);
    }
    public final synchronized void show(boolean block) { // generates (if needed) the plot and shows it, without writing
        if (!generated) {
            this.generatePlot(false);
        }
        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int newHeight;
        int newWidth;
        double heightRatio = ((double) super.fullHeight)/((double) screenSize.height);
        double widthRatio = ((double) super.fullWidth)/((double) screenSize.width);
        double plotRatio = ((double) super.fullWidth)/((double) super.fullHeight);
        if (heightRatio >= widthRatio) { // making sure the image fits within the screen
            newHeight = (int) (((double) screenSize.height)*(2d/3d));
            newWidth = (int) (newHeight*plotRatio);
        }
        else {
            newWidth = (int) (((double) screenSize.width)*(2d/3d));
            newHeight = (int) (newWidth/plotRatio);
        }
        JPanel panel = new JPanel() {
            @Override // painting the image inside the JPanel and making sure it always occupies the entire JPanel
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(Plot.super.fullImage, 0, 0, this.getWidth(), this.getHeight(), null);
            }
        };
        JFrame frame = new JFrame("Plot");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // app should not exit just because plot is closed
        frame.setSize(newWidth, newHeight);
        frame.add(panel);
        frame.setVisible(true);
        if (block) { // if specified, this will block program execution at the point where show() is called, until the
            Object o = new Object(); // window is closed
            Thread blocked = new Thread(()->{
                synchronized (o) { // can only be accessed by one Thread at a time
                    while (frame.isVisible()) {
                        try {
                            o.wait();// relinquish lock here: Thread stops until notify is called and lock is reacquired
                        } catch(InterruptedException e) {
                            return;
                        }
                    }
                }
            });
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(java.awt.event.WindowEvent e) {
                    synchronized (o) {
                        o.notify(); // this resumes execution within the 'blocked' Thread, and since the frame is no
                    } // longer visible after this, the Thread finishes execution and joins the main thread again
                }
            });
            blocked.start();
            try {
                blocked.join();
            } catch(InterruptedException e) {}
        }
    }
    public final synchronized void show() {
        show(true);
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
};
