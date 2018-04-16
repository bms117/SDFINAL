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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.demo.s3transferutility.R;
import com.amazonaws.http.HttpClient;
import com.amazonaws.http.HttpResponse;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URISyntaxException;
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
import static com.amazonaws.demo.s3transferutility.Constants.FileNameBeacon;
import static com.amazonaws.demo.s3transferutility.Constants.identity;
import static com.amazonaws.demo.s3transferutility.UploadActivity.isDownloadsDocument;
import static com.amazonaws.demo.s3transferutility.UploadActivity.isExternalStorageDocument;
import static com.amazonaws.demo.s3transferutility.UploadActivity.isMediaDocument;


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
    int LEVELglobal1 = -50;
    int LEVELglobal2 = -50;
    int LEVELglobal3 = -50;
    int LEVELglobal4 = -50;
    String Body1 = "-50\n";
    String Body2 = "-50\n";
    String Body3 = "-50\n";
    String Body4 = "-50\n";

    //initial file path
    String filePath = "";
    File file = new File("storage/emulated/0/" + FileName);


    private Util util;
    private TransferUtility transferUtility;

    // timer bool
    boolean timerBool = true; // true = ON, false = OFF

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initUI();
        setTransferUtility();

        file = new File("storage/emulated/0/" + FileName);

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

            View v = null;

            WifiReceiver wr = new WifiReceiver();
            wr.onReceive(myContext, myIntent);

            // Upload
            uploadFileToS3();

            // scan wifi again
            mainWifi.startScan();

            // delay
            handler.postDelayed(task, 6000);

            onResume();
        }
    };

    public void uploadFileToS3(){

        TransferObserver transferObserver = transferUtility.upload(
                Constants.BUCKET_NAME,          /* The bucket to upload to */
                Constants.FileName,/* The key for the uploaded object */
                file      /* The file where the data to upload exists */
        );
    }

    public void setTransferUtility(){
        util = new Util();
        transferUtility = util.getTransferUtility(this);
    }

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

            sb = new StringBuilder();
            wifiList = mainWifi.getScanResults();
            sb.append("\n        Number Of Wifi connections :"+wifiList.size()+"\n\n");

            writeToSDBEACON(identity + "\n");
            writeToSD(identity + "\n");

            for(ScanResult result : wifiList) {

                signalLevel = result.level;
                mSSID = result.SSID;
                mTimestamp = result.timestamp;

                sb.append("SSID  " + mSSID + "\n");
                sb.append("LEVEL  " + signalLevel + "\n");
                sb.append("TIMESTAMP  " + mTimestamp);
                sb.append("\n\n");

                //writes to sd
                textBodyConstructor2(mSSID, signalLevel, mTimestamp);
            }
            writeToSD(Body1+Body2+Body3+Body4);
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

    public void textBodyConstructor(String SSID, int lvl, long ts) {
        String tempSSID = SSID;
        int tempLevel = lvl;
        long tempTimestamp = ts;
        String Body = "";
        String Bodyb;
        switch (mSSID) {
            case "SSNT-01-5G":
                tempLevel = signalLevel;
                Body = tempLevel + "\n";
                writeToSD(Body);
                break;
            case "SSNT-02-5G":
                tempLevel = signalLevel;
                Body = tempLevel + "\n";
                writeToSD(Body);
                break;
            case "SSNT-03-5G":
                tempLevel = signalLevel;
                Body = tempLevel + "\n";
                writeToSD(Body);
                break;
            case "SSNT-04-5G":
                tempLevel = signalLevel;
                Body = tempLevel + "\n";
                writeToSD(Body);
                break;
            case "Beacon":
                //needs to write to it's own text file
                tempLevel = signalLevel;
                tempTimestamp = mTimestamp;
                Body = tempLevel + "\n" + tempTimestamp  + "\n";
                writeToSDBEACON(Body);
                break;
            default:
                // do nothing
                Body = "";
                writeToSD("");
                writeToSDBEACON("");
                break;
        }
    }

    public void textBodyConstructor2(String SSID, int lvl, long ts) {
        String tempSSID = SSID;
        int tempLevel = lvl;
        long tempTimestamp = ts;
        String Body = "";
        String Bodyb;
        switch (mSSID) {
            case "SSNT-01-5G":
                tempLevel = signalLevel;
                Body1 = tempLevel + "\n";
                writeToSD(Body);
                break;
            case "SSNT-02-5G":
                tempLevel = signalLevel;
                Body2 = tempLevel + "\n";
                writeToSD(Body);
                break;
            case "SSNT-03-5G":
                tempLevel = signalLevel;
                Body3 = tempLevel + "\n";
                writeToSD(Body);
                break;
            case "SSNT-04-5G":
                tempLevel = signalLevel;
                Body4 = tempLevel + "\n";
                writeToSD(Body);
                break;
            case "Beacon":
                //needs to write to it's own text file
                tempLevel = signalLevel;
                tempTimestamp = mTimestamp;
                Body = tempLevel + "\n" + tempTimestamp  + "\n";
                writeToSDBEACON(Body);
                break;
            default:
                // do nothing
                Body = "";
                writeToSD("");
                writeToSDBEACON("");
                break;
        }

    }

