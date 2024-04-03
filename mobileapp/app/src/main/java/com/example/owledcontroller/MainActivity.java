package com.example.owledcontroller;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.owledcontroller.databinding.ActivityMainBinding;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    public static final String REST_IP = "REST_IP";
    private static final String SERVER_IP = "CONTROLLER_IP";
    private static final int SERVER_PORT = 61025;

    private ActivityMainBinding binding;
    private ArrayList<Effect> effects;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new FetchEffectsTask().execute();
    }

    private class NetworkTask extends AsyncTask<JSONObject, Void, Void> {
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
        String apiUrl = "http://"+ REST_IP +":8080/api/increasepopularity/" + jsonObject.get("effect");
        Log.d("responseCode", apiUrl);
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        Log.d("responseCode", String.valueOf(connection.getResponseCode()));
    }

    private class FetchEffectsTask extends AsyncTask<Void, Void, ArrayList<Effect>> {
        @Override
        protected ArrayList<Effect> doInBackground(Void... voids) {
            try {
                return getEffectsFromServer();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ArrayList<Effect> result) {
            if (result != null) {
                effects = result;
                sortEffectsByPopularity();
                setupGridView();
                saveEffectsLocally(result);
            } else {
                ArrayList<Effect> savedEffects = loadEffectsLocally();
                if (savedEffects != null && !savedEffects.isEmpty()) {
                    effects = savedEffects;
                    sortEffectsByPopularity();
                    setupGridView();
                    Toast.makeText(MainActivity.this, "Error fetching data. Showing saved effects.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Error fetching data. No saved effects available.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private ArrayList<Effect> getEffectsFromServer() throws IOException {
        ArrayList<Effect> list = new ArrayList<>();
        String apiUrl = "http://"+ REST_IP +":8080/api/getalleffects";
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            list = readEffectsFromResponse(connection);
        }
        return list;
    }

    private ArrayList<Effect> readEffectsFromResponse(HttpURLConnection connection) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder responseText = new StringBuilder();
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            responseText.append(inputLine);
        }
        in.close();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(responseText.toString(), new TypeReference<ArrayList<Effect>>() {});
    }

    private void sortEffectsByPopularity() {
        effects.sort(Comparator.comparingInt(Effect::getPopularity).reversed());
    }


    private void setupGridView() {
        ArrayList<String> effectNames = getEffectNames();
        ArrayList<Integer> effectImages = getEffectImages();
        GridAdapter gridAdapter = new GridAdapter(MainActivity.this, effectNames, effectImages);
        binding.gridView.setAdapter(gridAdapter);
        binding.gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("effect", effects.get(i).getEffect());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                new NetworkTask().execute(jsonObject);
                Toast.makeText(MainActivity.this, "You clicked on " + jsonObject.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveEffectsLocally(ArrayList<Effect> effects) {
        SharedPreferences sharedPreferences = getSharedPreferences("EffectData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        ObjectMapper mapper = new ObjectMapper();
        try {
            String effectsJson = mapper.writeValueAsString(effects);
            editor.putString("effects", effectsJson);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Effect> loadEffectsLocally() {
        SharedPreferences sharedPreferences = getSharedPreferences("EffectData", MODE_PRIVATE);
        String effectsJson = sharedPreferences.getString("effects", null);
        if (effectsJson != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                return mapper.readValue(effectsJson, new TypeReference<ArrayList<Effect>>() {});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    private ArrayList<Integer> getEffectImages() {
        ArrayList<Integer> images = new ArrayList<>();
        effects.forEach(e -> {
            int data = getResources().getIdentifier(e.getGif(), "drawable", getPackageName());
            images.add(data != 0 ? data : R.drawable.ic_launcher_background);
            Log.d("imgs", e.getGif());
        });
        return images;
    }

    private ArrayList<String> getEffectNames() {
        ArrayList<String> names = new ArrayList<>();
        effects.forEach(e -> {
            names.add(e.getEffectName());
        });
        return names;
    }
}
