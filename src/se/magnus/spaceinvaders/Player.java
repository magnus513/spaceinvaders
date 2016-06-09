package se.magnus.spaceinvaders;

import javax.microedition.lcdui.game.Sprite;
import javax.microedition.media.MediaException;

/**
 *
 * @author magnus
 */
public class Player extends Sprite
{
    static final int WIDTH_PLAYER = 15, HEIGHT_PLAYER = 9, VIBRATE_MILLIS = 200, PIXELS_MOVING_SPEED = 3;
    private final SpaceInvadersCanvas spaceCanvas;
    private Missile playerMissile = null;
    /*
    private int[][][] animations = {{{0},                // stand up
                                     {1, 2, 3, 4}},      // run up
                                    {{5},                // stand left
                                     {6, 7, 8, 9}},      // run left
                                    {{10},               // stand down
                                     {11, 12, 13, 14}}}; // run down
     */

    private int[][] animations = {{0}, {1}, {2}};

    private int animationTick = 0, playerKilledScore = -10;
    private int currentDirection = SpaceInvadersCanvas.LEFT;

    public Player(SpaceInvadersCanvas spaceCanvas)
    {
        //super(Misc.getInstance().getPlayerImage(), WIDTH, HEIGHT);
        super(Misc.getInstance().getPlayerImage(), 15, 9);
        //defineCollisionRectangle(2, 2, WIDTH-4, HEIGHT-4);
        defineCollisionRectangle(0, 0, WIDTH_PLAYER, HEIGHT_PLAYER);
        defineReferencePixel(WIDTH_PLAYER/2, HEIGHT_PLAYER/2);

        this.spaceCanvas = spaceCanvas;
    }

    public void tick(int direction)
    {
        animationTick++;
        Field field = spaceCanvas.getField();
        boolean moving = false;

        switch(direction)
        {
            case SpaceInvadersCanvas.LEFT:
                currentDirection = direction;

                if((getX() > 0) && !field.containsImpassableArea(getX()-1, getY(), 1, getHeight()) && moveSuccesfully(-PIXELS_MOVING_SPEED, 0))
                    moving = true;
                else
                    Misc.getInstance().vibrate(VIBRATE_MILLIS, spaceCanvas.getSpaceInvaderMIDlet());

                break;
            case SpaceInvadersCanvas.RIGHT:
                currentDirection = direction;

                if((getX() + getWidth() < field.getWidth()) && !field.containsImpassableArea(getX() + getWidth(), getY(), 1, getHeight()) && moveSuccesfully(PIXELS_MOVING_SPEED, 0) )
                    moving = true;
                else
                    Misc.getInstance().vibrate(VIBRATE_MILLIS, spaceCanvas.getSpaceInvaderMIDlet());

                break;
            default:
                break;
        }
    }

    private boolean moveSuccesfully(int dx, int dy)
    {
        this.move(dx, dy);

        return true;
    }

    /**
     * Method will init player after an explosion.
     */
    public void init()
    {
        setFrameSequence(animations[0]);
    }

    public void fireMissile()
    {
        try
        {
            Sound.getInstance().playPlayerMissile();
        }
        catch(MediaException me)
        {
            me.printStackTrace();
        }
        playerMissile.start(this.getX() + 7, this.getY() + 1);
    }

    public Missile createPlayerMissile()
    {
        if(playerMissile == null)
            playerMissile = new Missile(spaceCanvas, Misc.getInstance().getPlayerMissile(), 3, 3, true);
        return getPlayerMissile();
    }

    /**
     * @return true if player has fired a missile.
     * @see se.magnus.spaceinvaders.Missile#isMissileFired()
     */
    public boolean isMissileFired()
    {
        return (playerMissile != null)?playerMissile.isMissileFired():false;
    }

    /**
     * @return Missile referance.
     */
    public Missile getPlayerMissile()
    {
        return playerMissile;
    }

    /*
     * Method will show animated player explosion.
     * @param animation swithing parameter.
     */
    public void showExplosionAnimation(boolean animation)
    {
        setFrameSequence(animations[animation?1:2]);
    }

     /**
     * @return player killed score.
     */
    public int getPlayerKilledScore()
    {
        return playerKilledScore;
    }

}