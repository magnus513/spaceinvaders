package se.magnus.spaceinvaders;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.media.MediaException;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/*
 * Game is adopted to display using 240 x 320 pixels.
 *
 * TODO1: build jar depending to build configuration using Adoption class.
 * rendate screen dependent upon screen resolution.
 * TODO2: fix debugging output.
 */

public class SpaceInvadersMIDlet extends MIDlet implements CommandListener
{
    private final long WELCOME_SCREEN_TIME = 3000;
    private Display display = null;
    private final Command /*newGameCommand,*/ exitGameCommand, optionsCommand, infoCommand, storyCommand, scoreScreen;
    private SpaceInvadersCanvas spaceCanvas;
    private WelcomeScreen welcome = null;
    private static ImageAndTextScreen testScreen = null;
    private final String INFO_TEXT = "You have never liked Mondays, but especially not this one." +
                                    "You awake to discover that your bases are under attack by a swarm of Doxithons " +
                                    "(vicious, nasty creatures) that will stop at nothing to destroy anything that breathes.\n\n" +
                                    "Your objective is simple - defeat the invading aliens!\n\n" + "Controls: \nLeft or '4'-Move left\n" +
                                    "Right or '6'-Move right\nFire or '5'-Fire pulse cannon";

    private final String STORY_TEXT = "The legion of space invaders are attempting to take over the earth, " +
                                      "but first they must establish a staging ground on the surface of the moon.\n\n" +
                                      "Your mission is to prevent the moon from being taken.\n\nYou must single handedly destroy " +
                                      "each faction of the space invader army, and you must not let a single alien " +
                                      "touch down on the moon's surface, or the humanity's future will be over.";

    public SpaceInvadersMIDlet()
    {
        display = Display.getDisplay(this);

        String useWelcomeScreen = String.valueOf(getAppProperty("spaceInvaders-useWelcomeScreen")).trim();
        if(useWelcomeScreen.equals("true"))
            welcome = new WelcomeScreen();

        String useDebugging = String.valueOf(getAppProperty("spaceInvaders-useDebugging")).trim();
        if("true".equalsIgnoreCase(useDebugging))
            Misc.getInstance().setDebugging(true);

        // only do http to JP>=8 due to jsr177, only works on SE devices.
        if(Misc.getInstance().getJP()>= 8)
        {
            String highScoreBaseURL = String.valueOf(getAppProperty("spaceInvaders-highScoreBaseURL")).trim();
            if(!highScoreBaseURL.equals("") && highScoreBaseURL.indexOf("http://") != -1)
                Misc.getInstance().setHighScoreBaseURL(highScoreBaseURL);
        }

        spaceCanvas = new SpaceInvadersCanvas(this);
        //testScreen = new ImageAndTextScreen(this, Misc.getInstance().getMonsterLargeImage(), Misc.getInstance().getNoImage(), Misc.getInstance().getYesImage(), "Start Wave One?", false);

        exitGameCommand = new Command("Exit Game", Command.EXIT, 1);
        //newGameCommand = new Command("New Game", Command.SCREEN, 1);
        optionsCommand = new Command("Options", Command.SCREEN, 2);
        infoCommand = new Command("Info", Command.SCREEN, 2);
        storyCommand = new Command("Story", Command.SCREEN, 2);
        scoreScreen = new Command("Alien kill scores", Command.SCREEN, 2);

        spaceCanvas.addCommand(exitGameCommand);
        //spaceCanvas.addCommand(newGameCommand);
        spaceCanvas.addCommand(optionsCommand);
        spaceCanvas.addCommand(infoCommand);
        spaceCanvas.addCommand(storyCommand);
        spaceCanvas.addCommand(scoreScreen);

        spaceCanvas.setCommandListener(this);

    }

    protected void destroyApp(boolean unconditional)
    {
       
    }

    protected void pauseApp()
    {
        if(spaceCanvas.getGameThread() != null)
            spaceCanvas.getGameThread().yield();
        if(spaceCanvas.getAliensThread() != null)
            spaceCanvas.getAliensThread().yield();
        if(spaceCanvas.getMotherShipThread() != null)
            spaceCanvas.getMotherShipThread().yield();
    }

