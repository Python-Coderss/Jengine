package com.example.engine.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.example.engine.logic.MyEngine;

public class DesktopLauncher {
    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("My Instanced 3D Engine");
        config.setWindowedMode(1280, 720);
        config.setBackBufferConfig(8, 8, 8, 8, 16, 0, 4);
        config.useVsync(false);
        config.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.GL30, 3, 0);
        new Lwjgl3Application(new MyEngine(), config);
    }
}
