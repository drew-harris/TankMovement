// If you would like to see what everything does, click the book icon in the 
// jGRASP toolbar.

import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.effect.*;
import javafx.stage.*;
import javafx.scene.paint.Color;
import javafx.animation.*;
import javafx.event.*;
import javafx.scene.input.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.canvas.GraphicsContext;
import javafx.util.Duration;
import javafx.scene.shape.*;
import javafx.geometry.Point2D;
import javafx.geometry.Bounds;
import javafx.geometry.BoundingBox;
import javafx.scene.image.*;
import java.util.*;
import java.io.*;

/*
  TODO: 
        add more front collision detection for tank
        tweak tank collision with walls
        MAKE IT FEEL LIKE A TANK
        decide on how balls should disappear
        actually start the real .java for the game
        PRIVATE VARIABLES
        
  REMINDERS FOR DREW: don't use generate csd
                      don't press tabs -> spaces
*/


/** Movement test file for tank game.
 * Still using inner classes which should be removed for the final version<p>
 * 
 * @author         Drew Harris
 *
 * @version 1.3.1  2/24/2020  Fixed corner glitch + improved collision
 *
 * @version 1.3.0  2/24/2020  Added balls disapearing
 * 
 * @version 1.2.1  2/22/2020  Added new map and fixed indentation / 
 *                            matched google style guide -indentation
 *
 * @version 1.2.0  2/22/2020  Added documentation
 *
 * @version 1.1.0  2/21/2020  Created tank movement and implemented bullet shooting
 *
 * @since          2020-02-22
 *
 */
public class TanksMovement extends Application {

    Bullet[] myBullets;
    boolean[] keyStatus;
    PixelReader reader;
    Tank player;

    public static final int BULLET_LIMIT = 20;
    public static final int BULLET_SPEED = 4;
    
    public static final int UP = 0;
    public static final int DOWN = 1;
    public static final int LEFT = 2;
    public static final int RIGHT = 3;

    public class KeyBoard implements EventHandler<KeyEvent> {
        @Override
        public void handle(KeyEvent e) {
            if (e.getEventType() == KeyEvent.KEY_PRESSED) {

                if (e.getCode() == KeyCode.RIGHT) {
                   keyStatus[3] = true;
                } else if (e.getCode() == KeyCode.LEFT) {
                   keyStatus[2] = true;
                } else if (e.getCode() == KeyCode.UP) {
                   keyStatus[0] = true;
                } else if (e.getCode() == KeyCode.DOWN) {
                    keyStatus[1] = true;
                }

                if (e.getCode() == KeyCode.END) {
                    player.shoot();
                }
            }

            if (e.getEventType() == KeyEvent.KEY_RELEASED) {

                if (e.getCode() == KeyCode.RIGHT) {
                   keyStatus[3] = false;
                } else if (e.getCode() == KeyCode.LEFT) {
                   keyStatus[2] = false;
                } else if (e.getCode() == KeyCode.UP) {
                   keyStatus[0] = false;
                } else if (e.getCode() == KeyCode.DOWN) {
                   keyStatus[1] = false;
                }
            }
        }
    }

    /** Contains the main game loop.*/
    public class TanksAnimationTimer extends AnimationTimer {

        /** Runs 60 times a second.*/
        public void handle(long e) {

            // changing angles
            if (keyStatus[2]) {
                player.changeAngle(3.5);
            } else if (keyStatus[3]) {
                player.changeAngle(-3.5);
            }

            // forward and backward
            if (keyStatus[0]) {
                player.go(true);
            } else if (keyStatus[1]) {
                player.go(false);
            }

            player.setPosition();

            for (Bullet bullet : myBullets) {
                if (bullet.enabled) {
                    bullet.moveBullet();
                    bullet.doReflect();
                }
            }
        }
    }
    
   /** Returns the first bullet in the array that is not active.
    * TODO: make the array a parameter
    * @return the first index of a bullet that is not being used.
    */
    public int getFirstBullet() {
        for (int i = 0; i < myBullets.length; i++) {
            if (!myBullets[i].enabled) {
                return i;
            }
       }

       return -1;
    }

