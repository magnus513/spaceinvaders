package se.magnus.spaceinvaders;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

/**
 *
 * @author magnus
 */
public class HighScore
{
    private static HighScore instance = null;
    private HttpConnection http = null;
    private OutputStream os = null;
    private InputStream is = null;
    private String baseURL = "", responseBody = "", playerName = "";
    private int playerScore = 0;
    private boolean highScoreMethod = true;
    private static String[] RMSResult;
    private static Hashtable table = null;
    private final int HIGH_SCORE_LIMIT = 60000;
    //***
    private static String digestAlgo = "SHA-1";
    private static int shaDigestLen = 20;
    private byte[] newDigest = new byte[shaDigestLen];
    private static final char[] kDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private final String KEY = "fd8UruW1";
    private static final String RS_NAME = "spaceinvaders";
    //***

    protected HighScore()
    {
        if(getHighScoreMethod())
            table = new Hashtable();
    }

    /**
     * @return Singleton Sound object.
     */
    public static HighScore getInstance()
    {
        if(instance == null)
            instance = new HighScore();
        return instance;
    }

    /**
     *
     * @param selection 1-3.
     * @return
     */
    public boolean setAndGetScore(int selection, String firstId, String lastId)
    {
        // use RMS.
        if(getHighScoreMethod())
        {
            boolean result = false;
            Hashtable returnTable = null;
            if(selection == 1)
            {
                table.clear();
                String oldRMSData[] = readRecordStore();
                // if rms is empty.
                if(oldRMSData == null)
                {
                    String highScore[] = {"1", getPlayerName(), getPlayerScore()};
                    result = writeRecordStore(highScore);
                    String temp = "1" + ";" + getPlayerName() + ";" + getPlayerScore() + ";";
                    table.put("1", temp);
                    setRMSResult(highScore);
                }
                //TODO: coco solution, use direct RMS handling instead of hastable buffer.
                else
                {
                    // build the hashtable.
                    for(int i=0; i<oldRMSData.length; i+=3)
                    {
                        String position = oldRMSData[i];
                        String name = oldRMSData[i+1];
                        String score = oldRMSData[i+2];
                        String combined = position + ";" + name + ";" + score + ";";
                        table.put(position, combined);
                    }

                    int newPosition = table.size() + 1;
                    String temp = String.valueOf(newPosition) + ";" + getPlayerName() + ";" + getPlayerScore() + ";";
                    table.put(String.valueOf(newPosition), temp);

                    //sort the hashtable.
                    returnTable = null;

                    returnTable = Misc.getInstance().bubbleSortWithHashMap(table);

                    String[] newRMSData = new String[returnTable.size() * 3];
                    int counter = 0, rank = 1;

                    for(int i=HIGH_SCORE_LIMIT; i>0; i--)
                    {
                        if(returnTable.containsKey(String.valueOf(i)))
                        {
                            String temp2 = (String) returnTable.get(String.valueOf(i));
                            String temp3[] = Misc.getInstance().split(temp2, ";");
                            newRMSData[counter] = String.valueOf(rank);
                            rank++;
                            counter++;
                            newRMSData[counter] = temp3[1];
                            counter++;
                            newRMSData[counter] = temp3[2];
                            counter++;
                        }
                    }

                    setRMSResult(newRMSData);
                    result = writeRecordStore(newRMSData);

                }
            }
            else if(selection == 2)
            {
                int nrOfNext = 0, counter = 0, rank = Integer.parseInt(lastId);
                String[] newRMSData = null;
                for(int i=Integer.parseInt(lastId); i<HIGH_SCORE_LIMIT; i++)
                {
                    if(returnTable != null && returnTable.containsKey(String.valueOf(i)))
                    {
                        String temp2 = (String) returnTable.get(String.valueOf(i));
                        String temp3[] = Misc.getInstance().split(temp2, ";");
                        newRMSData[counter] = String.valueOf(rank);
                        rank++;
                        counter++;
                        newRMSData[counter] = temp3[1];
                        counter++;
                        newRMSData[counter] = temp3[2];
                        counter++;
                        nrOfNext++;
                        if(nrOfNext == 11)
                            break;
                    }
                }
                setRMSResult(newRMSData);
                return true;
            }
            else if(selection == 3)
            {
                int nrOfNext = 0, counter = 0, rank = Integer.parseInt(firstId);
                String[] newRMSData = null;
                for(int i=Integer.parseInt(firstId); i>0; i--)
                {
                    if(returnTable != null && returnTable.containsKey(String.valueOf(i)))
                    {
                        String temp2 = (String) returnTable.get(String.valueOf(i));
                        String temp3[] = Misc.getInstance().split(temp2, ";");
                        newRMSData[counter] = String.valueOf(rank);
                        rank--;
                        counter++;
                        newRMSData[counter] = temp3[1];
                        counter++;
                        newRMSData[counter] = temp3[2];
                        counter++;
                        nrOfNext++;
                        if(nrOfNext == 11)
                            break;
                    }
                }
                setRMSResult(newRMSData);
                return true;
            }
            return result;
        }
        // use HTTP.
        else if(!getHighScoreMethod())
        {
            
            String[] params = new String[4];
            params[0] = getPlayerName() + KEY + getPlayerScore();

            if(selection == 1)
            {
                params[1] = "&name=" + getPlayerName();
                params[2] = "&score=" + getPlayerScore();
            }
            else if(selection == 2)
            {
                params[1] = "&name=" + getPlayerName();
                params[2] = "&score=" + getPlayerScore();
                params[3] = "&next=" + lastId;
            }
            else if(selection == 3)
            {
                params[1] = "&name=" + getPlayerName();
                params[2] = "&score=" + getPlayerScore();
                params[3] = "&prevoius=" + firstId;
            }

            setResponseBody( doHttpPostRequest(params) );
            if(!getResponseBody().equals("") )
            {
                params = null;
                return true;
            }
        }
        return false;
    }

