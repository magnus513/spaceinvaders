package se.magnus.spaceinvaders;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;

/**
 *
 * @author magnus
 */
public class ImageAndTextScreen extends GameCanvas implements CommandListener
{
    private final SpaceInvadersMIDlet midlet;
    private Command yesCommand = null, noCommand = null, backCommand = null, playAgainCommand = null, nextCommand = null, previosCommand = null, exitCommand = null;
    private Image screenImage = null, noImage = null, yesImage = null;
    String textMessage = null, firstId = "", lastId = "";
    private boolean fullscreen = false;
    private int id = 0;
    Graphics g = null;

    /**
     *
     * @param midlet
     * @param screenImage
     * @param noImage
     * @param yesImage
     * @param textMessage
     * @param fullscreen
     * @param id
     */
    public ImageAndTextScreen(SpaceInvadersMIDlet midlet, Image screenImage, Image noImage, Image yesImage, String textMessage, boolean fullscreen, int id)
    {
        super(true);
        this.midlet = midlet;
        this.fullscreen = fullscreen;
        g = getGraphics();

        this.id = id;

        this.screenImage = screenImage;
        if(this.id != 2)
            this.noImage = noImage;
        this.yesImage = yesImage;
        this.textMessage = textMessage;

        if(this.id == 1 || this.id == 4)
        {
            noCommand = new Command("No", Command.SCREEN, 1);
            addCommand(noCommand);
            yesCommand = new Command("Yes", Command.SCREEN, 2);
            addCommand(yesCommand);
        }
        else if(id == 2)
        {
            backCommand = new Command("Back", Command.BACK, 1);
            addCommand(backCommand);
        }
        else if(id == 3)
        {
            previosCommand = new Command("Prevoius 10", Command.SCREEN, 1);
            addCommand(previosCommand);
            nextCommand = new Command("Next 10", Command.SCREEN, 2);
            addCommand(nextCommand);
            playAgainCommand = new Command("Play again?", Command.SCREEN, 3);
            addCommand(playAgainCommand);
            exitCommand = new Command("Exit", Command.EXIT, 4);
            addCommand(exitCommand);
        }

        setCommandListener(this);

    }

