package com.example.wittelibrary;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.Intent;
import android.widget.Button;
import android.widget.ImageButton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.view.View;
import android.widget.Toast;
import okhttp3.ResponseBody;
import java.io.InputStream;

public class BookInfoActivity extends AppCompatActivity {

    private ImageView bookImage;
    private TextView bookName, bookAuthor, bookPublisher, bookDescription;
    private Button readButton;
    private ImageButton saveButton;

    private Book book;
    private boolean isBookDownloaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_info);

        bookImage = findViewById(R.id.bookImage);
        bookName = findViewById(R.id.bookName);
        bookAuthor = findViewById(R.id.bookAuthor);
        bookPublisher = findViewById(R.id.bookPublisher);
        bookDescription = findViewById(R.id.bookDescription);
        readButton = findViewById(R.id.readButton);
        saveButton = findViewById(R.id.saveButton);

        int bookId = getIntent().getIntExtra("book_id", -1);

        if (bookId != -1) {
            fetchBookInfo(bookId);
        }
        setupReadButton(bookId);

        saveButton.setOnClickListener(v -> {
            if (!isBookDownloaded) {
                downloadPdf(book.getPath());
            } else {
                Toast.makeText(this, "Книга уже скачена", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private File getBooksDirectory() {
        // Получаем директорию для сохранения книг
        File directory = new File(getExternalFilesDir(null), "books");

        // Проверяем, существует ли директория, и создаем ее, если не существует
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                return null; // В случае ошибки возвращаем null
            }
        }
        return directory;
    }

    private void fetchBookInfo(int bookId) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<Book> call = apiService.getBookById(bookId);
        call.enqueue(new Callback<Book>() {
            @Override
            public void onResponse(Call<Book> call, Response<Book> response) {
                if (response.isSuccessful() && response.body() != null) {
                    book = response.body();
                    bookName.setText(book.getName());
                    bookAuthor.setText("Авторы: " + book.getAuthor());
                    bookPublisher.setText("Издатель: " + book.getPublisher());
                    bookDescription.setText(book.getDescription());

                    Glide.with(BookInfoActivity.this)
                            .load(book.getImage())
                            .into(bookImage);

                    // Проверяем, скачана ли уже книга
                    checkIfBookDownloaded();
                }
            }

            @Override
            public void onFailure(Call<Book> call, Throwable t) {
                // Обработка ошибки
            }
        });
    }

    private void setupReadButton(final int bookId) {
        readButton.setOnClickListener(v -> {
            Intent intent = new Intent(BookInfoActivity.this, PdfViewerActivity.class);
            intent.putExtra("book_id", bookId);
            startActivity(intent);
        });
    }

    private void checkIfBookDownloaded() {
        File directory = getBooksDirectory();
        if (directory != null) {
            File file = new File(directory, book.getName() + ".pdf");
            isBookDownloaded = file.exists();
        }
    }

    private void downloadPdf(String pdfUrl) {
        // Создаем экземпляр ApiService
        ApiService apiService = ApiClient.getClient().create(ApiService.class);

        // Вызываем метод загрузки PDF файла
        Call<ResponseBody> call = apiService.downloadPdf(pdfUrl);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        savePdf(response.body());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Обработка ошибки загрузки PDF
            }
        });
    }

    private void savePdf(ResponseBody body) throws IOException {
        // Получаем директорию для сохранения книг
        File directory = getBooksDirectory();

        if (directory != null) {
            // Создаем файл внутри директории
            File file = new File(directory, book.getName() + ".pdf");

            // Открываем поток для записи данных
            try (InputStream inputStream = body.byteStream(); FileOutputStream outputStream = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                Toast.makeText(this, "Книга успешно сохранена", Toast.LENGTH_SHORT).show();
                isBookDownloaded = true;
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Ошибка при сохранении книги", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Если директория не может быть создана, выводим сообщение об ошибке
            Toast.makeText(this, "Ошибка создания директории для сохранения книг", Toast.LENGTH_SHORT).show();
        }
    }
}