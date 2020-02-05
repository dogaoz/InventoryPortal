package com.bitpops.barcode.Activities;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.bitpops.barcode.Adapters.TransferProductsListAdapter;
import com.bitpops.barcode.Helpers.HttpRequest;
import com.bitpops.barcode.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ProductDetail extends AppCompatActivity {

    TextView tv;
    ImageView iv;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        tv = findViewById(R.id.textViewProductInfo);
        iv = findViewById(R.id.imageViewProductImage);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        final String inventory_id = getIntent().getStringExtra("inventory_id");

        AsyncTask<String, Void, String> at = new AsyncTask<String, Void, String>() {



            /** progress dialog to show user that the backup is processing. */
            private ProgressDialog dialog = new ProgressDialog(ProductDetail.this);;
            /** application context. */
            private ListActivity activity;

            protected void onPreExecute() {
                this.dialog.setMessage("Retrieving Product Data...");
                this.dialog.show();
            }

            @Override
            protected void onPostExecute(final String json_response) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }

                Log.e("JSON Response", json_response);
                String result = "";
                String image_url = "";
                try
                {
                    JSONArray json = new JSONArray(json_response);
                    JSONObject json2 = json.getJSONObject(0);
                    for (Iterator<String> it = json2.keys(); it.hasNext(); ) {
                        String key = it.next();
                        String value = json2.get(key).toString();
                        if (key.equals("image_url_partial_res"))
                        {
                            image_url = value.replaceAll("\\s","");

                            Log.e("BARDO", image_url);
                        }
                        String prop = "<b>" + key + "</b>: " + value + "<br>";
                        result += prop;
                        Log.e("BARDO", prop);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.e("BARDO", image_url);
                tv.setText(Html.fromHtml(result));

                RequestOptions options = new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.ic_offline_bolt_24px)
                        .error(R.mipmap.ic_launcher_round);


                Glide.with(ProductDetail.this).load(image_url).apply(options).into(iv);



            }

            protected String doInBackground(final String... args) {
                HashMap<String, String> params = new HashMap<String,String>();
                params.put("do","SS_getSingleProduct");
                params.put("inventoryId",inventory_id);
                String result = HttpRequest.Request(ProductDetail.this, "POST", params);
                Log.e("BARDO", result);
                return result;
            }
        };
        at.execute();
    }

}
