package com.lpoo.gameworld;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.lpoo.gameobjects.Ball;
import com.lpoo.slash.Slash;

/**
 * Created by Diogo on 26-04-2016.
 * Renders everything in gameWorld to the screen.
 * Design Pattern: MVC - View component (Other components: InputHandler and GameWorld)
 */
public class GameRenderer {

    private OrthographicCamera cam;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batcher;

    private GameWorld gameWorld;

    Box2DDebugRenderer debugRenderer;
    Matrix4 debugMatrix;

    public GameRenderer(GameWorld world) {
        gameWorld = world;
        cam = new OrthographicCamera();
        cam.setToOrtho(true, Slash.screenDimensions.x, Slash.screenDimensions.y);
        //gameArea = gameWorld.getGameArea();
        //slasher = gameWorld.getSlasher();
        //balls = gameWorld.getBalls();

        batcher = new SpriteBatch();
        batcher.setProjectionMatrix(cam.combined); // Attach batcher to camera
        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setProjectionMatrix(cam.combined);

        debugRenderer = new Box2DDebugRenderer();
    }

    public void render(float runTime) {
        cam.update();

        // Fill the entire screen with black, to prevent potential flickering.
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Begin ShapeRenderer
        shapeRenderer.begin(ShapeType.Filled);

        // Draw Background color
        shapeRenderer.setColor(55 / 255.0f, 80 / 255.0f, 100 / 255.0f, 1);
        shapeRenderer.rect(0, 0,Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        //Draw GameArea
        shapeRenderer.setColor(255 / 255.0f, 255 / 255.0f, 255 / 255.0f, 1);
        Vector2[] points = gameWorld.getGameArea().getPoints();
        for(int i=0; i<points.length; i++) {
            Vector2 a = points[i];
            Vector2 b = points[i==points.length-1 ? 0 : i+1];
            shapeRenderer.line(a.x,a.y,b.x,b.y);
        }

        //Draw Slasher
        shapeRenderer.setColor(255 / 255.0f, 255 / 255.0f, 0 / 255.0f, 1);
        Vector2 fingerPos = gameWorld.getSlasher().getFinger();
        Vector2 slasherPos = gameWorld.getSlasher().getPosition();
        shapeRenderer.circle(slasherPos.x,slasherPos.y,Ball.getRadius());
        if(gameWorld.getSlasherIsMoving())
        {
            Vector2 tempSlasher = gameWorld.getSlasher().getBodyPosition();
            shapeRenderer.circle(tempSlasher.x,tempSlasher.y,Ball.getRadius());
            shapeRenderer.line(slasherPos,tempSlasher);
        }
        else if(fingerPos!=null)
        {
            //draw future Slasher
            shapeRenderer.circle(fingerPos.x,fingerPos.y,Ball.getRadius());
            //draw line
            shapeRenderer.line(slasherPos,fingerPos);
        }

        //Draw Balls
        shapeRenderer.setColor(255 / 255.0f, 255 / 255.0f, 255 / 255.0f, 1); //same color as gameArea
        for(int i = 0; i< gameWorld.getBalls().size(); i++)
            shapeRenderer.circle(gameWorld.getBalls().get(i).getBody().getPosition().x,
                                gameWorld.getBalls().get(i).getBody().getPosition().y,
                                Ball.getRadius());

        // End ShapeRenderer
        shapeRenderer.end();

        /*// Begin SpriteBatch
        batcher.setProjectionMatrix(cam.combined);*/
    }
}
