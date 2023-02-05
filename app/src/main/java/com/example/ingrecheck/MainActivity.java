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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView mTextViewResult;
    private RequestQueue mQueue;
    public String INGREDIENTS;
    public String PRODUCT_ID;
    private static String[] sugar = {
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
            "süssstoff",
            "süsstoff",
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
        Button buttonParse = findViewById(R.id.button_parse);
        Button button_scan = findViewById(R.id.button_scan);
        button_scan.setOnClickListener(v->
        {
            scanCode();
        });


        mQueue = Volley.newRequestQueue(this);

        buttonParse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
    }

    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(), result->
    {
        if(result.getContents() !=null)
        {
            PRODUCT_ID = result.getContents();
        }
    });


    private void jsonParse() {

        String url = "https://world.openfoodfacts.org/api/v0/product/" + PRODUCT_ID +".json";
        Log.d("debug-msg", "Started Parsing");

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
                            mTextViewResult.append(checkIngredients(INGREDIENTS));
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

    // Administrate Array
/*
    public static void addSugar(String newSugar) {
        List<String> sugarList = new ArrayList<>(Arrays.asList(sugar));
        sugarList.add(newSugar);
        sugar = sugarList.toArray(new String[0]);
    }

    public static void removeSugar(String oldSugar) {
        List<String> sugarList = new ArrayList<>(Arrays.asList(sugar));
        sugarList.remove(oldSugar);
        sugar = sugarList.toArray(new String[0]);
    }*/
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