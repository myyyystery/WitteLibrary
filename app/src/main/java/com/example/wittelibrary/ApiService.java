package com.example.wittelibrary;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import okhttp3.ResponseBody;
import retrofit2.http.Url;
import org.json.JSONObject;

public interface ApiService {
    @GET("get_books.php")
    Call<List<Book>> getBooks();

    @GET("get_book.php")
    Call<Book> getBookById(@Query("id") int id);

    @GET
    Call<ResponseBody> downloadPdf(@Url String fileUrl);

    @GET("get_profile.php")
    Call<User> getUserData(@Query("userId") int id);

}
