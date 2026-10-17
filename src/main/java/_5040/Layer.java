package _5040;

import java.awt.*;
/**
 * A Layer is one rectangular shape in the drawing program. All drawings are composed of layers
 * Each layer has:
 * - a color
 * - an opacity value
 * - a blend mode (how it mixes with lower layers)
 * - dimensions for width and height
 *
 * Layers can be moved, resized using corner handles, and drawn with
 * visual resize handles for user interaction.
 */
public class Layer {
    private Color color;
    private float opacity;
    private BlendMode blendMode;
    private Rectangle shape;

    // Handle size in pixels
    private static final int HANDLE_SIZE = 7;
    /**
     * Represents which resize handle a user is interacting with.
     * NONE means the click was not on any handle.
     * The other values correspond to the four corners of the rectangle.
     */
    public enum HandlePosition {
        NONE, TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }
    /**
     * Creates a new layer with the given color, opacity, blend mode,
     * and rectangular position/size.
     *
     * @param color     the layer's fill color
     * @param opacity   the transparency level (0.0 = transparent, 1.0 = opaque)
     * @param blendMode how this layer blends with lower layers
     * @param shape     the rectangle defining the layer's position and size
     */
    public Layer(Color color, float opacity, BlendMode blendMode, Rectangle shape) {
        this.color = color;
        this.opacity = opacity;
        this.blendMode = blendMode;
        this.shape = shape;
    }

    /**
     * Moves the entire layer by the specified horizontal and vertical offset.
     *
     * @param dx how far to move the layer horizontally
     * @param dy how far to move the layer vertically
     */
    public void move(int dx, int dy) {
        shape.translate(dx, dy);
    }

    /**
     * Resizes the layer based on which corner handle is being dragged.
     * The rectangle grows or shrinks according to the drag direction.
     *
     * Width and height are prevented from becoming too small or inverted.
     *
     * @param handle the handle being dragged
     * @param dx     how far the mouse moved horizontally
     * @param dy     how far the mouse moved vertically
     */
    public void resize(HandlePosition handle, int dx, int dy) {
        switch (handle) {
            case TOP_LEFT -> {
                shape.x += dx;
                shape.y += dy;
                shape.width -= dx;
                shape.height -= dy;
            }
            case TOP_RIGHT -> {
                shape.y += dy;
                shape.width += dx;
                shape.height -= dy;
            }
            case BOTTOM_LEFT -> {
                shape.x += dx;
                shape.width -= dx;
                shape.height += dy;
            }
            case BOTTOM_RIGHT -> {
                shape.width += dx;
                shape.height += dy;
            }
        }

        // Prevent inverted rectangles
        if (shape.width < 10) shape.width = 10;
        if (shape.height < 10) shape.height = 10;
    }

    /**
     * Determines whether the given point lies on any of the resize handles.
     *
     * @param p the point to check
     * @return which handle the point touches, or NONE if it touches none
     */
    public HandlePosition getHandleAt(Point p) {
        Rectangle tl = new Rectangle(shape.x - HANDLE_SIZE / 2, shape.y - HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE);
        Rectangle tr = new Rectangle(shape.x + shape.width - HANDLE_SIZE / 2, shape.y - HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE);
        Rectangle bl = new Rectangle(shape.x - HANDLE_SIZE / 2, shape.y + shape.height - HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE);
        Rectangle br = new Rectangle(shape.x + shape.width - HANDLE_SIZE / 2, shape.y + shape.height - HANDLE_SIZE / 2, HANDLE_SIZE, HANDLE_SIZE);

        if (tl.contains(p)) return HandlePosition.TOP_LEFT;
        if (tr.contains(p)) return HandlePosition.TOP_RIGHT;
        if (bl.contains(p)) return HandlePosition.BOTTOM_LEFT;
        if (br.contains(p)) return HandlePosition.BOTTOM_RIGHT;
        return HandlePosition.NONE;
    }

    /**
     * @return the fill color of the layer
     */
    public Color getColor() { return color; }
    /**
     * @return the opacity of the layer (0.0 to 1.0)
     */
    public float getOpacity() { return opacity; }
    /**
     * @return the blend mode used when compositing this layer
     */
    public BlendMode getBlendMode() { return blendMode; }
    /**
     * @return the rectangle that defines the layer's position and size
     */
    public Rectangle getShape() { return shape; }

    /**
     * Draws small black squares at the four corners of the layer.
     * These handles allow the user to see where they can click to resize.
     *
     * @param g2d the graphics context to draw into
     */
    public void drawHandles(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        int s = HANDLE_SIZE;
        g2d.fillRect(shape.x - s / 2, shape.y - s / 2, s, s);
        g2d.fillRect(shape.x + shape.width - s / 2, shape.y - s / 2, s, s);
        g2d.fillRect(shape.x - s / 2, shape.y + shape.height - s / 2, s, s);
        g2d.fillRect(shape.x + shape.width - s / 2, shape.y + shape.height - s / 2, s, s);
    }
}
