package com.lpoo.gameobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.lpoo.gameworld.GameWorld;
import com.lpoo.slashhelpers.Function;
import com.lpoo.slashhelpers.Utilities;

import java.util.ArrayList;

/**
 * Created by Diogo on 09-05-2016.
 *
 * The slasher is responsible for slashing the gameArea, so as to create a new GameArea.
 * However, if this moving slasher hits a ball, the game is over.
 */
public class Slasher {

    /**
     * GameWorld where all gameobjects are contained.
     */
    private GameWorld gameWorld;
    /**
     * Position where the slasher line intersects the gameArea if there is such, else is null.
     */
    private Vector2 finger;
    /**
     * Position of the corner where slasher was at the start.
     */
    private Vector2 position;
    /**
     * If slasher is moving, it is the body of the moving slasher.
     */
    private Body body;
    /**
     * If the slasher is moving, it is the body of the yellow line following it.
     */
    private Body bodyPath;
    /**
     * Radius of the slasher.
     */
    private final static float radius = Ball.getRadius();
    /**
     * Slasher's velocity when moving.
     */
    private final static float velocity = 60;

    /**
     * Default constructor.
     * @param pos position where it will appear when not moving. In the same position, there is a gameArea corner.
     * @param gameWorld GameWorld where all gameobjects are contained.
     */
    public Slasher(Vector2 pos, GameWorld gameWorld) {
        body=null;
        bodyPath=null;
        finger=null;
        position=pos;
        this.gameWorld=gameWorld;
    }

    /**
     *
     * @return position
     */
    public Vector2 getPosition() {
        return position;
    }

    /**
     *
     * @return body's position in case body exists. Null otherwise.
     */
    public Vector2 getBodyPosition() {
        return body==null ? null : body.getPosition();
    }

    /**
     *
     * @return finger.
     */
    public Vector2 getFinger() {
        return finger;
    }

    /**
     * If the line passing in the slasher's position and the received finger can be drawn inside the gameArea,
     * this.finger will keep the coordinates of the intersection between that line and a gameArea's edge;
     * ff not, this.finger=null.
     * @param finger Coordinates where the user's finger touched the screen.
     */
    public void setFinger(Vector2 finger) {
        if(body!=null)
            return;
        this.finger=null;
        if(finger==null)
            return;

        Vector2[] points = gameWorld.getGameArea().getPoints();
        Vector2 sideB=null, center=null, sideA=null;
        for(int i=0; i<4; i++)
        {
            if(points[i]==position)
            {
                sideA=points[(i+1)%4];
                center=points[(i+2)%4];
                sideB=points[(i+3)%4];
                break;
            }
        }

        /*   center
     .       /|  .
      .    /  | .       Now we will see if the finger's relative position is the same as the center, when compared to fa and fb.
       . /    |.        Example: in this example, center is above fa and fb. So, finger should also be above those two.
       A\    / B        The boundaries, for this example, "are continued" with dots, so that we can see them clearly.
         \  /           Concluding, if the finger is under any of those lines, the Slasher line wouldn't be drawn.
          \/
          SLASHER
         */

        Function fa = new Function(position,sideA);
        Function fb = new Function(position,sideB);

        boolean validFinger=false;
        if(center.y<fa.getY(center.x)) {
            if(center.y<fb.getY(center.x)) { //center under fa and fb
                if(finger.y<fa.getY(finger.x) && finger.y<fb.getY(finger.x))
                    validFinger=true;
            } else { //center under fa and above fb
                if(finger.y<fa.getY(finger.x) && finger.y>fb.getY(finger.x))
                    validFinger=true;
            }
        } else {
            if(center.y<fb.getY(center.x)) { //center above fa and under fb
                if(finger.y>=fa.getY(finger.x) && finger.y<fb.getY(finger.x))
                    validFinger=true;
            } else { //center above fa and fb
                if(finger.y>=fa.getY(finger.x) && finger.y>fb.getY(finger.x))
                    validFinger=true;
            }
        }
        if(!validFinger) //if it's not in a valid place to draw the line
            return;

        Function funcFinger = new Function(position, finger);
        //correct the line drawn, so that it stops when intersects a gameArea's edge.
        Vector2 intersect1, intersect2;
        intersect1 = new Function(center,sideA).intersect(funcFinger);
        intersect2 = new Function(center,sideB).intersect(funcFinger);
        float dist1=0, dist2=0;
        if(intersect1!=null && intersect2!=null)
        {
            dist1=(float)Math.sqrt(Math.pow(intersect1.x-position.x,2)+Math.pow(intersect1.y-position.y,2));
            dist2=(float)Math.sqrt(Math.pow(intersect2.x-position.x,2)+Math.pow(intersect2.y-position.y,2));
        }
        if(dist1>dist2 || intersect1==null) {
            this.finger = intersect2;
            gameWorld.getGameArea().setToDelete(sideB);
        } else {
            this.finger=intersect1;
            gameWorld.getGameArea().setToDelete(sideA);
        }
    }

