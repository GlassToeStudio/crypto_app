package com.glasstoestudio.crypto;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity {

    Button getPriceButton;
    //ImageButton cryptoCompareAPIButton;
    TextView outPutTextView;
    EditText fsymText;
    EditText tsymText;
    ImageView coinImage;
    AutoCompleteTextView acTextView;

    //https://www.cryptocompare.com/api/data/coinsnapshotfullbyid/?id=1182 for BTC, 7605 for ETH
    private static final String COIN_LIST_URL = "https://www.cryptocompare.com/api/data/coinlist/";
    String url_string = "https://min-api.cryptocompare.com/data/price?fsym=";
    String full_id_url = "https://www.cryptocompare.com/api/data/coinsnapshotfullbyid/?id=";
    String full_id_url_complete = "";
    String coinImage_url = "https://www.cryptocompare.com";
    String fsym = "BTC";
    String tsym = "USD";
    String complete_url = "";
    String coinID = "";
    JSONObject r_object = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Resources res = getResources();
        String[] coin_symbols = res.getStringArray(R.array.symbols_array);
        acTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this,android.R.layout.simple_dropdown_item_1line,coin_symbols);
        acTextView.setAdapter(adapter);
        acTextView.setThreshold(1);
        acTextView.setDropDownWidth(300);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        outPutTextView = (TextView)findViewById(R.id.priceText);
        getPriceButton = (Button)findViewById(R.id.getPrice);
        fsymText = (EditText)findViewById(R.id.fsym);
        tsymText = (EditText)findViewById(R.id.tsym);
        coinImage = (ImageView)findViewById(R.id.coinImage);
        //cryptoCompareAPIButton = (ImageButton)findViewById(R.id.cryptoCompareButton);

        GetCoinList(new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                Log.d("GlassToeStudio", "onSuccess: ");
                try {
                    r_object = result.getJSONObject("Data");
                    Log.d("GlassToeStudio", r_object.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        /*
        cryptoCompareAPIButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("https://www.cryptocompare.com/api");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
*/
        getPriceButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)  {
                Log.d("GlassToeStudio", "Clicked");
                fsym = fsymText.getText().toString();
                tsym = tsymText.getText().toString();

                if(fsym.matches("")){
                    fsym = "BTC";
                }
                if(tsym.matches("")){
                    tsym = "USD";
                }

                if(fsym.matches("BTC")){
                    coinID = "1182";
                }else{
                    coinID = "7605";
                }
                //GetCoinBySymbol();
                GetCoinById(fsym);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void GetCoinBySymbol(String fromSymbol) {
        complete_url = url_string + fromSymbol + "&tsyms=" + tsym;
        JsonObjectRequest jsonObjectRequest1 = new JsonObjectRequest(Request.Method.POST, complete_url,null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response){
                        Toast.makeText(MainActivity.this, response.toString(), Toast.LENGTH_LONG).show();
                        Log.d("GlassToeStudio", response.toString());
                        try {
                            outPutTextView.setText(response.getString(tsym));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //Toast.makeText(MainActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                outPutTextView.setText("Something Went wrong");
                error.printStackTrace();
            }
        });
        MySingleton.getInstance(MainActivity.this).addToRequestQueue(jsonObjectRequest1);
    }

    private void GetCoinList(final VolleyCallback callback) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, COIN_LIST_URL,null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response){
                        callback.onSuccess(response);
                    }
                }
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                outPutTextView.setText("Something Went wrong");
                error.printStackTrace();
            }
        });
        MySingleton.getInstance(MainActivity.this).addToRequestQueue(jsonObjectRequest);
    }
    public interface VolleyCallback{
        void onSuccess(JSONObject result);
    }

    private void GetCoinById(final String symbol) {
        String id = "";
        try{
            id = r_object.getJSONObject(symbol).getString("id");
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        if(fsym.matches("")){
            coinID = "1182";
        }
        full_id_url_complete = full_id_url + coinID;
        JsonObjectRequest jsonObjectRequest2 = new JsonObjectRequest(Request.Method.POST, full_id_url_complete,null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response){
                        //Toast.makeText(MainActivity.this, response.toString(), Toast.LENGTH_SHORT).show();
                        Log.d("GlassToeStudio", response.toString());
                        try {
                            //coinImage_url += response.getString("ImageUrl");
                            coinImage_url = "https://www.cryptocompare.com";
                            coinImage_url += r_object.getJSONObject(symbol).getString("ImageUrl");
                            Log.d("GlassToeStudio", coinImage_url);
                            //outPutTextView.setText(response.getString(tsym));
                            GetImage();
                            GetCoinBySymbol(fsym);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                , new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
                outPutTextView.setText("Something Went wrong");
                error.printStackTrace();
            }
        });
        MySingleton.getInstance(MainActivity.this).addToRequestQueue(jsonObjectRequest2);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Loads an image based on teh given coin (fsym)
    private void GetImage(){
        // Retrieves an image specified by the URL, displays it in the UI.
        ImageRequest imageRequest = new ImageRequest(coinImage_url,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        coinImage.setImageBitmap(bitmap);
                    }
                }, 150, 150, Bitmap.Config.ARGB_8888,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        coinImage.setImageResource(R.drawable.ic_launcher_background);
                    }
                });
        MySingleton.getInstance(MainActivity.this).addToRequestQueue(imageRequest);
    }
}
