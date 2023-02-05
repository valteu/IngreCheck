package com.example.ingrecheck;

import androidx.appcompat.app.AppCompatActivity;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import androidx.activity.result.ActivityResultLauncher;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private TextView mTextViewResult;
    private TextView mSugarResult;
    private RequestQueue mQueue;
    public String INGREDIENTS;
    public String PRODUCT_ID;
    private OnProductIdChangedListener listener;
    private static final String[] sugar = {
            "zucker",
            "monosaccharide",
            "glukose",
            "fruktose",
            "galaktose",
            "disaccharide",
            "maltose",
            "laktose",
            "saccharose",
            "honig",
            "malzextrakt",
            "süßstoff",
            "sirup",
            "aspartam",
            "saccharin",
            "stevia",
            "xylitol",
            "erythrit",
            "maltitol",
            "isomalt",
            "sorbit"
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextViewResult = findViewById(R.id.text_view_result);
        mSugarResult = findViewById(R.id.sugar);
        Button button_scan = findViewById(R.id.button_scan);
        mQueue = Volley.newRequestQueue(this);

        button_scan.setOnClickListener(v->
        {
            scanCode();
        });
        setOnProductIdChangedListener(new OnProductIdChangedListener() {
            @Override
            public void onProductIdChanged() {
                jsonParse();
            }
        });

    }
    private void scanCode()
    {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Volume up to flash on");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLaucher.launch(options);
        mTextViewResult.setText("");
        mSugarResult.setText("");
    }

    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(), result->
    {
        if(result.getContents() !=null)
        {
            PRODUCT_ID = result.getContents();
            if (listener != null) {
                listener.onProductIdChanged();
            }
        }
    });


    private void jsonParse() {

        String url = "https://world.openfoodfacts.org/api/v0/product/" + PRODUCT_ID +".json";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("debug-msg", "Response");
                            JSONObject jsonObject = response.getJSONObject("product");
                            String product = jsonObject.getString("ingredients_text");
                            //Log.d("debug-msg", ingredients_tag);
                            INGREDIENTS += product;
                            mTextViewResult.append(product + "\n\n");
                            mSugarResult.append(checkIngredients(product));
                        } catch (JSONException e) {
                            Log.d("debug-msg", "No Response");
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mQueue.add(request);
    }

    public interface OnProductIdChangedListener {
        void onProductIdChanged();
    }
    public void setOnProductIdChangedListener(OnProductIdChangedListener listener) {
        this.listener = listener;
    }
    public void setProductId(String productId) {
        this.PRODUCT_ID = productId;
        if (listener != null) {
            listener.onProductIdChanged();
        }
    }
    public static String checkIngredients(String ingredients) {
        ingredients = ingredients.toLowerCase().replaceAll("[^a-zA-Z]+", "");
        StringBuilder result = new StringBuilder();
        for (String s : sugar) {
            if (ingredients.contains(s)) {
                result.append(s).append(" ");
            }
        }
        return result.toString().trim();
    }
}