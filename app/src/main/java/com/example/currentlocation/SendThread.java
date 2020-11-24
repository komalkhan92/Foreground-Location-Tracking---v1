package com.example.currentlocation;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.UnknownHostException;

class SendThread implements Runnable {
    private static final int BUFFER_SIZE = 100000;
    // a thread sending amd receiving

    @Override
    public void run() {
        // a thread writing to socket
        try {

            // actual writing to socket
            PrintWriter out = new PrintWriter(new BufferedWriter(
                    new OutputStreamWriter(SockMngr.socket.getOutputStream())),
                    true);
            String msg_len = String.format("%04d", SockMngr.text2send.toString().length());
            out.print(msg_len + SockMngr.text2send);
            out.flush(); // flushing to force write

            // receiving response
            char[] buff = new char[BUFFER_SIZE];

            BufferedReader input = new BufferedReader(new InputStreamReader(SockMngr.socket.getInputStream()));
            int numOfBytes = input.read(buff, 0, BUFFER_SIZE);
            String rsp = new String(buff, 0, numOfBytes);
            Log.i("SendThread", "run: result " + rsp);
            SockMngr.response = rsp;

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("SendThread", "onClick: ", e);
            e.printStackTrace();
        }
        SockMngr.notifyDone();
    }

}