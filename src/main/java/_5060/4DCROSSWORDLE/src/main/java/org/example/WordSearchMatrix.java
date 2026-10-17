package org.example;

import java.io.*;
import java.util.*;

// greedy algorithm for generating random 3D matrix of intersecting words
public class WordSearchMatrix implements CharMatrix3D {

    // --- Data Structures ---
    private final char[][][] matrix; // The 3D grid of characters
    private final int width, height, depth;
    private final Random rand = new Random();
    private final List<WordData> placedWords = new ArrayList<>(); // List of all placed 5-letter words
    private final List<int[]> occupiedCells = new ArrayList<>(); // Tracks all letters for intersections
    private final Trie prefixTrie;
    private final List<String> allWords; // Full dictionary
    private final Map<Integer, Map<Character, BitSet>> positionalBitIndex; // For fast lookups

    private static final int WORD_LENGTH = 5;
    private static final char VOID_CHAR = '\0'; // Represents empty space

    /**
     * A record to store the data for a single 5-letter word puzzle.
     * @param word The 5-letter answer string.
     * @param x The starting x-coordinate.
     * @param y The starting y-coordinate.
     * @param z The starting z-coordinate.
     * @param dir The direction of the word (0=X, 1=Y, 2=Z).
     */
    public record WordData(String word, int x, int y, int z, int dir) {
        /**
         * get the 3D coordinates of a specific letter in the word.
         * @param index The index of the letter (0-4).
         * @return An int[3] array of {x, y, z}.
         */
        public int[] getCoords(int index) {
            int newX = x + (dir == 0 ? index : 0);
            int newY = y + (dir == 1 ? index : 0);
            int newZ = z + (dir == 2 ? index : 0);
            return new int[]{newX, newY, newZ};
        }

        public String getDirectionName() {
            return (dir == 0) ? "X-Axis (Horizontal)" : (dir == 1) ? "Y-Axis (Vertical)" : "Z-Axis (Deep)";
        }
    }

    // --- Trie Classes ---
    class TrieNode {
        TrieNode[] children = new TrieNode[26];
        boolean isEndOfWord = false;
    }

    class Trie {
        final TrieNode root = new TrieNode();
        public void insert(String word) {
            TrieNode node = root;
            for (char c : word.toCharArray()) {
                int index = c - 'A';
                if (node.children[index] == null) node.children[index] = new TrieNode();
                node = node.children[index];
            }
            node.isEndOfWord = true;
        }
        public boolean startsWith(String prefix) {
            TrieNode node = root;
            for (char c : prefix.toCharArray()) {
                int index = c - 'A';
                if (node.children[index] == null) return false;
                node = node.children[index];
            }
            return true;
        }
        public boolean search(String word) {
            TrieNode node = root;
            for (char c : word.toCharArray()) {
                int index = c - 'A';
                if (node.children[index] == null) return false;
                node = node.children[index];
            }
            return node != null && node.isEndOfWord;
        }
    }

    // --- Initialization ---
    public WordSearchMatrix(String csvFilePath, int width, int height, int depth) {
        if (width < WORD_LENGTH || height < WORD_LENGTH || depth < WORD_LENGTH) {
            throw new IllegalArgumentException("Dimensions must be at least " + WORD_LENGTH);
        }
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.matrix = new char[width][height][depth];
        this.prefixTrie = new Trie();
        this.allWords = new ArrayList<>();
        this.positionalBitIndex = new HashMap<>();

        // Init BitSet map
        for (int i = 0; i < WORD_LENGTH; i++) {
            positionalBitIndex.put(i, new HashMap<>());
            for (char c = 'A'; c <= 'Z'; c++) {
                positionalBitIndex.get(i).put(c, new BitSet());
            }
        }
        loadWordsFromCSV(csvFilePath);
        if (allWords.isEmpty()) {
            throw new IllegalStateException("No valid 5-letter words found in file.");
        }
    }

