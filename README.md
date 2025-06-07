# Jengine - A Java 3D Ray Tracing Engine

Jengine is a simple 3D ray tracing engine built in Java. It's designed to render voxel-based scenes with features like terrain generation, multiple block types, and basic lighting effects. The engine uses Swing for its graphical user interface.

## Features

- Ray tracing for realistic lighting, shadows, reflection, and refraction.
- Voxel-based terrain generation using Perlin noise.
- Multiple block types: Grass, Dirt, Stone, Bedrock.
- Camera controls: WASD for movement, right-click + drag for mouse look.
- Basic lighting: Directional (unit) lights and ambient light.
- Skybox for background rendering.
- Performance and debug overlay showing FPS, frame time, camera position, and orientation.

## How to Run

1.  **Compile the code:** Navigate to the `Jengine` directory and compile the Java source files. You can use a standard Java compiler (javac). For example: `find src -name '*.java' > sources.txt && javac @sources.txt -d bin` (This command might need adjustment based on your specific setup and OS).
2.  **Run the application:** Execute the `MainWindow` class. From the `Jengine/bin` directory (or wherever your compiled classes are), run: `java io.github.jengine.MainWindow`

## Controls

-   `WASD`: Move the camera forward, left, backward, and right.
-   `Right-Click + Drag Mouse`: Rotate the camera view.
-   `ESC`: Pause or unpause the rendering.

## Code Overview

-   `io.github.jengine.MainWindow`: The main class that sets up the application window using Swing, handles user input (keyboard and mouse), and manages the main render loop.
-   `io.github.jengine.Renderer`: Contains the core ray tracing logic. It generates rays, finds intersections with scene objects, and calculates pixel colors based on lighting, materials, reflection, and refraction.
-   `io.github.jengine.Scene`: Manages all objects in the 3D world, including blocks, lights, and the skybox. It provides methods for intersecting rays with objects.
-   `io.github.jengine.Camera`: Represents the viewer's perspective. It handles camera position, orientation, movement, and generates primary rays for the renderer.
-   `io.github.jengine.TerrainGenerator`: Responsible for creating the voxel-based terrain using Perlin noise algorithms. It can generate different layers of blocks (e.g., grass, dirt, stone).
-   `io.github.jengine.Block` (and its subclasses like `GrassBlock`, `DirtBlock`, `StoneBlock`, `BedrockBlock`): Abstract class and concrete implementations for different types of blocks in the world. Each block has a position and material properties.
-   `io.github.jengine.Vector3`: A utility class for representing 3D vectors and points, used extensively for positions, directions, and calculations in 3D space.
-   `io.github.jengine.Material`: Defines the visual properties of objects, such as color, reflectivity, and refractivity.
-   `io.github.jengine.Skybox`: Renders the background of the scene.

## Future Enhancements

-   More advanced lighting models (e.g., area lights, soft shadows).
-   Support for more complex object geometries beyond voxels.
-   Texture mapping for blocks and objects.
-   Performance optimizations (e.g., acceleration structures like BVH).
-   User interface for scene configuration or material editing.
-   Sound effects or music.
