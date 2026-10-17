package _5040;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
/**
 * LayerManager keeps track of all layers in the editor. It stores the list of
 * layers, handles selecting and moving layers, and manages undo history.
 *
 * Responsibilities of LayerManager:
 * - Add and remove layers
 * - Track which layer is currently selected
 * - Move or resize the selected layer
 * - Allow selecting a layer by clicking on it
 * - Store previous states so the user can undo changes
 *
 * Each layer is stored in order, such that earlier layers appear behind later ones when rendered.
 * The manager does not draw anything, it only maintains data that the Renderer uses.
 */
public class LayerManager {
    private List<Layer> layers = new ArrayList<>();
    private int selectedIndex = -1;
    private java.util.Stack<List<Layer>> history = new java.util.Stack<>();
    private void saveState() {
        List<Layer> snapshot = new ArrayList<>();
        for (Layer l : layers) {
            snapshot.add(new Layer(
                    l.getColor(),
                    l.getOpacity(),
                    l.getBlendMode(),
                    new Rectangle(l.getShape())
            ));
        }
        history.push(snapshot);
    }
    /**
     * Adds a new layer to the canvas and records the action for undo.
     * @param layer the layer to add
     */
    public void addLayer(Layer layer) {
        saveState();
        layers.add(layer);
    }
    /**
     * Removes a layer at the given index and saves the previous state.
     * @param index the position of the layer to remove
     */
    public void removeLayer(int index) {
        if (index >= 0 && index < layers.size()) {
            saveState();
            layers.remove(index);
        }
    }
    /**
     * Deletes whichever layer is currently selected.
     * After deletion, no layer is selected.
     */
    public void deleteSelectedLayer() {
        if (selectedIndex >= 0 && selectedIndex < layers.size()) {
            saveState();
            layers.remove(selectedIndex);
            selectedIndex = -1;
        }
    }
    /**
     * Restores the most recent saved state from the undo history.
     * If there is no saved state, nothing happens.
     */
    public void undo() {
        if (!history.isEmpty()) {
            layers = history.pop();
            selectedIndex = -1;
        }
    }
    public List<Layer> getLayers() { return layers; }
    /**
     * Returns the currently selected layer, or null if no layer is selected.
     * @return returns the layer the position of selected index. If out of bounds or not found, returns null
     */
    public Layer getSelectedLayer() {
        if (selectedIndex >= 0 && selectedIndex < layers.size()) return layers.get(selectedIndex);
        return null;
    }
    /**
     * Selects a layer by list index. Does nothing if the index is invalid.
     * @param index the index of the layer to select
     */
    public void selectLayer(int index) {
        if (index >= 0 && index < layers.size()) selectedIndex = index;
    }

    /**
     * Selects the topmost layer under the given screen point.
     * Layers are checked from front to back so the visible layer is chosen.
     * If no layer is under the point, the selection is cleared.
     * @param p the point where the user clicked
     */
    public void selectLayerByClick(Point p) {
        for (int i = layers.size() - 1; i >= 0; i--) {
            if (layers.get(i).getShape().contains(p)) {
                selectLayer(i);
                return;
            }
        }
        selectedIndex = -1; // clicked empty space
    }
    /**
     * Moves the selected layer by the given offset.
     * @param dx horizontal movement in pixels
     * @param dy vertical movement in pixels
     */
    public void moveSelectedLayer(int dx, int dy) {
        Layer l = getSelectedLayer();
        if (l != null) {
            l.move(dx, dy);
        }
    }
    /**
     * Checks whether the given point lies on any resize handle of the selected layer.
     * @param p the location to test
     * @return which handle is being hovered, or NONE if none match
     */
    public Layer.HandlePosition getHandleAt(Point p) {
        Layer selected = getSelectedLayer();
        if (selected != null) {
            return selected.getHandleAt(p);
        }
        return Layer.HandlePosition.NONE;
    }
    /**
     * Replaces all layers with a new list, usually after loading a file.
     * Clears the current selection.
     * @param newLayers the new list of layers
     */
    public void setLayers(List<Layer> newLayers) {
        this.layers = newLayers;
        selectedIndex = -1;
    }
    /**
     * Removes all layers and clears the selection.
     */
    public void clear() {
        layers.clear();
        selectedIndex = -1;
    }
}
