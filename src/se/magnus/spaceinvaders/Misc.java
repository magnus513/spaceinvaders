package se.magnus.spaceinvaders;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDlet;

/**
 * Class that contains common used method i.e. get diffrent images etc.
 * @author magnus
 */
public class Misc {

    public static final int BLACK = 0x00000000;
    public static final int WHITE = 0x00FFFFFF;
    public static final int RED = 0x00FF0000;
    public static final int BLUE = 0x000000FF;

    private static Misc instance = null;
    private static final Random random = new Random();
    private boolean debugging = false;
    private String highScoreBaseURL = "";

    protected Misc()
    {
    }

    /**
     * @return Singleton Misc object.
     */
    public static Misc getInstance()
    {
        if(instance == null)
            instance = new Misc();
        return instance;
    }

    /**
     * @param debugging true=enabled, false=disabled.
     */
    public void setDebugging(boolean debugging)
    {
        this.debugging = debugging;
    }
    /**
     * Parameter received from jad, or manifest.
     * @return true=enabled, false=disabled
     */
    public boolean getDebugging()
    {
        return debugging;
    }

    public void setHighScoreBaseURL(String highScoreBaseURL)
    {
        this.highScoreBaseURL = highScoreBaseURL;
    }
    /**
     * Parameter received from jad, or manifest.
     * @return base url to "high score" server.
     */
    public String getHighScoreBaseURL()
    {
        return highScoreBaseURL;
    }

    public Image getNotMuteImage()
    {
        return createImage("/image/noMute.png");
    }
    public Image getMuteImage()
    {
        return createImage("/image/mute.png");
    }
     /**
     * @return Image welcome image 176x240 pixel.
     */
    public Image getWelcomeSmallImage()
    {
        return createImage("/image/spaceInvadersWelcome_176x240.jpg");
    }

    /**
     * @return Image welcome image 240x320 pixel.
     */
    public Image getWelcomeLargeImage()
    {
        return createImage("/image/spaceInvadersWelcome_240x320.jpg");
    }

     /**
     * @return Image monster image 176x240 pixel.
     */
    public Image getMonsterSmallImage()
    {
        return createImage("/image/monsterScreen_176x220.png");
    }

    /**
     * @return Image monster image 240x320 pixel.
     */
    public Image getMonsterLargeImage()
    {
        return createImage("/image/monsterScreen_240x320.png");
    }

     /**
     * @return Image monster without text image 176x240 pixel.
     */
    public Image getMonsterNoTextSmallImage()
    {
        return createImage("/image/monsterScreenNoText_176x220.png");
    }

    /**
     * @return Image monster without text image 240x320 pixel.
     */
    public Image getMonsterNoTextLargeImage()
    {
        return createImage("/image/monsterScreenNoText_240x320.png");
    }

    /**
     * @return Image no image 24x24 pixel.
     */
    public Image getNoImage()
    {
        return createImage("/image/no.png");
    }

     /**
     * @return Image yes image 24x24 pixel.
     */
    public Image getYesImage()
    {
        return createImage("/image/yes.png");
    }

    /**
     * @return Image wall image 96x4 pixel, sprite size 6x4 pixel.
     */
    public Image getWallSmallImage()
    {
        return createImage("/image/wallSmall.png");
    }
    /**
     * @return Image wall image 48x8 pixel, sprite size 6x8 pixel.
     */
    public Image getWallLargeImage()
    {
        return createImage("/image/wallLarge.png");
    }
    /**
     * @return Image background image 176x220 pixel.
     */
    public Image getSmallBackgroundImage()
    {
        return createImage("/image/background2_176x220.png");
    }
    /**
     * @return Image background image 240x320 pixel.
     */
    public Image getLargeBackgroundImage()
    {
        return createImage("/image/background4_240x320.png");
    }
    public Image getPlayerImage()
    {
        return createImage("/image/player.png");
    }
    /**
     * @return Image 6x3 pixel image, sprite size 3x3 pixel.
     */
    public Image getPlayerMissile()
    {
        return createImage("/image/playerMissile.png");
    }
    /**
     * @return Image 48x8 pixel, sprite size 12x8 pixel.
     */
    public Image getAlienInvaderOneImage()
    {
        return createImage("/image/alien1.png");
    }
    /**
     * @return Image 44x8 pixel, sprite size 11x8 pixel.
     */
    public Image getAlienInvaderTwoImage()
    {
        return createImage("/image/alien2.png");
    }
    /**
     * @return Image 32x8 pixel, sprite size 8x8 pixel.
     */
    public Image getAlienInvaderThreeImage()
    {
        return createImage("/image/alien3.png");
    }
    /**
     * @return Image missile for alien variant 1 and 2. 6x6 pixel image, sprite size 3x6 pixel.
     */
    public Image getAlien1InvaderMissile()
    {
        return createImage("/image/alienMissile1.png");
    }
    /**
     * @return Image missile for alien variant 3. 6x9 pixel image, sprite size 3x9 pixel.
     */
    public Image getAlien2InvaderMissile()
    {
        return createImage("/image/alienMissile2.png");
    }
    /**
     * @return Image "mother ship", 39x6 pixel image.
     */
    public Image getMotherShipImage()
    {
        return createImage("/image/motherShip.png");
    }
    /**
     * @return Image missile for mothership. 6x9 pixel image, sprite size 3x9 pixel.
     */
    public Image getMotherShipMissile()
    {
        return createImage("/image/motherShipMissile.png");
    }

