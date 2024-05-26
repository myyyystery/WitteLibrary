package com.example.wittelibrary;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.content.Context;
import android.widget.Button;

public class ProfileActivity extends AppCompatActivity {

    private TextView textViewUsername;
    private TextView textViewName;
    private TextView textViewCourse;
    private TextView textViewGroup;
    private TextView textViewUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        textViewUsername = findViewById(R.id.textViewUsername);
        textViewName = findViewById(R.id.textViewName);
        textViewCourse = findViewById(R.id.textViewCourse);
        textViewGroup = findViewById(R.id.textViewGroup);
        textViewUserId = findViewById(R.id.textViewUserId);

        Button logoutButton = findViewById(R.id.buttonLogout);

        // Отправляем запрос на сервер для получения данных о пользователе
        fetchUserData();

        logoutButton.setOnClickListener(v -> {
            SharedPreferences prefs1 = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
            prefs1.edit().putBoolean("isSuccess", false).apply();
            Intent intent = new Intent(ProfileActivity.this, activity_login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

    }

    private void fetchUserData() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1); // -1 - значение по умолчанию, если userId не найден
        if (userId == -1) {
            Log.e("ProfileActivity", "User ID not found in SharedPreferences");
            return;
        }

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<User> call = apiService.getUserData(userId);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    textViewUsername.setText("Имя пользователя: " + user.getUsername());
                    textViewName.setText("ФИО: " + user.getName());
                    textViewCourse.setText("Курс: " + String.valueOf(user.getCourse()));
                    textViewGroup.setText("Учебная группа: " + user.getGroupname());
                    textViewUserId.setText("Уникальный индефикатор: " + String.valueOf(user.getId()));
                } else {
                    Log.e("ProfileActivity", "Error response: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("ProfileActivity", "Error fetching user data: " + t.getMessage());
            }
        });

    }
}