    public WordSearchMatrix(InputStream wordStream, int width, int height, int depth) {
        if (width < WORD_LENGTH || height < WORD_LENGTH || depth < WORD_LENGTH) {
            throw new IllegalArgumentException("Dimensions must be at least " + WORD_LENGTH);
        }
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.matrix = new char[width][height][depth];
        this.prefixTrie = new Trie();
        this.allWords = new ArrayList<>();
        this.positionalBitIndex = new HashMap<>();

        // Init BitSet map
        for (int i = 0; i < WORD_LENGTH; i++) {
            positionalBitIndex.put(i, new HashMap<>());
            for (char c = 'A'; c <= 'Z'; c++) {
                positionalBitIndex.get(i).put(c, new BitSet());
            }
        }
        // Load words from the provided InputStream
        loadWordsFromStream(wordStream);
        if (allWords.isEmpty()) {
            throw new IllegalStateException("No valid 5-letter words found in stream.");
        }
    }

    private void loadWordsFromStream(InputStream wordStream) {
        // Use a BufferedReader that reads from the InputStream
        try (BufferedReader br = new BufferedReader(new InputStreamReader(wordStream))) {
            String line;
            int wordId = 0;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                for (String token : tokens) {
                    String word = token.trim().toUpperCase();
                    if (word.length() == WORD_LENGTH && word.matches("[A-Z]+")) {
                        allWords.add(word);
                        prefixTrie.insert(word);
                        for (int i = 0; i < WORD_LENGTH; i++) {
                            positionalBitIndex.get(i).get(word.charAt(i)).set(wordId);
                        }
                        wordId++;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load word list from stream", e);
        }
    }

    private void loadWordsFromCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int wordId = 0;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                for (String token : tokens) {
                    String word = token.trim().toUpperCase();
                    if (word.length() == WORD_LENGTH && word.matches("[A-Z]+")) {
                        allWords.add(word);
                        prefixTrie.insert(word);
                        for (int i = 0; i < WORD_LENGTH; i++) {
                            positionalBitIndex.get(i).get(word.charAt(i)).set(wordId);
                        }
                        wordId++;
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load word list", e);
        }
    }

    // Public Methods

    /**
     * Resets and fills the matrix with a new puzzle.
     */
    @Override
    public void generate() {
        occupiedCells.clear();
        placedWords.clear();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    matrix[x][y][z] = VOID_CHAR;
                }
            }
        }

        // 1. Place the first word
        int maxAttempts = 100;
        boolean firstWordPlaced = false;
        for (int i = 0; i < maxAttempts && !firstWordPlaced; i++) {
            firstWordPlaced = tryPlaceFirstWord();
        }

