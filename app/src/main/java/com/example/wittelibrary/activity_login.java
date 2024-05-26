package com.example.wittelibrary;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class activity_login extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;
    private ImageButton buttonDownloadedBooks;
    private boolean isSessionActive;

    private static final String LOGIN_URL = "http://188.32.174.97/loginApplication.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        isSessionActive = prefs.getBoolean("isSuccess", false);


        if (isSessionActive) {
            startMainActivity();
            return; // Завершаем выполнение метода onCreate, чтобы экран входа не отображался
        }

        setContentView(R.layout.activity_login);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        ImageButton buttonGoToDownloadedBooks = findViewById(R.id.buttonDownloadedBooks);

        buttonGoToDownloadedBooks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Переход на активность скачанных книг
                Intent intent = new Intent(activity_login.this, DownloadedBooksActivity.class);
                startActivity(intent);
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                login(username, password);
            }
        });
    }

    private void login(String username, String password) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .build();

        Request request = new Request.Builder()
                .url(LOGIN_URL)
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(activity_login.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseData = response.body().string();
                    try {
                        JSONObject json = new JSONObject(responseData);
                        boolean isSuccess = json.getBoolean("isSuccess");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isSuccess) {
                                    try {
                                        // Получаем ID пользователя из JSON-ответа
                                        int userId = json.getInt("userId");
                                        Toast.makeText(activity_login.this, "Вы успешно вошли в систему", Toast.LENGTH_SHORT).show();
                                        // Сохраняем ID пользователя в SharedPreferences
                                        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                                        prefs.edit().putBoolean("isSuccess", true).putInt("userId", userId).apply();
                                        isSessionActive = true;
                                        // Запускаем главную активность
                                        startMainActivity();
                                    } catch (JSONException e) {
                                        // Если поле "userId" отсутствует в JSON-ответе, обрабатываем исключение
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(activity_login.this, "User ID not found in JSON response", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }else {
                                    Toast.makeText(activity_login.this, "Неправильное имя пользователя или пароль", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } catch (JSONException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity_login.this, "Response parsing error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity_login.this, "Server error: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(activity_login.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
