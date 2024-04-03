package com.example.owledcontroller;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

class NetworkTask extends AsyncTask<JSONObject, Void, Void> {
    private static final String SERVER_IP = "CONTROLLER_IP";
    private static final int SERVER_PORT = 61025;

    @Override
    protected Void doInBackground(JSONObject... jsonObjects) {
        try {
            sendJsonToServer(jsonObjects[0]);
            callApi(jsonObjects[0]);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sendJsonToServer(JSONObject jsonObject) throws IOException {
        Socket socket = new Socket(SERVER_IP, SERVER_PORT);
        PrintWriter out = new PrintWriter(socket.getOutputStream());
        out.println(jsonObject.toString());
        out.close();
        socket.close();
        Log.d("responseCode", jsonObject.toString());
    }

    private void callApi(JSONObject jsonObject) throws IOException, JSONException {
        String apiUrl = "http://192.168.0.28:8080/api/increasepopularity/" + jsonObject.get("effect");
        Log.d("responseCode", apiUrl);
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        Log.d("responseCode", String.valueOf(connection.getResponseCode()));
    }
}

public class EffectSettings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_effect_settings);

        Intent intent = getIntent();
        if (intent.hasExtra("selectedEffect")) {
            Effect selectedEffect = intent.getParcelableExtra("selectedEffect");

            SeekBar seekBar = findViewById(R.id.brightness_bar);
            Button startEffectButton = findViewById(R.id.send_json);

            startEffectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int progress = seekBar.getProgress();

                    float floatValue = progress / 100.0f;

                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("effect", selectedEffect.getEffect());
                        jsonObject.put("brightness", floatValue);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    new NetworkTask().execute(jsonObject);
                }
            });
        }
    }
}