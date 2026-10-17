package _5040;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

/**
 * The ToolbarPanel provides all UI controls.
 *
 * Right side of the editor and lets the user:
 * - Choose a color for a new layer using JColorChooser
 * - Set the width and height of a layer, by typing
 * - Choose a blend mode (Add, Multiply, Subtract)
 * - Add a new layer to the canvas
 * - Save the current artwork to a file
 * - Load artwork from a file
 * - Load a challenge file for the "Test Your Drawing Skills" mode
 *
 * It also communicates with the provided LayerManager to update or add layers,
 * and uses a Runnable repaint callback provided by Main to refresh the canvas.
 */
public class ToolbarPanel extends JPanel {
    private final LayerManager manager;
    private final JPanel previewPanel;
    private final JComboBox<BlendMode> modeSelector;
    private final JSpinner widthSpinner;
    private final JSpinner heightSpinner;
    /**
     * Creates the toolbar panel with all editing controls.
     *
     * @param manager the LayerManager used to create and modify layers
     * @param repaintCallback a function that forces the canvas to repaint
     *                        after a change (usually Main::repaint)
     * The constructor builds the interface on the right side, which includes:
     * - A color preview box and color chooser dialog with JColorChooser
     * - Width and height text boxes
     * - A blend mode selector
     * - Buttons for adding layers, saving files, loading files,
     *   and starting the drawing challenge mode
     */
    public ToolbarPanel(LayerManager manager, Runnable repaintCallback) {
        this.manager = manager;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(220, 600));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Use built in JColorChooser dialog menu to choose color
        JLabel colorLabel = new JLabel("Layer Color");
        add(colorLabel);

        previewPanel = new JPanel();
        previewPanel.setBackground(Color.CYAN);
        previewPanel.setPreferredSize(new Dimension(100, 50));
        add(previewPanel);

        JButton colorButton = new JButton("Choose Color");
        colorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Select Layer Color", previewPanel.getBackground());
            if (newColor != null) {
                previewPanel.setBackground(newColor);
            }
        });
        add(colorButton);
        add(Box.createVerticalStrut(10));

        // Change Size
        JLabel sizeLabel = new JLabel("Layer Size (px)");
        add(sizeLabel);

        JPanel sizePanel = new JPanel(new GridLayout(2, 2, 5, 5));
        sizePanel.add(new JLabel("Width:"));
        widthSpinner = new JSpinner(new SpinnerNumberModel(150, 10, 800, 10));
        sizePanel.add(widthSpinner);
        sizePanel.add(new JLabel("Height:"));
        heightSpinner = new JSpinner(new SpinnerNumberModel(150, 10, 800, 10));
        sizePanel.add(heightSpinner);
        add(sizePanel);
        add(Box.createVerticalStrut(10));

        // Select Blend Mode
        JLabel modeLabel = new JLabel("Blend Mode");
        add(modeLabel);
        modeSelector = new JComboBox<>(BlendMode.values());
        add(modeSelector);
        add(Box.createVerticalStrut(10));

        // Add button
        JButton addButton = new JButton("Add Layer");
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.addActionListener((ActionEvent e) -> {
            Color color = previewPanel.getBackground();
            BlendMode mode = (BlendMode) modeSelector.getSelectedItem();
            int width = (int) widthSpinner.getValue();
            int height = (int) heightSpinner.getValue();

            // Create layer roughly centered on 800x600 canvas
            Rectangle rect = new Rectangle(400 - width / 2, 300 - height / 2, width, height);
            Layer newLayer = new Layer(color, 0.8f, mode, rect);

            manager.addLayer(newLayer);
            repaintCallback.run();
        });
        add(addButton);

        // FILE operations for save file
        add(Box.createVerticalStrut(10));

        JButton saveButton = new JButton("Save File");
        saveButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    FileHandler.saveLayers(manager.getLayers(), file);
                    JOptionPane.showMessageDialog(this, "File saved successfully!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage());
                }
            }
        });
        add(saveButton);

        JButton loadButton = new JButton("Load File");
        loadButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    FileHandler.loadLayers(manager, file);
                    repaintCallback.run(); // Refresh canvas
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error loading file: " + ex.getMessage());
                }
            }
        });
        add(loadButton);
        JButton testButton = new JButton("Test your Drawing Skills");
        testButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    // load the challenge layers
                    List<Layer> challengeLayers = FileHandler.loadChallenge(file);

                    // show challenge window
                    new ChallengeWindow(challengeLayers, manager);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error loading challenge: " + ex.getMessage());
                }
            }
        });
        add(testButton);
        // Stretch Fill to make the right side not an empty void
        add(Box.createVerticalGlue());
    }
}
