package com.example.currentlocation;

import java.net.Socket;


public class SockMngr {



    public static  Socket socket;
    public static String text2send;
    public static final int TIMEOUT = 1000;
    public static String response;
    private final static Object syncObj = new Object();
    private static boolean waitDone = false;

    public static  void initiate()
    {

        ClientThread clntThrd = new ClientThread();
        new Thread(clntThrd).start();
        waitForSock();
    }

    public static void sendAndReceive(String msg)
    {
        // socket cant be used on main thread
        // Starting new thread for socket management
        text2send = msg;
        new Thread(new SendThread()).start();
        waitForSock();

    }

    private static void waitForSock()
    {
        try {
            synchronized (syncObj) {
                waitDone = false;
                //Wait the current Thread for 15 seconds
                while (!waitDone)
                    syncObj.wait(TIMEOUT);
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void notifyDone()
    {

        synchronized(syncObj) {
            waitDone = true;
            syncObj.notify();
        }
    }
}
