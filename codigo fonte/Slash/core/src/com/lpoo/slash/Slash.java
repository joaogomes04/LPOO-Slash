package com.lpoo.slash;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.math.Vector2;


import java.io.File;

public class Slash extends Game {

    public static final Vector2 screenDimensions = new Vector2(352,200);
    private static File filesDir;

    public Slash(File fileDir)
    {
        filesDir=fileDir;
    }
    public static File getFilesDir() {
        return filesDir;
    }

    public Slash()
    {}

    @Override
    public void create() {
        System.out.println("Slash Game Created!");

        setScreen(new MenuScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();

    }
}