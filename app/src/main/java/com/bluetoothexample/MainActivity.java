package com.bluetoothexample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button buttonOn, buttonScan, buttonOff;
    private ListView listView;
    private final String TAG = "MainActivity";

    //BT Adapter
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> arrayAdapter;

    //Manejadores y conexiones
    private Handler handler;
    private ConnectedThread connectedThread;
    private BluetoothSocket bluetoothSocket;

    //Constants
    private final static int REQUEST_ENABLE_BLUETOOTH = 1;
    private final static int MESSAGE_READ = 2;
    private final static int CONNECTION_STATUS = 3;


    public class ConnectedThread{}


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonOn = findViewById(R.id.powerOneButton);
        buttonOn.setEnabled(false);
        buttonOff = findViewById(R.id.powerOffeButton);
        buttonOff.setEnabled(false);
        buttonScan = findViewById(R.id.scanButton);
        buttonScan.setEnabled(false);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView = findViewById(R.id.listBluetooth);
        listView.setAdapter(arrayAdapter);

        requestPermission();
        initHandler();
        validateBluetoothDevice();

        initActionButtons();
    }

    private void validateBluetoothDevice() {
        if (bluetoothAdapter == null){
            Toast.makeText(this, R.string.bt_no_supported, Toast.LENGTH_LONG).show();
        }else {
            buttonOn.setEnabled(true);
            buttonScan.setEnabled(true);
            buttonOff.setEnabled(true);
        }
    }


    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION},1);
        }
    }

    @SuppressLint("HandlerLeak")
    private void initHandler() {
        handler = new Handler() {
            public void handleMessage(Message msg){
                if (msg.what == MESSAGE_READ){
                    String messageRead = "";
                    try {
                        messageRead = new String((byte[])msg.obj,"UTF-8");
                    } catch (Exception e) {
                        Log.e(TAG, e.getMessage());
                    }
                    Log.i(TAG,messageRead);
                }
                if (msg.what == CONNECTION_STATUS){
                    if (msg.arg1 == 1){
                        Log.i(TAG,getString(R.string.bt_connected)+msg.obj);
                    }else {
                        Log.i(TAG,getString(R.string.bt_connection_fail));
                    }
                }
            }
        };
    }

    public void initActionButtons(){
        buttonOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!bluetoothAdapter.isEnabled()){
                    Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBTIntent,REQUEST_ENABLE_BLUETOOTH);
                    Toast.makeText(MainActivity.this, R.string.bt_on, Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(MainActivity.this, R.string.bt_already_on, Toast.LENGTH_LONG).show();
                }
            }
        });
        //buttonOff.setEnabled(false);
        buttonOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothAdapter.disable();
                Toast.makeText(MainActivity.this, R.string.bt_off, Toast.LENGTH_LONG).show();
            }
        });
        //buttonScan.setEnabled(false);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bluetoothAdapter.isEnabled()){
                    arrayAdapter.clear();
                    bluetoothAdapter.startDiscovery();
                    Toast.makeText(MainActivity.this, R.string.bt_started, Toast.LENGTH_LONG).show();
                    registerReceiver(btReceiver,new IntentFilter(BluetoothDevice.ACTION_FOUND));
                }else{
                    Toast.makeText(MainActivity.this, R.string.bt_off, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    final BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //add device to adapter
                arrayAdapter.add(device.getName()+"\n"+device.getAddress());
                arrayAdapter.notifyDataSetChanged();
            }
        }
    };

    /*public void buttonOn(){
    }
    public void buttonOff(){}
    public void buttonScan(){}*/
}
