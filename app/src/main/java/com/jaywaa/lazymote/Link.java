package com.jaywaa.lazymote;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by James on 11/19/2015.
 *
 * acts as the link between the client and the server
 */
public class Link {

    String HOST = "10.0.0.3";
    int PORT = 53366;

    private boolean connecting = true;

    private DatagramSocket socket;
    private Socket tcpSocket;

    private PrintWriter tcpWriter;
    private BufferedReader tcpReader;

    public Link (String ip, int p)
    {
        HOST = ip;
        PORT = p;

        establishConnection();
    }
    public Link()
    {
        establishConnection();
    }

    private void establishConnection()
    {
        Log.v("LINK", "Establishing connection using host: " + HOST + " and port: " + PORT);
        new EstablishConnection().execute();

    }

    /* MOUSE CONTROLS */
    public void sendMovement(int x, int y)
    {
        String msgStr = "CM#"+x+"#"+y;

        send(msgStr);
    }
    public void sendLeftClick(){send("LC");}
    public void sendLeftPress(){send("LP");}
    public void sendLeftRelease(){send("LR");}
    public void sendRightClick(){send("RC");}

    /* KEYBOARD CONTROLS */
    public void sendUnicodePress(int keycode){send("KP#"+keycode);}

    /* VOLUME CONTROLS */
    public void sendToggleMute(){send("VM");}
    public void sendIncreaseVolume(){send("VI");}
    public void sendDecreaseVolume(){send("VD");}

    /* MISC */
    public void thisMethodIsOnlyForDebugging__Seriously__PleaseDontUseThisMethodInARelease(String msg)
    {
        send(msg);
    }
    private void send(String msg)
    {
        byte [] msgbytes;
        try {
            msgbytes = msg.getBytes("UTF-8");
        } catch (UnsupportedEncodingException uee)
        {
            Log.e("LINK", "UnsupportedEncodingException w/ encoding: UTF-8. Using default encoding.");
            msgbytes= msg.getBytes();
        }

        DatagramPacket packet = new DatagramPacket(msgbytes, msgbytes.length);

        try {
            if (socket != null && socket.isConnected()) {
                Log.v("LINK", "Sending: \""+msg+"\"");
                socket.send(packet);
            } else Log.e("Link->send()", "socket is null or disconnected");

        } catch (IOException ioe)
        {
            ioe.printStackTrace();
        }

    }

    public void close()
    {
        if (socket != null)
            socket.close();
    }
    public boolean isConnected()
    {
        return socket.isConnected();

    }
    public String getHost()
    {
        return HOST;
    }
    public int getPort()
    {
        return PORT;
    }
    private class EstablishConnection extends AsyncTask<Void, Void, Boolean>
    {
        protected Boolean doInBackground(Void ...voids)
        {

                try
                {
                    Log.v("LINK", "PORT: "+PORT);
                    socket = new DatagramSocket();
                    socket.setReuseAddress(true);
                    socket.connect(InetAddress.getByName(HOST), PORT);

                } catch (SocketException se) {
                    se.printStackTrace();
                    tcpWriter.println("PORT_INVALID");
                } catch (UnknownHostException uhe) {
                    uhe.printStackTrace();
                }

            return true;
        }
    }
}
