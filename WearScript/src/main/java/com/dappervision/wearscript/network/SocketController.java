package com.dappervision.wearscript.network;

import com.codebutler.android_websockets.WebSocketClient;
import com.dappervision.wearscript.Log;

import org.apache.http.message.BasicNameValuePair;
import org.msgpack.MessagePack;
import org.msgpack.type.Value;
import org.msgpack.type.ValueFactory;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.SynchronousQueue;

import static org.msgpack.template.Templates.TValue;
import static org.msgpack.template.Templates.tList;

public class SocketController {
    private static final String TAG = "SocketController";

    private static MessagePack mMsgpack = new MessagePack();

    private WebSocketClient mSocketClient;
    private Listener mListener;
    private boolean mConnected, mInUse;
    private Queue<byte[]> mBuffer;

    public SocketController(URI uri) {
        mInUse = true;
        mConnected = false;
        mBuffer = new SynchronousQueue<byte[]>();
        List<BasicNameValuePair> extraHeaders = Arrays.asList();
        mSocketClient = new WebSocketClient(uri, mLocalListener, extraHeaders);
        mSocketClient.connect();
    }

    public void send(byte[] msg) {
        if(isConnected()) {
            mSocketClient.send(msg);
        }else {
            mBuffer.add(msg);
        }
    }

    public boolean isConnected() {
        return mConnected;
    }

    public void disconnect() {
        mInUse = false;
        mBuffer.clear();
        mSocketClient.disconnect();
        mSocketClient.getHandlerThread().quit();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private void reconnect() {
        mSocketClient.connect();
    }

    private WebSocketClient.Listener mLocalListener = new WebSocketClient.Listener() {
        @Override
        public void onConnect() {
            mConnected = true;
            while(!mBuffer.isEmpty()) {
                mSocketClient.send(mBuffer.poll());
            }
            if(mInUse && mListener != null) {
                mListener.onConnect();
            }
        }

        @Override
        public void onMessage(String s) {
            // Unused
        }

        @Override
        public void onDisconnect(int i, String s) {
            Log.w(TAG, "Lifecycle: Underlying socket disconnected: i: " + i + " s: " + s);
            mConnected = false;
            if(mInUse) {
                mListener.onDisconnect();
                reconnect();
            }
        }

        @Override
        public void onError(Exception e) {
            Log.w(TAG, "Lifecycle: Underlying socket errored: " + e.getLocalizedMessage());
            mConnected = false;
            if(mInUse) {
                mListener.onDisconnect();
                reconnect();
            }
        }

        @Override
        public void onMessage(byte[] message) {
            if (!mInUse) return;
                try {
                    handleMessage(message);
                } catch (Exception e) {
                    Log.e(TAG, String.format("onMessage: %s", e.toString()));
                }
        }

        private void handleMessage(byte[] message) throws IOException {
            String channel = "";
            List<Value> input = mMsgpack.read(message, tList(TValue));
            channel = input.get(0).asRawValue().getString();
            Log.d(TAG, String.format("Got %s", channel));

            mListener.onMessage(channel, message, input);
        }
    };

    interface Listener {

        void onConnect();

        void onDisconnect();

        void onMessage(String channel, byte[] message, List<Value> input);
    }

    public static byte[] encode(Object... data) {
        List<Value> out = new ArrayList<Value>();
        for (Object i : data) {
            Class c = i.getClass();
            if (c.equals(String.class))
                out.add(ValueFactory.createRawValue((String) i));
            else if (c.equals(Double.class))
                out.add(ValueFactory.createFloatValue((Double) i));
            else if (Value.class.isAssignableFrom(c))
                out.add((Value) i);
            else if (c.equals(Boolean.class))
                out.add(ValueFactory.createBooleanValue((Boolean) i));
            else {
                android.util.Log.e(TAG, "Unhandled class: " + c);
                return null;
            }
        }
        try {
            return mMsgpack.write(out);
        } catch (IOException e) {
            android.util.Log.e(TAG, "Could not encode msgpack");
        }
        return null;
    }
}
