package _5010;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * InputManager - handles keyboard input for tube runner
 */
public class InputManager implements KeyListener {
    private boolean[] keys = new boolean[256];
    private boolean[] prevKeys = new boolean[256];
    
    // Key codes
    public static final int KEY_W = KeyEvent.VK_W;
    public static final int KEY_S = KeyEvent.VK_S;
    public static final int KEY_A = KeyEvent.VK_A;
    public static final int KEY_D = KeyEvent.VK_D;
    public static final int KEY_SPACE = KeyEvent.VK_SPACE;
    public static final int KEY_SHIFT = KeyEvent.VK_SHIFT;
    public static final int KEY_ENTER = KeyEvent.VK_ENTER;
    public static final int KEY_R = KeyEvent.VK_R;
    public static final int KEY_UP = KeyEvent.VK_UP;
    public static final int KEY_DOWN = KeyEvent.VK_DOWN;
    public static final int KEY_LEFT = KeyEvent.VK_LEFT;
    public static final int KEY_RIGHT = KeyEvent.VK_RIGHT;
    
    private GameState gameState;
    
    public InputManager(GameState gameState) {
        this.gameState = gameState;
    }
    
    public void poll(double deltaTime) {
        // Handle menu input
        if (gameState.getMode() == GameState.GameMode.MENU) {
            if (isKeyJustPressed(KeyEvent.VK_ENTER)) {
                gameState.setMode(GameState.GameMode.PLAYING);
                gameState.startNewGame();
            }
        }
        
        // Handle game over input
        if (gameState.getMode() == GameState.GameMode.GAME_OVER) {
            if (isKeyJustPressed(KEY_R) || isKeyJustPressed(KeyEvent.VK_ENTER)) {
                gameState.setMode(GameState.GameMode.PLAYING);
                gameState.startNewGame();
            }
        }
        
        // Handle gameplay input - movement within tube
        if (gameState.getMode() == GameState.GameMode.PLAYING) {
            Camera camera = gameState.getCamera();
            
            // Movement with WASD or Arrow keys
            if (isKeyDown(KEY_A) || isKeyDown(KEY_LEFT)) {
                camera.moveLeft(deltaTime);
            }
            if (isKeyDown(KEY_D) || isKeyDown(KEY_RIGHT)) {
                camera.moveRight(deltaTime);
            }
            
            // Spacebar for flying
            if (isKeyDown(KEY_SPACE)) {
                camera.setFlying(true);
            } else {
                camera.setFlying(false);
            }
        }
        
        // Store previous key states
        System.arraycopy(keys, 0, prevKeys, 0, keys.length);
    }
    
    public boolean isKeyDown(int keyCode) {
        if (keyCode < 0 || keyCode >= keys.length)
            return false;
        return keys[keyCode];
    }
    
    public boolean isKeyJustPressed(int keyCode) {
        if (keyCode < 0 || keyCode >= keys.length)
            return false;
        return keys[keyCode] && !prevKeys[keyCode];
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length) {
            keys[code] = true;
        }
        // Handle ENTER key immediately
        if (code == KeyEvent.VK_ENTER) {
            if (gameState.getMode() == GameState.GameMode.MENU) {
                gameState.setMode(GameState.GameMode.PLAYING);
                gameState.startNewGame();
            } else if (gameState.getMode() == GameState.GameMode.GAME_OVER) {
                gameState.setMode(GameState.GameMode.PLAYING);
                gameState.startNewGame();
            }
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code >= 0 && code < keys.length) {
            keys[code] = false;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        char keyChar = e.getKeyChar();
        if (keyChar == '\n' || keyChar == '\r') {
            int code = KeyEvent.VK_ENTER;
            if (code >= 0 && code < keys.length) {
                keys[code] = true;
            }
        }
    }
}