//    public void buildFileInput(String SSID, int lvl, long ts) {
//        String Body = "";
//        if(SSID == "SSNT-01-5G" || SSID == "SSNT-02-5G" || SSID == "SSNT-03-5G" || SSID == "SSNT-04-5G") {
//            Body = "" +lvl;
//            writeToSD("");
//            writeToSDBEACON("");
//        } else {
//
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

            File file = new File(fileDir, FileName);
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

    public Boolean writeToSDBEACON(String text){

        Boolean write_successful = false;
        File root=null;
        // <span id="IL_AD8" class="IL_AD">check for</span> SDcard
        root = Environment.getExternalStorageDirectory();
        Log.i(TAG,"path.." +root.getAbsolutePath());

        //check sdcard permission
        if (root.canWrite()) {
            File fileDir = new File(root.getAbsolutePath());
            fileDir.mkdirs();

            File file = new File(fileDir, FileNameBeacon);
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

//    public void uploadWithTransferUtility() {
//
//        TransferUtility transferUtility =
//                TransferUtility.builder()
//                        .context(getApplicationContext())
//                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
//                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
//                        .build();
//
//        TransferObserver uploadObserver =
//                transferUtility.upload(
//                        "https://s3.console.aws.amazon.com/s3/" + FileName,
//                        new File(getExternalFilesDir(null) + "/" + FileName));
//
//
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();

            try {
                String path = getPath(uri);
                beginUpload(path);
            } catch (URISyntaxException e) {
                Toast.makeText(this,
                        "Unable to get the file from the given URI.  See error log for details",
                        Toast.LENGTH_LONG).show();
                Log.e(TAG, "Unable to upload file from the given uri", e);
            }
        }
    }

    /*
     * Begins to upload the file specified by the file path.
     */
    private void beginUpload(String filePath) {
        if (filePath == null) {
            Toast.makeText(this, "Could not find the filepath of the selected file",
                    Toast.LENGTH_LONG).show();
            return;
        }
        File file = new File(filePath);
        TransferObserver observer = transferUtility.upload(Constants.BUCKET_NAME, file.getName(),
                file);
        /*
         * Note that usually we set the transfer listener after initializing the
         * transfer. However it isn't required in this sample app. The flow is
         * click upload button -> start an activity for image selection
         * startActivityForResult -> onActivityResult -> beginUpload -> onResume
         * -> set listeners to in progress transfers.
         */
        // observer.setTransferListener(new UploadListener());
    }

    @SuppressLint("NewApi")
    private String getPath(Uri uri) throws URISyntaxException {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[] {
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

}


