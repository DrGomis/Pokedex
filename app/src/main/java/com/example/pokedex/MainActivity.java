package com.example.pokedex;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;


public class MainActivity extends AppCompatActivity {

    public static Activity act;

    // Control
    public static SharedPreferences sharedPref;
    public boolean isTypeSearch = false;
    public int searchByTypeCount = 0;

    // Interface
    public static TextView txtDisplay;
    public static TextView statsDisplay1;
    public static TextView statsDisplay2;
    public static ImageView imgPok;
    public static ImageView[] imgType;

    public static String[] spinnerTypes = {"Bug ", "Dark", "Dragon", "Electric", "Fairy", "Fighting", "Fire", "Flying", "Ghost", "Grass", "Ground", "Ice", "Normal", "Posion", "Psychic", "Rock", "Steel", "Water"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#BF201D")));

        sharedPref = getApplicationContext().getSharedPreferences("POKEMON_INFO", Context.MODE_PRIVATE);

        act = this;
        imgType = new ImageView[2];

        txtDisplay = findViewById(R.id.txtDisplay);
        statsDisplay1 = findViewById(R.id.statsDisplay1);
        statsDisplay2 = findViewById(R.id.statsDisplay2);
        imgPok = findViewById(R.id.imgPok);
        imgType[0] = findViewById(R.id.imgType0);
        imgType[1] = findViewById(R.id.imgType1);

        // Gets first Pokémon
        fetchData firstPokemon = new fetchData("pokemon/1");
        firstPokemon.execute();

        // showAnimatedArrows(true);

        // Search by NAME or ID
        ImageButton btnSearch = findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                isTypeSearch = false;
                searchByNameOrId();
            }
        });

        // Search by TYPE
        ImageButton btnTypes = findViewById(R.id.btnTypes);
        btnTypes.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                searchByType();
            }
        });

        // D-PAD DOWN Button
        Button btnDown = findViewById(R.id.btnDown);
        btnDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downButtonAction();
            }
        });

        // D-PAD UP Button
        Button btnUp = findViewById(R.id.btnUp);
        btnUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upButtonAction();
            }
        });


        // D-PAD RIGHT Button
        Button btnRight = findViewById(R.id.btnRight);
        btnRight.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                rightButtonAction();
            }
        });

        // D-PAD LEFT Button
        Button btnLeft = findViewById(R.id.btnLeft);
        btnLeft.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                leftButtonAction();
            }
        });

    }

    public void rightButtonAction() {
        if (!isTypeSearch) {
            int pkmnID = Integer.parseInt(sharedPref.getString("POKEMON_ID", "-2"));
            pkmnID++;

            // Max 898
            if (pkmnID > 898) {
                pkmnID = 898;
            }

            fetchData process = new fetchData("pokemon/" + String.valueOf(pkmnID));
            process.execute();
        }
    }

    public void leftButtonAction() {
        if (!isTypeSearch) {
            int pkmnID = Integer.parseInt(sharedPref.getString("POKEMON_ID", "-2"));
            pkmnID--;

            // Min 1
            if (pkmnID <= 0) {
                pkmnID = 1;
            }

            fetchData process = new fetchData("pokemon/" + String.valueOf(pkmnID));
            process.execute();
        }
    }

    public void upButtonAction() {
        if (isTypeSearch) {
            String loadInfo = sharedPref.getString("POKEMON_BY_TYPE", "prueba");
            String[] pokemonList = loadInfo.split(";;");
            searchByTypeCount--;

            if (searchByTypeCount <= 0) {
                searchByTypeCount = 0;
            }

            fetchData process = new fetchData("pokemon/" + pokemonList[searchByTypeCount]);
            process.execute();
        }
    }

    public void downButtonAction() {
        if (isTypeSearch) {
            String loadInfo = sharedPref.getString("POKEMON_BY_TYPE", "prueba");
            String[] pokemonList = loadInfo.split(";;");
            searchByTypeCount++;

            if (searchByTypeCount == pokemonList.length) {
                searchByTypeCount = pokemonList.length - 1;
            }

            fetchData process = new fetchData("pokemon/" + pokemonList[searchByTypeCount]);
            process.execute();
        }
    }


    public void searchByType() {
        ArrayAdapter<String> adp = new ArrayAdapter<String>(MainActivity.this,
                android.R.layout.simple_spinner_item, spinnerTypes);

        Spinner sp = new Spinner(MainActivity.this);
        sp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        sp.setAdapter(adp);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Search by Pokémon type");
        builder.setView(sp);
        builder.setPositiveButton("SEARCH", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                showAnimatedArrows(false);

                searchByTypeCount = 0;
                isTypeSearch = true;
                fetchData process = new fetchData("type/" + sp.getSelectedItem().toString().toLowerCase());
                process.execute();
            }
        });
        builder.create().show();
    }


    public void searchByNameOrId() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Search a Pokémon");
        EditText input = new EditText(this);
        input.setHint("Pokémon name or ID");
        alert.setView(input);
        alert.setPositiveButton("SEARCH", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                showAnimatedArrows(true);

                String pokSearch = input.getText().toString().toLowerCase();
                if (!pokSearch.isEmpty()) {
                    fetchData process = new fetchData("pokemon/" + pokSearch);
                    process.execute();
                }

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });
        alert.show();
    }

    public void showAnimatedArrows(Boolean isNormalSearch) {
        ImageView arrowsUDGif = (ImageView) findViewById(R.id.gif_up_down);
        Glide.with(MainActivity.act).asGif().load(R.drawable.up_down).into(arrowsUDGif);

        ImageView arrowsLRGif = (ImageView) findViewById(R.id.gif_left_right);
        // Glide.with(MainActivity.act).asGif().load(R.drawable.left_right).apply(new RequestOptions().override(300, 100)).into(arrowsLRGif);
        Glide.with(MainActivity.act).asGif().load(R.drawable.left_right).into(arrowsLRGif);

        if (isNormalSearch) {
            arrowsUDGif.setVisibility(View.GONE);
            arrowsLRGif.setVisibility(View.VISIBLE);
        } else {
            arrowsUDGif.setVisibility(View.VISIBLE);
            arrowsLRGif.setVisibility(View.GONE);
        }
    }

}