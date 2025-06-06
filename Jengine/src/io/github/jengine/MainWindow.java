package io.github.jengine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * MainWindow sets up the Swing GUI and renders the ray-traced image.
 * <p>
 * Initializes the scene, camera, renderer, and displays the output.
 * This is the entry point and render loop for the program.
 */
public class MainWindow extends JFrame {
    private final Scene scene;
    private final Camera camera;
    private final Renderer renderer;
    private final JLabel label;
    private boolean paused = false;
    private boolean mouseLookEnabled = false;
    private Point lastMousePosition;
    private final double MOUSE_SENSITIVITY = 0.2;
    private final double MOVE_SPEED = 0.5;
    
    // Performance and debug tracking
    private long lastFrameTime = System.nanoTime();
    private int frameCount = 0;
    private double fps = 0.0;
    private double frameTime = 0.0;
    private long fpsUpdateTime = System.currentTimeMillis();

    public MainWindow() {
        setTitle("Ray Tracer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        scene = new Scene();
        camera = new Camera();
        renderer = new Renderer(scene, camera);
        label = new JLabel();

        getContentPane().add(label);

        setSize(800, 600);
        setLocationRelativeTo(null);
        setVisible(true);

        // Generate terrain
        TerrainGenerator terrainGen = new TerrainGenerator(12345L);
        List<Block> terrain = terrainGen.generateFlatTerrain(-10, 10, -10, 10);
        scene.addBlocks(terrain);
        
        // Add unit light sources (like the sun)
        UnitLight sunLight = new UnitLight(
            0, 20, 0,                   // Position above the terrain (unit coordinates)
            new Color(255, 255, 200),   // Warm sunlight color
            2.0                         // Intensity
        );
        scene.addUnitLight(sunLight);
        
        // Position camera above the terrain
        camera.setPosition(new Vector3(0, 5, 5));
        camera.setDirection(new Vector3(0, -0.3, -1));

        renderScene();

        // Mouse motion listener for look controls (only when dragging)
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (mouseLookEnabled && lastMousePosition != null) {
                    int dx = e.getX() - lastMousePosition.x;
                    int dy = e.getY() - lastMousePosition.y;

                    camera.rotateHorizontal(dx * MOUSE_SENSITIVITY);
                    camera.rotateVertical(-dy * MOUSE_SENSITIVITY);

                    renderScene();
                }
                lastMousePosition = e.getPoint();
            }
        });

        // Mouse listener for enabling/disabling look mode
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    mouseLookEnabled = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    mouseLookEnabled = false;
                    lastMousePosition = null;
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });

        // Keyboard listener for movement and pause
        addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                case KeyEvent.VK_ESCAPE:
                    paused = !paused;
                    if (paused) {
                        Graphics g = label.getGraphics();
                        g.setColor(new Color(0, 0, 0, 150));
                        g.fillRect(0, 0, getWidth(), getHeight());
                        g.setColor(Color.WHITE);
                        g.setFont(new Font("Arial", Font.BOLD, 36));
                        g.drawString("PAUSED", getWidth() / 2 - 80, getHeight() / 2);
                    } else {
                        renderScene();
                    }
                    break;
                case KeyEvent.VK_W:
                    if (!paused) {
                        camera.moveForward(MOVE_SPEED);
                        renderScene();
                    }
                    break;
                case KeyEvent.VK_S:
                    if (!paused) {
                        camera.moveBackward(MOVE_SPEED);
                        renderScene();
                    }
                    break;
                case KeyEvent.VK_A:
                    if (!paused) {
                        camera.moveLeft(MOVE_SPEED);
                        renderScene();
                    }
                    break;
                case KeyEvent.VK_D:
                    if (!paused) {
                        camera.moveRight(MOVE_SPEED);
                        renderScene();
                    }
                    break;
                }
            }
        });
    }

    private void renderScene() {
        if (paused) return;
        
        new Thread(() -> {
            long startTime = System.nanoTime();
            
            BufferedImage img = renderer.render(getWidth(), getHeight());
            
            // Calculate frame time and FPS
            long endTime = System.nanoTime();
            frameTime = (endTime - startTime) / 1_000_000.0; // Convert to milliseconds
            
            frameCount++;
            long currentTime = System.currentTimeMillis();
            if (currentTime - fpsUpdateTime >= 1000) { // Update FPS every second
                fps = frameCount * 1000.0 / (currentTime - fpsUpdateTime);
                frameCount = 0;
                fpsUpdateTime = currentTime;
            }
            
            // Add debug overlay
            addDebugOverlay(img);
            
            SwingUtilities.invokeLater(() -> {
                label.setIcon(new ImageIcon(img));
            });
        }).start();
    }
    
    /**
     * Add FPS, frame time, position and heading overlay to the rendered image
     */
    private void addDebugOverlay(BufferedImage img) {
        Graphics2D g2d = img.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Set up text properties
        Font font = new Font("Monospaced", Font.BOLD, 14);
        g2d.setFont(font);
        
        // Create semi-transparent background for better readability
        g2d.setColor(new Color(0, 0, 0, 180));
        g2d.fillRect(10, 10, 300, 120);
        
        // Set text color
        g2d.setColor(Color.WHITE);
        
        // Get camera position and direction
        Vector3 pos = camera.getPosition();
        Vector3 dir = camera.getDirection();
        
        // Calculate heading in degrees (yaw angle)
        double heading = Math.toDegrees(Math.atan2(dir.x, -dir.z));
        if (heading < 0) heading += 360;
        
        // Calculate pitch in degrees
        double pitch = Math.toDegrees(Math.asin(-dir.y));
        
        // Draw debug information
        int y = 30;
        int lineHeight = 18;
        
        g2d.drawString(String.format("FPS: %.1f", fps), 15, y);
        y += lineHeight;
        
        g2d.drawString(String.format("Frame Time: %.1f ms", frameTime), 15, y);
        y += lineHeight;
        
        g2d.drawString(String.format("Position: (%.1f, %.1f, %.1f)", pos.x, pos.y, pos.z), 15, y);
        y += lineHeight;
        
        g2d.drawString(String.format("Heading: %.1f°", heading), 15, y);
        y += lineHeight;
        
        g2d.drawString(String.format("Pitch: %.1f°", pitch), 15, y);
        y += lineHeight;
        
        // Add controls hint
        g2d.setColor(new Color(200, 200, 200));
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 12));
        g2d.drawString("WASD: Move | Right-click+drag: Look | ESC: Pause", 15, img.getHeight() - 15);
        
        g2d.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainWindow::new);
    }
}