    public static void main(String[] args) {
        launch(args);
    }


    public void start(Stage stage) {

        Group root = new Group();
        Scene scene = new Scene(root, 1280, 720, Color.WHITE);

        // TODO: new functions for different maps
        Image map = new Image("test.png");
        ImageView mapView = new ImageView();
        mapView.setImage(map);
        root.getChildren().add(mapView);
        reader = map.getPixelReader();

        //signature rectangle 
        Rectangle rKey = new Rectangle(0,0,5,5);
        rKey.setFill(Color.RED);
        KeyBoard kb = new KeyBoard();
        root.getChildren().add(rKey);
        rKey.addEventHandler(KeyEvent.ANY, kb);
        rKey.requestFocus();

        myBullets = new Bullet[BULLET_LIMIT];
        for(int i = 0; i < myBullets.length; i++) {
           myBullets[i] = new Bullet(root);
           myBullets[i].setStatus(false);
        }

        player = new Tank(root);

        keyStatus = new boolean[4];
        stage.setTitle("Tanks Movement Test");
        stage.setScene(scene);
        stage.show();

        TanksAnimationTimer aniTimer = new TanksAnimationTimer();
        aniTimer.start();
    }

   //-------------------------------------------------------------------------------------------

   /** Player controlled image*/
    public class Tank {

        Rectangle dispTank;
        Rectangle testBounds;

        double xPos;
        double yPos;
        double xVel;
        double yVel;

        /** The speed of the tank going in any direction*/
        double rawSpeed = 4;
        double angle;

        boolean[] collisionList;  // direction for the tank's collision, uses class constants
                        // UP DOWN LEFT RIGHT
        
        boolean canShoot;
        
        public Tank(Group root) {
            dispTank = new Rectangle(0, 0, 30, 55);
            dispTank.setFill(Color.BLUE);
            root.getChildren().add(dispTank);
            angle = 0;
            xVel = 0;
            yVel = 0;
            canShoot = true;
            setX(640);  // starting x and
            setY(360);  // y positions
            dispTank.setRotate(angle * -1 - 90);    // initally sets tank to face RIGHT
            
        }
        
        

        /** Called whenever user presses END key.*/
        public void shoot() {
            int index = getFirstBullet();
            if (index != -1 && canShoot) {
            
                myBullets[index].setStatus(true);
                myBullets[index].setX(getMiddlePoint().getX() + 27.5 * Math.cos( Math.toRadians( angle)));
                myBullets[index].setY(getMiddlePoint().getY() + 27.5 * -1 *Math.sin( Math.toRadians( angle)));
                
                myBullets[index].setSpeed( (Math.cos( Math.toRadians( angle)) * BULLET_SPEED),
                                           (Math.sin( Math.toRadians( angle )) * - BULLET_SPEED) );
                                           
                canShoot = false;
                
                PauseTransition waitT = new PauseTransition(Duration.millis(200));
                waitT.setOnFinished((new EventHandler<ActionEvent>() {
    			    public void	handle(ActionEvent e){
                        canShoot = true;
    			    }
    		    })); 
                waitT.play();
                
            }
        }

        /** Gets the center point of the tank in terms of its bounds.
        * @return Point2D middlePoint - the center of the tank
        */
        public Point2D getMiddlePoint() {
            Bounds bound = dispTank.getBoundsInParent();
            return new Point2D(bound.getMinX() + bound.getWidth()/2, bound.getMinY() + bound.getHeight()/2);
        }

