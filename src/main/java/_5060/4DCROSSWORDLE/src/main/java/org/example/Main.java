package org.example;

import com.jme3.renderer.queue.RenderQueue;

import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;

import com.jme3.scene.control.BillboardControl;
import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;
import com.simsilica.lemur.*;
import com.simsilica.lemur.component.QuadBackgroundComponent;
import com.simsilica.lemur.component.SpringGridLayout;
import com.simsilica.lemur.style.BaseStyles;

import java.io.*;
import java.util.*;

public class Main extends SimpleApplication implements ActionListener {

    // --- Constants ---
    private static final int WORD_LENGTH = 5;
    private static final int MAX_GUESSES = 6;

    // --- Game Data ---
    private WordSearchMatrix matrix;
    private List<WordSearchMatrix.WordData> puzzles;
    private WordSearchMatrix.WordData activePuzzle;
    private final Map<String, GuessState> sharedBoard = new HashMap<>();
    private final Map<WordSearchMatrix.WordData, List<Guess>> history = new HashMap<>();
    private Set<String> validWords;
    private final Map<String, List<WordSearchMatrix.WordData>> coordToPuzzlesMap = new HashMap<>();

    // --- 3D Scene ---
    private Node puzzleNode;
    private final Map<String, Geometry> cubeSpatials = new HashMap<>();
    private final Map<String, BitmapText> textSpatials = new HashMap<>();
    private Geometry activePuzzleHighlight;
    private ChaseCamera chaseCam; // Stored as field to access later

    // --- Materials & Colors ---
    private Material matBase, matGray, matYellow, matGreen;
    private Material matOutline;
    private final ColorRGBA colorBase = new ColorRGBA(0.3f, 0.3f, 0.3f, 1.0f);
    private final ColorRGBA colorGray = new ColorRGBA(0.23f, 0.23f, 0.24f, 1.0f);
    private final ColorRGBA colorYellow = new ColorRGBA(0.71f, 0.62f, 0.23f, 1.0f);
    private final ColorRGBA colorGreen = new ColorRGBA(0.32f, 0.55f, 0.31f, 1.0f);

    // --- UI (Lemur) ---
    private Container uiContainer;
    private Label puzzleInfoLabel;
    private Label messageLabel;
    private TextField guessField;
    private Container puzzleListContainer;
    private Button switchPuzzleButton;
    private Container historyContainer;
    private final List<Label> historyLabels = new ArrayList<>();

    // --- Game Enums/Records ---
    enum GuessResult { GREEN, YELLOW, GRAY, BASE }
    record Guess(String word, GuessResult[] results) {}
    record GuessState(char letter, Map<Integer, GuessResult> states) {
        public GuessState(char letter) {
            this(letter, new HashMap<>());
        }
        public void setState(int direction, GuessResult result) { states.put(direction, result); }
    }

