package com.example.myapplication;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private Map<Integer, List<String>> groupedItems;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listview);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);

        groupedItems = new HashMap<>();

        FetchItemsTask fetchItemsTask = new FetchItemsTask();
        fetchItemsTask.execute("https://fetch-hiring.s3.amazonaws.com/hiring.json");
    }

    private class FetchItemsTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                result = stringBuilder.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                JSONArray jsonArray = new JSONArray(s);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    int listId = jsonObject.optInt("listId");
                    String name = jsonObject.optString("name");
                    if (name != null && !name.trim().isEmpty()) {
                        if (!groupedItems.containsKey(listId)) {
                            groupedItems.put(listId, new ArrayList<String>());
                        }
                        groupedItems.get(listId).add(name);
                    }
                }
                printList();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
            }
        }

        private void printList() {
            List<Integer> listIds = new ArrayList<>(groupedItems.keySet());
            Collections.sort(listIds);
            for (int listId : listIds) {
                List<String> items = groupedItems.get(listId);
                if (items != null) {
                    Collections.sort(items);
                    for (String item : items) {
                        adapter.add("List ID: " + listId + ", Name: " + item);
                    }
                }
            }
        }
    }
}
