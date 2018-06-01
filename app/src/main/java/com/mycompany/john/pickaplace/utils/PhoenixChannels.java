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

    private static String SOCKET_URI = "ws://" + Statics.LOCALHOST_IP + "/socket/websocket";
    public static String CHANNEL_NAME = "live_tracking:general";

    // channel messages
    public static final String CHANNEL_MSG_GET_LIVE_LOCATION = "live_tracking:get_live_location";
    public static final String CHANNEL_MSG_UPDATE_LIVE_LOCATION = "live_tracking:update_live_location";
    public static final String CHANNEL_MSG_DELETE_LIVE_LOCATION = "live_tracking:delete_live_location";

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
