package com.example.finalproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.snackbar.Snackbar;

import org.xmlpull.v1.XmlPullParser;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<Article> adapter;
    ArrayList<Article> articles = new ArrayList<>();
    ArrayList<Article> filteredArticles = new ArrayList<>();
    EditText searchEditText;
    Toolbar toolbar;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DBHelper(this);

        // Setup toolbar
        toolbar = findViewById(R.id.myToolbar);
        setSupportActionBar(toolbar);

        // Search bar
        searchEditText = findViewById(R.id.search_edit_text);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filters the article list based on the input string
                filterArticles(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // ListView and adapter
        listView = findViewById(R.id.articleList);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, filteredArticles);
        listView.setAdapter(adapter);

        new FetchFeedTask().execute("https://feeds.bbci.co.uk/news/world/us_and_canada/rss.xml");

        // Click to open details
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Article selected = filteredArticles.get(position);
            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
            intent.putExtra("article", selected);
            startActivity(intent);
        });

        // Long click to favorite
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            Article selected = filteredArticles.get(position);

            if (!dbHelper.isFavorite(selected.link)) {
                dbHelper.addFavorite(selected);

                Snackbar.make(view, R.string.added_to_favorites, Snackbar.LENGTH_LONG)
                        .setAction(R.string.undo, v -> {
                            dbHelper.removeFavorite(selected.link);
                            Toast.makeText(MainActivity.this, R.string.removed_from_favorites, Toast.LENGTH_SHORT).show();
                        }).show();
            } else {
                Toast.makeText(this, R.string.already_in_favorites, Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    // Filters the article list based on the input string
    private void filterArticles(String query) {
        filteredArticles.clear();
        for (Article article : articles) {
            if (article.title.toLowerCase().contains(query.toLowerCase())) {
                filteredArticles.add(article);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private class FetchFeedTask extends AsyncTask<String, Void, List<Article>> {
        protected List<Article> doInBackground(String... urls) {
            List<Article> result = new ArrayList<>();
            try {
                URL url = new URL(urls[0]);
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(url.openConnection().getInputStream(), null);

                boolean insideItem = false;
                String title = "", description = "", pubDate = "", link = "";
                int eventType = parser.getEventType();

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    String tagName = parser.getName();

                    if (eventType == XmlPullParser.START_TAG && "item".equals(tagName)) {
                        insideItem = true;
                    } else if (insideItem && eventType == XmlPullParser.START_TAG) {
                        switch (tagName) {
                            case "title": title = parser.nextText(); break;
                            case "description": description = parser.nextText(); break;
                            case "pubDate": pubDate = parser.nextText(); break;
                            case "link": link = parser.nextText(); break;
                        }
                    } else if (eventType == XmlPullParser.END_TAG && "item".equals(tagName)) {
                        result.add(new Article(title, description, pubDate, link));
                        insideItem = false;
                    }
                    eventType = parser.next();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        protected void onPostExecute(List<Article> result) {
            articles.clear();
            articles.addAll(result);
            filterArticles(searchEditText.getText().toString());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_favorites) {
            Intent intent = new Intent(this, FavoritesActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
