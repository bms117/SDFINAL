/*
 * Copyright 2015-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazonaws.demo.s3transferutility;

import android.app.Activity;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.amazonaws.demo.s3transferutility.R;
import com.amazonaws.http.HttpClient;
import com.amazonaws.http.HttpResponse;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import static android.content.ContentValues.TAG;
import static com.amazonaws.demo.s3transferutility.Constants.FileName;


/*
 * This is the beginning screen that lets the user select if they want to upload or download
 */
public class MainActivity extends Activity {

    private Button btnDownload;
    private Button btnUpload;

    TextView mainText;
    WifiManager mainWifi;
    WifiReceiver receiverWifi;
    List<ScanResult> wifiList;
    StringBuilder sb = new StringBuilder();

    //Context and Intent
    Context myContext;
    Intent myIntent;

    //Declare timer
    CountDownTimer cTimer = null;

    // wifi info
    int signalLevel;
    String mSSID;
    long mTimestamp;
    // String Body = mSSID + "\n" + signalLevel + "\n" + mTimestamp  + "\n\n";

    // timer bool
    boolean timerBool = true; // true = ON, false = OFF

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        mainText = (TextView) findViewById(R.id.tv1);
        mainWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mainWifi.isWifiEnabled() == false)
        {
            // If wifi disabled then enable it
            Toast.makeText(getApplicationContext(), "wifi is disabled..making it enabled",
                    Toast.LENGTH_LONG).show();

            mainWifi.setWifiEnabled(true);
        }
        receiverWifi = new WifiReceiver();
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        mainWifi.startScan();
        mainText.setText("Starting Scan...");

        // repeats wifi scans
        task.run();




    }

    final Handler handler = new Handler();
    final Runnable task = new Runnable() {
        @Override
        public void run() {

            WifiReceiver wr = new WifiReceiver();
            wr.onReceive(myContext, myIntent);

            mainWifi.startScan();

            handler.postDelayed(task, 6000);
            onPause();
            onResume();
        }
    };

    private void initUI() {
        btnDownload = (Button) findViewById(R.id.buttonDownloadMain);
        btnUpload = (Button) findViewById(R.id.buttonUploadMain);

        btnDownload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(MainActivity.this, DownloadActivity.class);
                startActivity(intent);
            }
        });

        btnUpload.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(MainActivity.this, UploadActivity.class);
                startActivity(intent);
            }
        });
    }

    // gets time stamp
    public String getTimeStamp() {
        long millis = System.currentTimeMillis();

        long days = TimeUnit.MILLISECONDS.toDays(millis);
        millis -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        millis -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

        StringBuilder sb = new StringBuilder(64);
        sb.append(hours);
        sb.append(".");
        sb.append(minutes);
        sb.append(".");
        sb.append(seconds);
        sb.append(".");
        sb.append(millis);

        return (sb.toString());
    }

    //start timer function
    public void startTimer() {
        timerBool = true;
        int duration = generateRandomInt();
        cTimer = new CountDownTimer(duration, 1000) {
            public void onTick(long millisUntilFinished) {
            }
            public void onFinish() {
            }
        };
        cTimer.start();
    }

    // cancel timer to stop memory leaks
    public void cancelTimer() {
        if(cTimer!=null)
            cTimer.cancel();

        timerBool = false;
    }

    // generate random number
    public int generateRandomInt() {
        Random r = new Random();
        int i1 = r.nextInt(1 - 5) + 1; // [1,5]
        int timer = i1 * 1000;
        return timer;
    }

    class WifiReceiver extends BroadcastReceiver {

        // This method call when number of wifi connections changed
        public void onReceive(Context c, Intent intent) {

            UploadActivity ua = new UploadActivity();


            sb = new StringBuilder();
            wifiList = mainWifi.getScanResults();
            sb.append("\n        Number Of Wifi connections :"+wifiList.size()+"\n\n");

            String text;
            for(ScanResult result : wifiList) {

                signalLevel = result.level;
                mSSID = result.SSID;
                mTimestamp = result.timestamp;

                sb.append("SSID  " + mSSID + "\n");
                sb.append("LEVEL  " + signalLevel + "\n");
                sb.append("TIMESTAMP  " + mTimestamp);
                sb.append("\n\n");

                text = textBodyConstructor();
                writeToSD(text);
            }

            mainText.setText(sb);
        }

    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        mainWifi.startScan();
        mainText.setText("Starting Scan");
        return super.onMenuItemSelected(featureId, item);
    }

    protected void onPause() {
        unregisterReceiver(receiverWifi);
        super.onPause();
    }

    protected void onResume() {
        registerReceiver(receiverWifi, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    public String textBodyConstructor() {
        int tempSSID = -1;
        switch (mSSID) {
            case "SSNT-01-5G":
                tempSSID = 01;
                break;
            case "SSNT-02-5G":
                tempSSID = 02;
                break;
            case "SSNT-03-5G":
                tempSSID = 03;
                break;
            case "SSNT-04-5G":
                tempSSID = 04;
                break;
            case "Beacon":
                tempSSID = 00;
            default:
                // do nothing
                break;
        }
        String Body = tempSSID + "\n" + signalLevel + "\n" + mTimestamp  + "\n\n";
        return Body;
    }

//    public void Write() {
//        // add-write text into file
//        try {
//            FileOutputStream fileout = openFileOutput(FileName, MODE_PRIVATE);
//            OutputStreamWriter outputWriter = new OutputStreamWriter(fileout);
//            outputWriter.write("this is a test");
//            outputWriter.close();
//
//            //display file saved message
//            Toast.makeText(getBaseContext(), "File saved successfully!",
//                    Toast.LENGTH_SHORT).show();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public Boolean writeToSD(String text){

        Boolean write_successful = false;
        File root=null;
        // <span id="IL_AD8" class="IL_AD">check for</span> SDcard
        root = Environment.getExternalStorageDirectory();
        Log.i(TAG,"path.." +root.getAbsolutePath());

        //check sdcard permission
        if (root.canWrite()) {
            File fileDir = new File(root.getAbsolutePath());
            fileDir.mkdirs();

            File file = new File(fileDir, "TEST5.txt");
            try {
                FileOutputStream fileinput = new FileOutputStream(file, true);
                PrintStream printstream = new PrintStream(fileinput);
                printstream.print(text);
                fileinput.close();

            }
            catch (Exception e) {
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
        return write_successful;
    }

}


