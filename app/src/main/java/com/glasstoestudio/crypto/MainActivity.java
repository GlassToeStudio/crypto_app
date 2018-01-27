package com.glasstoestudio.crypto;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
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

    private static final String COIN_LIST_URL = "https://www.cryptocompare.com/api/data/coinlist/";

    Button getPriceButton;
    TextView outPutTextView;
    EditText fsymText;
    EditText tsymText;
    ImageView coinImage;
    AutoCompleteTextView acTextView;

    // Slide bar thing
    ListView mDrawerList;
    ArrayAdapter<String> mAdapter;

    String complete_url = "";
    JSONObject allCoinData = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Not my shit
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Test the autoScrollText
        Resources res = getResources();
        String[] coin_symbols = res.getStringArray(R.array.symbols_array);
        acTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this,android.R.layout.simple_dropdown_item_1line,coin_symbols);
        acTextView.setAdapter(adapter);
        acTextView.setThreshold(1);
        acTextView.setDropDownWidth(300);

        // Actually my stuff
        outPutTextView = (TextView)findViewById(R.id.priceText);
        getPriceButton = (Button)findViewById(R.id.getPrice);
        fsymText = (EditText)findViewById(R.id.fsym);
        tsymText = (EditText)findViewById(R.id.tsym);
        coinImage = (ImageView)findViewById(R.id.coinImage);

        //nav bar
        mDrawerList = (ListView)findViewById(R.id.navList);

        addDrawerItems();

        // Immediately populate the coins list.
        getCoinList(new VolleyCallback() {
            @Override
            public void onSuccess(JSONObject result) {
                try {
                    allCoinData = result.getJSONObject("Data");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        // The button click.
        getPriceButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)  {
                //Toast.makeText(MainActivity.this, "Refreshing", Toast.LENGTH_LONG).show();
                String fsym = fsymText.getText().toString();
                String tsym = tsymText.getText().toString();

                // Empty argument, set to default BTC
                if(fsym.matches("")){ fsym = "BTC"; }
                // Empty argument, set to default USD
                if(tsym.matches("")){ tsym = "USD"; }

                fsym = fsym.toUpperCase();
                tsym = tsym.toUpperCase();

                fsymText.setText(fsym);
                tsymText.setText(tsym);

                getCoinInfo(fsym, tsym);

                updateDrawerItems(fsym);
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Refreshing Coin List", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                getCoinList(new VolleyCallback() {
                    @Override
                    public void onSuccess(JSONObject result) {
                        try {
                            allCoinData = result.getJSONObject("Data");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                getPriceButton.callOnClick();
            }
        });
    }

    private void addDrawerItems() {

        String[] infoArray = {
                "",
                "Symbol: ",
                "Name: ",
                "Algorithm: ",
                "ProofType: "
        };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, infoArray);
        mDrawerList.setAdapter(mAdapter);
    }

    private void updateDrawerItems(String fromSymbol) {

        String Symbol = fromSymbol;
        String Name = "N/A";
        String Algorithm = "N/A";
        String ProofType = "N/A";

        JSONObject coinObject = null;
        try {
            coinObject = allCoinData.getJSONObject(fromSymbol);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        try {
            Name = coinObject.getString("CoinName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            Algorithm = coinObject.getString("Algorithm");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            ProofType = coinObject.getString("ProofType");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String[] infoArray = {
                "",
                String.format("Name: %s", Name),
                String.format("Symbol: %s", Symbol),
                String.format("Algorithm: %s", Algorithm),
                String.format("ProofType: %s", ProofType)
        };

        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, infoArray);
        mDrawerList.setAdapter(mAdapter);
    }

    private void getCoinInfo(final String fromSymbol, String toSymbol) {
        String coinImage_url = null;
        try {
            coinImage_url = String.format("https://www.cryptocompare.com%s", allCoinData.getJSONObject(fromSymbol).getString("ImageUrl"));
            getCoinImage(coinImage_url);
            getCoinBySymbol(fromSymbol, toSymbol);

        } catch (JSONException e) {
            e.printStackTrace();
            getCoinImage(coinImage_url);
            getCoinBySymbol(fromSymbol, toSymbol);
        }
    }

    private void getCoinBySymbol(String fromSymbol, final String toSymbol) {
        complete_url = String.format("https://min-api.cryptocompare.com/data/price?fsym=%s&tsyms=%s ",fromSymbol, toSymbol);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, complete_url,null,
                new Response.Listener<JSONObject>(){
                    @Override
                    public void onResponse(JSONObject response){
                        //Toast.makeText(MainActivity.this, response.toString(), Toast.LENGTH_LONG).show();
                        Log.d("GlassToeStudio", response.toString());
                        try {
                            outPutTextView.setText(response.getString(toSymbol));
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
        MySingleton.getInstance(MainActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    private void getCoinList(final VolleyCallback callback) {
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
                error.printStackTrace();
            }
        });
        MySingleton.getInstance(MainActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    private void getCoinImage(String image_url){
        if(image_url == null){
            coinImage.setImageResource(R.mipmap.ic_launcher_round);
            return;
        }
        ImageRequest imageRequest = new ImageRequest(image_url,
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

    public interface VolleyCallback{
        void onSuccess(JSONObject result);
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
}