    protected void startApp() throws MIDletStateChangeException
    {
        //TODO: REMOVE do not play sound
        Sound.getInstance().setMute(true);
        try
        {
            Sound.getInstance().playBackGroundMusic(-1);
        }
        catch(MediaException me)
        {
            me.printStackTrace();
        }

        // show welcome screen.
        if(welcome != null)
        {
            welcome.start();
            //display.setCurrent(welcome);
            setCurrentScreen(welcome);
            if(Misc.getInstance().getDebugging())
                System.out.println(getClass().toString() + " thread name " + Thread.currentThread().getName());
            try
            {
                Thread.currentThread().sleep(WELCOME_SCREEN_TIME);
            }
            catch(InterruptedException ie)
            {
                ie.printStackTrace();
            }
        }
        setNextWaveScreen(false, 1);
    }

    public void commandAction(Command c, Displayable screen)
    {
        if( c == exitGameCommand )
        {
            quit();
        }
        /*
        else if( c == newGameCommand )
        {
            if(Misc.getInstance().getDebugging())
                System.out.println( c.getLabel() );
        }
        */
        else if( c == optionsCommand )
        {
            if(Misc.getInstance().getDebugging())
                System.out.println( c.getLabel() );
            display.setCurrent( new OptionScreen(this) );
        }
        else if( c == infoCommand )
        {
            if(Misc.getInstance().getDebugging())
                System.out.println( c.getLabel() );
            display.setCurrent( new TextScreenScreen(this, "Instructions", INFO_TEXT) );
        }
        else if( c == storyCommand )
        {
            if(Misc.getInstance().getDebugging())
                System.out.println( c.getLabel() );
            display.setCurrent( new TextScreenScreen(this, "Story", STORY_TEXT) );
        }
        else if( c == scoreScreen )
        {
            if(Misc.getInstance().getDebugging())
                System.out.println( c.getLabel() );
            testScreen = new ImageAndTextScreen(this, Misc.getInstance().getLargeBackgroundImage(), Misc.getInstance().getNoImage(), Misc.getInstance().getYesImage(), "Alien kill scores.", false, 2);
            testScreen.start();
            setCurrentScreen(testScreen);
        }
    }

    public void newGame()
    {
        setNextWaveScreen(false, 1);
        spaceCanvas.playAgain();
        //start();
    }
    
    public void backToSpaceInvadersCanvasScreen()
    {
        Display.getDisplay(this).setCurrent(spaceCanvas);
    }

    public void start()
    {
        spaceCanvas.start();
        //display.setCurrent(spaceCanvas);
        setCurrentScreen(spaceCanvas);
    }

    public void getHighScoreName()
    {
        TextScreenScreen highScore = new TextScreenScreen(this, "High Score");
        display.setCurrent(highScore);
    }

    public void setNextWaveScreen(boolean fullscreen, int id)
    {
        testScreen = new ImageAndTextScreen(this, Misc.getInstance().getMonsterLargeImage(), Misc.getInstance().getNoImage(), Misc.getInstance().getYesImage(), "Start Wave One?", fullscreen, id);
        testScreen.start();
        setCurrentScreen(testScreen);
    }

    public void setHighScoreName()
    {
        testScreen = new ImageAndTextScreen(this, Misc.getInstance().getMonsterNoTextLargeImage(), Misc.getInstance().getNoImage(), Misc.getInstance().getYesImage(), "Game Over", false, 3);
        testScreen.start();
        //setCurrentScreen(testScreen);
        Display.getDisplay(this).setCurrent(testScreen);
        //spaceCanvas.stop();
    }

    public void updateHighScoreName()
    {
        if(testScreen != null)
        {
            testScreen.drawHighScoreStatistics();
            Display.getDisplay(this).setCurrent(testScreen);
        }
    }

    public void quit()
    {
        //destroyApp(false);
        // will stop playing background music.
        Sound.getInstance().setMute(true);
        if(spaceCanvas != null)
        {
            spaceCanvas.stop();
            //spaceCanvas.getGameThread().interrupt();
            spaceCanvas = null;
        }
        notifyDestroyed();
    }

    public SpaceInvadersCanvas getSpaceInvadersCanvas()
    {
        return spaceCanvas;
    }

    /**
     *
     * @return Display
     */
    public synchronized Display getCurrentDisplay()
    {
        return display;
    }

    /**
     *
     * @param Displayable screen
     */
    public void setCurrentScreen(Displayable screen)
    {
        if(display != null)
        {
            display.setCurrent(screen);
        }
    }

}