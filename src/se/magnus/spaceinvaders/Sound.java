package se.magnus.spaceinvaders;

import java.io.InputStream;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;

/**
 * Sound class that playes background music and sound effects.
 * @author magnus
 */
public class Sound
{
    private static Sound instance = null;
    private boolean mute = false;
    private Player backGroundMusicPlayer = null;
    private Player playerPlayer = null, invaderPlayer = null;

    protected Sound()
    {
    }

    /**
     * @return Singleton Sound object.
     */
    public static Sound getInstance()
    {
        if(instance == null)
            instance = new Sound();
        return instance;
    }

    /**
     * @param mute if true all sound will be disabled.
     */
    public void setMute(boolean mute)
    {
        boolean temp = isMute();
        this.mute = mute;
        if(this.mute && backGroundMusicPlayer != null)
            stop(backGroundMusicPlayer);
        else if(!this.mute && temp && backGroundMusicPlayer != null)
        {
            try
            {
                playBackGroundMusic(-1);
            }
            catch(MediaException me)
            {
                me.printStackTrace();
            }
        }
    }
    /**
     * @return true if sound is set to mute, else true.
     */
    public boolean isMute()
    {
        return mute;
    }


    public Player getBackGroundMusicPlayer()
    {
        return backGroundMusicPlayer;
    }

    /**
     * @param times -1 gives indefintely loop.
     */
    public void playBackGroundMusic(int times) throws MediaException
    {
        backGroundMusicPlayer = createPlayer("/audio/spaceinvaders1.mpeg", "audio/mpeg", times);
        if(backGroundMusicPlayer != null && !isMute())
            backGroundMusicPlayer.start();
    }
    public void playAlienMissile() throws MediaException
    {
        invaderPlayer = createPlayer("/audio/ufo_highpitch.wav", "audio/x-wav", 1);
        if(invaderPlayer != null && !isMute())
            invaderPlayer.start();
    }
    public void playAlienKilled() throws MediaException
    {
        invaderPlayer = createPlayer("/audio/invaderKilled.wav", "audio/x-wav", 1);
        if(invaderPlayer != null && !isMute())
            invaderPlayer.start();
    }
    /**
     * Plays sound that indicates that alien formation speeds up i.e. increases level of difficulty.
     * @throws javax.microedition.media.MediaException
     */
    public void playAlienFaster() throws MediaException
    {
        invaderPlayer = createPlayer("/audio/fastinvader1.wav", "audio/x-wav", 1);
        if(invaderPlayer != null && !isMute())
            invaderPlayer.start();
        invaderPlayer = createPlayer("/audio/fastinvader2.wav", "audio/x-wav", 1);
        if(invaderPlayer != null && !isMute())
            invaderPlayer.start();
        invaderPlayer = createPlayer("/audio/fastinvader3.wav", "audio/x-wav", 1);
        if(invaderPlayer != null && !isMute())
            invaderPlayer.start();
        invaderPlayer = createPlayer("/audio/fastinvader4.wav", "audio/x-wav", 1);
        if(invaderPlayer != null && !isMute())
            invaderPlayer.start();
    }
    public void playMotherShipIsVisiable() throws MediaException
    {
        invaderPlayer = createPlayer("/audio/motherShipIsVisiable.wav", "audio/x-wav", 1);
        if(invaderPlayer != null && !isMute())
            invaderPlayer.start();
    }
    public void playMotherShipMissile() throws MediaException
    {
        invaderPlayer = createPlayer("/audio/motherShipMissile.wav", "audio/x-wav", 1);
        if(invaderPlayer != null && !isMute())
            invaderPlayer.start();
    }
    public void playPlayerMissile() throws MediaException
    {
        playerPlayer = createPlayer("/audio/playerMissile.wav", "audio/x-wav", 1);
        if(playerPlayer != null && !isMute())
            playerPlayer.start();
    }
    public void playPlayerExplosion() throws MediaException
    {
        invaderPlayer = createPlayer("/audio/playerKilled.wav", "audio/x-wav", 1);
        if(invaderPlayer != null && !isMute())
            invaderPlayer.start();
    }
    /**
     *
     * @param playerHitWall true if player missile hit wall, otherwise alien missile.
     * @throws javax.microedition.media.MediaException
     */
    public void playWallExplosion(boolean playerHitWall) throws MediaException
    {
        if(playerHitWall)
        {
            playerPlayer = createPlayer("/audio/wallExplosion.mid", "audio/midi", 1);
            if(playerPlayer != null && !isMute())
                playerPlayer.start();
        }
        else if(!playerHitWall)
        {
            invaderPlayer = createPlayer("/audio/wallExplosion.mid", "audio/midi", 1);
            if(invaderPlayer != null && !isMute())
                invaderPlayer.start();
        }
    }
    public void playGameOver() throws MediaException
    {
        playerPlayer = createPlayer("/audio/gameOver.mid", "audio/midi", 1);
        if(playerPlayer != null && !isMute())
            playerPlayer.start();
    }

    /**
     * @param fileName path (in jar) to audio content.
     * @param mimeType audio content mime-type.
     * @param useLoop number of loops, -1 gives indefintely loop.
     * @return Player created player.
     */
    private Player createPlayer(String fileName, String mimeType, int useLoop)
    {
        Player p = null;
        InputStream in = getClass().getResourceAsStream(fileName);
        try
        {
            p = Manager.createPlayer(in, mimeType);
            p.setLoopCount(useLoop);
            p.prefetch();
            p.realize();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return p;
    }

    private void stop(Player player)
    {
        if(player != null)
        {
            try
            {
                player.stop();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}