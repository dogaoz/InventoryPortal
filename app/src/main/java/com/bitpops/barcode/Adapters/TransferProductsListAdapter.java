package com.bitpops.barcode.Adapters;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bitpops.barcode.Helpers.HttpRequest;
import com.bitpops.barcode.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class TransferProductsListAdapter extends RecyclerView.Adapter<TransferProductsListAdapter.ViewHolder> {

    private List<String> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    public TransferProductsListAdapter(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final String product_id = mData.get(position);
        holder.myTextView.setText("ID: " + product_id);
        holder.myTextViewID.setText(Integer.toString((position+1)));
        AsyncTask<String, Void, String> at = new AsyncTask<String, Void, String>() {




            protected void onPreExecute() {

                holder.productNameTextView.setText("Loading...");
                holder.productLocationTextView.setText("Loading...");
                holder.productStatusTextView.setText("Loading...");
            }

            @Override
            protected void onPostExecute(final String json_response) {

                // Parse JSON

                String image_thumb_url = "";
                String product_name = "N/A";
                String product_location = "N/A";
                String product_status = "N/A";
                Log.e("JSON Response", json_response);
                try
                {
                    JSONArray json = new JSONArray(json_response);
                    JSONObject product = json.getJSONObject(0);
                    product_name = "Name: " +product.getString("Name");
                    product_location = "Location: " + product.getString("Warehouse Location");
                    product_status = "Status: " + product.getString("Status");
                    image_thumb_url = product.getString("thumb_image_url");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                holder.productNameTextView.setText(product_name);
                holder.productLocationTextView.setText(product_location);
                holder.productStatusTextView.setText(product_status);
                RequestOptions options = new RequestOptions()
                        .centerCrop()
                        .placeholder(R.drawable.ic_offline_bolt_24px)
                        .error(R.mipmap.ic_launcher_round);
                Glide.with(mInflater.getContext()).load(image_thumb_url).apply(options).into(holder.slab_image_view);
            }

            protected String doInBackground(final String... args) {
                HashMap<String, String> params = new HashMap<String,String>();
                params.put("do","SS_getSingleProduct");
                params.put("inventoryId",product_id);
                String result = HttpRequest.Request(mInflater.getContext(), "POST", params);
                Log.e("BARDO", result);
                return result;
            }
        };
        at.execute();

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView, myTextViewID, productNameTextView, productLocationTextView, productStatusTextView;
        ImageView slab_image_view;

        ViewHolder(View itemView) {
            super(itemView);
            myTextView = itemView.findViewById(R.id.textViewProductID);
            myTextViewID = itemView.findViewById(R.id.textViewID);
            productNameTextView = itemView.findViewById(R.id.textViewProductName);
            productLocationTextView = itemView.findViewById(R.id.textViewCurrentLocation);
            productStatusTextView = itemView.findViewById(R.id.textViewStatus);
            slab_image_view = itemView.findViewById(R.id.slabImageView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(TransferProductsListAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}