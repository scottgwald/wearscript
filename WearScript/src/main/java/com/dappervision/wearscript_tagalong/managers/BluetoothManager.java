package com.dappervision.wearscript_tagalong.managers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Base64;

import com.dappervision.wearscript_tagalong.BackgroundService;
import com.dappervision.wearscript_tagalong.Log;
import com.dappervision.wearscript_tagalong.events.BluetoothBondEvent;
import com.dappervision.wearscript_tagalong.events.BluetoothModeEvent;
import com.dappervision.wearscript_tagalong.events.BluetoothWriteEvent;
import com.dappervision.wearscript_tagalong.events.CallbackRegistration;
import com.dappervision.wearscript_tagalong.events.PhoneConnectEvent;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.String;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

public class BluetoothManager extends Manager {
    public static final String DATA = "DATA";
    public static final String LIST = "LIST";
    public static final String READ = "READ:";
    public static final String DISCOVERY_START = "DISCOVERY_START";
    public static final String DISCOVERY_STOP = "DISCOVERY_STOP";
    TreeMap<String, BluetoothSocket> mSockets;
    TreeMap<String, BluetoothDevice> mDevices;
    TreeMap<String, String> mDevicePins;
    static String TAG = "BluetoothManager";
    private BluetoothAdapter mBluetoothAdapter;
    private boolean isSetup;
    private ConnectedThread mConnectedThread;
    private AcceptThread mAcceptThread;
    public static final int STATE_CONNECTION_STARTED = 0;
    public static final int STATE_CONNECTION_LOST = 1;
    public static final int READY_TO_CONN = 2;


    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                Log.d(TAG, "Discovery: " + device.getAddress());
                mDevices.put(device.getAddress(), device);
                JSONObject deviceJS = new JSONObject();
                deviceJS.put("name", device.getName());
                deviceJS.put("address", device.getAddress());
                makeCall(DISCOVERY_START, "'" + deviceJS.toJSONString() + "'");
            }
        }
    };

    private BroadcastReceiver mReceiverRequiresPin = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            int pairingVariant = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, 0);
            Log.d(TAG, "Received pairing request: " + pairingVariant);
            try {
                BluetoothDevice newDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Class<?> btDeviceInstance = Class.forName(BluetoothDevice.class.getCanonicalName());

                Method convert = btDeviceInstance.getMethod("convertPinToBytes", String.class);

                String pinString = mDevicePins.get(newDevice.getAddress());
                if (pinString == null || pinString.equals("")) {
                    return;
                }
                byte[] pin = (byte[]) convert.invoke(newDevice, pinString);
                Method setPin = btDeviceInstance.getMethod("setPin", byte[].class);
                boolean success = (Boolean) setPin.invoke(newDevice, pin);
            } catch (Exception e) {
                Log.e(TAG, "Couldn't set pin");
            }
        }
    };

    public BluetoothManager(BackgroundService bs) {
        super(bs);
        isSetup = false;
        mSockets = new TreeMap<String, BluetoothSocket>();
        reset();
        if (mBluetoothAdapter != null)
            mBluetoothAdapter.cancelDiscovery();
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        bs.registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        final String actionPinRequested = "android.bluetooth.device.action.PAIRING_REQUEST";
        IntentFilter intentFilterPinRequested = new IntentFilter(actionPinRequested);
        bs.registerReceiver(mReceiverRequiresPin, intentFilterPinRequested);
    }

    public void onEvent(PhoneConnectEvent e){
        setup();
        if(mAcceptThread == null)
            mAcceptThread = new AcceptThread();
        mAcceptThread.start();
    }

    public void shutdown() {
        super.shutdown();
        service.unregisterReceiver(mReceiver);
        service.unregisterReceiver(mReceiverRequiresPin);
        if(mAcceptThread != null && mAcceptThread.isAlive()) {
            Log.e("HERE","closing studd");
            mAcceptThread.cancel();
            mAcceptThread.interrupt();
            mAcceptThread = null;
        }
    }

    public void reset() {
        super.reset();
        mDevices = new TreeMap<String, BluetoothDevice>();
        mDevicePins = new TreeMap<String, String>();
        for (BluetoothSocket sock : mSockets.values()) {
            try {
                sock.close();
            } catch (IOException e) {
                // TODO(brandyn): Add
            }
        }
        mSockets = new TreeMap<String, BluetoothSocket>();
    }

    private class BluetoothReadTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... addresses) {
            String address = addresses[0];
            String read = READ + address;
            byte data[] = new byte[1];
            // TODO(brandyn): Ensure that these tasks are getting shutdown on reset
            while (true) {
                if (!mSockets.containsKey(address)) {
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        // TODO(brandyn): Handle
                    }
                }
                try {

                    // TODO(brandyn): Bug here if you access with an unpaired address
                    data[0] = (byte)mSockets.get(address).getInputStream().read();
                    makeCall(read, "'" + Base64.encodeToString(data, Base64.NO_WRAP) + "'");
                } catch (IOException e) {
                    Log.w(TAG, "Could not read");
                    closeDevice(address);
                    return null;
                }
            }
        }
    }

    public void closeDevice(String address) {
        try {
            mSockets.get(address).close();
        } catch (IOException e1) {
            Log.w(TAG, "Could not close");
        }
        mSockets.remove(address);
    }

    public void setup() {
        if (isSetup) {
            log();
            return;
        }
        isSetup = true;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        log();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Log.e(TAG, "Bluetooth not available");
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Log.w(TAG, "BT not enabled");
        }
    }

    public void log() {
        if (mBluetoothAdapter != null)
            Log.d(TAG, String.format("Bluetooth: Mode: %d State: %d Discovering: %s Enabled: %s", mBluetoothAdapter.getScanMode(), mBluetoothAdapter.getState(), mBluetoothAdapter.isDiscovering(), mBluetoothAdapter.isEnabled()));
    }

    protected void setupCallback(CallbackRegistration e) {
        super.setupCallback(e);
        setup();
        if (e.getEvent() == LIST) {
            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            // If there are paired devices
            JSONArray devices = new JSONArray();
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    JSONObject deviceJS = new JSONObject();
                    deviceJS.put("name", device.getName());
                    deviceJS.put("address", device.getAddress());
                    devices.add(deviceJS);
                }
            }
            String devicesJS = devices.toJSONString();
            Log.d(TAG, devicesJS);
            makeCall(LIST, "'" + devicesJS + "'");
            unregisterCallback(LIST);
        } else if (e.getEvent() == DISCOVERY_START) {
            mBluetoothAdapter.startDiscovery();
        } else if (e.getEvent().startsWith(READ)) {
            String address = e.getEvent().substring(READ.length());
            if (!mSockets.containsKey(address)) {
                pairWithDevice(address);
            }
            BluetoothReadTask task = new BluetoothReadTask();
            task.execute(address);
        }
    }

    public void onEventAsync(BluetoothModeEvent e) {
        setup();
        if (mBluetoothAdapter == null)
            return;
        if (e.isEnable()) {
            mBluetoothAdapter.enable();
        } else {
            mBluetoothAdapter.disable();
        }
    }

    public BluetoothDevice findBondedDevice(String address) {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                if (device.getAddress().equals(address)) {
                    return device;
                }
            }
        }
        return null;
    }

    public void pairWithDevice(String address) {
        mBluetoothAdapter.cancelDiscovery();
        BluetoothDevice device = findBondedDevice(address);
        if (device == null) {
            Log.e(TAG, "Could find bonded device: " + address);
            return;
        }
        device.fetchUuidsWithSdp();
        if (device.getUuids() == null) {
            Log.e(TAG, "No uuids");
            return;
        }
        // TODO(brandyn): Use intent
        for (ParcelUuid uid: device.getUuids()) {
            Log.d(TAG, "Uuid: " + uid.toString());
        }
        ParcelUuid uuid = device.getUuids()[0];
        Log.d(TAG, "UUID: " + uuid.toString());
        Log.d(TAG, "Trying to connect");
        try {
            mSockets.put(device.getAddress(), device.createInsecureRfcommSocketToServiceRecord(uuid.getUuid()));
        } catch (IOException e2) {
            Log.e(TAG, "Cannot create rfcom");
            return;
        }
        try {
            mSockets.get(device.getAddress()).connect();
        } catch (IOException e3) {
            mSockets.remove(device.getAddress());
            Log.e(TAG, "Cannot connect");
            return;
        }
        Log.d(TAG, "Connected");
    }

    public void onEventAsync(BluetoothBondEvent e) {
        setup();
        BluetoothDevice device = (BluetoothDevice)mDevices.get(e.getAddress());

        if (device == null) {
            Log.w(TAG, "Bond device not found: " + e.getAddress());
            return;
        }
        Log.d(TAG, "Bond device found: " + e.getAddress());
        mBluetoothAdapter.cancelDiscovery();
        mDevicePins.put(e.getAddress(), e.getPin());
        if (device.createBond()) {
            Log.d(TAG, "Creating bond");
        } else {
            Log.w(TAG, "Not creating bond");
        }
    }

    public void onEventAsync(BluetoothWriteEvent e) {
        setup();
        Log.d(TAG, "Addr: " + e.getAddress() + " Buffer: " + new String(e.getBuffer()));
        if (!mSockets.containsKey(e.getAddress()))
            pairWithDevice(e.getAddress());
        if (!mSockets.containsKey(e.getAddress())) {
            Log.e(TAG, "Not paired");
            return;
        }
        Log.d(TAG, "Size:" + mSockets.size());
        try {
            mSockets.get(e.getAddress()).getOutputStream().write(Base64.decode(e.getBuffer(), Base64.NO_WRAP));
        } catch (IOException e1) {
            closeDevice(e.getAddress());
            Log.e(TAG, "Cannot write");
        }
    }

    private class AcceptThread extends Thread {
        private BluetoothServerSocket mmServerSocket;
        private  boolean running = false;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord("phone_connection",UUID.fromString("07f2934c-1e81-4554-bb08-44aa761afbfb") );
                Log.d("BTDEBUG","REGISTERED SERVICE");
            } catch (IOException e) {
                 e.printStackTrace();

            }
            mmServerSocket = tmp;
            running = true;
        }

        public void run() {
            Log.e(TAG,"Running");
            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (running) {

                try {
                    socket = mmServerSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // If a connection was accepted
                if (socket != null) {
                    manageConnectedSocket(socket);
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                running = false;
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }

    private void manageConnectedSocket(BluetoothSocket socket) {
        // start our connection thread
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        boolean reading = true;

    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private boolean reading = true;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            int bytes;

             // Keep listening to the InputStream while connected
            while (reading) {
                try {

                    int availableBytes = mmInStream.available();
                    if(availableBytes > 0) {
                        byte[] buffer = new byte[availableBytes];  // buffer store for the stream
                        bytes = mmInStream.read(buffer);

                        if (bytes > 0) {
                            makeCall(BluetoothManager.DATA, "'" + new String(buffer) + "'");
                            Log.d("MESSAGE", new String(buffer));
                            mmSocket.close();
                        }

                    }

                    //byte[] blah = ("System Time:" +System.currentTimeMillis()).getBytes();
                } catch (Exception e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                }
            }
        }
        public void connectionLost() {
            Log.d("HERE","LOST");

            reading = false;
            cancel();


        }
        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
                connectionLost();
            }
        }

        public void cancel() {
            try {
                Log.d("HERE","Closing thread");
                mmSocket.close();

            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
