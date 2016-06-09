package se.magnus.spaceinvaders;

import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

/**
 *
 * @author magnus
 */
public class OptionScreen extends Form implements CommandListener
{
    private final SpaceInvadersMIDlet midlet;
    private Command backCommand = null, okCommand = null;
    private ChoiceGroup choiceMuteGroup = null, highScoreGroup = null;

    public OptionScreen(SpaceInvadersMIDlet midlet)
    {
        super("Options");
        this.midlet = midlet;

        choiceMuteGroup = new ChoiceGroup("Mute Sound?", ChoiceGroup.EXCLUSIVE);
        choiceMuteGroup.append("Yes", Misc.getInstance().getMuteImage());
        choiceMuteGroup.append("No", Misc.getInstance().getNotMuteImage());

        if(Sound.getInstance().isMute())
            choiceMuteGroup.setSelectedIndex(0, true);
        else
            choiceMuteGroup.setSelectedIndex(1, true);

        if(Misc.getInstance().getJP() >= 8)
        {
            highScoreGroup = new ChoiceGroup("High Score Usage Method?", ChoiceGroup.EXCLUSIVE);
            highScoreGroup.append("RMS", null);
            highScoreGroup.append("HTTP", null);

            if(HighScore.getInstance().getHighScoreMethod())
                highScoreGroup.setSelectedIndex(0, true);
            else
                highScoreGroup.setSelectedIndex(1, true);
        }

        backCommand = new Command("Back", Command.BACK, 1);
        this.addCommand(backCommand);
        okCommand = new Command("Ok", Command.OK, 1);
        this.addCommand(okCommand);
        this.append(choiceMuteGroup);
        if(Misc.getInstance().getJP() >= 8)
            this.append(highScoreGroup);
        setCommandListener(this);

    }

    public void commandAction(Command command, Displayable display)
    {
        if(command == backCommand)
        {
            midlet.backToSpaceInvadersCanvasScreen();
        }
        else if(command == okCommand)
        {
            if( "yes".equalsIgnoreCase(choiceMuteGroup.getString(choiceMuteGroup.getSelectedIndex())))
            {
                Sound.getInstance().setMute(true);
                midlet.backToSpaceInvadersCanvasScreen();
            }
            else
            {
                Sound.getInstance().setMute(false);
                midlet.backToSpaceInvadersCanvasScreen();
            }
            if(Misc.getInstance().getJP()>= 8)
            {
                if( "HTTP".equalsIgnoreCase(highScoreGroup.getString(highScoreGroup.getSelectedIndex())))
                {
                    if(!Misc.getInstance().getHighScoreBaseURL().equals(""))
                        HighScore.getInstance().setHighScoreMethod(false);
                    midlet.backToSpaceInvadersCanvasScreen();
                }
                else
                {
                    HighScore.getInstance().setHighScoreMethod(true);
                    midlet.backToSpaceInvadersCanvasScreen();
                }
            }
        }
    }
}
/*
public class OptionScreen extends Form implements CommandListener, Runnable
{
    private final SpaceInvadersMIDlet midlet;
    private Gauge gauVolume;
    private final Command backCommand;
    private volatile Thread optionsThread = null;
    private static VolumeControl vc = null;

    public OptionScreen(SpaceInvadersMIDlet midlet)
    {
        super("Options");
        this.midlet = midlet;
        
        // Create the gauge
        gauVolume = new Gauge("Sound Level", true, 30, 0);
        this.append(gauVolume);

        backCommand = new Command("Back", Command.BACK, 1);
        addCommand(backCommand);
        setCommandListener(this);

        vc = (VolumeControl) Sound.getInstance().getBackGroundMusicPlayer().getControl("VolumeControl");

        // do not activate this since its fucks out...
        //optionsThread = new Thread(this);
        //optionsThread.start();
    }

    public void commandAction(Command command, Displayable display)
    {
        if(command == backCommand)
        {
            vc = null;
            optionsThread = null;
            midlet.optionBack();
        }
    }

    public synchronized void run()
    {

        while(Thread.currentThread() == optionsThread)
        {
            if(isShown())
            {
                //System.out.println(gauVolume.getValue() + "/" + gauVolume.getMaxValue());
                if( (gauVolume.getValue() <= 0) || (gauVolume.getValue() > 100) )
                {
                    vc.setMute(true);
                    Sound.getInstance().setMute(true);
                }
                else if(gauVolume.getValue() != 0)
                {
                    if( vc.isMuted() )
                        vc.setMute(false);
                    if(!Sound.getInstance().isMute())
                    {
                        try
                        {
                            Sound.getInstance().playBackGroundMusic(-1);
                        }
                        catch(MediaException me)
                        {
                            //Misc.getInstance().showAlertMessage(midlet, "MediaException", me.getMessage(), 5000);
                            me.printStackTrace();
                        }

                    }
                    vc.setLevel(gauVolume.getValue());
                }
                try
                {
                    optionsThread.sleep(1000);
                }
                catch(InterruptedException ie)
                {
                    //Misc.getInstance().showAlertMessage(midlet, "InterruptedException", ie.getMessage(), 5000);
                    ie.printStackTrace();
                }
            }
        }
    }

}
*/