package com.dappervision.wearscript.network;

import android.util.Base64;
import android.util.Log;

import org.msgpack.MessagePack;
import org.msgpack.type.Value;
import org.msgpack.type.ValueFactory;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import static org.msgpack.template.Templates.TValue;
import static org.msgpack.template.Templates.tList;

public abstract class WearScriptConnection {
    private static final String TAG = "WearScriptConnection";
    private static final String LISTEN_CHAN = "subscriptions";

    private static MessagePack mMsgpack = new MessagePack();

    protected String device, group, groupDevice;
    private URI uri;
    // deviceToChannels is always updated, then externalChannels is rebuilt
    private TreeMap<String, ArrayList<String>> deviceToChannels;
    private TreeSet<String> externalChannels;
    private TreeSet<String> scriptChannels;
    private SocketController mSocket;

    public WearScriptConnection(String group, String device) {
        this.group = group;
        this.device = device;
        groupDevice = channel(group, device);
        scriptChannels = new TreeSet<String>();
        resetExternalChannels();
        pinger();
    }

    public static Value listValue(Iterable<String> channels) {
        ArrayList<Value> channelsArray = new ArrayList<Value>();
        for (String c : channels)
            channelsArray.add(ValueFactory.createRawValue(c));
        return ValueFactory.createArrayValue(channelsArray.toArray(new Value[channelsArray.size()]));
    }

    public static Value mapValue(Map<String, ArrayList<String>> data) {
        ArrayList<Value> mapArray = new ArrayList<Value>();
        for (String k : data.keySet()) {
            mapArray.add(ValueFactory.createRawValue(k));
            mapArray.add(listValue(data.get(k)));
        }
        return ValueFactory.createMapValue(mapArray.toArray(new Value[mapArray.size()]));
    }

    protected abstract void onReceive(String channel, byte[] dataRaw, List<Value> data);

    protected abstract void onConnect();

    protected abstract void onDisconnect();

    private void onReceiveDispatch(String channel, byte[] dataRaw, List<Value> data) {
        String channelPart = existsInternal(channel);
        if (channelPart != null) {
            Log.i(TAG, "ScriptChannel: " + channelPart);
            if (dataRaw != null && data == null) {
                try {
                    data = mMsgpack.read(dataRaw, tList(TValue));
                } catch (IOException e) {
                    Log.e(TAG, "Could not decode msgpack");
                    return;
                }
            }
            onReceive(channelPart, dataRaw, data);
        }
    }

    private void resetExternalChannels() {
        deviceToChannels = new TreeMap<String, ArrayList<String>>();
        externalChannels = new TreeSet<String>();
    }

    public void publish(Object... data) {
        String channel = (String) data[0];
        if (!exists(channel) && !channel.equals(LISTEN_CHAN))
            return;
        Log.d(TAG, "Publishing...: " + channel);
        if (channel.equals(LISTEN_CHAN)) {
            Log.d(TAG, "Channels: " + Base64.encodeToString(SocketController.encode(data), Base64.NO_WRAP));
        }
        publish(channel, SocketController.encode(data));
    }

    public void publish(String channel, byte[] outBytes) {
        if (!exists(channel)) {
            Log.d(TAG, String.format("Not publishing[%s] Client: %s outBytes: %s", channel, mSocket != null, outBytes != null));
            return;
        }
        if (outBytes != null) {
            onReceiveDispatch(channel, outBytes, null);
            if (mSocket != null && existsExternal(channel))
                mSocket.send(outBytes);
        }
    }

    private Value channelsValue() {
        synchronized (this) {
            return listValue(scriptChannels);
        }
    }

    public void subscribe(String channel) {
        synchronized (this) {
            if (!scriptChannels.contains(channel)) {
                scriptChannels.add(channel);
                publish(LISTEN_CHAN, this.groupDevice, channelsValue());
            }
        }
    }

    public void subscribe(Iterable<String> channels) {
        synchronized (this) {
            boolean added = false;
            for (String channel : channels) {
                if (!scriptChannels.contains(channel)) {
                    added = true;
                    scriptChannels.add(channel);
                }
            }
            if (added)
                publish(LISTEN_CHAN, this.groupDevice, channelsValue());
        }
    }

    public String channel(Object... channels) {
        String out = "";
        for (Object c : channels) {
            if (out.isEmpty())
                out += (String) c;
            else {
                out += ":" + (String) c;
            }

        }
        return out;
    }

