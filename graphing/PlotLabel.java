/*
This is a convenience class which exists purely for adding Labels to a Plot object in terms of data/axes coordinates.
*/

package greg.graphing;

import java.awt.Font;
import java.awt.Color;

public final class PlotLabel <T extends Number> extends Label {
    T xPos;
    T yPos;
    T w;
    public PlotLabel(String text, Font font, Color bgColor, Color txtColor, T xPosition, T yPosition, T width,
                     double rotationInRadians, int textJustification, boolean pushLongWordToNextLine) {
        super(text, font, textJustification, pushLongWordToNextLine);
        super.backgroundColor = bgColor;
        super.textColor = txtColor;
        super.rotation = rotationInRadians;
        xPos = xPosition;
        yPos = yPosition;
        w = width;
    }
}