        /** Returns the class constant for direction 
        * if the tank's bounds are touching a black pixel.
        * Will require access to Pixel reader in the future
        * @return The class constant for direction representing the direction of collision
        */
        public boolean[] findCollision() {
            boolean[] collisionList = new boolean[6];   //up down left right front back
        
            Bounds bound = dispTank.getBoundsInParent();
            
            int cornerOffset = 30;
            int scannerSteps = 2;
            
            // up && down
            
            if (reader.getColor( (int)(bound.getMinX() + bound.getWidth()/2), (int)bound.getMinY() ).equals(Color.BLACK)) {
                 collisionList[UP] = true;
            } else if (reader.getColor( (int)(bound.getMinX() + bound.getWidth()/2), (int)bound.getMaxY() ).equals(Color.BLACK)) {
                collisionList[DOWN] = true;
            }
            
            
            // right && left
            if (reader.getColor( (int)bound.getMinX(), (int)(bound.getMinY() + bound.getHeight()/2) ).equals(Color.BLACK)) {
                collisionList[LEFT] = true;
            } else if (reader.getColor( (int)bound.getMaxX(), (int)(bound.getMinY() + bound.getHeight()/2) ).equals(Color.BLACK)) {
                collisionList[RIGHT] = true;
            }
            
            // front & back
            if (reader.getColor( (int)(getMiddlePoint().getX() + 27.5 *Math.cos( Math.toRadians( angle))) ,
             (int) ((getMiddlePoint().getY() + 27.5 * -1 *Math.sin( Math.toRadians( angle))) ) ).equals(Color.BLACK)) {
                collisionList[4] = true;
            }else if (reader.getColor( (int)(getMiddlePoint().getX() - 27.5 * Math.cos( Math.toRadians( angle))) ,
             (int) ((getMiddlePoint().getY() - 27.5 * -1 *Math.sin( Math.toRadians( angle))) ) ).equals(Color.BLACK)) {
                collisionList[5] = true;
            }

            
            return collisionList;
        }

        /** Updates the classes xPos and yPos but does not actually move image node.
         * TODO: Change the way that the position and velocity interface
         * @param fwd True if the user is moving foreward. (false for reverse)
         */
        public void go(boolean fwd) {

            collisionList = findCollision();

            if (fwd) {
                xVel = Math.cos( Math.toRadians( angle ))* rawSpeed;
                yVel = Math.sin( Math.toRadians( angle ))* -rawSpeed;
            } else {
                xVel = Math.cos( Math.toRadians( angle ))* -rawSpeed;
                yVel = Math.sin( Math.toRadians( angle ))* rawSpeed;
            }
            
            if (collisionList[4] && fwd){
                
            } else if (collisionList[5] && !fwd){
                
            } else{
            
                if (!collisionList[UP] && yVel < 0){
                    yPos += yVel;
                }else if (!collisionList[DOWN] && yVel > 0){
                    yPos += yVel;
                }
                
                if (!collisionList[LEFT] && xVel < 0){
                    xPos += xVel;
                }else if (!collisionList[RIGHT] && xVel > 0){
                    xPos += xVel;
                }
                
            }
            

        }

        /** Called when user presses left or right arrow keys.
         * Changes the angle of the tank.
         * @param angleBy How many degrees to change the angle of the tank by.
         */
        public void changeAngle(double angleBy) {
            collisionList = findCollision();
            double newAngle = angle + angleBy * 2;

            // TODO: make turning easier up against a wall
            if (collisionList[UP] || collisionList[DOWN]) {

                if (  (Math.sin( Math.toRadians(newAngle))) <= (Math.sin( Math.toRadians(angle))) ) {
                        angle += angleBy;
                }

            } else if (collisionList[LEFT] || collisionList[RIGHT]) {

                if (  (Math.cos( Math.toRadians(newAngle))) <= (Math.cos( Math.toRadians(angle))) ) {
                    angle += angleBy;
                }

            } else {  // if no collision found
                angle += angleBy;
            }

            // actually moves the image node
            dispTank.setRotate(angle * -1 - 90);
        }

        /** Actually moves the node to the classes x and y position doubles. */
        public void setPosition() {
            dispTank.setTranslateX(xPos);
            dispTank.setTranslateY(yPos);
        }

        /** Sets classes x position value.
        * @param x The x value you would like the tank moved to
        */
        public void setX(double x) {
            xPos = x;
        }

