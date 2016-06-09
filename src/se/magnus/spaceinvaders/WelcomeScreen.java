package se.magnus.spaceinvaders;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;

/**
 *
 * @author magnus
 */
public class WelcomeScreen extends GameCanvas implements Runnable
{
    Graphics g = null;
    private volatile Thread dotThread = null;
    private static final int MILLIS_PER_DOT = 1000;      // ms main loop thread.
    int dotCounter = 0;
    public WelcomeScreen()
    {
        super(true);
        g = getGraphics();
    }
    
    public void paint(Graphics g)
    {
        this.setFullScreenMode(true);
        //Set color to white
        g.setColor(0x0FFFFFF);
        //Fill entire screen
        g.fillRect( 0, 0, getWidth(), getHeight() );

        Image splash = Misc.getInstance().getWelcomeLargeImage();
        if(splash != null)
            g.drawImage(splash, 0, 0, Graphics.TOP | Graphics.LEFT);

        g.drawString("SpaceInvaders...", getWidth()/2, getHeight()/2, Graphics.HCENTER | Graphics.BOTTOM);
        //dotThread = new Thread(this);
        //dotThread.start();
        flushGraphics();

    }
    /*
    public void update(Graphics g)
    {
        g.setColor(0, 0, 0);
        g.fillRect(0, 0, getWidth(), getHeight());
    }
    */

    public synchronized void start()
    {
        if(Misc.getInstance().getDebugging())
            System.out.println(getClass().toString() + " thread name " + Thread.currentThread().getName());
        paint(g);
    }

    /**
     * Thread that should draw ., .., ... each MILLIS_PER_DOT.
     */
    public void run() {

        Thread currentThread = Thread.currentThread();
        try
        {
            while(currentThread == dotThread)
            {
                long startTime = System.currentTimeMillis();
                long timeTaken = System.currentTimeMillis() - startTime;
                if(timeTaken < MILLIS_PER_DOT)
                {
                    synchronized(this)
                    {
                        /*
                        dotCounter++;
                        if(dotCounter==1)
                            g.drawString("SpaceInvaders.", getWidth()/2, getHeight()/2, Graphics.HCENTER | Graphics.BOTTOM);
                        else if(dotCounter==2)
                            g.drawString("SpaceInvaders..", getWidth()/2, getHeight()/2, Graphics.HCENTER | Graphics.BOTTOM);
                        else if(dotCounter==3)
                        {
                            g.drawString("SpaceInvaders...", getWidth()/2, getHeight()/2, Graphics.HCENTER | Graphics.BOTTOM);
                            dotCounter = 0;
                        }
                        flushGraphics();
                        */
                        wait(MILLIS_PER_DOT - timeTaken);
                        g.drawString("SpaceInvaders...", getWidth()/2, getHeight()/2, Graphics.HCENTER | Graphics.BOTTOM);
                        flushGraphics();
                    }
                }
            }
        }
        catch(InterruptedException ie)
        {
            ie.printStackTrace();
        }
    }

}