    /**
     * @param params post parameter(s)
     * @return server reponse body if http 200 is received, otherwise null.
     */
    private String doHttpPostRequest(String[] params)
    {
        baseURL = Misc.getInstance().getHighScoreBaseURL();
        String sb = "";

        try
        {

            http = (HttpConnection) Connector.open(baseURL);
            http.setRequestMethod(HttpConnection.POST);
            http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            os = http.openOutputStream();

            if(params[0] != null)
            {
                String temp = new String(bytesToHex(performDigest(params[0])));
                if(Misc.getInstance().getDebugging())
                    System.out.println("sha-1 message: " + temp);
                String temp2 = "&auth=" + temp;
                os.write(temp2.getBytes());
            }
            if(params[1] != null)
                os.write(params[1].getBytes());
            if(params[2] != null)
                os.write(params[2].getBytes());
            if(params[3] != null)
                os.write(params[3].getBytes());
            //os.flush();

            if(http.getResponseCode() == HttpConnection.HTTP_OK)
            {
                if(Misc.getInstance().getDebugging())
                    System.out.println("response content-length from server: " + String.valueOf(http.getLength()));
                if(http.getLength() != -1)
                {
                    is = http.openInputStream();
                    byte[] raw = new byte[(int) http.getLength()];
                    int length = is.read(raw);
                    sb = new String(raw, 0, length);
                }
                else
                {
                    StringBuffer sb2 = new StringBuffer();
                    is = http.openInputStream();
                    int chr;
                    while((chr = is.read()) != -1)
                        sb2.append((char) chr);
                    sb = sb2.toString();
                }
            }

            if(is!= null)
                is.close();
            if(os != null)
                os.close();
            if(http != null)
                http.close();

        }
        catch(IOException io)
        {
            io.printStackTrace();
        }
        return sb;
    }

    private byte[] performDigest(String inputMessage)
    {
        // original message
        byte[] message = inputMessage.getBytes();

        try
        {
            MessageDigest md;
            md = MessageDigest.getInstance(digestAlgo);
            md.update(message, 0, message.length);
            md.digest(newDigest, 0, shaDigestLen);

            return newDigest;
        }
        catch (Exception e)
        {
            // Handle NoSuchAlgorithmException or DigestException
            e.printStackTrace();
            return null;
        }
    }

    private boolean checkDigest(String inputMessage)
    {
        byte[] originalDigest = inputMessage.getBytes();

        // Verify the calculated digest against the original message digest.
        boolean verifyOK = true;
        for (int i=0; i<shaDigestLen; i++)
        {
            if (originalDigest[i] != newDigest[i]) {
                verifyOK = false;
                break;
            }
        }
        // Are the digests the same?
        if (verifyOK == false) {
            // Data integrity test failed...
        }
        return verifyOK;
    }

    public void setHighScoreMethod(boolean highScoreMethod)
    {
        this.highScoreMethod = highScoreMethod;
    }
    /**
     * @return true if RMS is used otherwise false if HTTP.
     */
    public boolean getHighScoreMethod()
    {
        return highScoreMethod;
    }

    public void setPlayerName(String playerName)
    {
        this.playerName = playerName;
    }
    public String getPlayerName()
    {
        return playerName;
    }

    public void setPlayerScore(int playerScore)
    {
        this.playerScore = playerScore;
    }
    public String getPlayerScore()
    {
        return String.valueOf(playerScore);
    }

    public void setResponseBody(String responseBody)
    {
        //TODO: REMOVE
        System.out.println("*** responseBody= " + responseBody);

        if(Misc.getInstance().getDebugging())
            System.out.println("responseBody= " + responseBody);
        this.responseBody = responseBody;
    }
    /**
     * @return http response body, ; seperated list if success.
     */
    public String getResponseBody()
    {
        return responseBody;
    }
    
    private void setRMSResult(String[] RMSResult)
    {
        this.RMSResult = RMSResult;
    }
    /**
     * @return rms result list.
     */
    public String[] getRMSResult()
    {
        return RMSResult;
    }

    //*** help methods ****
    public static char[] bytesToHex(byte[] raw)
    {
        int length = raw.length;
        char[] hex = new char[length * 2];
        for (int i = 0; i < length; i++)
        {
            int value = (raw[i] + 256) % 256;
            int highIndex = value >> 4;
            int lowIndex = value & 0x0f;
            hex[i * 2 + 0] = kDigits[highIndex];
            hex[i * 2 + 1] = kDigits[lowIndex];
        }
        return hex;
    }

