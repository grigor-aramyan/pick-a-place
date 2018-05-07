package com.mycompany.john.pickaplace.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.phoenixframework.channels.Channel;
import org.phoenixframework.channels.Socket;

import java.io.IOException;

public class PhoenixChannels {

    private static Socket socket;
    private static Channel channel;

    private static String SOCKET_URI = "ws://" + Statics.LOCALHOST_IP + ":4000/socket/websocket";
    private static String CHANNEL_NAME = "channel:live_tracking";

    public static Channel getChannel() {
        if (channel == null) {
            channel = socket.chan(CHANNEL_NAME, null);
            return channel;
        } else {
            return channel;
        }
    }

    public static Socket getSocket(Context context) {
        if (socket == null) {
            try {
                socket = new Socket(SOCKET_URI);
                return socket;
            } catch (IOException ioExp) {
                Toast.makeText(context, "Problems occured! Live position broadcasting " +
                                "and Live tracking won't be available. We are trying hard to fix issues",
                        Toast.LENGTH_LONG).show();
                Log.e("mmm", "socket connection exception: " + ioExp.getLocalizedMessage());

                return null;
            }
        } else {
            return socket;
        }
    }
}