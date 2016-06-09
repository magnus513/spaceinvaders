package se.magnus.spaceinvaders;

import javax.microedition.lcdui.game.Sprite;

/**
 * Their are four walls (shields) that absorbs missiles, protecting the player from alien fire.
 * @author magnus
 */
public class Wall extends Sprite
{
    private final SpaceInvadersCanvas spaceCanvas;
    static final int WALL_WIDTH = 6, WALL_HEIGHT = 8;

    private int[][] animations = {{0}, {1}, {2}, {3}, {4}, {5}, {6}, {7}};

    public Wall(SpaceInvadersCanvas spaceCanvas)
    {
        super(Misc.getInstance().getWallLargeImage(), WALL_WIDTH, WALL_HEIGHT);
        defineCollisionRectangle(0, 0, WALL_WIDTH, WALL_HEIGHT);
        defineReferencePixel(WALL_WIDTH/2, WALL_HEIGHT/2);

        this.spaceCanvas = spaceCanvas;
        this.setVisible(false);
    }

    /**
     * Set the frame sequence for this Wall (Sprite) and makes it visible.
     * @param part frame sequence number 0-7.
     */
    public void setWallPart(int part)
    {
        if(part==0)
        {
            setFrameSequence(animations[0]);
            this.setVisible(true);
        }
        else if(part==1)
        {
            setFrameSequence(animations[1]);
            this.setVisible(true);
        }
        else if(part==2)
        {
            setFrameSequence(animations[2]);
            this.setVisible(true);
        }
        else if(part==3)
        {
            setFrameSequence(animations[3]);
            this.setVisible(true);
        }
        else if(part==4)
        {
            setFrameSequence(animations[4]);
            this.setVisible(true);
        }
        else if(part==5)
        {
            setFrameSequence(animations[5]);
            this.setVisible(true);
        }
        else if(part==6)
        {
            setFrameSequence(animations[6]);
            this.setVisible(true);
        }
        else if(part==7)
        {
            setFrameSequence(animations[7]);
            this.setVisible(true);
        }
    }

}