        /** Sets classes y position value.
        * @param y The y value you would like the tank moved to
        */
        public void setY(double y) {
            yPos = y;
        }

        public double getX() {
            return xPos;
        }

        public double getY() {
             return yPos;
        }


    }

    //-------------------------------------------------------------------------------------------
    /** Represents a JavaFX circle node that can bounce off walls using black pixel detection.
    * Shot by tank
    *
    */
    public class Bullet{
        double xPos;
        double yPos;
        double xVel;
        double yVel;
        
        int collisionCount; 
        
        boolean enabled;

        int radius; // TODO: make radius a class final constant

        Circle displayCircle;

        /** Constuctor for the bullet.
         * Used only once in the <code>start</code> method.
         *
         * @param root THIS SHOULD ONLY EVER BE THE ROOT NODE
         */
        public Bullet(Group root) {
            radius = 4;
            displayCircle = new Circle(0, 0, radius);
            displayCircle.setFill(Color.BLUE);
            root.getChildren().add(displayCircle);
            
        }

        
        
        public void doEvaporate() {
            FillTransition waitT = new	FillTransition(Duration.millis(300), displayCircle, Color.BLUE, Color.TRANSPARENT);
            waitT.setOnFinished((new EventHandler<ActionEvent>() {
    			public void	handle(ActionEvent e){
                    collisionCount = 0;
    				displayCircle.setFill(Color.TRANSPARENT);
                    enabled = false;
    			}
    		})); 
            waitT.play();
        }

        /** Sets the status of the bullet.
         * @param state true if the bullet is relevant
         *
         */
        public void setStatus(boolean state) {
            enabled = state;
            collisionCount = 0;
            if (state) {
                displayCircle.setFill(Color.BLUE);
            }
        }

        /** Moves the bullet by adding velocities to class position integers than
         * moves the node itself.
         *
         */
        public void moveBullet() {
            
            xPos += xVel;
            yPos += yVel;
            displayCircle.setTranslateX(xPos);
            displayCircle.setTranslateY(yPos);
            
        }
        
        
        /** Tests to see if the point is a black pixel on the image.
         * Try to keep the parameter point object not on a decimal place
         * @param point The point that you want to check
         * @return true if the pixel is black
         */
        public boolean inBlack(Point2D point) {
            if (point.getX() > 0 && point.getX() < 1280 &&
                point.getY() > 0 && point.getY() < 720) {
                if (reader.getColor((int)point.getX(),
                   (int)point.getY()).equals(Color.BLACK)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }

        /** Tests to see if there is a collision then reflects the bullet accordingly.
         * Ran on enabled bullets in the game loop
         */
        public void doReflect() {
            Point2D homePoint = new Point2D(xPos, yPos);
            if (inBlack(homePoint)) {
                Point2D refX = homePoint.add(-1 * xVel, yVel);
                Point2D refY = homePoint.add(xVel, yVel * -1);

                if (!inBlack(refX) && inBlack(refY)) {    //reflecting x works
                    xVel *= -1;
                } else if (!inBlack(refY) && inBlack(refX)) {
                    yVel *= -1;
                } else if (inBlack(refY) && inBlack(refX)) {
                    yVel *= -1;
                    xVel *= -1;
                }
                collisionCount++;
                if (collisionCount == 3) {
                    doEvaporate();
                }
            }
        }

        /** @param x the x value to move the bullet to */
        public void setX(double x) {
            xPos = x;
        }

        /** @param y the y value to move the bullet to */
        public void setY(double y) {
            yPos = y;
        }

        /** Gets the current x value of the bullet's position.
         *  @return double - the x value of the bullet
         */
        public double getX() {
            return xPos;
        }

        /** Gets the current y value of the bullet's position.
         *  @return double - the y value of the bullet
         */
        public double getY() {
            return yPos;
        }

        /** Sets the speed of the bullet.
         * Mostly likely only used in the tanks' {@code shoot} method
         * @param x horizontal speed
         * @param y vertical speed
         */
        public void setSpeed(double x, double y) {
            xVel = x;
            yVel = y;
        }
    }
}