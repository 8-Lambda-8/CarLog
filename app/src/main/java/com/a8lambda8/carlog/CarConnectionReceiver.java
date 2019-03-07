package com.a8lambda8.carlog;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import static com.a8lambda8.carlog.MainActivity.TAG;

/**
 * Created by jwasl on 07.03.2019.
 */
public class CarConnectionReceiver extends BroadcastReceiver {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        Log.d(TAG, "\n"+device.getAddress()+"\n"+device.getName()+"\n");

        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            //Device found
            Log.d(TAG, "Found");
        }else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            //Device is now connected
            Log.d(TAG, "Connected");
        }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
            //Done searching#
            Log.d(TAG, "Finished");
        }else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) {
            //Device is about to disconnect
            Log.d(TAG, "Requested");
        }else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            //Device has disconnected
            Log.d(TAG, "Disconnected");
        }

        if(device.getName().equals("JAKOB-LAPTOP")||device.getName().equals("SEAT BT Jakob")){
            Intent main = new Intent(context, MainActivity.class);
            main.putExtra("fromReceiver",true);
            context.startActivity(main);
        }
    }
}
