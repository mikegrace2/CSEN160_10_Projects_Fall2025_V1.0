package _5040;

import org.json.JSONArray;
import org.json.JSONObject;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
/**
 * The FileHandler class is responsible for reading and writing layer data
 * to and from JSON files. It allows saving the user's current artwork,
 * loading it back later, and loading challenge files used in the game mode.
 *
 * All saved data includes:
 *  - layer color (RGB)
 *  - opacity
 *  - blend mode
 *  - position and size (rectangle)
 *
 * JSON structure:
 * {
 *     "layers": [
 *         { "r": ..., "g": ..., "b": ..., "opacity": ..., "blendMode": "...",
 *           "x": ..., "y": ..., "width": ..., "height": ... }
 *     ]
 * }
 */
public class FileHandler {
    /**
     * Saves all layers from the LayerManager into a JSON file.
     * Each layer is converted into a JSON object storing its color,
     * opacity, blend mode, and rectangle geometry.
     *
     * @param layers the list of layers to save
     * @param file   the file to write to
     * @throws IOException if writing to disk fails
     */
    public static void saveLayers(List<Layer> layers, File file) throws IOException {
        JSONArray arr = new JSONArray();
        for (Layer layer : layers) {
            JSONObject obj = new JSONObject();
            Color c = layer.getColor();
            Rectangle r = layer.getShape();
            obj.put("r", c.getRed());
            obj.put("g", c.getGreen());
            obj.put("b", c.getBlue());
            obj.put("opacity", layer.getOpacity());
            obj.put("blendMode", layer.getBlendMode().name());
            obj.put("x", r.x);
            obj.put("y", r.y);
            obj.put("width", r.width);
            obj.put("height", r.height);
            arr.put(obj);
        }
        JSONObject root = new JSONObject();
        root.put("layers", arr);
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(root.toString(4)); // pretty print
        }
    }
    /**
     * Loads layer data from a JSON file and replaces the contents
     * of the given LayerManager with the new layers.
     *
     * @param manager the LayerManager that will receive the loaded layers
     * @param file    the file to read from
     * @throws IOException if the file cannot be read
     */
    public static void loadLayers(LayerManager manager, File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        JSONObject root = new JSONObject(sb.toString());
        JSONArray arr = root.getJSONArray("layers");

        List<Layer> newLayers = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            Color c = new Color(obj.getInt("r"), obj.getInt("g"), obj.getInt("b"));
            float opacity = (float) obj.getDouble("opacity");
            BlendMode mode = BlendMode.valueOf(obj.getString("blendMode"));
            Rectangle rect = new Rectangle(
                    obj.getInt("x"), obj.getInt("y"),
                    obj.getInt("width"), obj.getInt("height")
            );
            newLayers.add(new Layer(c, opacity, mode, rect));
        }
        manager.setLayers(newLayers);
    }
    /**
     * Loads a list of layers from a JSON file for use in challenge mode.
     * Unlike loadLayers(), this does not replace the user's current layers;
     * it simply returns the list so the caller can compare them.
     *
     * @param file the challenge file containing the target layer layout
     * @return a list of layers representing the challenge
     * @throws IOException if the file cannot be read
     */
    public static List<Layer> loadChallenge(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        JSONObject root = new JSONObject(sb.toString());
        JSONArray arr = root.getJSONArray("layers");
        List<Layer> layers = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject obj = arr.getJSONObject(i);
            Color c = new Color(obj.getInt("r"), obj.getInt("g"), obj.getInt("b"));
            float opacity = (float) obj.getDouble("opacity");
            BlendMode mode = BlendMode.valueOf(obj.getString("blendMode"));
            Rectangle rect = new Rectangle(
                    obj.getInt("x"), obj.getInt("y"),
                    obj.getInt("width"), obj.getInt("height")
            );
            layers.add(new Layer(c, opacity, mode, rect));
        }
        return layers;
    }
}
