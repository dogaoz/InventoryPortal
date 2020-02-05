package com.bitpops.barcode.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.bitpops.barcode.Adapters.TransferProductsListAdapter;
import com.bitpops.barcode.Helpers.HttpRequest;
import com.bitpops.barcode.Helpers.PosPrinter;
import com.bitpops.barcode.Model.Action;
import com.bitpops.barcode.Model.Product;
import com.bitpops.barcode.Model.Transfer;
import com.bitpops.barcode.R;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

public class TransferProductActivity extends AppCompatActivity {

    RecyclerView product_codes_view;
    PosPrinter printer;
    Button add_new_button;
    Button location_selector_button;
    ArrayList<String> products;
    TransferProductsListAdapter adapter;
    ExtendedFloatingActionButton fab;
    private static final int SECOND_ACTIVITY_REQUEST_CODE = 0;

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        printer = PosPrinter.getInstance(TransferProductActivity.this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_product);
        product_codes_view = findViewById(R.id.added_products_list);
        printer = PosPrinter.getInstance(TransferProductActivity.this);
        add_new_button = findViewById(R.id.scan_add_new);
        location_selector_button = findViewById(R.id.button_location_selector);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        this.setTitle("Choose Products to Transfer");

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                AsyncTask<String, Void, String> at = new AsyncTask<String, Void, String>() {




                    protected void onPreExecute() {
                        Snackbar.make(view, "Please wait...", Snackbar.LENGTH_LONG)
                                .show();
                    }

                    @Override
                    protected void onPostExecute(final String json_response) {

                        if (!json_response.equals("Success"))
                        {
                            Action action = new Action();

                            action.setActionType("Transfer");
                            action.setProductStatuses("Received");
                            action.setLocationTo(location_selector_button.getText().toString());
                            ArrayList<Product> p = new ArrayList<Product>();
                            for (int i = 0; i < products.size(); i++)
                            {
                                Product pr = new Product();
                                pr.addProductProperty("InventoryID", products.get(i));
                                p.add(pr);
                            }
                            action.setProducts(p);
                            printer.printConfirmation(action);
                            Snackbar.make(view, "Transfer completed.", Snackbar.LENGTH_LONG)
                                    .show();
                            // Build an AlertDialog
                            AlertDialog.Builder builder = new AlertDialog.Builder(TransferProductActivity.this);
                            builder.setTitle("Transfer Completed!");
                            builder.setMessage("Action ID: -99");
                            // Set the alert dialog yes button click listener
                            builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                   TransferProductActivity.this.finish();
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.setCanceledOnTouchOutside(false);
                            // Display the alert dialog on interface
                            dialog.show();

                        }
                        else
                        {
                            Snackbar.make(view, "Transfer error:" + json_response, Snackbar.LENGTH_LONG)
                                    .setAction("Action", null)
                                    .show();
                        }



                    }

                    protected String doInBackground(final String... args) {
                        String json = new Gson().toJson(products);
                        HashMap<String, String> params = new HashMap<String,String>();
                        params.put("do","SS_transfer_products");
                        params.put("locationTo", location_selector_button.getText().toString());
                        params.put("json", json);
                        String result = HttpRequest.Request(TransferProductActivity.this, "POST", params);
                        Log.e("BARDO", result);
                        return result;
                    }
                };
                at.execute();



            }
        });



        String s = getIntent().getStringExtra("code_scanned");
        // data to populate the RecyclerView with
        products = new ArrayList<>();
        addNewItem(s,products);

        // set up the RecyclerView
        product_codes_view.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransferProductsListAdapter(this, products);
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
                Toast.makeText(TransferProductActivity.this, "Clicked", Toast.LENGTH_LONG);

            }

        });
        product_codes_view.addItemDecoration(new DividerItemDecoration(product_codes_view.getContext(), DividerItemDecoration.VERTICAL));

        product_codes_view.setAdapter(adapter);

        add_new_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the SecondActivity

                Intent intent = new Intent(TransferProductActivity.this, ScannedBarcodeActivity.class).putExtra("add_one_more", true);;
                startActivityForResult(intent, SECOND_ACTIVITY_REQUEST_CODE);
            }
        });

        location_selector_button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(TransferProductActivity.this, view);

//                for (Element e: elementsList) {
//
//                    popup.getMenu().add(e.id).setTitle(e.title);
//                }
                String data = getIntent().getStringExtra("LocationList");
                if (data != null)
                {
                    try {
                        JSONArray json = new JSONArray(data);
                        for (int i = 0; i < json.length(); i++)
                        {
                            popup.getMenu().add(json.getJSONObject(i).getString("Choice")).setTitle(json.getJSONObject(i).getString("Choice"));
                        }

                    } catch (JSONException e) {
                        popup.getMenu().add("a").setTitle("Error");
                    }
                }
                else
                {
                    popup.getMenu().add("a").setTitle("Error");
                }

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

                    public boolean onMenuItemClick(MenuItem item) {
                        location_selector_button.setText(item.getTitle());
                        return true;
                    }
                });

                popup.show();
            }
        });
    }

    public static void addNewItem(String s, ArrayList<String> products)
    {
        if (s != null) {
            final String[] str = s.split("-");
            if (str.length > 1)
                products.add(str[0] + '-' + str[1]);
            else
                products.add(str[0]);
        }
    }
    // This method is called when the second activity finishes
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check that it is the SecondActivity with an OK result
        if (requestCode == SECOND_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                // Get String data from Intent
                String returnString = data.getStringExtra("code_scanned");

                addNewItem(returnString,products);
                adapter.notifyDataSetChanged();
                Snackbar.make(fab, "Reading product information...", Snackbar.LENGTH_LONG);
                        //.setAction("Action", null).show();
            }
        }
    }

}
