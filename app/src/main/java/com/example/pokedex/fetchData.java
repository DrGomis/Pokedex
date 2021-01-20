package com.example.pokedex;


import android.os.AsyncTask;
import android.util.Log;

//import com.ahmadrosid.svgloader.SvgLoader;
//import com.squareup.picasso.Picasso;

//import com.ahmadrosid.svgloader.SvgLoader;

import com.bumptech.glide.Glide;
//import com.squareup.picasso.Picasso;

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

public class fetchData extends AsyncTask<Void, Void, Void> {

    protected String data = "";
    protected String results = "";
    protected ArrayList<String> strTypes; // Create an ArrayList object
    protected String pokSearch;
    protected String pkID;
    protected String stats1 = "";
    protected String stats2 = "";
    protected boolean isATypeSearch = false;
    protected String img = "";


    public fetchData(String pokSearch) {
        this.pokSearch = pokSearch;
        strTypes = new ArrayList<String>();
    }

    @Override
    protected Void doInBackground(Void... voids) {

        try {
            //Make API connection
            URL url = new URL("https://pokeapi.co/api/v2/" + pokSearch);

            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

            // Read API results
            InputStream inputStream = httpURLConnection.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sBuilder = new StringBuilder();

            // Build JSON String
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                sBuilder.append(line + "\n");
            }

            inputStream.close();
            data = sBuilder.toString();

        } catch (IOException e) {
            //e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        try {
            if (data.isEmpty()) {
                // Generates a MissingNo. pokémon when the information is wrong
                loadMissingNoInformation();
            } else {
                // Generates the pokémon when the information is correct
                loadPokemonInformation();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!isATypeSearch) {
            // Resets types
            if (strTypes.size() < 2) {
                MainActivity.imgType[1].setImageResource(android.R.color.transparent);
            }

            // Sets info
            MainActivity.txtDisplay.setText(this.results);
            MainActivity.statsDisplay1.setText(this.stats1);
            MainActivity.statsDisplay2.setText(this.stats2);

            if (img.isEmpty()) {
                MainActivity.imgPok.setImageResource(R.drawable.missingno);
            } else {
                Glide.with(MainActivity.act).load(img).into(MainActivity.imgPok);
            }

            // Sets image on TYPE/TYPES
            for (int i = 0; i < strTypes.size(); i++) {
                MainActivity.imgType[i].setImageResource(MainActivity.act.getResources().getIdentifier(strTypes.get(i), "drawable", MainActivity.act.getPackageName()));
            }

        }
    }

    private void SetPokemonByType(JSONArray amountOfPokemon) throws JSONException {
        String storedPokemon = "";
        String firstPokemon = "";

        for (int i = 0; i < amountOfPokemon.length(); i++) {
            JSONObject params = new JSONObject(amountOfPokemon.getString(i));
            JSONObject paramsName = new JSONObject(params.getString("pokemon"));

            if (i == 0) {
                firstPokemon = (paramsName.getString("name"));
            }
            storedPokemon = storedPokemon + (paramsName.getString("name")) + ";;";
        }
        MainActivity.sharedPref.edit().putString("POKEMON_BY_TYPE", storedPokemon).commit();

        fetchData process = new fetchData("pokemon/" + firstPokemon);
        process.execute();
    }

    private void loadPokemonInformation() throws JSONException {
        JSONObject jObject = null;

        jObject = new JSONObject(data);
        // Checks if is a 'Search By Type' or not
        if (jObject.has("pokemon")) {
            isATypeSearch = true;
            SetPokemonByType(new JSONArray(jObject.getString("pokemon")));
        } else {
            // Stores pokémon ID to use in MainActivity as reference
            pkID = jObject.getString("id");
            MainActivity.sharedPref.edit().putString("POKEMON_ID", pkID).commit();

            // If the pokémon ID is BIGGER than 10000 then it is a special pokémon
            if (Integer.parseInt(pkID) > 10000) {
                pkID = "★";
            } else {
                pkID = "#" + pkID;
            }

            // Gets the pokémon INFO (name, height, weight, etc.)
            results += pkID + " " + jObject.getString("name").toUpperCase() + "\n\n" +
                    "Height: " + (Double.parseDouble(jObject.getString("height")) / 10) + "m\n" +
                    "Weight: " + (Double.parseDouble(jObject.getString("weight"))/ 10) + "Kg";

            // Gets the IMG URL
            JSONObject sprites = new JSONObject(jObject.getString("sprites"));
            JSONObject other = new JSONObject(sprites.getString("other"));
            // Gets the official Artwork
            JSONObject official_artwork = new JSONObject(other.getString("official-artwork"));
            img = official_artwork.getString("front_default");

            // Gets the pokémon TYPE/TYPES
            JSONArray types = new JSONArray(jObject.getString("types"));
            for (int i = 0; i < types.length(); i++) {
                JSONObject type = new JSONObject(types.getString(i));
                JSONObject type2 = new JSONObject(type.getString("type"));
                strTypes.add(type2.getString("name"));
            }

            // Gets the pokémon BASE STATS
            JSONArray pkmnStats = new JSONArray(jObject.getString("stats"));
            String[] pkmnStatValues = new String[6];
            for (int i = 0; i < pkmnStats.length(); i++) {
                JSONObject statPos = new JSONObject(pkmnStats.getString(i));
                pkmnStatValues[i] = String.valueOf(statPos.getInt("base_stat"));
            }

            stats1 = "HP: " + pkmnStatValues[0] +
                    "\nAT: " + pkmnStatValues[1] +
                    "\nDF: " + pkmnStatValues[2];
            stats2 = "SA: " + pkmnStatValues[3] +
                    "\nSD: " + pkmnStatValues[4] +
                    "\nSP: " + pkmnStatValues[5];
        }
    }

    public void loadMissingNoInformation() {
        results += "#0 MISSINGNO.\n\n" +
                "Height: ???m\n" +
                "Weight: ???Kg";
        strTypes.add("normal");
        strTypes.add("unknown");

        stats1 = "HP: ???" +
                "\nAT: ???" +
                "\nDF: ???";
        stats2 = "SA: ???" +
                "\nSD: ???" +
                "\nSP: ???";
    }

}
