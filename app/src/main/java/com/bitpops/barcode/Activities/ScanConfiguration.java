package com.bitpops.barcode.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bitpops.barcode.Helpers.PosPrinter;
import com.bitpops.barcode.Helpers.SavedData;
import com.bitpops.barcode.R;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.Result;

import androidx.appcompat.app.AppCompatActivity;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanConfiguration extends AppCompatActivity  implements ZXingScannerView.ResultHandler {

    private ZXingScannerView mScannerView;   // Programmatically initialize the scanner view
    SurfaceView surfaceView;
    TextView txtBarcodeValue;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    Button btnAction;
    String intentData = "";
    boolean isEmail = false;
    boolean isScanned = false;
    PosPrinter printer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode);
        printer = PosPrinter.getInstance(ScanConfiguration.this);

        initViews();
    }
    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();
        printer = PosPrinter.getInstance(ScanConfiguration.this);
    }
    private boolean readerEnabled = false;
    private void initViews() {
        txtBarcodeValue = findViewById(R.id.txtBarcodeValue);
       // surfaceView = findViewById(R.id.surfaceView);
        btnAction = findViewById(R.id.btnAction);
        btnAction.setText("SCAN");
        btnAction.setVisibility(View.GONE);


        mScannerView = (ZXingScannerView) findViewById(R.id.surfaceView);
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
       // mScannerView.setFlash(true);
    }
    // Called when a QR is decoded
    // "text" : the text encoded in QR
    // "points" : points where QR control points are placed in View
    @Override
    public void handleResult(final Result rawResult) {

        // Do something with the result here
        Log.v("log", rawResult.getText()); // Prints scan results
        Log.v("log", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        String result = rawResult.getText();

        String[] config = result.split("#SPRTR#");
        if (config.length == 4)
        {
            SavedData sd = new SavedData(ScanConfiguration.this);
            sd.setServerAPIAddress(config[0]);
            sd.setDeviceAuthToken(config[1]);
            sd.setDeviceId(config[2]);
            sd.setCompanyName(config[3]);
            finish();
        }
        else
        {
            Toast.makeText(ScanConfiguration.this,"Error while updating configuration!", Toast.LENGTH_LONG);
        }

    }





}
