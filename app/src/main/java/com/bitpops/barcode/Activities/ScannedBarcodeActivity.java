package com.bitpops.barcode.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bitpops.barcode.Helpers.PosPrinter;
import com.bitpops.barcode.R;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.Result;

public class ScannedBarcodeActivity extends AppCompatActivity  implements ZXingScannerView.ResultHandler {

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
        printer = PosPrinter.getInstance(ScannedBarcodeActivity.this);

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
        printer = PosPrinter.getInstance(ScannedBarcodeActivity.this);
    }
    private boolean readerEnabled = false;
    private void initViews() {
        txtBarcodeValue = findViewById(R.id.txtBarcodeValue);
       // surfaceView = findViewById(R.id.surfaceView);
        btnAction = findViewById(R.id.btnAction);
        btnAction.setText("SCAN");


        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (intentData.length() > 0) {

                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(intentData)));

                }
                else
                {
                    //
                    //
                    //

                    readerEnabled = true;
                }


            }
        });

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
        if (!readerEnabled)
        {
            // If you would like to resume scanning, call this method below:
            mScannerView.resumeCameraPreview(ScannedBarcodeActivity.this);
            return;
        }

        // Do something with the result here
        Log.v("log", rawResult.getText()); // Prints scan results
        Log.v("log", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)
        final String text = rawResult.getText();
        btnAction.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){

                    txtBarcodeValue.setText("WAITING FOR SCAN...");
                    btnAction.setVisibility(View.GONE);


                    txtBarcodeValue.post(new Runnable()
                    {

                        @Override
                        public void run() {
                            ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                            toneGen1.startTone(ToneGenerator.TONE_CDMA_ABBR_INTERCEPT,500);

                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(ScannedBarcodeActivity.this, android.R.style.ThemeOverlay_Material_Dark));
                            builder.setTitle("Code Scanned");
                            builder.setMessage(text);
                            builder.setPositiveButton("Go to transfer", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                    // Put the String to pass back into an Intent and close this activity
                                    if ( ScannedBarcodeActivity.this.getIntent().getBooleanExtra("add_one_more", false) == true)
                                    {
                                        Intent intent = new Intent();
                                        intent.putExtra("code_scanned", rawResult.getText());
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    }
                                    else
                                    {
                                        Intent myintent=new Intent(ScannedBarcodeActivity.this, TransferProductActivity.class).putExtra("code_scanned", rawResult.getText());
                                        startActivity(myintent);
                                    }

                                    // printer.printConfirmation(text, information);


                                }
                            });
                            builder.setNegativeButton("Scan Another", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    txtBarcodeValue.setText("");
                                    btnAction.setText("TOUCH HERE TO SCAN");
                                    btnAction.setVisibility(View.VISIBLE);
                                    mScannerView.resumeCameraPreview(ScannedBarcodeActivity.this);

                                }
                            });
                            builder.setIcon(android.R.drawable.ic_dialog_alert);
                            final AlertDialog dialog = builder
                                    .show();
                            //2. now setup to change color of the button
                            dialog.setOnShowListener( new DialogInterface.OnShowListener() {
                                @Override
                                public void onShow(DialogInterface arg0) {
                                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
                                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
                                }
                            });

                        }
                    });
                    return true;
                }


                return false;
            }
        });

    }





}
