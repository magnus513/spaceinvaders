package se.magnus.spaceinvaders;

import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.game.LayerManager;
import javax.microedition.media.MediaException;

/**
 * Class contains main loop that detects key action and re-draws game.
 * @author magnus
 *
 * TODO1: If the player's bullet collides with an alien's bullet, one bullet will cancel the other out, and it can be either your bullet or the alien's that continues on.
 * TODO2: Every time you complete a stage and start a new one, the alien formation begins one row lower than before, up to a certain minimum height above the surface.
 * (From screen 2 through to screen 9 they start progressively lower down the screen. At screen 10 the game reverts to the screen 1 start position and the cycle begins again.)
 * TODO3: Saucer scoring trick, http://strategywiki.org/wiki/Space_Invaders/How_to_play
 * TODO4: You have four shields spread out over the surface that absorb shots, but erode over time?
 */
public class SpaceInvadersCanvas extends GameCanvas implements Runnable
{
    // shared direction constants
    static final int NONE = -1;
    static final int UP = 0;
    static final int LEFT = 1;
    static final int DOWN = 2;
    static final int RIGHT = 3;

    private static final int MILLIS_PER_TICK = 50;      // ms main loop thread.

    private int alienMoveTime = 1000;    // ms how often aliens will move its formation.
    private int alienAttackFrequencyTime = 5000;     // ms how often aliens will lanch missile.
    private int alienMissileSpeedTime = 40;     // ms how often alien or mothership missile moves.
    private int motherShipMoveTime = 150;    // ms how often mothership will move its formation.
    private int motherShipFrequencyTime = 30000;     // ms random interval how often mothership is visiable.
    // delay mothership apperance if killed.
    private int motherShipDelayIfKilled = 10000;     // ms wait 10sec before enable mothership if killed.
    Timer MotherShiptimer = null;
    TimerTask MotherShiptask = null;

    private static int playerScore = 0, playerScoreTemp = 0, playerLives = 1;
    private static final int ROWS_OF_ALIENS = 5, COLUMNS_OF_ALIENS = 11;
    private int PLAYER_SCORE_FOR_EXTRA_LIFE = 1500, extraLifeCounter = 0;
    private final SpaceInvadersMIDlet midlet;
    private static ImageAndTextScreen testScreen = null;

    private LayerManager layerManager;
    private Player player;
    private Field field;

    private Alien[][] aliens;       // aliens matrix [5][11].
    private MotherShip motherShip;
    private Wall[] theWalls = new Wall[32];     // build four walls, containing 32 parts.
    private Alien currentExplosionAlien = null;

    private volatile Thread gameThread = null;
    private Thread aliensThread = null, motherShipThread = null;

    private static boolean direction = true, gameOver = false;
    private boolean difficultyLevelOneEnabled = false, difficultyLevelTwoEnabled = false, difficultyLevelThreeEnabled = false, difficultyLevelFourEnabled = false;
    private static int gameTotalTime = 0, alienTotalTime = 0, sequenceAnimationCounter=1, alienKilled = 0, bonusScore = 0;

    Graphics g = getGraphics();

    SpaceInvadersCanvas(SpaceInvadersMIDlet midlet)
    {
        // suppress key events for game keys
        super(true);
        this.midlet = midlet;
        //setFullScreenMode(true);
        newGame();
    }

    public void playAgain()
    {
        //setGameOver(false);
        setScore(-getScore());
        setLives(1);
        HighScore.getInstance().setPlayerName("");
        HighScore.getInstance().setPlayerScore(0);

        newGame();
    }

    private void newGame()
    {
        //*** add all components into the LayerManager ***
        layerManager = new LayerManager();

        player = new Player(this);
        layerManager.append(player);
        layerManager.append( player.createPlayerMissile() );
        aliens = new Alien[ROWS_OF_ALIENS][COLUMNS_OF_ALIENS];
        for(int r=0; r<aliens.length; r++)
        {
            for(int c=0; c<aliens[r].length; c++)
            {
                // build the alien formation by alien type.
                if(r==0)
                {
                    aliens[r][c] = new Alien(this, Misc.getInstance().getAlienInvaderThreeImage(), 8, 8);
                    aliens[r][c].setAlienType(3);
                }
                else if(r==1 || r==2)
                {
                    aliens[r][c] = new Alien(this, Misc.getInstance().getAlienInvaderTwoImage(), 11, 8);
                    aliens[r][c].setAlienType(2);
                }
                else
                {
                    aliens[r][c] = new Alien(this, Misc.getInstance().getAlienInvaderOneImage(), 12, 8);
                    aliens[r][c].setAlienType(1);
                }

                layerManager.append(aliens[r][c]);
                layerManager.append(aliens[r][c].createAlienMissile());
            }
        }

        motherShip = new MotherShip(this);
        motherShip.setMotherShipAlive(true);
        layerManager.append(motherShip);
        layerManager.append(motherShip.createAlienMissile());

        // build the four walls
        buildLargeWalls();

        field = new Field();
        // last layer, behind sprites
        layerManager.append(field);
        //***

        initPlayer(0);
        initAliens();
    }

    public void paint(Graphics g)
    {
        flushGraphics();
    }

    public synchronized void start()
    {
        gameThread = new Thread(this);
        //gameThread.setPriority(Thread.MAX_PRIORITY);
        gameThread.start();

        // thread for moving the alien formation and randomly fire alien missile(s).
        startAlienThread();
        // thread for moving randomly visiable mothership and fire missile.
        startMotherShipThread();

    }

    public synchronized void stop()
    {
        stopAlienThread();
        stopMotherShipThread();
        if(gameThread != null)
        {
            if(gameThread.isAlive())
                gameThread.interrupt();
            gameThread = null;
        }
    }

