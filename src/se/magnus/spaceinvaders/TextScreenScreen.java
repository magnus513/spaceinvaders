package se.magnus.spaceinvaders;

import javax.microedition.lcdui.*;

/**
 * Simple class that view text.
 * @author magnus
 */
public class TextScreenScreen extends Form implements CommandListener {

    private final SpaceInvadersMIDlet midlet;
    private Command backCommand = null, exitCommand = null, okCommand = null;
    private TextField highScoreNameField = null;

    public TextScreenScreen(SpaceInvadersMIDlet midlet, String heading)
    {
        super(heading);
        this.midlet = midlet;
        highScoreNameField = new TextField("Enter name: ", null, 32, TextField.ANY);
        append(highScoreNameField);

        exitCommand = new Command("Exit", Command.SCREEN, 1);
        addCommand(exitCommand);
        okCommand = new Command("Yes", Command.SCREEN, 1);
        addCommand(okCommand);
        
        setCommandListener(this);
    }

    /**
     *
     * @param midlet
     * @param heading
     * @param text
     */
    public TextScreenScreen(SpaceInvadersMIDlet midlet, String heading, String text)
    {
        super(heading);
        this.midlet = midlet;
        append(new StringItem(null, text));
        
        //Ticker ticker = new Ticker("testing");
        //this.setTicker(ticker);

        backCommand = new Command("Back", Command.BACK, 1);
        addCommand(backCommand);
        setCommandListener(this);
    } 
    
    public void commandAction(Command command, Displayable displayable)
    {
        if(command == backCommand)
            midlet.backToSpaceInvadersCanvasScreen();
        else if(command == exitCommand)
        {
            midlet.quit();
        }
        else if(command == okCommand)
        {
            if(!highScoreNameField.getString().trim().equals(""))
                HighScore.getInstance().setPlayerName(highScoreNameField.getString().trim());
            midlet.setHighScoreName();
        }
    }
}