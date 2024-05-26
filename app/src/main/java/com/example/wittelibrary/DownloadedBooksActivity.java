package com.example.wittelibrary;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadedBooksActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DownloadedBookAdapter downloadedBookAdapter;
    private List<File> downloadedBooks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloaded_books);

        recyclerView = findViewById(R.id.recyclerViewDownloadedBooks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Изменено на LinearLayoutManager
        downloadedBookAdapter = new DownloadedBookAdapter(this, downloadedBooks);
        recyclerView.setAdapter(downloadedBookAdapter);

        loadDownloadedBooks();
    }

    private void loadDownloadedBooks() {
        // Получаем директорию с загруженными книгами
        File directory = new File(getExternalFilesDir(null), "books");

        // Проверяем, существует ли директория
        if (directory.exists() && directory.isDirectory()) {
            // Получаем список файлов в директории
            File[] files = directory.listFiles();

            // Проверяем, есть ли файлы в директории
            if (files != null && files.length > 0) {
                for (File file : files) {
                    // Добавляем файлы (книги) в список
                    downloadedBooks.add(file);
                }
                // Уведомляем адаптер о изменениях в данных
                downloadedBookAdapter.notifyDataSetChanged();
            }
        }
    }
}