    public static void main(String[] args) {
        Main app = new Main();
        AppSettings settings = new AppSettings(true);
        settings.setTitle("3D Intersecting Wordle");
        settings.setWidth(1280);
        settings.setHeight(720);
        app.setSettings(settings);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        // Initialize Logic
        try {
            loadWords("valid-words.csv");
            InputStream wordListStream = Main.class.getResourceAsStream("/valid-words.csv");
            if (wordListStream == null) {
                throw new RuntimeException("Could not find valid-words.csv in the JAR!");
            }
            matrix = new WordSearchMatrix(wordListStream, 20, 20, 20);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        matrix.generate();
        puzzles = matrix.getPlacedWords();

        if (puzzles.isEmpty()) {
            System.err.println("Generator failed, no puzzles loaded.");
            stop();
            return;
        }
        activePuzzle = puzzles.get(0);

        puzzleNode = new Node("PuzzleNode");
        rootNode.attachChild(puzzleNode);

        // 2. Setup 3D Scene
        setupCameraAndLights();
        setupMaterials();

        // 3. Create 3D Grid
        create3DGrid();

        // 4. Setup UI
        setupUI();
        setupInput();

        // 5. Initial State
        updateUI();
    }

    private void loadWords(String resourcePath) {
        validWords = new HashSet<>();

        try (InputStream is = Main.class.getResourceAsStream("/" + resourcePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().length() == WORD_LENGTH) {
                    validWords.add(line.trim().toUpperCase());
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load word list from resource: " + resourcePath);
            validWords.add("HELLO");
        }

//        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
//            String line;
//            while ((line = br.readLine()) != null) {
//                if (line.trim().length() == WORD_LENGTH) {
//                    validWords.add(line.trim().toUpperCase());
//                }
//            }
//        } catch (IOException e) {
//            System.err.println("Could not load word list: " + path);
//            validWords.add("HELLO");
//        }
    }

    private void setupCameraAndLights() {
        flyCam.setEnabled(false);
        viewPort.setBackgroundColor(new ColorRGBA(0.1f, 0.1f, 0.12f, 1.0f));

        rootNode.addLight(new AmbientLight(new ColorRGBA(0.6f, 0.6f, 0.6f, 1.0f)));

        // Store chaseCam in class field
        chaseCam = new ChaseCamera(cam, inputManager);
        rootNode.addControl(chaseCam);
        chaseCam.setSpatial(puzzleNode);
        chaseCam.setSmoothMotion(true);
        chaseCam.setRotationSpeed(5f);
        chaseCam.setDefaultDistance(15f);
        chaseCam.setMinDistance(5f);
        chaseCam.setMaxDistance(50f);
        chaseCam.setEnabled(true);
    }

    private void setupMaterials() {
        matBase = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matBase.setColor("Color", colorBase);

        matGray = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matGray.setColor("Color", colorGray);

        matYellow = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matYellow.setColor("Color", colorYellow);

        matGreen = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matGreen.setColor("Color", colorGreen);

        matOutline = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matOutline.setColor("Color", ColorRGBA.Black);
        matOutline.getAdditionalRenderState().setWireframe(true);
    }

    private void create3DGrid() {

        Box cubeMesh = new Box(0.45f, 0.45f, 0.45f);

        Box outlineMesh = new Box(0.451f, 0.451f, 0.451f);

        for (WordSearchMatrix.WordData puzzle : puzzles) {
            for (int i = 0; i < WORD_LENGTH; i++) {
                int[] c = puzzle.getCoords(i);
                String key = c[0] + "," + c[1] + "," + c[2];

                coordToPuzzlesMap.computeIfAbsent(key, k -> new ArrayList<>()).add(puzzle);

                if (!cubeSpatials.containsKey(key)) {
                    Node cellNode = new Node("CellNode-" + key);
                    cellNode.setLocalTranslation(c[0], c[1], c[2]);

                    Geometry cubeGeo = new Geometry("Cube-" + key, cubeMesh);
                    cubeGeo.setLocalTranslation(c[0], c[1], c[2]);

                    Material cubeMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                    cubeMat.setColor("Color", colorBase);
                    cubeGeo.setMaterial(cubeMat);

                    puzzleNode.attachChild(cubeGeo);
                    cubeSpatials.put(key, cubeGeo);

                    BitmapText letter = new BitmapText(guiFont);
                    letter.setSize(0.5f);
                    letter.setColor(ColorRGBA.White);
                    letter.setText("");
                    letter.setLocalTranslation(c[0] - 0.15f, c[1] + 0.15f, c[2] + 0.5f);
                    letter.setQueueBucket(RenderQueue.Bucket.Transparent);
                    puzzleNode.attachChild(letter);
                    textSpatials.put(key, letter);

                    letter.addControl(new BillboardControl());

                    Geometry outlineGeo = new Geometry("Outline-" + key, outlineMesh); // Uses 0.451f box
                    outlineGeo.setMaterial(matOutline);
                    cellNode.attachChild(outlineGeo);

                    puzzleNode.attachChild(cellNode);
                }
            }
        }

        puzzleNode.setLocalTranslation(
                -matrix.getWidth() / 2f,
                -matrix.getHeight() / 2f,
                -matrix.getDepth() / 2f
        );
    }

    private void setupUI() {
        GuiGlobals.initialize(this);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        uiContainer = new Container("glass");
        uiContainer.setLocalTranslation(10, cam.getHeight() - 10, 0);
        guiNode.attachChild(uiContainer);

        puzzleInfoLabel = uiContainer.addChild(new Label("Active Puzzle: #0"));

        historyContainer = new Container(new SpringGridLayout(Axis.Y, Axis.X, FillMode.None, FillMode.None));
        for (int i = 0; i < MAX_GUESSES; i++) {
            Container row = new Container(new SpringGridLayout(Axis.X, Axis.Y));
            for (int j = 0; j < WORD_LENGTH; j++) {
                Label letterLabel = new Label(" ");
                letterLabel.setTextHAlignment(HAlignment.Center);
                letterLabel.setBackground(new QuadBackgroundComponent(getColorForGuessResult(GuessResult.BASE)));
                letterLabel.setPreferredSize(new Vector3f(30, 30, 1));
                row.addChild(letterLabel);
                historyLabels.add(letterLabel);
            }
            historyContainer.addChild(row);
        }
        uiContainer.addChild(historyContainer);

        messageLabel = uiContainer.addChild(new Label("Guess a 5-letter word."));
        messageLabel.setColor(ColorRGBA.Yellow);

        Container inputRow = new Container(new SpringGridLayout(Axis.X, Axis.Y));
        guessField = inputRow.addChild(new TextField(""));
        Button guessButton = inputRow.addChild(new Button("Guess"));
        guessButton.addClickCommands(button -> submitGuess());
        uiContainer.addChild(inputRow);

        switchPuzzleButton = uiContainer.addChild(new Button("Switch Puzzle"));
        switchPuzzleButton.addClickCommands(button -> togglePuzzleList());

        puzzleListContainer = new Container("glass");
        puzzleListContainer.setLocalTranslation(10, cam.getHeight() - 250, 0);
        guiNode.attachChild(puzzleListContainer);
        puzzleListContainer.setCullHint(Spatial.CullHint.Always);

        buildPuzzleList();
    }

    private void setupInput() {
        inputManager.addMapping("SubmitGuess", new KeyTrigger(KeyInput.KEY_RETURN));
        inputManager.addMapping("ClickSelect", new MouseButtonTrigger(MouseInput.BUTTON_LEFT)); // <-- ADD
        inputManager.addListener(this, "SubmitGuess");
        inputManager.addListener(this, "ClickSelect"); // <-- ADD
    }

    private void selectPuzzleFromClick() {
        CollisionResults results = new CollisionResults();
        Vector2f click2d = inputManager.getCursorPosition();
        Vector3f click3d = cam.getWorldCoordinates(click2d, 0f);
        Vector3f dir = cam.getWorldCoordinates(click2d, 1f).subtractLocal(click3d).normalizeLocal();
        Ray ray = new Ray(click3d, dir);

        puzzleNode.collideWith(ray, results);

        for (int i = 0; i < results.size(); i++) {
            Geometry hitGeo = results.getCollision(i).getGeometry();
            String geoName = hitGeo.getName();
            String coordKey = null;

            if (geoName.startsWith("Cube-")) {
                coordKey = geoName.substring(5);
            } else if (geoName.startsWith("Outline-")) {
                coordKey = geoName.substring(8);
            }

            if (coordKey != null) {
                List<WordSearchMatrix.WordData> candidates = coordToPuzzlesMap.get(coordKey);

                if (candidates != null && !candidates.isEmpty()) {
                    int activeIndex = candidates.indexOf(activePuzzle);
                    int nextIndex = (activeIndex + 1) % candidates.size();
                    WordSearchMatrix.WordData newPuzzle = candidates.get(nextIndex);
                    activePuzzle = newPuzzle;
                    updateUI();
                    return;
                }
            }
        }
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.equals("SubmitGuess") && isPressed) {
            submitGuess();
        }

        if (name.equals("ClickSelect") && isPressed) {
            selectPuzzleFromClick();
        }
    }

    private void togglePuzzleList() {
        boolean isVisible = puzzleListContainer.getCullHint() == Spatial.CullHint.Never;
        puzzleListContainer.setCullHint(isVisible ? Spatial.CullHint.Always : Spatial.CullHint.Never);
    }

    private void buildPuzzleList() {
        puzzleListContainer.clearChildren();
        for (int i = 0; i < puzzles.size(); i++) {
            WordSearchMatrix.WordData p = puzzles.get(i);
            Button btn = puzzleListContainer.addChild(new Button(
                    String.format("ID %d: %s @ (%d,%d,%d)", i, p.getDirectionName(), p.x(), p.y(), p.z())
            ));
            final int puzzleIndex = i;
            btn.addClickCommands(button -> {
                activePuzzle = puzzles.get(puzzleIndex);
                updateUI();
                togglePuzzleList();
            });
        }
    }

    private void submitGuess() {
        String guess = guessField.getText().trim().toUpperCase();
        guessField.setText("");

        if (guess.length() != WORD_LENGTH) {
            messageLabel.setText("Guess must be " + WORD_LENGTH + " letters.");
            return;
        }
        if (!validWords.contains(guess)) {
            messageLabel.setText("'" + guess + "' is not in the dictionary.");
            return;
        }
        if (history.getOrDefault(activePuzzle, new ArrayList<>()).size() >= MAX_GUESSES) {
            messageLabel.setText("No more guesses for this word.");
            return;
        }

        processGuess(guess, activePuzzle);
        messageLabel.setText("Guessed: " + guess);
        updateUI();
    }

    private void processGuess(String guess, WordSearchMatrix.WordData puzzle) {
        String answer = puzzle.word();
        GuessResult[] results = new GuessResult[WORD_LENGTH];
        char[] answerChars = answer.toCharArray();
        boolean[] answerCharsUsed = new boolean[WORD_LENGTH];

        for (int i = 0; i < WORD_LENGTH; i++) {
            if (guess.charAt(i) == answerChars[i]) {
                results[i] = GuessResult.GREEN;
                answerCharsUsed[i] = true;
            }
        }

        for (int i = 0; i < WORD_LENGTH; i++) {
            if (results[i] == null) {
                boolean foundYellow = false;
                for (int j = 0; j < WORD_LENGTH; j++) {
                    if (!answerCharsUsed[j] && guess.charAt(i) == answerChars[j]) {
                        results[i] = GuessResult.YELLOW;
                        answerCharsUsed[j] = true;
                        foundYellow = true;
                        break;
                    }
                }
                results[i] = foundYellow ? GuessResult.YELLOW : GuessResult.GRAY;
            }
        }

        history.computeIfAbsent(puzzle, k -> new ArrayList<>()).add(new Guess(guess, results));

        for (int i = 0; i < WORD_LENGTH; i++) {
            int[] coords = puzzle.getCoords(i);
            String key = coords[0] + "," + coords[1] + "," + coords[2];
            char guessChar = guess.charAt(i);

            final int letterIndex = i;
            GuessState cellState = sharedBoard.computeIfAbsent(key, k -> new GuessState(puzzle.word().charAt(letterIndex)));
            cellState.setState(puzzle.dir(), results[i]);

            if (results[i] == GuessResult.GREEN) {
                textSpatials.get(key).setText(String.valueOf(guessChar));
            }
        }
    }

    private void updateUI() {
        int puzzleId = puzzles.indexOf(activePuzzle);
        puzzleInfoLabel.setText(String.format("Puzzle #%d: %s @ (%d,%d,%d)", puzzleId, activePuzzle.getDirectionName(), activePuzzle.x(), activePuzzle.y(), activePuzzle.z()));

        List<Guess> puzzleHistory = history.getOrDefault(activePuzzle, new ArrayList<>());
        for (int i = 0; i < MAX_GUESSES; i++) {
            for (int j = 0; j < WORD_LENGTH; j++) {
                int index = i * WORD_LENGTH + j;
                Label label = historyLabels.get(index);
                if (i < puzzleHistory.size()) {
                    Guess guess = puzzleHistory.get(i);
                    label.setText(String.valueOf(guess.word().charAt(j)));
                    ((QuadBackgroundComponent)label.getBackground()).setColor(getColorForGuessResult(guess.results()[j]));
                } else {
                    label.setText(" ");
                    ((QuadBackgroundComponent)label.getBackground()).setColor(getColorForGuessResult(GuessResult.BASE));
                }
            }
        }

        updateGridVisuals();
        updateHighlightBox();
    }

    private void updateGridVisuals() {
        cubeSpatials.forEach((key, geo) -> {
            GuessState cellState = sharedBoard.get(key);
            ColorRGBA displayColor;

            if (cellState != null) {
                GuessResult bestResult = GuessResult.BASE;

                // Green > Yellow > Gray
                for (GuessResult r : cellState.states().values()) {
                    if (r == GuessResult.GREEN) {
                        bestResult = GuessResult.GREEN;
                        break;
                    }
                    if (r == GuessResult.YELLOW && bestResult != GuessResult.GREEN) {
                        bestResult = GuessResult.YELLOW;
                    }
                    if (r == GuessResult.GRAY && bestResult == GuessResult.BASE) {
                        bestResult = GuessResult.GRAY;
                    }
                }
                displayColor = getColorForGuessResult(bestResult);
            } else {
                displayColor = getColorForGuessResult(GuessResult.BASE);
            }

            geo.getMaterial().setColor("Color", displayColor);
        });
    }

    private void updateHighlightBox() {
        if (activePuzzleHighlight == null) {
            Box box = new Box(1, 1, 1);
            activePuzzleHighlight = new Geometry("Highlight", box);
            Material highlightMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
            highlightMat.setColor("Color", new ColorRGBA(0.0f, 0.7f, 1.0f, 0.5f));
            highlightMat.getAdditionalRenderState().setWireframe(true);
            highlightMat.getAdditionalRenderState().setLineWidth(3f);
            activePuzzleHighlight.setMaterial(highlightMat);
            puzzleNode.attachChild(activePuzzleHighlight);

            if (chaseCam != null) {
                chaseCam.setSpatial(activePuzzleHighlight);
            }
        }

        Vector3f min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
        Vector3f max = new Vector3f(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);

        for (int i = 0; i < WORD_LENGTH; i++) {
            int[] c = activePuzzle.getCoords(i);
            min.minLocal(new Vector3f(c[0], c[1], c[2]));
            max.maxLocal(new Vector3f(c[0], c[1], c[2]));
        }

        Vector3f center = min.add(max).mult(0.5f);
        activePuzzleHighlight.setLocalTranslation(center);

        Vector3f extent = max.subtract(min).add(1, 1, 1);
        activePuzzleHighlight.setLocalScale(extent.mult(0.5f));

        puzzleNode.updateGeometricState();
    }

    private ColorRGBA getColorForGuessResult(GuessResult result) {
        return switch (result) {
            case GREEN -> colorGreen;
            case YELLOW -> colorYellow;
            case GRAY -> colorGray;
            default -> colorBase;
        };
    }
}
