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
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.AsyncTask;
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


public class MainActivity extends AppCompatActivity {

    RelativeLayout page1, page2, page_settings, page_products;
    BottomNavigationView mBottomNav;
    RecyclerView page_products_recycler_view;
    TextView customView;
    PosPrinter printer;
    TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        updateApiCredentialsTextView();
        printer = PosPrinter.getInstance(MainActivity.this);
        customView = (TextView)
                LayoutInflater.from(this).inflate(R.layout.actionbar_custom_title_view_centered,
                        null);


        setTitle("Home");
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
                            private ProgressDialog dialog = new ProgressDialog(MainActivity.this);;
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
                                JSONArray json = null;
                                try {
                                    json = new JSONArray(json_response);

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
                                        Toast.makeText(MainActivity.this, "Clicked", Toast.LENGTH_LONG);
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
                                HashMap<String, String> params = new HashMap<String,String>();
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
                Toast.makeText(MainActivity.this,"Printing Last Confirmation", Toast.LENGTH_LONG);
                printer.printLastPrinted(MainActivity.this);
            }
        });

        findViewById(R.id.newTransfer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncTask<String, Void, String> at = new AsyncTask<String, Void, String>() {



                    /** progress dialog to show user that the backup is processing. */
                    private ProgressDialog dialog = new ProgressDialog(MainActivity.this);;
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


                        if (json_response != "404" && json_response != "" && json_response != null)
                        {
                            Intent intent = new Intent(MainActivity.this, TransferProductActivity.class);
                            intent.putExtra("LocationList", json_response);
                            startActivity(intent);
                            Toast.makeText(MainActivity.this, "OK", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Error while accessing DB", Toast.LENGTH_LONG).show();
                        }
                    }

                    protected String doInBackground(final String... args) {
                        HashMap<String, String> params = new HashMap<String,String>();
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