    public void unsubscribe(String channel) {
        synchronized (this) {
            if (scriptChannels.contains(channel)) {
                scriptChannels.remove(channel);
                publish(LISTEN_CHAN, this.groupDevice, channelsValue());
            }
        }
    }

    public void unsubscribe(Iterable<String> channels) {
        synchronized (this) {
            boolean removed = false;
            for (String channel : channels) {
                if (scriptChannels.contains(channel)) {
                    removed = true;
                    scriptChannels.remove(channel);
                }
            }
            if (removed)
                publish(LISTEN_CHAN, this.groupDevice, channelsValue());
        }
    }

    private void setDeviceChannels(String device, Value[] channels) {
        synchronized (this) {
            ArrayList<String> channelsArray = new ArrayList();
            for (Value channel : channels)
                channelsArray.add(channel.asRawValue().getString());
            deviceToChannels.put(device, channelsArray);
            TreeSet<String> externalChannelsNew = new TreeSet<String>();
            for (ArrayList<String> deviceChannels : deviceToChannels.values())
                for (String channel : deviceChannels)
                    externalChannelsNew.add(channel);
            externalChannels = externalChannelsNew;
        }
    }

    public void connect(URI uri) {
        Log.d(TAG, "Lifecycle: Connect called");
        synchronized (this) {
            Log.i(TAG, uri.toString());
            if (uri.equals(this.uri) && mSocket != null && mSocket.isConnected()) {
                onConnect();
                return;
            }
            this.uri = uri;
            Log.i(TAG, "Lifecycle: Socket connecting");
            mSocket = new SocketController(uri);
            mSocket.setListener(mSocketListener);
        }
    }

    public String subchannel(String part) {
        return channel(part, this.groupDevice);
    }

    public TreeSet<String> channelsInternal() {
        return scriptChannels;
    }

    public TreeMap<String, ArrayList<String>> channelsExternal() {
        return deviceToChannels;
    }

    public String group() {
        return group;
    }

    public String groupDevice() {
        return groupDevice;
    }

    public String device() {
        return device;
    }

    public String ackchannel(String c) {
        return channel(c, "ACK");
    }

    private boolean existsExternal(String channel) {
        if (channel.equals(LISTEN_CHAN))
            return true;
        String channelPartial = "";
        String[] parts = channel.split(":");
        if (externalChannels.contains(channelPartial))
            return true;
        for (String part : parts) {
            if (channelPartial.isEmpty())
                channelPartial += part;
            else
                channelPartial += ":" + part;
            if (externalChannels.contains(channelPartial))
                return true;
        }
        return false;
    }

    private String existsInternal(String channel) {
        String channelPartial = "";
        String[] parts = channel.split(":");
        String channelPartialExists = null;
        for (String part : parts) {
            if (channelPartial.isEmpty())
                channelPartial += part;
            else
                channelPartial += ":" + part;
            if (scriptChannels.contains(channelPartial))
                channelPartialExists = channelPartial;
        }
        return channelPartialExists;
    }

    public boolean exists(String channel) {
        return existsExternal(channel) || existsInternal(channel) != null;
    }

    public void disconnect() {
        mSocket.disconnect();
    }

    public void shutdown() {
        disconnect();
        mSocket = null;
    }

    private void pinger() {
        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    if (mSocket == null) return;
                    Log.i(TAG, "Lifecycle: Pinger...");
                    if(mSocket.isConnected()) {
                        publish(LISTEN_CHAN, WearScriptConnection.this.groupDevice, channelsValue());
                    }
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }).start();
    }

    private void publishChannels() {
        new Thread(new Runnable() {
            public void run() {
                Log.w(TAG, "Lifecycle: Pinger...");
                Log.d(TAG, "Channels: " + channelsValue());
                publish(LISTEN_CHAN, WearScriptConnection.this.groupDevice, channelsValue());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
            }
        }).start();
    }

    private SocketController.Listener mSocketListener = new SocketController.Listener() {
        @Override
        public void onConnect() {
            publishChannels();
            WearScriptConnection.this.onConnect();
        }

        @Override
        public void onDisconnect() {
            WearScriptConnection.this.onDisconnect();
        }

        @Override
        public void onMessage(String channel, byte[] message, List<Value> input) {
            if (channel.equals(LISTEN_CHAN)) {
                String d = input.get(1).asRawValue().getString();
                Value[] channels = input.get(2).asArrayValue().getElementArray();
                setDeviceChannels(d, channels);
            }
            onReceiveDispatch(channel, message, input);
        }
    };
}
