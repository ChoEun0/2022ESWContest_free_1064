package com.example.was;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class bt extends AppCompatActivity {

    TextView btStatus;

    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> mPairedDevices;
    List<String> mListPairedDevices;

    Handler mBluetoothHandler;
    //ConnectedBluetoothThread mThreadConnectedBluetooth;
    BluetoothDevice mBluetoothDevice;
    BluetoothSocket mBluetoothSocket;

    private BluetoothSPP bt;


    public static final String channel1ID = "channel1ID";
    public static final String channel1Name = "channel1";

    public static final String[] btName1 = new String[10];
    public static byte[] btName2 = new byte[30];


    private NotificationManager manager;


    final static int BT_REQUEST_ENABLE = 1;
    final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;
    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt);

        btStatus = (TextView)findViewById(R.id.btStatus);
        Button btnBtOn = (Button)findViewById(R.id.btnBtOn);
        Button btnBtOff = (Button)findViewById(R.id.btnBtOff);
        Button btConnect = (Button)findViewById(R.id.btConnect);
        //TextView btName = (TextView)findViewById(R.id.btName);



        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        ImageButton home = (ImageButton) findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "????????? ??????", Toast.LENGTH_SHORT).show();
                Intent intent_home = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent_home);
            }
        });

        bt = new BluetoothSPP(this); //Initializing

        if (!bt.isBluetoothAvailable()) { //???????????? ?????? ??????   cencer
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        // ------------------------------ ????????? ????????? ----------------------------- //
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() { //????????? ??????
            TextView temp = findViewById(R.id.temp);
            //TextView humd = findViewById(R.id.humd);

            public void onDataReceived(byte[] data, String message) { //????????? ????????????

                String[] array = message.split(",",2);
                temp.setText(array[0].concat("??"));

                double dTemp = Double.parseDouble(array[0]);
                //temp.setText(Integer.parseInt(array[0]));
                //humd.setText(array[1].concat("%") );
                //dTemp = Integer.parseInt(array[0]);

                Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                if(dTemp<0 ){
                    //vibrator.vibrate(1000);
                    showNoti();
                    //temp.setText("!!!");
                }
            }

        });


        // ------------------------------ ????????? ????????? ----------------------------- //

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() { //???????????? ???
            public void onDeviceConnected(String btConnectName, String address) {

                Toast.makeText(getApplicationContext()
                        , "Connected to " + btConnectName + "\n" + address
                        , Toast.LENGTH_SHORT).show();
                //btName.setText(btConnectName);
                btName1[0] = btConnectName;
                //btName.setText(btName1[0]);
                btStatus.setText("????????? " + btName1[0]);

                //byte[] btName2 = new byte[30];
                btName2 = btConnectName.getBytes();


            }

            public void onDeviceDisconnected() { //????????????
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() { //????????????
                Toast.makeText(getApplicationContext()
                        , "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });

        btConnect.setOnClickListener(new View.OnClickListener() { //cencer
            public void onClick(View v) {
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();     // ??? ??? ??? ???????????? ???????????????  -> notification??? ?????????
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });


        //bt ??????

        btnBtOn.setOnClickListener(new Button.OnClickListener() { //bt
            @Override
            public void onClick(View view) {

                bluetoothOn();

                //btStatus.setText("?????????" + btName1[0]);
                //btStatus.setText("????????? " + btName2);
            }

        });

        btnBtOff.setOnClickListener(new Button.OnClickListener() { //bt
            @Override
            public void onClick(View view) {
                bluetoothOff();
            }
        });

        /*btConnect.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                listPairedDevices();
            }
        });*/

    }






    void bluetoothOn() { //bt
        if(mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "??????????????? ???????????? ?????? ???????????????.", Toast.LENGTH_LONG).show();
        }
        else {
            if (mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "??????????????? ?????? ????????? ?????? ????????????.", Toast.LENGTH_LONG).show();
                btStatus.setText("?????????");
                //btName.setText(btConnectName.getName());
            }
            else {
                //Toast.makeText(getApplicationContext(), "??????????????? ????????? ?????? ?????? ????????????.", Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "??????????????? ????????? ???????????????.", Toast.LENGTH_LONG).show();
                btStatus.setText("?????????");
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
            }
        }
    }

    void bluetoothOff() { //bt
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            Toast.makeText(getApplicationContext(), "??????????????? ???????????? ???????????????.", Toast.LENGTH_SHORT).show();
            btStatus.setText("????????????");
        }
        else {
            Toast.makeText(getApplicationContext(), "??????????????? ?????? ???????????? ?????? ????????????.", Toast.LENGTH_SHORT).show();
        }
    }





    public void onDestroy() {  //cencer
        super.onDestroy();
        bt.stopService(); //???????????? ??????
    }

    public void onStart() { //cencer
        super.onStart();
        if (!bt.isBluetoothEnabled()) { //
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID??? ??????????????? ?????? ??????
                //setup();
            }
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {   //cencer
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
                //setup();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    /*void listPairedDevices() {
        if (mBluetoothAdapter.isEnabled()) {
            mPairedDevices = mBluetoothAdapter.getBondedDevices();

            if (mPairedDevices.size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("?????? ??????");

                mListPairedDevices = new ArrayList<String>();
                for (BluetoothDevice device : mPairedDevices) {
                    mListPairedDevices.add(device.getName());
                    //mListPairedDevices.add(device.getName() + "\n" + device.getAddress());
                }
                final CharSequence[] items = mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);
                mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        connectSelectedDevice(items[item].toString());
                    }
                });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                Toast.makeText(getApplicationContext(), "???????????? ????????? ????????????.", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "??????????????? ???????????? ?????? ????????????.", Toast.LENGTH_SHORT).show();
        }
    }

    void connectSelectedDevice(String selectedDeviceName) {
        for(BluetoothDevice tempDevice : mPairedDevices) {
            if (selectedDeviceName.equals(tempDevice.getName())) {
                mBluetoothDevice = tempDevice;
                break;
            }
        }
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
            mBluetoothSocket.connect();
            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
            mThreadConnectedBluetooth.start();
            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "???????????? ?????? ??? ????????? ??????????????????.", Toast.LENGTH_LONG).show();
        }
    }

    private class ConnectedBluetoothThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedBluetoothThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "?????? ?????? ??? ????????? ??????????????????.", Toast.LENGTH_LONG).show();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(100);
                        bytes = mmInStream.available();
                        bytes = mmInStream.read(buffer, 0, bytes);
                        mBluetoothHandler.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }
        public void write(String str) {
            byte[] bytes = str.getBytes();
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "????????? ?????? ??? ????????? ??????????????????.", Toast.LENGTH_LONG).show();
            }
        }
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "?????? ?????? ??? ????????? ??????????????????.", Toast.LENGTH_LONG).show();
            }
        }
    }*/



    private void showNoti(){  //cencer   notification

        NotificationCompat.Builder builder = getNotificationBuilder("cheannel1","??? ?????? ??????");
        //Ticker ????????? ?????? ?????? ???????????? ?????? ??????
        builder.setTicker("Ticker ?????????");
        //?????? ????????? ??????.
        builder.setSmallIcon(android.R.drawable.ic_menu_search);
        //??? ????????? ??????
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
        builder.setLargeIcon(bitmap);
        // ?????? ??????
        builder.setNumber(100);
        //????????? ??????
        builder.setContentTitle("warning");
        //?????? ??????
        builder.setContentText("???????????? ???????????? ???????????????.");

        //????????? ????????? ??????
        Notification notification = builder.build();

        //?????? ????????? ?????? ????????? ????????????.
        NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        //?????? ???????????? ????????????.
        manager.notify(10,notification);


    }

    //??????????????? 8.0 ????????? ???????????? ?????? Notfication.Builder ?????? ?????????  cencer
    public NotificationCompat.Builder getNotificationBuilder(String id, String name){
        NotificationCompat.Builder builder = null;

        //os???????????? ????????????.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //?????? ???????????? ???????????? ????????? ????????????.
            NotificationManager manager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            //?????? ????????? ????????????.
            NotificationChannel channel = new NotificationChannel(id,name,NotificationManager.IMPORTANCE_HIGH);
            //??????????????? ????????? ?????? ?????? ?????????....
            //????????? ????????? ????????? led??? ????????? ?????????...
            channel.enableLights(true);
            //led????????? ??????.
            channel.setLightColor(Color.RED);
            //?????? ?????? ??????
            channel.enableVibration(true);
            //?????? ???????????? ???????????? ????????? ????????? ????????????.
            manager.createNotificationChannel(channel);
            //????????? ????????? ?????? ????????? ????????????.
            builder = new NotificationCompat.Builder(this,id);
        }
        else{
            builder = new NotificationCompat.Builder(this);
        }

        return builder;
    }


}