    /**
     * @param Image file path to image within jar file.
     * @return Image created image referance.
     */
    public static Image createImage(String filePath)
    {
        Image image = null;
        try
        {
            image = Image.createImage(filePath);
        }
        catch (IOException io)
        {
            io.printStackTrace();
        }
        return image;
    }

    public void flashBacklight(int millis, MIDlet midlet)
    {
        Display.getDisplay(midlet).flashBacklight(millis);
    }

    public void vibrate(int millis, MIDlet midlet)
    {
        Display.getDisplay(midlet).vibrate(millis);
    }

    /**
     * @param midlet
     * @param titelText
     * @param messageText
     * @param alertTime time (ms) to diplay titelText and messageText.
     */
    public void showAlertMessage(SpaceInvadersMIDlet midlet, String titelText, String messageText, int alertTime)
    {
        vibrate(1500, midlet);
        Alert alert = new Alert(titelText, messageText, null, AlertType.WARNING);
        //alert.setTimeout(Alert.FOREVER);
        alert.setTimeout(alertTime);
        //midlet.getCurrentDisplay().setCurrent(alert);
        Display.getDisplay(midlet).setCurrent(alert);
    }

    /**
     * @param n interval.
     * @return generated random number within interval 0 and (n-1).
     */
    public int getRandomNbr(int n)
    {
        //Math.abs(random.nextInt())%n;
        //return (random.nextInt() & 0x7FFFFFFF) % size;
        return random.nextInt(n);
    }

    /**
     * 
     * @param str
     * @param ch delimeter character.
     * @return
     */
    public static String[] split(String str, String ch)
    {
        java.util.Vector v=new java.util.Vector();
        while(str.indexOf(ch) != -1)
        {
            String tmp=str.substring(0, str.indexOf(ch)).trim();
            if(tmp.length()>0)
                v.addElement(tmp);
            str=str.substring(str.indexOf(ch)+1,str.length());
        }
        String[] returned=new String[v.size()];
        for (int i=0;i<v.size();i ++)
        {
            returned[i]=(String)v.elementAt(i);
        }
        return returned;
    }

    /**
     * @return device java platform as integer e.g. JP-8.1 return 8.
     */
    public int getJP()
    {
        String jpStr = "";
        int jp = -1;
        try
        {
            jpStr = System.getProperty("com.sonyericsson.java.platform").substring(3, 4);
            jp = Integer.parseInt(jpStr);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return jp;
    }

    //***

    public Hashtable bubbleSortWithHashMap(Hashtable t)
    {
        int[] a = new int[t.size()];
        for(int i=1; i<=t.size(); i++)
        {
            String [] temp = Misc.getInstance().split((String) t.get(String.valueOf(i)), ";");
            a[i-1] = Integer.parseInt( temp[2] );
        }
        int out, in;

        for (out = a.length - 1; out > 1; out--)
            for (in = 0; in < out; in++)
                if (a[in] < a[in + 1])
                    swapWithHashMap(in, in + 1, a, t);

        Hashtable tt = new Hashtable();
        Enumeration en = t.elements();
        while(en.hasMoreElements())
        {
            String temp = (String)en.nextElement();
            String temp2[] = Misc.getInstance().split(temp, ";");

            if(temp2[2].equals("0"))
                temp2[2] = "1";

            if(tt.containsKey(temp2[2]))
            {
                int tempChange = Integer.parseInt(temp2[2]);
                temp2[2] = String.valueOf(tempChange++);
            }

            tt.put(temp2[2], temp);
        }
        return tt;
    }

    private void swapWithHashMap(int one, int two, int[] a, Hashtable t) {
        int temp = a[one];
        String tempOne = (String) t.get(String.valueOf(one+1));

        a[one] = a[two];
        t.remove(String.valueOf(one+1));
        t.put(String.valueOf(one+1), (String) t.get(String.valueOf(two+1)));
        
        a[two] = temp;
        t.remove(String.valueOf(two+1));
        t.put(String.valueOf(two+1), tempOne);
    }

    public int[] bubbleSort(int[] a)
    {
        int out, in;

        for (out = a.length - 1; out > 1; out--)
            for (in = 0; in < out; in++)
                if (a[in] > a[in + 1])
                    swap(in, in + 1, a);
        return a;
    }

    private void swap(int one, int two, int[] a) {
        int temp = a[one];
        a[one] = a[two];
        a[two] = temp;
    }

    //***

}