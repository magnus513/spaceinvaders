package se.magnus.spaceinvaders;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

/**
 * Genric class that describes a Missile. A missle could belong to the player or the aliens/mothership.
 * @author magnus
 */
public class Missile extends Sprite implements Runnable
{
    static final int WIDTH_PLAYER_MISSILE = 3, HEIGHT_PLAYER_MISSILE = 3, WIDTH_ALIEN_MISSILE = 3, HEIGHT_ALIEN_MISSILE = 9;
    private final static int MILLIS_PER_TICK = 32, MISSILE_PLAYER_PIXELS_MOVING_SPEED = 5, MISSILE_ALIEN_PIXELS_MOVING_SPEED = 4;
    private final SpaceInvadersCanvas spaceCanvas;
    private boolean missileFired= false, isMissilePlayer = true;
    private static boolean missileAnimation = true;     //used to animate the missile.
    private volatile Thread missileThread = null;
    private int x=0, y=0;

    private static int[][] animations = {{0}, {1}, {2}};

    /**
     *
     * @param spaceCanvas
     * @param missileImage
     * @param missileImageSpriteWidth
     * @param missileImageSpriteHeigh
     * @param isMissilePlayer
     */
    public Missile(SpaceInvadersCanvas spaceCanvas, Image missileImage, int missileImageSpriteWidth, int missileImageSpriteHeigh, boolean isMissilePlayer)
    {
        //super(Misc.getInstance().getPlayerMissile(), 3, 3);
        //super(isMissilePlayer?Misc.getInstance().getPlayerMissile():Misc.getInstance().getAlien1InvaderMissile(), 3, isMissilePlayer?3:5);
        //super(missileImage, isMissilePlayer?WIDTH_PLAYER_MISSILE:WIDTH_ALIEN_MISSILE, isMissilePlayer?HEIGHT_PLAYER_MISSILE:HEIGHT_ALIEN_MISSILE);
        super(missileImage, missileImageSpriteWidth, missileImageSpriteHeigh);
        this.isMissilePlayer = isMissilePlayer;

        //defineCollisionRectangle(2, 2, WIDTH_PLAYER-1, isMissilePlayer?(HEIGHT_PLAYER-1):HEIGHT_ALIEN-2);
        //defineCollisionRectangle(0, 0, isMissilePlayer?WIDTH_PLAYER_MISSILE:WIDTH_ALIEN_MISSILE, isMissilePlayer?HEIGHT_PLAYER_MISSILE:HEIGHT_ALIEN_MISSILE);
        defineCollisionRectangle(0, 0, missileImageSpriteWidth, missileImageSpriteHeigh);
        //defineReferencePixel(isMissilePlayer?WIDTH_PLAYER_MISSILE/2:WIDTH_ALIEN_MISSILE/2, isMissilePlayer?HEIGHT_PLAYER_MISSILE/2:HEIGHT_ALIEN_MISSILE/2);
        defineReferencePixel(missileImageSpriteWidth/2, missileImageSpriteHeigh/2);

        this.spaceCanvas = spaceCanvas;
        this.setVisible(false);
    }

    /**
     * Method will start missile thread and make the missile visiable.
     * @param x x-asis start coordinate.
     * @param y y-axis start coordinate.
     */
    public synchronized void start(int x, int y)
    {
        setMissileFired(true);
        this.setVisible(true);
        this.x = x;
        this.y = y;
        this.setPosition(x, y);
        missileThread = new Thread(this);
        missileThread.start();
    }

    public synchronized void stop()
    {
        missileAnimation = true;
        setMissileFired(false);
        this.setVisible(false);
        missileThread = null;
    }

    public synchronized void run()
    {
        Thread currentThread = Thread.currentThread();
        try
        {
            while(currentThread == missileThread)
            {

                long startTime = System.currentTimeMillis();

                // player has lanched missile, direction in negative y-axis.
                if(isMissilePlayer)
                {
                    if(Misc.getInstance().getDebugging())
                        System.out.println("player missile x= " + this.getX() + ", y= " + this.getY());
                    // make the missile animation.
                    this.setFrameSequence(animations[(missileAnimation)?0:1]);
                    missileAnimation = (missileAnimation)?false:true;

                    this.move(0, -MISSILE_PLAYER_PIXELS_MOVING_SPEED);
                    if(this.getY() <= 0)
                        stop();
                    //this.move(x, y);
                    //this.setPosition(x, y);
                }
                // alien has lanched missile, direction in positive y-axis.
                else if(!isMissilePlayer)
                {
                    if(Misc.getInstance().getDebugging())
                        System.out.println("alien missile x= " + this.getX() + ", y= " + this.getY());
                    // make the missile animation.
                    this.setFrameSequence(animations[(missileAnimation)?0:1]);
                    missileAnimation = (missileAnimation)?false:true;

                    this.move(0, MISSILE_ALIEN_PIXELS_MOVING_SPEED);
                    if(this.getY() >= spaceCanvas.getField().getHeight())
                        stop();
                }
                long timeTaken = System.currentTimeMillis() - startTime;

                if(!isMissilePlayer)
                {
                    if(timeTaken < spaceCanvas.getAlienMissileSpeedTime())
                    {
                        synchronized(this)
                        {
                            wait(spaceCanvas.getAlienMissileSpeedTime() - timeTaken);
                        }
                    }
                    else
                        currentThread.yield();
                }
                else
                {
                    if(timeTaken < MILLIS_PER_TICK)
                    {
                        synchronized(this)
                        {
                            wait(MILLIS_PER_TICK - timeTaken);
                        }
                    }
                    else
                        currentThread.yield();
                }
            }
        }
        catch(InterruptedException ie)
        {
            ie.printStackTrace();
        }
    }

    public void setMissileFired(boolean missileFired)
    {
        this.missileFired = missileFired;
    }

    public boolean isMissileFired()
    {
        return missileFired;
    }

}