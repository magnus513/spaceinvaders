package se.magnus.spaceinvaders;

import javax.microedition.lcdui.game.Sprite;
import javax.microedition.media.MediaException;

/**
 * The alien "mother ship" is randomly visiable on the screen giving diffrent scores if destroyed.
 * @author magnus
 */
public class MotherShip extends Sprite
{
    private final SpaceInvadersCanvas spaceCanvas;
    private static final int MOTHERSHIP_WIDTH = 13, MOTHERSHIP_HEIGHT = 6, MOTHERSHIP_COLLISION_X = 0, MOTHERSHIP_COLLISION_Y = 0;
    private Missile motherShipMissile = null;
    private boolean isMotherShipAlive = true;
    private int motherShipKilledScore = 50;
    private boolean hasMotherShipSoundBeenPlayed = false;

    private static int[][] animations = {{0}, {1}, {2}};

    public MotherShip(SpaceInvadersCanvas spaceCanvas)
    {
        super(Misc.getInstance().getMotherShipImage(), MOTHERSHIP_WIDTH, MOTHERSHIP_HEIGHT);
        defineCollisionRectangle(MOTHERSHIP_COLLISION_X, MOTHERSHIP_COLLISION_Y, MOTHERSHIP_WIDTH, MOTHERSHIP_HEIGHT);
        defineReferencePixel(MOTHERSHIP_WIDTH/2, MOTHERSHIP_HEIGHT/2);

        this.spaceCanvas = spaceCanvas;
        this.setVisible(false);
    }

    /**
     * Method will show animated mothership explosion.
     * @param animation
     */
    public void showExplosionAnimation(boolean animation)
    {
        setFrameSequence(animations[animation?1:2]);
    }

    /**
     * Method will init mothership after an explosion.
     */
    public void init()
    {
        hasMotherShipSoundBeenPlayed = false;
        setFrameSequence(animations[0]);
        //do not display mothership, hence put it outside display.
        this.setPosition(-14, 0);
    }

    public void fireMissile()
    {
        try
        {
            Sound.getInstance().playMotherShipMissile();
        }
        catch(MediaException me)
        {
            me.printStackTrace();
        }
        motherShipMissile.start(this.getX() + 7, this.getY() + 7);
    }

    public Missile createAlienMissile()
    {
        if(motherShipMissile == null)
            motherShipMissile = new Missile(spaceCanvas, Misc.getInstance().getMotherShipMissile(), 3, 9, false);
        return getAliendMissile();
    }

    /**
     * @return true if mothership has fired a missile.
     * @see se.magnus.spaceinvaders.Missile#isMissileFired()
     */
    public boolean isMissileFired()
    {
        return (motherShipMissile != null)?motherShipMissile.isMissileFired():false;
    }

    /*
     * @return Missile referance.
     */
    public Missile getAliendMissile()
    {
        return motherShipMissile;
    }

    /**
     * Method will setVisible() according to parameter.
     *
     * @param isMotherShipAlive true if mothership is alive otherwise mothership is dead.
     * @see javax.​microedition.​lcdui.​game.​Layer#setVisible()
     */
    public void setMotherShipAlive(boolean isMotherShipAlive)
    {
        this.isMotherShipAlive = isMotherShipAlive;
        this.setVisible( isMotherShipAlive?true:false );
    }

    /**
     * @return true if mothership is alive otherwise mothership is dead.
     */
    public boolean isMotherShipAlive()
    {
        return isMotherShipAlive;
    }

    /**
     * @return mothership killed score.
     */
    public int getMotherShipKilledScore()
    {
        return motherShipKilledScore;
    }

    public void setHasMotherShipSoundBeenPlayed(boolean hasMotherShipSoundBeenPlayed)
    {
        this.hasMotherShipSoundBeenPlayed = hasMotherShipSoundBeenPlayed;
    }

    /**
     * @return true if mothership appperance sound has been played otherwise false.
     */
    public boolean getHasMotherShipSoundBeenPlayed()
    {
        return hasMotherShipSoundBeenPlayed;
    }

}