    /**
     * Used to start slasher's movement, including the creation of the bodies.
     */
    public void startedMoving()
    {
        Vector2 direction = new Vector2(finger.x-position.x,finger.y-position.y);
        float norm = (float)Math.sqrt(Math.pow(direction.x,2)+Math.pow(direction.y,2));
        direction.x/=norm; //unitary
        direction.y/=norm; //unitary

        //criar bola
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        bodyDef.position.set(position.x,position.y); //starts a bit ahead so doesnt instantly collide w/ gameArea
        bodyDef.linearVelocity.set(1.5f*velocity*direction.x,1.5f*velocity*direction.y); //def linearVelocity
        body = gameWorld.getWorld().createBody(bodyDef);
        PolygonShape dynamicBox = new PolygonShape();
        dynamicBox.setAsBox(radius,radius);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = dynamicBox;
        fixtureDef.density = 1.0f;
        fixtureDef.friction = 0.0f;
        fixtureDef.restitution=1.0f;
        body.createFixture(fixtureDef);

        //criar path
        bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(new Vector2());
        bodyPath = gameWorld.getWorld().createBody(bodyDef);
    }

    /**
     * Called as an animation of the Slasher
     * @return message indicating the status of slasher.
     */
    public String isMoving()
    {
        String message = checkCollisions();
        if(message=="Game Over" || message=="Slasher End Reached")
        {
            finishedMoving();
            return message;
        }
        //pre-calculations
        Vector2 midPoint = new Vector2((body.getPosition().x+position.x)/2,(body.getPosition().y+position.y)/2);
        double distancePTP = Math.sqrt(Math.pow((body.getPosition().x-position.x),2)+Math.pow((body.getPosition().y-position.y),2));
        double angle = Math.atan((body.getPosition().y-position.y)/(body.getPosition().x-position.x));

        //dispose path
        gameWorld.getWorld().destroyBody(bodyPath);

        //recreate path
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(midPoint);
        bodyPath = gameWorld.getWorld().createBody(bodyDef);
        PolygonShape pathBox = new PolygonShape();
        pathBox.setAsBox((float)distancePTP/2, 0.1f);
        bodyPath.setTransform(midPoint,(float)angle);
        bodyPath.createFixture(pathBox, 0);

        return "OK";
    }

    /**
     * Stops Slasher's movement and deletes the bodies.
     */
    private void finishedMoving()
    {
        //dispose both
        gameWorld.getWorld().destroyBody(body);
        gameWorld.getWorld().destroyBody(bodyPath);

        //point to null
        body=null;
        bodyPath=null;
    }

    /**
     * Check if the moving slasher collides with a gameArea line or a Ball.
     * @return "Game Over" if collided with a ball, "Slasher End Reached" if collided with a line, "OK" otherwise.
     */
    private String checkCollisions()
    {
        //check with balls
        ArrayList<Ball> balls=gameWorld.getBalls();
        for(int i=0; i<balls.size(); i++)
        {
            Vector2 ball=balls.get(i).getBody().getPosition();
            double distance=Utilities.distance(ball,body.getPosition()); //distance between the centers o slasher and ball
            if(distance<2.8f*radius) //should be 2*radius, but as the bodies have a bit more radius, here we account for a bit more too
            {
                return "Game Over";
            }
        }

        //check with gameArea
        Vector2[] pts = gameWorld.getGameArea().getPoints();
        Function[] functions = new Function[2];
        for(int i=0; i<4; i++) //getting the slasher's opposite functions
        {
            if(pts[i]==position)
            {
                functions[0]=new Function(pts[(i+1)%4],pts[(i+2)%4]);
                functions[1]=new Function(pts[(i+2)%4],pts[(i+3)%4]);
                break;
            }
        }

        for(int i=0; i<2; i++) //checking slasher's proximity to the 2 functions
        {
            double distance1 = Math.abs(body.getPosition().y-functions[i].getY(body.getPosition().x)),
                    distance2 = Math.abs(body.getPosition().x-functions[i].getX(body.getPosition().y));
            double distance = Math.min(distance1,distance2);
            if(distance < 2) {
                return "Slasher End Reached";
            }
        }

        return "OK";
    }

}
