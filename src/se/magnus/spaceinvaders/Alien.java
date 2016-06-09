package se.magnus.spaceinvaders;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;
import javax.microedition.media.MediaException;

/**
 * An alien is an evil creature. There are three kinds of alien represented in this class.
 * @author magnus
 */
public class Alien extends Sprite
{
    private final SpaceInvadersCanvas spaceCanvas;
    //static final int ALIEN1_WIDTH = 11, ALIEN1_HEIGHT = 8;
    static final int ALIEN1_COLLISION_X = 0, ALIEN1_COLLISION_Y = 0;
    private Missile alienMissile = null;
    private boolean isAlienAlive = true;
    private int alienType = 1;

    //private static int[][] animations = {{1}, {2}, {3}};
    private static int[][] animations = {{0}, {1}, {2}, {3}};

    /**
     *
     * @param spaceCanvas
     * @param alienImage
     * @param alienImageSpriteWidth
     * @param alienImageSpriteHeight
     */
    public Alien(SpaceInvadersCanvas spaceCanvas, Image alienImage, int alienImageSpriteWidth, int alienImageSpriteHeight)
    {
        //super(Misc.getInstance().getAlienInvaderOneImage(), ALIEN1_WIDTH, ALIEN1_HEIGHT);
        super(alienImage, alienImageSpriteWidth, alienImageSpriteHeight);
        defineCollisionRectangle(ALIEN1_COLLISION_X, ALIEN1_COLLISION_Y, alienImageSpriteWidth, alienImageSpriteHeight);
        defineReferencePixel(alienImageSpriteWidth/2, alienImageSpriteHeight/2);

        this.spaceCanvas = spaceCanvas;
        this.setVisible(false);
    }

    public void tick(boolean moving)
    {
        setFrameSequence(animations[moving?0:1]);
        //setTransform(TRANS_NONE);
    }

    /**
     * Method will show animated alien explosion.
     * @param animation
     */
    public void showExplosionAnimation(boolean animation)
    {
        setFrameSequence(animations[animation?2:3]);
        //this.move(this.getX()+1, this.getY()+1);
    }

    public void fireMissile()
    {
        //dummy check
        //if(this.isMissileFired())
        //    return;

        //alienMissile = new Missile(spaceCanvas, false);
        //spaceCanvas.getLayerManager().append(alienMissile);
        try
        {
            Sound.getInstance().playAlienMissile();
        }
        catch(MediaException me)
        {
            me.printStackTrace();
        }
        alienMissile.start(this.getX() + 6, this.getY() + 11);
        //spaceCanvas.flushGraphics();
    }

    public Missile createAlienMissile()
    {
        if(alienMissile == null)
        {
            if(getAlienType()==3)
                alienMissile = new Missile(spaceCanvas, Misc.getInstance().getAlien2InvaderMissile(), 3, 9, false);
            else
                alienMissile = new Missile(spaceCanvas, Misc.getInstance().getAlien1InvaderMissile(), 3, 6, false);
        }
        return getAliendMissile();
    }

    /**
     * @return true if alien has fired a missile.
     * @see se.magnus.spaceinvaders.Missile#isMissileFired()
     */
    public boolean isMissileFired()
    {
        return (alienMissile != null)?alienMissile.isMissileFired():false;
    }

    /*
     * @return Missile referance.
     */
    public Missile getAliendMissile()
    {
        return alienMissile;
    }

    /**
     * Method will setVisible() according to parameter.
     *
     * @param isAlienAlive true if alien is alive otherwise alien is dead.
     * @see javax.​microedition.​lcdui.​game.​Layer#setVisible()
     */
    public void setAlienAlive(boolean isAlienAlive)
    {
        this.isAlienAlive = isAlienAlive;
        this.setVisible( isAlienAlive?true:false );
    }

    /**
     * @return true if alien is alive otherwise alien is dead.
     */
    public boolean isAlienAlive()
    {
        return isAlienAlive;
    }

    /**
     * @param alienType 1=10 point invader (Bottom 2 rows), 2=20 point invader (middle 2 rows), 
     * 3=30 point invader (Top row).
     */
    public void setAlienType(int alienType)
    {
        this.alienType = alienType;
    }

    /**
     * @return alienType 1=10 points invader (Bottom 2 rows), 2=20 points invader (middle 2 rows),
     * 3=30 points invader (Top row).
     */
    public int getAlienType()
    {
        return alienType;
    }

    /**
     * @return alien killed score accoding to typr 1=10 points, type 2=20 points and type 3=30 points.
     */
    public int getAlienKilledScore()
    {
        if(alienType==1)
            return 10;
        else if(alienType==2)
            return 20;
        else if(alienType==3)
            return 30;
        return 0;
    }

}