        // 2. Place subsequent intersecting words
        if (firstWordPlaced) {
            int wordsToEmbed = (width + height + depth) * 2;
            int wordsSuccessfullyEmbedded = 1;
            int attempts = 0;
            int limit = maxAttempts * 20;

            while (wordsSuccessfullyEmbedded < wordsToEmbed && attempts < limit) {
                if (tryPlaceIntersectingWord()) {
                    wordsSuccessfullyEmbedded++;
                    attempts = 0; // Reset attempts on success
                } else {
                    attempts++;
                }
            }
            System.out.println("Generator placed " + wordsSuccessfullyEmbedded + " words.");
        } else {
            System.out.println("Generator FAILED to place first word.");
        }
    }

    /**
     * @return The list of all 5-letter words placed in the grid.
     */
    public List<WordData> getPlacedWords() {
        return placedWords;
    }

    // --- Word Placement Logic ---

    private boolean tryPlaceFirstWord() {
        String word = allWords.get(rand.nextInt(allWords.size()));
        int dir = rand.nextInt(3);
        int x = 0, y = 0, z = 0;

        switch (dir) {
            case 0: x = rand.nextInt(width - WORD_LENGTH + 1); y = rand.nextInt(height); z = rand.nextInt(depth); break;
            case 1: x = rand.nextInt(width); y = rand.nextInt(height - WORD_LENGTH + 1); z = rand.nextInt(depth); break;
            case 2: x = rand.nextInt(width); y = rand.nextInt(height); z = rand.nextInt(depth - WORD_LENGTH + 1); break;
        }

        if (isPathFlanked(x, y, z, dir)) return false; // Fail if it would create 6+ letter word
        if (!isPathClear(x, y, z, dir)) return false; // Fail if it overlaps

        // Place word
        for (int i = 0; i < WORD_LENGTH; i++) {
            int[] c = new WordData(word, x, y, z, dir).getCoords(i);
            matrix[c[0]][c[1]][c[2]] = word.charAt(i);
            occupiedCells.add(new int[]{c[0], c[1], c[2]});
        }
        placedWords.add(new WordData(word, x, y, z, dir));
        return true;
    }

    public boolean tryPlaceIntersectingWord() {
        if (occupiedCells.isEmpty()) return false;

        // 1. Pick anchor
        int[] anchorCell = occupiedCells.get(rand.nextInt(occupiedCells.size()));
        int anchorX = anchorCell[0], anchorY = anchorCell[1], anchorZ = anchorCell[2];

        // 2. Pick new direction (must be different from any existing words at this anchor)
        int dir = rand.nextInt(3);
        int anchorOffset = rand.nextInt(WORD_LENGTH);

        int startX = anchorX - (dir == 0 ? anchorOffset : 0);
        int startY = anchorY - (dir == 1 ? anchorOffset : 0);
        int startZ = anchorZ - (dir == 2 ? anchorOffset : 0);

        // 3. Bounds Check
        if (startX < 0 || startY < 0 || startZ < 0 ||
                startX + (dir == 0 ? WORD_LENGTH - 1 : 0) >= width ||
                startY + (dir == 1 ? WORD_LENGTH - 1 : 0) >= height ||
                startZ + (dir == 2 ? WORD_LENGTH - 1 : 0) >= depth) {
            return false;
        }

        // 4. Flank Check
        if (isPathFlanked(startX, startY, startZ, dir)) return false;

        // 5. Build Constraints
        Map<Integer, Character> constraints = new HashMap<>();
        for (int i = 0; i < WORD_LENGTH; i++) {
            int[] c = new WordData(null, startX, startY, startZ, dir).getCoords(i);
            char existingChar = matrix[c[0]][c[1]][c[2]];
            if (existingChar != VOID_CHAR) {
                constraints.put(i, existingChar);
            }
        }

        // 6. Find word
        String newWord = findWordWithConstraints(constraints);
        if (newWord == null) return false;

        // 7. Perpendicular Check
        if (!validatePerpendiculars(newWord, startX, startY, startZ, dir)) {
            return false;
        }

        // 8. Place Word
        for (int i = 0; i < WORD_LENGTH; i++) {
            int[] c = new WordData(newWord, startX, startY, startZ, dir).getCoords(i);
            if (matrix[c[0]][c[1]][c[2]] == VOID_CHAR) {
                matrix[c[0]][c[1]][c[2]] = newWord.charAt(i);
                occupiedCells.add(new int[]{c[0], c[1], c[2]});
            }
        }
        placedWords.add(new WordData(newWord, startX, startY, startZ, dir));
        return true;
    }

    // --- Constraint Checking ---

    /**
     * Checks perpendicular axes for validity.
     */
    private boolean validatePerpendiculars(String word, int startX, int startY, int startZ, int direction) {
        for (int i = 0; i < WORD_LENGTH; i++) {
            int[] c = new WordData(word, startX, startY, startZ, direction).getCoords(i);
            int x = c[0], y = c[1], z = c[2];
            char charToPlace = word.charAt(i);

            if (matrix[x][y][z] == charToPlace) continue;

            int axis1 = (direction + 1) % 3;
            int axis2 = (direction + 2) % 3;

            if (!checkAxisValidity(x, y, z, axis1, charToPlace)) return false;
            if (!checkAxisValidity(x, y, z, axis2, charToPlace)) return false;
        }
        return true;
    }

    /**
     * Checks if adding 'newChar' creates an invalid (2, 3, 4, 6+) segment.
     */
    private boolean checkAxisValidity(int x, int y, int z, int axisToCheck, char newChar) {
        StringBuilder sb = new StringBuilder();

        // 1. Scan backwards
        int curr = (axisToCheck == 0 ? x : (axisToCheck == 1 ? y : z)) - 1;
        while (curr >= 0) {
            char c = getCharOnAxis(x, y, z, axisToCheck, curr);
            if (c == VOID_CHAR) break;
            sb.insert(0, c);
            curr--;
        }

        // 2. Add new char
        sb.append(newChar);

        // 3. Scan forwards
        curr = (axisToCheck == 0 ? x : (axisToCheck == 1 ? y : z)) + 1;
        int limit = (axisToCheck == 0) ? width : (axisToCheck == 1 ? height : depth);
        while (curr < limit) {
            char c = getCharOnAxis(x, y, z, axisToCheck, curr);
            if (c == VOID_CHAR) break;
            sb.append(c);
            curr++;
        }

        String formedString = sb.toString();
        int len = formedString.length();

        // 4. 1 or WORD_LENGTH
        if (len == 1) return true;
        if (len == WORD_LENGTH) return prefixTrie.search(formedString); // 5-letter must be word

        return false; // 2, 3, 4, 6+ are invalid
    }

    private char getCharOnAxis(int x, int y, int z, int axis, int index) {
        if (axis == 0) return matrix[index][y][z];
        if (axis == 1) return matrix[x][index][z];
        return matrix[x][y][index];
    }

    private boolean isPathClear(int x, int y, int z, int dir) {
        for (int i = 0; i < WORD_LENGTH; i++) {
            int[] c = new WordData(null, x, y, z, dir).getCoords(i);
            if (matrix[c[0]][c[1]][c[2]] != VOID_CHAR) return false;
        }
        return true;
    }

    private boolean isPathFlanked(int startX, int startY, int startZ, int direction) {
        int[] before = new int[]{startX, startY, startZ};
        if (direction == 0) before[0]--; else if (direction == 1) before[1]--; else before[2]--;
        if (inBounds(before[0], before[1], before[2]) && matrix[before[0]][before[1]][before[2]] != VOID_CHAR) return true;

        int[] after = new WordData(null, startX, startY, startZ, direction).getCoords(WORD_LENGTH);
        if (inBounds(after[0], after[1], after[2]) && matrix[after[0]][after[1]][after[2]] != VOID_CHAR) return true;

        return false;
    }

    private boolean inBounds(int x, int y, int z) {
        return x >= 0 && y >= 0 && z >= 0 && x < width && y < height && z < depth;
    }

    private String findWordWithConstraints(Map<Integer, Character> constraints) {
        if (constraints.isEmpty()) {
            return allWords.get(rand.nextInt(allWords.size()));
        }

        BitSet candidates = null;
        for (Map.Entry<Integer, Character> entry : constraints.entrySet()) {
            BitSet bitSetForConstraint = positionalBitIndex.get(entry.getKey()).get(entry.getValue());
            if (bitSetForConstraint == null || bitSetForConstraint.isEmpty()) return null;
            if (candidates == null) candidates = (BitSet) bitSetForConstraint.clone();
            else candidates.and(bitSetForConstraint);
            if (candidates.isEmpty()) return null;
        }

        List<Integer> validIDs = new ArrayList<>();
        for (int i = candidates.nextSetBit(0); i >= 0; i = candidates.nextSetBit(i + 1)) validIDs.add(i);
        if (validIDs.isEmpty()) return null;

        return allWords.get(validIDs.get(rand.nextInt(validIDs.size())));
    }

    // Interface Visualization Methods
    public int getWidth() {return this.width;}
    public int getHeight() {return this.height;}
    public int getDepth() {return this.depth;}
    @Override public void printSlice(int z) {
        System.out.println("--- Z Slice " + z + " ---");
        for (int y = 0; y < height; y++) {
            System.out.printf("y%02d: ", y);
            for (int x = 0; x < width; x++) {
                System.out.print((matrix[x][y][z] == VOID_CHAR ? "." : matrix[x][y][z]) + " ");
            }
            System.out.println();
        }
    }
    @Override public void printSliceX(int x) {
        if (x < 0 || x >= width) return;
        System.out.println("--- X Slice " + x + " (Y-Z plane) ---");
        for (int y = 0; y < height; y++) {
            System.out.printf("y%02d: ", y);
            for (int z = 0; z < depth; z++) {
                System.out.print((matrix[x][y][z] == VOID_CHAR ? "." : matrix[x][y][z]) + " ");
            }
            System.out.println();
        }
    }
    @Override public void printSliceY(int y) {
        if (y < 0 || y >= height) return;
        System.out.println("--- Y Slice " + y + " (X-Z plane) ---");
        for (int z = 0; z < depth; z++) {
            System.out.printf("z%02d: ", z);
            for (int x = 0; x < width; x++) {
                System.out.print((matrix[x][y][z] == VOID_CHAR ? "." : matrix[x][y][z]) + " ");
            }
            System.out.println();
        }
    }
    @Override public void visualize() { for (int z = 0; z < depth; z++) printSlice(z); }
    @Override public void visualizeCrossSections() {
        System.out.println("\n=== 3D Cross-Section ===");
        printSlice(0);
        printSliceX(0);
        printSliceY(0);
        System.out.println("========================\n");
    }
}