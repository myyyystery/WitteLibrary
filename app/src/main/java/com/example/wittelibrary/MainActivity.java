package com.example.wittelibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.EditText;
import android.widget.TextView;
import android.view.KeyEvent;
import java.util.ArrayList;
import android.view.inputmethod.EditorInfo;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;


public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private List<Book> allBooks = new ArrayList<>();
    private EditText editTextSearch;

    private boolean isSessionActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        restoreSession();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        editTextSearch = findViewById(R.id.editTextSearch);

        // Инициализируем адаптер с пустым списком книг
        bookAdapter = new BookAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(bookAdapter);

        fetchBooks();

        ImageButton buttonProfile = findViewById(R.id.buttonProfile);
        ImageButton buttonGoToDownloadedBooks = findViewById(R.id.buttonDownloadedBooks);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        if (!prefs.getBoolean("isSuccess", true)) {
            Intent intent = new Intent(MainActivity.this, activity_login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        buttonProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Создать намерение для перехода к активности профиля
                Intent profileIntent = new Intent(MainActivity.this, ProfileActivity.class);

                // Запустить активность профиля
                startActivity(profileIntent);
            }
        });

        buttonGoToDownloadedBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Переход на активность скачанных книг
                Intent intent = new Intent(MainActivity.this, DownloadedBooksActivity.class);
                startActivity(intent);
            }
        });

        // Обработчик для текста поиска
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Не требуется
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim();
                performSearch(query);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Не требуется
            }
        });

    }


    @Override
    protected void onPause() {
        super.onPause();
        // Сохраняем состояние сессии перед закрытием приложения
        saveSession();
    }

    private void saveSession() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isSuccess", isSessionActive);
        editor.apply();
    }

    private void restoreSession() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        isSessionActive = prefs.getBoolean("isSuccess", true);
        if (!isSessionActive) {
            Intent intent = new Intent(MainActivity.this, activity_login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Завершаем активность только в том случае, если сеанс неактивен
        }
    }



    @Override
    public void onBackPressed() {
        // Проверяем, есть ли текст в поле поиска
        if (editTextSearch.getText().toString().isEmpty()) {
            // Если поле поиска пустое, вызываем стандартное поведение кнопки "назад"
            super.onBackPressed();
        } else {
            // Если в поле поиска есть текст, очищаем его и показываем все книги
            editTextSearch.setText("");
            performSearch("");
        }
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            bookAdapter.setData(allBooks); // Показать все книги, если запрос пустой
            return;
        }

        List<Book> filteredBookList = new ArrayList<>();
        for (Book book : allBooks) {
            if ((book.getName() != null && book.getName().toLowerCase().contains(query)) ||
                    (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(query)) ||
                    (book.getPublisher() != null && book.getPublisher().toLowerCase().contains(query))) {
                filteredBookList.add(book);
            }
        }
        bookAdapter.setData(filteredBookList);
    }

    private void fetchBooks() {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<List<Book>> call = apiService.getBooks();
        call.enqueue(new Callback<List<Book>>() {
            @Override
            public void onResponse(Call<List<Book>> call, Response<List<Book>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allBooks = response.body();
                    bookAdapter.setData(allBooks); // Устанавливаем данные в адаптере
                } else {
                    Toast.makeText(MainActivity.this, "Failed to retrieve data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Book>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

}