    public synchronized void run()
    {
        if(Misc.getInstance().getDebugging())
            System.out.println(getClass().toString() + " with thread name " + Thread.currentThread().getName());

        Thread currentThread = Thread.currentThread();
        try
        {
            while(currentThread == gameThread)
            {
                long startTime = System.currentTimeMillis();
                if(isShown())
                {
                    //main loop
                    tick();
                    draw();
                    checkStatus();
                    flushGraphics();
                }
                long timeTaken = System.currentTimeMillis() - startTime;
                //gameTotalTime += timeTaken;

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
        catch(InterruptedException ie)
        {
            ie.printStackTrace();
        }
        //paint(g);
    }

    /**
     * Method checks game status e.g. if "game over", level of difficulty etc.
     */
    private void checkStatus()
    {
        if(getGameOver())
        {
            //Misc.getInstance().vibrate(2000, midlet);
            //Misc.getInstance().flashBacklight(2000, midlet);
            bonusScore = 0;
            try
            {
                Sound.getInstance().playGameOver();
            }
            catch(MediaException me)
            {
                me.printStackTrace();
            }

            if(Misc.getInstance().getDebugging())
                System.out.println("GAME OVER...");
            player.setVisible(false);
            player.getPlayerMissile().setVisible(false);

            setAllNotVisiable();

            g.fillRect(0, 0, getWidth(), getHeight());
            /*
            // draw background and sprites
            layerManager.paint(g, 0, 0);
            g.setColor(Misc.getInstance().RED);
            //g.setFont(Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_LARGE));
            g.drawString("GAME OVER...", (getWidth()/2)-40, getHeight()/2-60, Graphics.TOP | Graphics.LEFT);
            g.setColor(Misc.getInstance().WHITE);
            g.setFont(Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_MEDIUM));
            g.drawString("YOUR SCORE IS " + getScore(), getWidth()/2-40, getHeight()/2 -20, Graphics.TOP | Graphics.LEFT);
            */

            stopAlienThread();
            stopMotherShipThread();

            // save score statistics in rms or over http db.
            HighScore.getInstance().setPlayerScore(getScore());
            midlet.getHighScoreName();
            //setScore(-getScore());      //reset score.
            //layerManager.paint(g, field.getWidth()/2, field.getHeight()/2);
            //flushGraphics();
            //stop();

            boolean nameOK = false;

            while(true)
            {
                // wait for player name input.
                if(!HighScore.getInstance().getPlayerName().equals("") && !nameOK)
                {
                    nameOK = true;
                    boolean temp = HighScore.getInstance().setAndGetScore(1, "", "");
                    break;
                }
            }
            //setGameOver(false);
            midlet.updateHighScoreName();
        }
        // give player extra life each 1500 * n, n=1,2,3... score points show info on screen for appr. 1500ms.
        if(getScore() >= PLAYER_SCORE_FOR_EXTRA_LIFE && 0 < extraLifeCounter && extraLifeCounter <= 30)
        {
            g.setColor(Misc.getInstance().RED);
            g.drawString("Extra life awarded to player!", (getWidth()/2)-100, getHeight()/2, Graphics.TOP | Graphics.LEFT);
            extraLifeCounter++;
            if(extraLifeCounter==30)
            {
                int temp = getLives() + 1;
                setLives(temp);
                PLAYER_SCORE_FOR_EXTRA_LIFE = 2 * PLAYER_SCORE_FOR_EXTRA_LIFE;
                extraLifeCounter = 0;
            }

        }
        // advance to next wave if all aliens is killed i.e. 55 aliens.
        else if(getAlienKilled() == 55)
        {
            if(Misc.getInstance().getDebugging())
                System.out.println("Next Level...");
            stopAlienThread();
            stopMotherShipThread();
            //setAllNotVisiable();

            // init difficulty level.
            setAlienMoveTime(1000);
            setAlienAttackFrequencyTime(5000);
            setAlienMissileSpeedTime(40);
            setMotherShipMoveTime(140);
            setMotherShipFrequencyTime(30000);
            setMotherShipDelayIfKilled(10000);

            testScreen = new ImageAndTextScreen(midlet, Misc.getInstance().getMonsterNoTextLargeImage(), Misc.getInstance().getNoImage(), Misc.getInstance().getYesImage(), "Start next wave?", false, 1);
            testScreen.start();
            midlet.setCurrentScreen(testScreen);

            bonusScore += 200;
            /*
            for(int i=0; i<theWalls.length; i++)
            {
                theWalls[i].setVisible(true);
            }
            */
            initPlayer(bonusScore);
            initAliens();
        }
        //*** increase level of difficulty, depending on aliens killed.
        else if(getAlienKilled() > 22 && !difficultyLevelThreeEnabled)
        {
            if(Misc.getInstance().getDebugging())
                System.out.println("difficultyLevelThreeEnabled.");
            int temp = 0;

            try
            {
                Sound.getInstance().playAlienFaster();
            }
            catch(MediaException me)
            {
                me.printStackTrace();
            }
            temp = getAlienMoveTime() - 100;
            setAlienMoveTime(temp);

            temp = getMotherShipFrequencyTime() - 5000;
            setMotherShipFrequencyTime(temp);

            temp = getAlienAttackFrequencyTime() - 1000;
            setAlienAttackFrequencyTime(temp);

            difficultyLevelThreeEnabled = true;
        }
        else if(getAlienKilled() > 33 && !difficultyLevelFourEnabled)
        {
            if(Misc.getInstance().getDebugging())
                System.out.println("difficultyLevelFourEnabled.");
            int temp = 0;

            try
            {
                Sound.getInstance().playAlienFaster();
            }
            catch(MediaException me)
            {
                me.printStackTrace();
            }
            temp = getAlienMoveTime()-100;
            setAlienMoveTime(temp);

            temp = getAlienAttackFrequencyTime() - 2000;
            setAlienAttackFrequencyTime(temp);

            temp = getAlienMissileSpeedTime() - 10;
            setAlienMissileSpeedTime(temp);

            difficultyLevelFourEnabled = true;
        }
        //increase level of difficulty, depending on alien formation y-axis.
        else
        {
            if(!getGameOver())
            {
                for(int r=aliens.length-1; r>=0; r--)
                {
                    for(int c=aliens[r].length-1; c>=0; c--)
                    {
                        if(aliens[r][c].isAlienAlive() && aliens[r][c].getY() > 160 && !difficultyLevelOneEnabled)
                        {
                            if(Misc.getInstance().getDebugging())
                            {
                                System.out.println("difficultyLevelOneEnabled.");
                                System.out.println("alien at x= " + aliens[r][c].getX() + ",y= " + aliens[r][c].getY());
                            }
                            try
                            {
                                Sound.getInstance().playAlienFaster();
                            }
                            catch(MediaException me)
                            {
                                me.printStackTrace();
                            }
                            int temp = 0;
                            temp = getAlienMoveTime() - 100;
                            setAlienMoveTime(temp);
                            difficultyLevelOneEnabled = true;
                        }
                        else if(aliens[r][c].isAlienAlive() && aliens[r][c].getY() > 200 && !difficultyLevelTwoEnabled)
                        {
                            if(Misc.getInstance().getDebugging())
                            {
                                System.out.println("difficultyLevelTwoEnabled.");
                                System.out.println("alien at x= " + aliens[r][c].getX() + ",y= " + aliens[r][c].getY());
                            }
                            try
                            {
                                Sound.getInstance().playAlienFaster();
                            }
                            catch(MediaException me)
                            {
                                me.printStackTrace();
                            }
                            int temp = 0;
                            temp = getAlienMoveTime()-100;
                            setAlienMoveTime(temp);
                            difficultyLevelTwoEnabled = true;
                        }
                    }
                }
            }
        }
        //***
    }

    private void tick()
    {
        // If player presses two or more direction buttons, we ignore them all.
        // But pressing fire is independent. The code below also ignores
        // direction buttons if GAME_A..GAME_D are pressed.
        int keyStates = getKeyStates();
        boolean firePressed = false;
        firePressed = (keyStates & FIRE_PRESSED) != 0;

        if(firePressed)
        {
            if(!player.isMissileFired())
            {
                player.fireMissile();
            }
        }

        keyStates &= ~FIRE_PRESSED;

        int direction = (keyStates == UP_PRESSED) ? UP :
                        (keyStates == LEFT_PRESSED) ? LEFT:
                        (keyStates == DOWN_PRESSED) ? DOWN :
                        (keyStates == RIGHT_PRESSED) ? RIGHT : NONE;

        player.tick(direction);
    }

    /**
     * Draws the game.
     */
    private void draw()
    {
        // method for (all) collision checks.
        checkCollision();

        g.setColor(Misc.getInstance().BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        // clip and translate to centre
        int dx = origin(player.getX() + player.getWidth()/2, field.getWidth(), getWidth());
        int dy = origin(player.getY() + player.getHeight()/2, field.getHeight(), getHeight());

        g.setClip(dx, dy, field.getWidth(), field.getHeight());
        g.translate(dx, dy);
        //g.setClip(0, 0, field.getWidth(), field.getHeight());
        //g.setClip(0, 0, this.getWidth(), this.getHeight());

        // draw background and sprites
        layerManager.paint(g, 0, 0);

        // undo clip & translate
        g.translate(-dx, -dy);
        g.setClip(0, 0, getWidth(), getHeight());

        // display score and live(s).
        g.setColor(Misc.getInstance().WHITE);

        //g.setFont(Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_LARGE));
        g.drawString("Score: ", 2, 2, Graphics.TOP | Graphics.LEFT);
        int scoreDx = 50;
        if(Integer.toString(getScore()).length() == 4)
            scoreDx = 52;
        g.drawString(Integer.toString(getScore()), scoreDx, 2, Graphics.TOP | Graphics.LEFT);
        g.drawString("Lives: ", getWidth()-8, 2, Graphics.TOP | Graphics.RIGHT);
        g.drawString(Integer.toString(getLives()), getWidth()-2, 2, Graphics.TOP | Graphics.RIGHT);
    }

    /**
     * Method checks if missile from player/alien(s) hits its target (player/alien) or a wall.
     */
    private void checkCollision()
    {
        // check if player has lanched a missile and if so check if it collides with one of the four walls (shields).
        if(player.isMissileFired())
        {
            for(int i=0; i<theWalls.length; i++)
            {
                if(player.getPlayerMissile().collidesWith(theWalls[i], true))
                {
                    try
                    {
                        Sound.getInstance().playWallExplosion(true);
                    }
                    catch(MediaException me)
                    {
                        me.printStackTrace();
                    }
                    theWalls[i].setVisible(false);
                    player.getPlayerMissile().stop();
                }
            }
        }

        // check if mothership have lanched a missile and if so check if the missile collides with the player or one of the four walls (shields).
        if(motherShip.isMotherShipAlive() && motherShip.isMissileFired())
        {

            //REMOVE: remove player missile if mothership missile collides?
            if(player.isMissileFired() && motherShip.getAliendMissile().collidesWith(player.getPlayerMissile(), false))
            {
                player.getPlayerMissile().stop();
            }

            if(motherShip.getAliendMissile().collidesWith(player, true))
            {
                try
                {
                    Sound.getInstance().playPlayerExplosion();
                }
                catch(MediaException me)
                {
                    me.printStackTrace();
                }
                if(Misc.getInstance().getDebugging())
                    System.out.println("mothership killed player at x= " + motherShip.getAliendMissile().getX() + ", y= " + motherShip.getAliendMissile().getY());
                motherShip.getAliendMissile().stop();
                // player explosion animation thread.
                new Thread(){
                    public void run(){
                        long threadStartTime = System.currentTimeMillis();
                        long threadTakenTime = 0;
                        boolean animation = true;
                        do{
                            player.showExplosionAnimation(animation);
                            animation = (animation)?false:true;
                            threadTakenTime = System.currentTimeMillis() - threadStartTime;
                        } while(threadTakenTime <= 1000);
                        player.init();
                    }
                }.start();
                setScore(player.getPlayerKilledScore());
                setLives(-1);
            }
            else
            {
                for(int i=0; i<theWalls.length; i++)
                {
                    if(motherShip.getAliendMissile().collidesWith(theWalls[i], true))
                    {
                        try
                        {
                            Sound.getInstance().playWallExplosion(false);
                        }
                        catch(MediaException me)
                        {
                            me.printStackTrace();
                        }
                        theWalls[i].setVisible(false);
                        motherShip.getAliendMissile().stop();
                    }
                }
            }
        }

        //TODO: optimize loop, start with last row, since this is the closest row for the player.
        for(int r=0; r<aliens.length; r++)
        {
            for(int c=0; c<aliens[r].length; c++)
            {
                // check if player has lanched a missile and if so check if the missile collides with an alien or if avaliable the mothership.
                if(player.isMissileFired())
                {

                    if(player.getPlayerMissile().collidesWith(aliens[r][c], true))
                    {
                        try
                        {
                            Sound.getInstance().playAlienKilled();
                        }
                        catch(MediaException me)
                        {
                            me.printStackTrace();
                        }
                        currentExplosionAlien = aliens[r][c];
                        // do not show explosion animation for last killed alien.
                        if(getAlienKilled() != 54)
                        {
                            // alien explosion animation thread.
                            new Thread(){
                                public void run(){
                                    long threadStartTime = System.currentTimeMillis();
                                    long threadTakenTime = 0;
                                    boolean animation = true;
                                    do{
                                        currentExplosionAlien.showExplosionAnimation(animation);
                                        animation = (animation)?false:true;
                                        threadTakenTime = System.currentTimeMillis() - threadStartTime;
                                    } while(threadTakenTime <= 500);
                                    currentExplosionAlien.setAlienAlive(false);
                                }
                            }.start();
                        }
                        player.getPlayerMissile().stop();
                        setScore(aliens[r][c].getAlienKilledScore());
                        setAlienKilled(1);
                        //return;
                    }
                    else if(motherShip.isMotherShipAlive() && motherShip.isVisible() && player.getPlayerMissile().collidesWith(motherShip, true))
                    {
                        try
                        {
                            Sound.getInstance().playAlienKilled();
                        }
                        catch(MediaException me)
                        {
                            me.printStackTrace();
                        }
                        // mothership explosion animation thread.
                        new Thread(){
                            public void run(){
                                long threadStartTime = System.currentTimeMillis();
                                long threadTakenTime = 0;
                                boolean animation = true;
                                do{
                                    motherShip.showExplosionAnimation(animation);
                                    animation = (animation)?false:true;
                                    threadTakenTime = System.currentTimeMillis() - threadStartTime;
                                } while(threadTakenTime <= 1000);
                                motherShip.setMotherShipAlive(false);
                            }
                        }.start();
                        player.getPlayerMissile().stop();
                        setScore(motherShip.getMotherShipKilledScore());

                        //delay mothership apperance.
                        MotherShiptimer = new Timer();
                        MotherShiptask = new TimerTask(){
                            boolean animation = true;
                            public void run()
                            {
                                if(Misc.getInstance().getDebugging())
                                    System.out.println("mothership has been revoked!");
                                motherShip.setMotherShipAlive(true);
                                motherShip.init();
                            }
                        };
                        MotherShiptimer.schedule(MotherShiptask, getMotherShipDelayIfKilled());

                    }
                }

                // check if alien has lanched a missile and if so check if the missile collides with the player or one of the four walls (shields).
                if(aliens[r][c].isMissileFired())
                {
                    //REMOVE: remove player missile if alien missile collides?
                    if(player.isMissileFired() && aliens[r][c].getAliendMissile().collidesWith(player.getPlayerMissile(), false))
                    {
                        player.getPlayerMissile().stop();
                    }

                    if(aliens[r][c].getAliendMissile().collidesWith(player, true))
                    {
                        if(Misc.getInstance().getDebugging())
                            System.out.println("alien[" + r + "][" + c + "] killed player at x= " + aliens[r][c].getAliendMissile().getX() + ", y= " + aliens[r][c].getAliendMissile().getY());
                        try
                        {
                            Sound.getInstance().playPlayerExplosion();
                        }
                        catch(MediaException me)
                        {
                            me.printStackTrace();
                        }
                        aliens[r][c].getAliendMissile().stop();
                        // player explosion animation thread.
                        new Thread(){
                            public void run(){
                                long threadStartTime = System.currentTimeMillis();
                                long threadTakenTime = 0;
                                boolean animation = true;
                                do{
                                    player.showExplosionAnimation(animation);
                                    animation = (animation)?false:true;
                                    threadTakenTime = System.currentTimeMillis() - threadStartTime;
                                } while(threadTakenTime <= 1000);
                                player.init();
                            }
                        }.start();
                        setScore(player.getPlayerKilledScore());
                        setLives(-1);
                    }
                    else
                    {
                        for(int i=0; i<theWalls.length; i++)
                        {
                            if(aliens[r][c].getAliendMissile().collidesWith(theWalls[i], true))
                            {
                                try
                                {
                                    Sound.getInstance().playWallExplosion(false);
                                }
                                catch(MediaException me)
                                {
                                    me.printStackTrace();
                                }
                                theWalls[i].setVisible(false);
                                aliens[r][c].getAliendMissile().stop();
                            }
                        }
                    }

                }
            }

        }
    }

    /**
     * Alien formation move forth and back sideways over the screen.
     */
    private void moveAlienFormation()
    {
        // move aliens -> right direction
        if(getAlienDirection())
        {
            for(int r=aliens.length-1; r>=0; r--)
            {
                for(int c=aliens[r].length-1; c>=0; c--)
                {
                    //swith direction
                    if( aliens[r][c].isAlienAlive() && aliens[r][c].getX()+14 >= field.getWidth() )
                    {
                        for(int rr=aliens.length-1; rr>=0; rr--)
                        {
                            for(int cc=aliens[rr].length-1; cc>=0; cc--)
                            {
                                aliens[rr][cc].setPosition(aliens[rr][cc].getX(), aliens[rr][cc].getY() + 9);

                                // check if aliens manage "touch down".
                                if(aliens[rr][cc].getY() + 8 >= field.getHeight() && aliens[rr][cc].isAlienAlive())
                                {
                                    setGameOver(true);
                                    return;
                                }

                                // if alien makes it to the wall, then remove touched wall part.
                                for(int i=0; i<theWalls.length; i++)
                                {
                                    if(aliens[rr][cc].isAlienAlive() && aliens[rr][cc].collidesWith(theWalls[i], true))
                                        theWalls[i].setVisible(false);
                                }

                                // if an alien collides with the player a live is lost and alien formation should go back to init position.
                                if(aliens[rr][cc].isAlienAlive() && aliens[rr][cc].collidesWith(player, true))
                                {
                                    try
                                    {
                                        Sound.getInstance().playPlayerExplosion();
                                    }
                                    catch(MediaException me)
                                    {
                                        me.printStackTrace();
                                    }
                                    // player explosion animation thread.
                                    new Thread(){
                                        public void run(){
                                            long threadStartTime = System.currentTimeMillis();
                                            long threadTakenTime = 0;
                                            boolean animation = true;
                                            do{
                                                player.showExplosionAnimation(animation);
                                                animation = (animation)?false:true;
                                                threadTakenTime = System.currentTimeMillis() - threadStartTime;
                                            } while(threadTakenTime <= 1000);
                                            player.init();
                                        }
                                    }.start();
                                    setScore(player.getPlayerKilledScore());
                                    setLives(-1);

                                    //TODO: alien formation should go back to init position.
                                    //setAlienDirection(true);
                                    int totalDx = 20;
                                    int dx = (field.getWidth()/COLUMNS_OF_ALIENS)-5;
                                    //since round is missing in cldc.
                                    int y = (int) ((field.getHeight() * 0.28) + 0.5);

                                    for(int rrr=0; rrr<aliens.length; rrr++)
                                    {
                                        for(int ccc=0; ccc<aliens[rrr].length; ccc++)
                                        {
                                            totalDx += dx;
                                            aliens[rrr][ccc].setPosition(totalDx, y);
                                        }
                                        totalDx = 20;
                                        y += 14;
                                    }
                                    return;
                                }

                            }
                        }
                        setAlienDirection(false);
                        return;
                    }
                    aliens[r][c].tick(sequenceAnimationCounter%2==0);
                    aliens[r][c].move(5, 0);

                    // if an alien collides with the player a live is lost and alien formation should go back to init position.
                    if(aliens[r][c].isAlienAlive() && aliens[r][c].collidesWith(player, true))
                    {
                        try
                        {
                            Sound.getInstance().playPlayerExplosion();
                        }
                        catch(MediaException me)
                        {
                            me.printStackTrace();
                        }
                        // player explosion animation thread.
                        new Thread(){
                            public void run(){
                                long threadStartTime = System.currentTimeMillis();
                                long threadTakenTime = 0;
                                boolean animation = true;
                                do{
                                    player.showExplosionAnimation(animation);
                                    animation = (animation)?false:true;
                                    threadTakenTime = System.currentTimeMillis() - threadStartTime;
                                } while(threadTakenTime <= 1000);
                                player.init();
                            }
                        }.start();
                        setScore(player.getPlayerKilledScore());
                        setLives(-1);

                        //TODO: alien formation should go back to init position.
                        //setAlienDirection(true);
                        int totalDx = 20;
                        int dx = (field.getWidth()/COLUMNS_OF_ALIENS)-5;
                        //since round is missing in cldc.
                        int y = (int) ((field.getHeight() * 0.28) + 0.5);

                        for(int rrr=0; rrr<aliens.length; rrr++)
                        {
                            for(int ccc=0; ccc<aliens[rrr].length; ccc++)
                            {
                                totalDx += dx;
                                aliens[rrr][ccc].setPosition(totalDx, y);
                            }
                            totalDx = 20;
                            y += 14;
                        }
                        return;
                    }

                    // if alien makes it to the wall, then remove touched wall part.
                    for(int i=0; i<theWalls.length; i++)
                    {
                        if(aliens[r][c].isAlienAlive() && aliens[r][c].collidesWith(theWalls[i], true))
                            theWalls[i].setVisible(false);
                    }
                }
            }
        }
        // move aliens <- left direction
        else
        {
            for(int r=0; r<aliens.length; r++)
            {
                for(int c=0; c<aliens[r].length; c++)
                {
                    //swith direction
                    if( aliens[r][c].isAlienAlive() && aliens[r][c].getX() <= 2 )
                    {
                        for(int rr=aliens.length-1; rr>=0; rr--)
                        {
                            for(int cc=aliens[rr].length-1; cc>=0; cc--)
                            {
                                aliens[rr][cc].setPosition(aliens[rr][cc].getX(), aliens[rr][cc].getY() + 9);

                                // check if aliens manage "touch down".
                                if(aliens[rr][cc].getY() + 8 >= field.getHeight() && aliens[rr][cc].isAlienAlive())
                                {
                                    setGameOver(true);
                                    return;
                                }

                                // if alien makes it to the wall, then remove touched wall part.
                                for(int i=0; i<theWalls.length; i++)
                                {
                                    if(aliens[rr][cc].isAlienAlive() && aliens[rr][cc].collidesWith(theWalls[i], true))
                                        theWalls[i].setVisible(false);
                                }

                                // if an alien collides with the player a live is lost and alien formation should go back to init position.
                                if(aliens[rr][cc].isAlienAlive() && aliens[rr][cc].collidesWith(player, true))
                                {
                                    try
                                    {
                                        Sound.getInstance().playPlayerExplosion();
                                    }
                                    catch(MediaException me)
                                    {
                                        me.printStackTrace();
                                    }
                                    // player explosion animation thread.
                                    new Thread(){
                                        public void run(){
                                            long threadStartTime = System.currentTimeMillis();
                                            long threadTakenTime = 0;
                                            boolean animation = true;
                                            do{
                                                player.showExplosionAnimation(animation);
                                                animation = (animation)?false:true;
                                                threadTakenTime = System.currentTimeMillis() - threadStartTime;
                                            } while(threadTakenTime <= 1000);
                                            player.init();
                                        }
                                    }.start();
                                    setScore(player.getPlayerKilledScore());
                                    setLives(-1);

                                    //TODO: alien formation should go back to init position.
                                    //setAlienDirection(true);
                                    int totalDx = 20;
                                    int dx = (field.getWidth()/COLUMNS_OF_ALIENS)-5;
                                    //since round is missing in cldc.
                                    int y = (int) ((field.getHeight() * 0.28) + 0.5);

                                    for(int rrr=0; rrr<aliens.length; rrr++)
                                    {
                                        for(int ccc=0; ccc<aliens[rrr].length; ccc++)
                                        {
                                            totalDx += dx;
                                            aliens[rrr][ccc].setPosition(totalDx, y);
                                        }
                                        totalDx = 20;
                                        y += 14;
                                    }
                                    return;
                                }

                            }
                        }
                        setAlienDirection(true);
                        return;
                    }
                    aliens[r][c].tick(sequenceAnimationCounter%2==0);
                    // aliens moves faster to left.
                    aliens[r][c].move(-6, 0);

                    // if an alien collides with the player a live is lost and alien formation should go back to init position.
                    if(aliens[r][c].isAlienAlive() && aliens[r][c].collidesWith(player, true))
                    {
                        try
                        {
                            Sound.getInstance().playPlayerExplosion();
                        }
                        catch(MediaException me)
                        {
                            me.printStackTrace();
                        }
                        // player explosion animation thread.
                        new Thread(){
                            public void run(){
                                long threadStartTime = System.currentTimeMillis();
                                long threadTakenTime = 0;
                                boolean animation = true;
                                do{
                                    player.showExplosionAnimation(animation);
                                    animation = (animation)?false:true;
                                    threadTakenTime = System.currentTimeMillis() - threadStartTime;
                                } while(threadTakenTime <= 1000);
                                player.init();
                            }
                        }.start();
                        setScore(player.getPlayerKilledScore());
                        setLives(-1);

                        //TODO: alien formation should go back to init position.
                        //setAlienDirection(true);
                        int totalDx = 20;
                        int dx = (field.getWidth()/COLUMNS_OF_ALIENS)-5;
                        //since round is missing in cldc.
                        int y = (int) ((field.getHeight() * 0.28) + 0.5);

                        for(int rrr=0; rrr<aliens.length; rrr++)
                        {
                            for(int ccc=0; ccc<aliens[rrr].length; ccc++)
                            {
                                totalDx += dx;
                                aliens[rrr][ccc].setPosition(totalDx, y);
                            }
                            totalDx = 20;
                            y += 14;
                        }
                        return;
                    }

                    // if alien makes it to the wall, then remove touched wall part.
                    for(int i=0; i<theWalls.length; i++)
                    {
                        if(aliens[r][c].isAlienAlive() && aliens[r][c].collidesWith(theWalls[i], true))
                            theWalls[i].setVisible(false);
                    }

                }
            }
        }
        sequenceAnimationCounter++;
    }

    /**
     * @param direction true moves mothership in the right direction, otherwise in left direction.
     */
    private void moveMotherShip(boolean direction)
    {

        if(!motherShip.getHasMotherShipSoundBeenPlayed())
        {
            try
            {
                Sound.getInstance().playMotherShipIsVisiable();
            }
            catch(MediaException me)
            {
                me.printStackTrace();
            }
            motherShip.setHasMotherShipSoundBeenPlayed(true);
        }
        // move mothership -> right direction
        if(direction)
        {
            motherShip.move(3, 0);
            if(motherShip.getX() >= getField().getWidth() + 13)
            {
                motherShip.setVisible(false);
                motherShip.setHasMotherShipSoundBeenPlayed(false);
            }
        }
        // move mothership <- left direction
        else
        {
            motherShip.move(-3, 0);
            if(motherShip.getX() <= -13)
            {
                motherShip.setVisible(false);
                motherShip.setHasMotherShipSoundBeenPlayed(false);
            }
        }
    }

    /**
     * Starts thread for moving the alien formation and randomly fire alien missile(s).
     */
    private synchronized void startAlienThread()
    {
        aliensThread = new Thread() {
            boolean alienHasFiredMissile = false;
            public void run() {
                try
                {
                    do
                    {
                        long alienStartTime = System.currentTimeMillis();
                        sleep(getAlienMoveTime());
                        moveAlienFormation();
                        long alienTimeTaken = System.currentTimeMillis() - alienStartTime;
                        alienTotalTime += alienTimeTaken;

                        if(alienTotalTime >= getAlienAttackFrequencyTime())
                        {
                            alienHasFiredMissile = false;
                            // always lanch an alien missile.
                            do {
                                int randomAlienRow = Misc.getInstance().getRandomNbr(ROWS_OF_ALIENS);
                                int randomAlienColumn = Misc.getInstance().getRandomNbr(COLUMNS_OF_ALIENS);
                                //TODO: preferably an alien with a clear shot downwards.
                                if(aliens[randomAlienRow][randomAlienColumn].isAlienAlive() && !aliens[randomAlienRow][randomAlienColumn].isMissileFired() )
                                {
                                    aliens[randomAlienRow][randomAlienColumn].fireMissile();
                                    if(Misc.getInstance().getDebugging())
                                        System.out.println("alien[" + randomAlienRow + "[]" + randomAlienColumn + "] fired!");
                                    alienHasFiredMissile = true;
                                }
                                alienTotalTime = 0;
                            } while(!alienHasFiredMissile);
                        }
                    }while( this.isAlive() );
                }
                catch(InterruptedException ie)
                {
                    ie.printStackTrace();
                }
            }
        };
        //aliensThread.setPriority(Thread.MAX_PRIORITY);
        aliensThread.start();

    }

    private synchronized void stopAlienThread()
    {
        if(aliensThread != null)
        {
            if(aliensThread.isAlive())
                aliensThread.interrupt();
            aliensThread = null;
        }
    }

    /**
     * Starts mothership thread for moving randomly visiable mothership and fire missile.
     */
    private synchronized void startMotherShipThread()
    {
        motherShipThread = new Thread() {
            public void run() {
                try
                {
                    do
                    {
                        int motherShipFrequencyRandomTime = Misc.getInstance().getRandomNbr(getMotherShipFrequencyTime());
                        sleep(motherShipFrequencyRandomTime);
                        //random a number when mothership should fire missile, correct with 80?
                        int motherShipRandomFire = Misc.getInstance().getRandomNbr(80);
                        int counter=0;
                        if(motherShip.isMotherShipAlive())
                        {
                            motherShip.setVisible(true);
                            boolean direction = (motherShipFrequencyRandomTime%2==0)?true:false;
                            //move to right
                            if(direction)
                                motherShip.setPosition(0, 80);
                            //move to left
                            else
                                motherShip.setPosition(getField().getWidth(), 80);
                            do{
                                if(Misc.getInstance().getDebugging())
                                    System.out.println("mothership x=" + motherShip.getX() + " ,y=" + motherShip.getY());
                                long motherShipStartTime = System.currentTimeMillis();
                                // play mothership apperance sound.
                                moveMotherShip(direction);
                                long motherShipTimeTaken = System.currentTimeMillis() - motherShipStartTime;

                                counter++;
                                if(counter==motherShipRandomFire)
                                    motherShip.fireMissile();

                                if(motherShipTimeTaken < getMotherShipMoveTime())
                                    this.sleep(getMotherShipMoveTime() - motherShipTimeTaken);

                            } while( motherShip.isVisible() );

                        }
                    }while( this.isAlive() );
                }
                catch(InterruptedException ie)
                {
                    ie.printStackTrace();
                }

            }
        };
        motherShipThread.start();

    }

    private synchronized void stopMotherShipThread()
    {
        if(motherShipThread != null)
        {
            if(motherShipThread.isAlive())
                motherShipThread.interrupt();
            motherShipThread = null;
        }
    }

    // If the screen is bigger than the field, we center the field
    // in the screen. Otherwise we center the screen on the focus, except
    // that we don't scroll beyond the edges of the field.
    private int origin(int focus, int fieldLength, int screenLength)
    {
        int origin;
        if(screenLength >= fieldLength)
        {
            origin = (screenLength - fieldLength) / 2;
        }
        else if(focus <= (screenLength/2))
        {
            origin = 0;
        }
        else if(focus >= ((fieldLength - screenLength) / 2) )
        {
            origin = screenLength - fieldLength;
        }
        else
        {
            origin = (screenLength / 2) - focus;
        }
        return origin;

    }

    /**
     * Method initilize player parameters if new game or next wave.
     * @param score increase player score
     */
    public void initPlayer(int score)
    {
        if(MotherShiptimer != null)
            MotherShiptimer.cancel();
        if(MotherShiptask != null)
            MotherShiptask.cancel();

        difficultyLevelOneEnabled = false;
        difficultyLevelTwoEnabled = false;
        difficultyLevelThreeEnabled = false;
        difficultyLevelFourEnabled = false;

        setAlienDirection(true);
        setAlienMoveTime(1000);
        setAlienAttackFrequencyTime(5000);
        setMotherShipMoveTime(100);
        setMotherShipFrequencyTime(30000);
        setMotherShipDelayIfKilled(10000);
        setAlienMissileSpeedTime(40);

        //if(score != 0)
        setScore(score);
        setGameOver(false);
        setAlienKilled(-getAlienKilled());
        player.setPosition(field.getWidth()/2-8, field.getHeight()-10);
    }

    /**
     * Draws the 11 aliens into the original position within the canvas.
     */
    public void initAliens()
    {
        setAlienDirection(true);
        int totalDx = 20;
        int dx = (field.getWidth()/COLUMNS_OF_ALIENS)-5;
        //since round is missing in cldc.
        int y = (int) ((field.getHeight() * 0.28) + 0.5);

        for(int r=0; r<aliens.length; r++)
        {
            for(int c=0; c<aliens[r].length; c++)
            {
                totalDx += dx;
                aliens[r][c].setPosition(totalDx, y);
                aliens[r][c].setAlienAlive(true);
            }
            totalDx = 20;
            y += 14;
        }
    }

    /**
     * Method will set aliens, mothership and the walls to non visiable.
     */
    private void setAllNotVisiable()
    {
        for(int r=0; r<aliens.length; r++)
        {
            for(int c=0; c<aliens[r].length; c++)
            {
                //aliens[r][c].setAlienAlive(false);
                aliens[r][c].setAlienAlive(false);
                aliens[r][c].getAliendMissile().setVisible(false);
            }
        }

        if(MotherShiptimer != null)
            MotherShiptimer.cancel();
        if(MotherShiptask != null)
            MotherShiptask.cancel();
        motherShip.setVisible(false);
        motherShip.getAliendMissile().setVisible(false);

        for(int i=0; i<theWalls.length; i++)
        {
            theWalls[i].setVisible(false);
        }
    }

    public Field getField()
    {
        return field;
    }

    public SpaceInvadersMIDlet getSpaceInvaderMIDlet()
    {
        return midlet;
    }

    public LayerManager getLayerManager()
    {
        return layerManager;
    }

    /**
     * @param playerScore increases/decrease the totally score.
     */
    public void setScore(int playerScore)
    {
        this.playerScore += playerScore;
        playerScoreTemp += this.playerScore;
        // give player extra life each 1500 * n, n=1,2,3... score points
        if(playerScoreTemp >= PLAYER_SCORE_FOR_EXTRA_LIFE && extraLifeCounter == 0)
        {
            extraLifeCounter = 1;
        }
    }

    public int getScore()
    {
        //if(playerScore <= 0)
        //    playerScore = 1;
        return playerScore;
    }

    /**
     *
     * @param playerLives sets player live(s).
     */
    public void setLives(int playerLives)
    {
        if(playerLives != -1)
            this.playerLives = playerLives;
        else
            this.playerLives--;
        if(this.playerLives == 0)
            setGameOver(true);
    }

    public int getLives()
    {
        return playerLives;
    }

    /**
     * @param direction true=right, false=left. Needed to determine moving
     * sideways movement direction.
     */
    public void setAlienDirection(boolean direction)
    {
        this.direction = direction;
    }

    /**
     *@return alien formation direction true=right, false=left.
     */
    public boolean getAlienDirection()
    {
        return direction;
    }

    private void setGameOver(boolean gameOver)
    {
        this.gameOver = gameOver;
    }

    /**
     * @return true if players 3 lives is over or if a alien performs a "touch down" i.e. reach the players surface.
     */
    public boolean getGameOver()
    {
        return gameOver;
    }

    /**
     *
     * @param alienkilled
     */
    private void setAlienKilled(int alienkilled)
    {
        alienKilled += alienkilled;
    }

    /**
     * @return int number of killed aliens.
     */
    private int getAlienKilled()
    {
        return alienKilled;
    }

    //*** should be inner class

    private synchronized void setAlienMoveTime(int alienMoveTime)
    {
        if(this.alienMoveTime - alienMoveTime < 0 && !getGameOver())
            return;
        this.alienMoveTime = alienMoveTime;
    }
    /**
     * @return time (ms) move updater.
     */
    private int getAlienMoveTime()
    {
        return alienMoveTime;
    }

    private void setAlienAttackFrequencyTime(int alienAttackFrequencyTime)
    {
        if(this.alienAttackFrequencyTime - alienAttackFrequencyTime < 0 && !getGameOver())
            return;
        this.alienAttackFrequencyTime = alienAttackFrequencyTime;
    }
    /**
     * @return time (ms) how often aliens lanches missiles.
     */
    private int getAlienAttackFrequencyTime()
    {
        return alienAttackFrequencyTime;
    }

    private void setMotherShipMoveTime(int motherShipMoveTime)
    {
        if(this.motherShipMoveTime - motherShipMoveTime < 0 && !getGameOver())
            return;
        this.motherShipMoveTime = motherShipMoveTime;
    }
    /**
     * @return time (ms) move updater.
     */
    private int getMotherShipMoveTime()
    {
        return motherShipMoveTime;
    }

    private void setMotherShipFrequencyTime(int motherShipFrequencyTime)
    {
        if(this.motherShipFrequencyTime - motherShipFrequencyTime < 0 && !getGameOver())
            return;
        this.motherShipFrequencyTime = motherShipFrequencyTime;
    }
    /**
     * @return time (ms) how often (value is a random interval.) the mothership is visiable on screen.
     */
    private int getMotherShipFrequencyTime()
    {
        return motherShipFrequencyTime;
    }

    private void setMotherShipDelayIfKilled(int motherShipDelayIfKilled)
    {
        if(this.motherShipDelayIfKilled - motherShipDelayIfKilled < 0 && !getGameOver())
            return;
        this.motherShipDelayIfKilled = motherShipDelayIfKilled;
    }
    /**
     * @return delay time (ms) if killed.
     */
    private int getMotherShipDelayIfKilled()
    {
        return motherShipDelayIfKilled;
    }

    private void setAlienMissileSpeedTime(int alienMissileSpeedTime)
    {
        if(this.alienMissileSpeedTime - alienMissileSpeedTime < 0 && !getGameOver())
            return;
        this.alienMissileSpeedTime = alienMissileSpeedTime;
    }
    /**
     * @return time (ms) alien and mothership missile update speed.
     */
    public int getAlienMissileSpeedTime()
    {
        return alienMissileSpeedTime;
    }

    //***

    public synchronized Thread getGameThread()
    {
        return gameThread;
    }
    public synchronized Thread getAliensThread()
    {
        return aliensThread;
    }
    public synchronized Thread getMotherShipThread()
    {
        return motherShipThread;
    }
    /**
     * Builds the four walls (shields) on the screen.
     */
    private void buildLargeWalls()
    {
        for(int i=0; i<theWalls.length; i++)
        {
            theWalls[i] = new Wall(this);
        }
        int dx = this.getWidth()/5-15;
        int dy = 282;
        //build wall one.
        for(int i=0; i<8; i++)
        {
            theWalls[i].setWallPart(i);
            layerManager.append(theWalls[i]);
            theWalls[i].setPosition(dx, dy);
            dx += 6;
            if(i==3)
            {
                dx = this.getWidth()/5-15;
                dy = 290;
            }
        }
        //build wall two.
        dx = this.getWidth()/5+35;
        dy = 282;
        for(int i=8; i<16; i++)
        {
            theWalls[i].setWallPart(i-8);
            layerManager.append(theWalls[i]);
            theWalls[i].setPosition(dx, dy);
            dx += 6;
            if(i==11)
            {
                dx = this.getWidth()/5+35;
                dy = 290;
            }
        }
        //build wall three.
        dx = this.getWidth()/5+85;
        dy = 282;
        for(int i=16; i<24; i++)
        {
            theWalls[i].setWallPart(i-16);
            layerManager.append(theWalls[i]);
            theWalls[i].setPosition(dx, dy);
            dx += 6;
            if(i==19)
            {
                dx = this.getWidth()/5+85;
                dy = 290;
            }
        }
        //build wall four..
        dx = this.getWidth()/5+135;
        dy = 282;
        for(int i=24; i<32; i++)
        {
            theWalls[i].setWallPart(i-24);
            layerManager.append(theWalls[i]);
            theWalls[i].setPosition(dx, dy);
            dx += 6;
            if(i==27)
            {
                dx = this.getWidth()/5+135;
                dy = 290;
            }
        }
    }

}