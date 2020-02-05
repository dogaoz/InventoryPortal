package com.bitpops.barcode.Activities;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;

import com.bitpops.barcode.Adapters.TransferProductsListAdapter;
import com.bitpops.barcode.Helpers.HttpRequest;
import com.bitpops.barcode.Helpers.PosPrinter;
import com.bitpops.barcode.Helpers.SavedData;
import com.bitpops.barcode.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

import static android.Manifest.permission.GET_ACCOUNTS;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission_group.CAMERA;


public class MainActivity extends AppCompatActivity {

    RelativeLayout page1, page2, page_settings, page_products;
    BottomNavigationView mBottomNav;
    RecyclerView page_products_recycler_view;
    PosPrinter printer;
    TextView tv;
    private static final int REQUEST_GET_ACCOUNT = 112;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), GET_ACCOUNTS);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), CAMERA);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{GET_ACCOUNTS, CAMERA}, REQUEST_GET_ACCOUNT);
        ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0) {

                    boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (locationAccepted && cameraAccepted)
                        Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access location data and camera", Toast.LENGTH_LONG).show();
                    else {
                        Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access location data and camera", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE},
                                                            PERMISSION_REQUEST_CODE);
                                                }
                                            }
                                        });
                                return;
                            }
                        }

                    }
                }

                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        updateApiCredentialsTextView();
        printer = PosPrinter.getInstance(MainActivity.this);


        setTitle("Home");
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "Requesting permission", Toast.LENGTH_LONG).show();

                requestPermission();
            }
        }
        else
        {
            Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        printer = PosPrinter.getInstance(MainActivity.this);
        updateApiCredentialsTextView();
    }

    private void initViews() {

        mBottomNav = findViewById(R.id.mBottomNav);
        page1 = findViewById(R.id.page_qr_code_scanner);
        page_products = findViewById(R.id.page_products);
        page_products_recycler_view = findViewById(R.id.all_products_list);
        page2 = findViewById(R.id.page_2);
        page_settings = findViewById(R.id.page_settings);
        tv = findViewById(R.id.currentAPIandTokenText);
        page1.bringToFront();
        mBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId())
                {
                    case R.id.menu_home:
                        page1.bringToFront();
                        setTitle("Home");
                        break;
                    case R.id.menu_products:
                        page_products.bringToFront();
                        setTitle("All Products");
                        AsyncTask<String, Void, String> at = new AsyncTask<String, Void, String>() {



                            /** progress dialog to show user that the backup is processing. */
                            private ProgressDialog dialog = new ProgressDialog(MainActivity.this);
                            /** application context. */
                            private ListActivity activity;

                            protected void onPreExecute() {
                                this.dialog.setMessage("Refreshing Products List...");
                                this.dialog.show();
                            }

                            @Override
                            protected void onPostExecute(final String json_response) {
                                if (dialog.isShowing()) {
                                    dialog.dismiss();
                                }

                                final ArrayList<String> products = new ArrayList<>();
                                try {
                                    JSONArray json = new JSONArray(json_response);

                                    for (int i = 0; i < json.length(); i++)
                                        TransferProductActivity.addNewItem(json.getString(i),products);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }



                                // set up the RecyclerView
                                page_products_recycler_view.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                                TransferProductsListAdapter adapter = new TransferProductsListAdapter(MainActivity.this, products);
                                adapter.setClickListener(new TransferProductsListAdapter.ItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, int position) {
                                        final String[] information = products.get(position).split("-");
                                        String text_data = "";
                                        for (int i = 0; i < information.length; i++) {
                                            switch (i) {
                                                case 0:
                                                    text_data = text_data.concat("Product Code: ");
                                                    break;
                                                case 1:
                                                    text_data = text_data.concat("Product No: ");
                                                    break;
                                                case 2:
                                                    text_data = text_data.concat("Sq/ft: ");
                                                    break;
                                                case 3:
                                                    text_data = text_data.concat("Dimensions(inches): ");
                                                    break;
                                                case 4:
                                                    text_data = text_data.concat("Product Name: ");
                                                    break;


                                            }
                                            text_data = text_data.concat(information[i] + "\n");
                                        }
                                        Toast.makeText(MainActivity.this, "Clicked", Toast.LENGTH_LONG).show();
                                        //printer.printConfirmation(products.get(position), information);
                                    }

                                });
                                page_products_recycler_view.addItemDecoration(new DividerItemDecoration(page_products_recycler_view.getContext(), DividerItemDecoration.VERTICAL));

                                page_products_recycler_view.setAdapter(adapter);
                                adapter.setClickListener(new TransferProductsListAdapter.ItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, int position) {
                                        final String inventory_id = products.get(position);
                                        Intent intent = new Intent(MainActivity.this, ProductDetail.class);
                                        intent.putExtra("inventory_id", inventory_id);
                                        startActivity(intent);
                                    }

                                });

                            }

                            protected String doInBackground(final String... args) {
                                HashMap<String, String> params = new HashMap<>();
                                params.put("do","SS_getAllProducts");
                                String result = HttpRequest.Request(MainActivity.this, "POST", params);
                                Log.e("tag", result);
                                return result;
                            }
                        };
                        at.execute();
                        break;
                    case R.id.menu_page2:
                        setTitle("Last Actions");
                        page2.bringToFront();
                        break;
                    case R.id.menu_page_settings:
                        setTitle("Settings");
                        page_settings.bringToFront();
                        break;
                    default:
                        break;
                }
                // handle desired action here
                // One possibility of action is to replace the contents above the nav bar
                // return true if you want the item to be displayed as the selected item
                 // Toast.makeText(MainActivity.this,"test",Toast.LENGTH_LONG).show();
                return true;
            }
        });


        findViewById(R.id.printLastConfirmation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"Printing Last Confirmation", Toast.LENGTH_LONG).show();
                printer.printLastPrinted(MainActivity.this);
            }
        });

        findViewById(R.id.newTransfer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncTask<String, Void, String> at = new AsyncTask<String, Void, String>() {



                    /** progress dialog to show user that the backup is processing. */
                    private ProgressDialog dialog = new ProgressDialog(MainActivity.this);
                    /** application context. */
                    private ListActivity activity;

                    protected void onPreExecute() {
                        this.dialog.setMessage("Progress start");
                        this.dialog.show();
                    }

                    @Override
                    protected void onPostExecute(final String json_response) {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }


                        if (!json_response.equals("404") && !json_response.equals("") && json_response != null)
                        {
                            Intent intent = new Intent(MainActivity.this, TransferProductActivity.class);
                            intent.putExtra("LocationList", json_response);
                            startActivity(intent);
                            Toast.makeText(MainActivity.this, "OK", Toast.LENGTH_LONG).show();
                        } else {
                            Intent intent = new Intent(MainActivity.this, TransferProductActivity.class);
                            intent.putExtra("LocationList", json_response);
                            startActivity(intent);
                            Toast.makeText(MainActivity.this, "Error while accessing DB", Toast.LENGTH_LONG).show();
                        }
                    }

                    protected String doInBackground(final String... args) {
                        HashMap<String, String> params = new HashMap<>();
                        params.put("do","SS_getAllLocations");
                        String result = HttpRequest.Request(MainActivity.this, "POST", params);
                        Log.e("tag", result);
                        return result;
                    }
                };
                at.execute();

            }
        });

        findViewById(R.id.updateDeviceTokenQR).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ScanConfiguration.class);
                //intent.putExtra("LocationList", json_response);
                startActivity(intent);
            }
        });
    }


    public void updateApiCredentialsTextView()
    {
        SavedData data = new SavedData(MainActivity.this);
        String result = data.getServerAPIAddress() + " -- " + data.getDeviceAuthToken() + " -- " + data.getDeviceId() + " -- " + data.getCompanyName();
        tv.setText(result);

    }

}