    public void paint(Graphics g)
    {
        this.setFullScreenMode(fullscreen);
        //Set color to black
        g.setColor(Misc.getInstance().BLACK);
        //Fill entire screen
        g.fillRect( 0, 0, getWidth(), getHeight() );

        if(id == 1 || id == 4)
        {
            g.drawImage(screenImage, 0, 0, Graphics.TOP | Graphics.LEFT);
            if(!fullscreen)
            {
                if(textMessage != null)
                {
                    g.setColor(Misc.getInstance().WHITE);
                    g.drawString(textMessage, 50, 10, Graphics.TOP | Graphics.LEFT);
                }
                if(noImage != null)
                    g.drawImage(noImage, 5, getHeight()-5, Graphics.BOTTOM | Graphics.LEFT);
                if(yesImage != null)
                    g.drawImage(yesImage, getWidth()-5, getHeight()-5, Graphics.BOTTOM | Graphics.RIGHT);
            }
            //g.drawString("SpaceInvaders...", getWidth()/2, getHeight()/2, Graphics.HCENTER | Graphics.BOTTOM);
        }
        else if(id == 2)
        {
            g.drawImage(screenImage, 0, 0, Graphics.TOP | Graphics.LEFT);
            if(!fullscreen)
            {
                if(textMessage != null)
                {
                    g.setColor(Misc.getInstance().WHITE);
                    g.drawString(textMessage, 60, 10, Graphics.TOP | Graphics.LEFT);
                }

                Image alien1 = Misc.getInstance().createImage("/image/alien11.png");
                if(alien1 != null)
                    g.drawImage(alien1, getWidth()/2 - 46 , getHeight()/2-74, Graphics.TOP | Graphics.LEFT);
                g.drawString(" = 10 PTS", getWidth()/2 - 30, getHeight()/2-80, Graphics.TOP | Graphics.LEFT);

                Image alien2 = Misc.getInstance().createImage("/image/alien22.png");
                if(alien2 != null)
                    g.drawImage(alien2, getWidth()/2 - 46 , getHeight()/2-53, Graphics.TOP | Graphics.LEFT);
                g.drawString(" = 20 PTS", getWidth()/2 - 30, getHeight()/2-60, Graphics.TOP | Graphics.LEFT);

                Image alien3 = Misc.getInstance().createImage("/image/alien33.png");
                if(alien3 != null)
                    g.drawImage(alien3, getWidth()/2 - 44 , getHeight()/2-34, Graphics.TOP | Graphics.LEFT);
                g.drawString(" = 30 PTS", getWidth()/2 - 30, getHeight()/2-40, Graphics.TOP | Graphics.LEFT);

                Image alien4 = Misc.getInstance().createImage("/image/alien44.png");
                if(alien4 != null)
                    g.drawImage(alien4, getWidth()/2 - 46 , getHeight()/2-10, Graphics.TOP | Graphics.LEFT);
                g.drawString(" = ??? PTS", getWidth()/2 - 30, getHeight()/2-20, Graphics.TOP | Graphics.LEFT);

                if(yesImage != null)
                    g.drawImage(yesImage, 5, getHeight()-5, Graphics.BOTTOM | Graphics.LEFT);
            }
        }
        else if(id == 3)
        {
            g.drawImage(screenImage, 0, 0, Graphics.TOP | Graphics.LEFT);
            if(!fullscreen)
            {
                g.setColor(Misc.getInstance().RED);
                g.setFont(Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_MEDIUM));
                g.drawString("GAME OVER...", (getWidth()/2)-60, 30, Graphics.TOP | Graphics.LEFT);
                g.setColor(Misc.getInstance().WHITE);
                g.drawString("YOUR SCORE IS " + HighScore.getInstance().getPlayerScore(), (getWidth()/2)-60, 50, Graphics.TOP | Graphics.LEFT);

                if(!HighScore.getInstance().getHighScoreMethod())
                    g.drawString("fetching http data...", (getWidth()/2)-60, 90, Graphics.TOP | Graphics.LEFT);
                else
                    g.drawString("fetching rms data...", (getWidth()/2)-60, 90, Graphics.TOP | Graphics.LEFT);

                /*
                if(!midlet.getSpaceInvadersCanvas().getGameOver())
                {
                    int counter = 1;
                    g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
                    g.drawString("***************************", (getWidth()/2)-60, (getHeight()/2)-64, Graphics.TOP | Graphics.LEFT);
                    g.drawString("Rank      Name        Score", (getWidth()/2)-60, (getHeight()/2)-60, Graphics.TOP | Graphics.LEFT);
                    String[] highScoresData = Misc.getInstance().split(HighScore.getInstance().getResponseBody(), ";");
                    for(int i=0; i<highScoresData.length; i++)
                    {
                        if(counter==1)
                            g.drawString(highScoresData[i], (getWidth()/2)-60, (getHeight()/2)-50-i, Graphics.TOP | Graphics.LEFT);
                        else if(counter==2)
                            g.drawString(highScoresData[i], (getWidth()/2)-25, (getHeight()/2)-50-i, Graphics.TOP | Graphics.LEFT);
                        else if(counter==3)
                            g.drawString(highScoresData[i], (getWidth()/2)-50, (getHeight()/2)-50-i, Graphics.TOP | Graphics.LEFT);
                        counter++;
                        if(counter==3)
                            counter=1;
                    }
                }
                */

                /*
                new Thread(){
                
                    public void start()
                    {
                        if(!midlet.getSpaceInvadersCanvas().getGameOver())
                        {
                            //repaint();
                            drawHighScoreStat();
                            try
                            {
                                this.join();
                            }
                            catch(InterruptedException ie)
                            {
                                ie.printStackTrace();
                            }
                        }
                    }
                
                }.start();
                */
            }
        }
        else
        {
            g.drawImage(screenImage, 0, 0, Graphics.TOP | Graphics.LEFT);

        }
        flushGraphics();

    }

    public void start()
    {
        if(Misc.getInstance().getDebugging())
            System.out.println(getClass().toString() + " thread name " + Thread.currentThread().getName());
        paint(g);
    }

    public void commandAction(Command c, Displayable screen)
    {
        if( c == noCommand )
        {
            screenImage = null;
            noImage = null;
            yesImage = null;
            textMessage = null;
            midlet.quit();
        }
        else if( c == yesCommand)
        {
            screenImage = null;
            noImage = null;
            yesImage = null;
            textMessage = null;
            if(id == 1)
                midlet.start();
            else if(id == 4)
                midlet.newGame();
        }
        else if( c == backCommand)
        {
            screenImage = null;
            noImage = null;
            yesImage = null;
            textMessage = null;
            midlet.backToSpaceInvadersCanvasScreen();
        }
        else if( c == playAgainCommand)
        {
            midlet.newGame();
        }
        else if( c == nextCommand)
        {
            //TODO: fetch next 10 high scores.
            //midlet.backToSpaceInvadersCanvasScreen();
            boolean temp = HighScore.getInstance().setAndGetScore(2, firstId, lastId);
            midlet.updateHighScoreName();
        }
        else if( c == previosCommand)
        {
            //TODO: fetch prevoius 10 high scores.
            //midlet.backToSpaceInvadersCanvasScreen();
            boolean temp = HighScore.getInstance().setAndGetScore(3, firstId, lastId);
            midlet.updateHighScoreName();
        }
        else if( c == exitCommand)
        {
            midlet.quit();
        }
    }

    public void drawHighScoreStatistics()
    {
        if(id == 3)
        {
            g.drawImage(screenImage, 0, 0, Graphics.TOP | Graphics.LEFT);
            if(!fullscreen)
            {
                g.setColor(Misc.getInstance().RED);
                g.setFont(Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_MEDIUM));
                g.drawString("GAME OVER...", (getWidth()/2)-60, 30, Graphics.TOP | Graphics.LEFT);
                g.setColor(Misc.getInstance().WHITE);
                g.drawString("YOUR SCORE IS " + HighScore.getInstance().getPlayerScore(), (getWidth()/2)-60, 50, Graphics.TOP | Graphics.LEFT);

                //if(!midlet.getSpaceInvadersCanvas().getGameOver())
                //{
                    g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
                    g.drawString("***************************", 10, 70, Graphics.TOP | Graphics.LEFT);
                    g.drawString("Rank      Name        Score", 10, 90, Graphics.TOP | Graphics.LEFT);
                    
                    String[] highScoresData;
                    if(!HighScore.getInstance().getHighScoreMethod())
                        highScoresData = Misc.getInstance().split(HighScore.getInstance().getResponseBody(), ";");
                    else
                        highScoresData = HighScore.getInstance().getRMSResult();

                    int newline = 0, sep = 110;
                    boolean playerMarker = false;
                    for(int i=0; i<highScoresData.length; i++)
                    {
                        if((i < highScoresData.length - 2) && HighScore.getInstance().getPlayerName().equals(highScoresData[i+1]) && HighScore.getInstance().getPlayerScore().equals(highScoresData[i+2]))
                        {
                            g.setColor(Misc.getInstance().RED);
                            playerMarker = true;
                        }
                        else if(!playerMarker)
                            g.setColor(Misc.getInstance().WHITE);

                        if(i == 0)
                        {
                            firstId = highScoresData[i];
                            //TODO: REMOVE
                            System.out.println("*** first "+firstId);
                        }
                        if(i == highScoresData.length-1)
                        {
                            lastId = highScoresData[i-2];
                            //TODO: REMOVE
                            System.out.println("*** last "+lastId);
                        }

                        if(newline == 0)
                            g.drawString(highScoresData[i], 10, sep, Graphics.TOP | Graphics.LEFT);
                        else if(newline == 1)
                            g.drawString(highScoresData[i], 62, sep, Graphics.TOP | Graphics.LEFT);
                        else if(newline == 2)
                            g.drawString(highScoresData[i], 128, sep, Graphics.TOP | Graphics.LEFT);
                        newline++;
                        if(newline == 3)
                        {
                            newline = 0;
                            sep += 14;
                            playerMarker = false;
                        }

                    }
                //}
            }
        }
        flushGraphics();
    }

}