    public static byte[] hexToBytes(char[] hex)
    {
        int length = hex.length / 2;
        byte[] raw = new byte[length];
        for (int i = 0; i < length; i++)
        {
            int high = Character.digit(hex[i * 2], 16);
            int low = Character.digit(hex[i * 2 + 1], 16);
            int value = (high << 4) | low;
            if (value > 127)
                value -= 256;
            raw[i] = (byte)value;
        }
        return raw;
    }

    public static byte[] hexToBytes(String hex)
    {
        return hexToBytes(hex.toCharArray());
    }

    // Displays encrypted data in hex
    String byteToHex(byte[] data)
    {
        StringBuffer hexString = new StringBuffer();
        String hexCodes = "0123456789ABCDEF";

        for (int i=0; i < data.length; i++)
        {
            hexString.append( hexCodes.charAt( (data[i] >> 4) & 0x0f) );
            hexString.append( hexCodes.charAt( data[i] & 0x0f) );
            if (i< data.length - 1)
            {
                hexString.append(":");
            }
            if ( ((i+1)%8) == 0)
                hexString.append("\n");
        }
        return hexString.toString();
    }
    //*** end ***

    private String[] readRecordStore()
    {
        String RMSData[] = null;
        RecordStore rs = null;
        RecordEnumeration re = null;
        ByteArrayInputStream bais = null;
        DataInputStream dis = null;
        
        if(RecordStore.listRecordStores() == null)
            return RMSData;
        else
            System.out.println("rms name(s)= " + rs.listRecordStores().toString());

        try
        {
            rs = RecordStore.openRecordStore(RS_NAME, false);
            re = rs.enumerateRecords(null, null, false);

            //byte[] data = rs.getRecord(1);
            //bais = new ByteArrayInputStream(data);
            //dis = new DataInputStream(bais);
            //bestTime = dis.readLong();
            //dis.readLong();

            //while (re.hasNextElement()) {
            RMSData = new String[re.numRecords()*3];
            int counter = 0;
            for(int i=0; i<re.numRecords() && re.hasNextElement(); i++)
            {
                byte[] data = re.nextRecord();
                //bais = new ByteArrayInputStream(data);
                //dis = new DataInputStream(bais);
                //RMSData[i] = dis.readUTF().toString();
                String temp[] = Misc.getInstance().split(new String(data), ";");
                for(int j=0; j<temp.length; j++)
                {
                    //RMSData[i] = new String(data);
                    RMSData[counter] = temp[j];
                    System.out.println("*** reading= " + RMSData[counter]);
                    counter++;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (dis != null)
            {
                try
                {
                    dis.close();
                }
                catch (IOException ex)
                {
                    
                }
            }
            if (bais != null)
            {
                try
                {
                    bais.close();
                }
                catch (IOException ex)
                {
                    
                }
            }
            if (rs != null)
            {
                try
                {
                    rs.closeRecordStore();
                }
                catch (RecordStoreException ex)
                {
                    
                }
            }
        }
        return RMSData;
    }

    private boolean writeRecordStore(String[] writeData)
    {
        RecordStore rs = null;
        ByteArrayOutputStream baos = null;
        DataOutputStream dos = null;
        try
        {

            // Delete any previous record store with same name.
            if(RecordStore.listRecordStores() != null)
                RecordStore.deleteRecordStore(RS_NAME);

            rs = RecordStore.openRecordStore(RS_NAME, true);
            /*
            baos = new ByteArrayOutputStream();
            dos = new DataOutputStream(baos);
            //dos.writeLong(bestTime);
            dos.writeLong(1);
            byte[] data = baos.toByteArray();
            if (rs.getNumRecords() == 0)
            {
                // new record store
                rs.addRecord(data, 0, data.length);
            }
            else
            {
                // existing record store: will have one record, id 1
                rs.setRecord(1, data, 0, data.length);
            }
            */
            for(int i=0; i<writeData.length; i+=3)
            {
                System.out.println("*** writing= " + writeData[i] + ";" + writeData[i+1] + ";" + writeData[i+2] + ";");
                byte[] record = (writeData[i] + ";" + writeData[i+1] + ";" + writeData[i+2] + ";").getBytes();
                rs.addRecord(record, 0, record.length);
            }
        }
        catch (Exception e)
        {
            // just leave the best time unrecorded
            e.printStackTrace();
            return false;
        }
        finally
        {
            if (dos != null)
            {
                try
                {
                    dos.close();
                }
                catch (IOException ex)
                {
                    // no error handling necessary here
                }
            }
            if (baos != null)
            {
                try
                {
                    baos.close();
                }
                catch (IOException ex)
                {
                    // no error handling necessary here
                }
            }
            if (rs != null)
            {
                try
                {
                    rs.closeRecordStore();
                }
                catch (RecordStoreException rsex)
                {
                    // no error handling necessary here
                }
            }
        }
        return true;
    }

}