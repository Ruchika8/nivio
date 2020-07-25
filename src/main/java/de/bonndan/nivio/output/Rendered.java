package de.bonndan.nivio.output;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.bonndan.nivio.model.Labeled;

/**
 * A landscape component that has been rendered.
 *
 *
 */
public interface Rendered extends Labeled {

    String LABEL_PREFIX_RENDERED = "nivio.rendered.";
    String LABEL_RENDERED_COLOR = LABEL_PREFIX_RENDERED + "color";
    String LY = LABEL_PREFIX_RENDERED + "y";
    String LX = LABEL_PREFIX_RENDERED + "x";
    String LABEL_RENDERED_ICON = LABEL_PREFIX_RENDERED + "icon";
    String LABEL_FILL = "fill";
    String LABEL_ICON = "icon";
    String LABEL_RENDERED_WIDTH = LABEL_PREFIX_RENDERED + "width";
    String LABEL_RENDERED_HEIGHT = LABEL_PREFIX_RENDERED + "height";

    default void setWidth(Long width) {
        setLabel(LABEL_RENDERED_WIDTH, String.valueOf(width));
    }

    @JsonIgnore
    default Double getWidth() {
        String width = getLabel(LABEL_RENDERED_WIDTH);
        return width == null ? null : Double.parseDouble(width);
    }

    default void setHeight(Long height) {
        setLabel(LABEL_RENDERED_HEIGHT, String.valueOf(height));
    }

    @JsonIgnore
    default Double getHeight() {
        String height = getLabel(LABEL_RENDERED_HEIGHT);
        return height == null ? null : Double.parseDouble(height);
    }

    default void setColor(String color) {
        setLabel(LABEL_RENDERED_COLOR, color);
    }

    default String getColor() {
        return getLabel(LABEL_RENDERED_COLOR);
    }

    default void setFill(String fill) {
        setLabel(LABEL_FILL, fill);
    }

    @JsonIgnore
    default String getFill() {
        return getLabel(LABEL_FILL);
    }

    default void setX(Double x) {
        setLabel(LX, String.valueOf(x));
    }

    @JsonIgnore
    default Double getX() {
        String x = getLabel(LX);
        return x == null ? null : Double.parseDouble(x);
    }

    default void setY(Double y) {
        setLabel(LY, String.valueOf(y));
    }

    @JsonIgnore
    default Double getY() {
        String y = getLabel(LY);
        return y == null ? null : Double.parseDouble(y);
    }

}
