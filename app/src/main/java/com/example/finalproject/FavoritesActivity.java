package com.example.finalproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private ListView favoritesListView;
    private ArrayAdapter<Article> adapter;
    private List<Article> favorites;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Toolbar with back button
        Toolbar toolbar = findViewById(R.id.favorites_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        dbHelper = new DBHelper(this);
        favorites = dbHelper.getAllFavorites();

        favoritesListView = findViewById(R.id.favorites_list);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, favorites);
        favoritesListView.setAdapter(adapter);

        favoritesListView.setOnItemClickListener((parent, view, position, id) -> {
            Article selected = favorites.get(position);
            Intent intent = new Intent(FavoritesActivity.this, DetailActivity.class);
            intent.putExtra("article", selected);
            startActivity(intent);
        });

        // On long click remove the favorite
        favoritesListView.setOnItemLongClickListener((parent, view, position, id) -> {
            Article removed = favorites.remove(position);
            dbHelper.removeFavorite(removed.link);
            adapter.notifyDataSetChanged();

            // Add an undo
            Snackbar.make(view, "Removed from favorites", Snackbar.LENGTH_LONG)
                    .setAction(R.string.undo, v -> {
                        dbHelper.addFavorite(removed);
                        favorites.add(position, removed);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(FavoritesActivity.this, R.string.restored, Toast.LENGTH_SHORT).show();
                    }).show();
            return true;
        });
    }
}
