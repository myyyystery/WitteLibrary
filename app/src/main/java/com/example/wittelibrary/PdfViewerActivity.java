package com.example.wittelibrary;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Button;
import android.util.DisplayMetrics;
import android.graphics.Matrix;
import android.animation.ValueAnimator;
public class PdfViewerActivity extends AppCompatActivity {

    private ImageView pdfView;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private int pageNumber;
    private int pageCount;
    private int bookId;
    private TextView pageInfoTextView;
    private SeekBar seekBar;
    private Button prevButton;
    private Button nextButton;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private float mScaleFactor = 1.0f;
    private float posX = 0, posY = 0;

    private float startX = 0, startY = 0;
    private float lastTouchX, lastTouchY;
    private float initialImageWidth, initialImageHeight;
    private float screenWidth, screenHeight;
    private float currentTranslateX = 0;
    private float currentTranslateY = 0;



    private Matrix matrix = new Matrix(); // Для применения трансформаций к изображению
    private float[] matrixValues = new float[9]; // Массив для получения значений трансформаций

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_viewer);

        pdfView = findViewById(R.id.pdfView);
        pageInfoTextView = findViewById(R.id.pageInfo);
        seekBar = findViewById(R.id.seekBar);
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        gestureDetector = new GestureDetector(this, new GestureListener());

        // Получение BookID из Intent
        bookId = getIntent().getIntExtra("book_id", -1);

        // Проверка на получение правильного BookID
        if (bookId != -1) {
            // Загрузка информации о книге и PDF файла с сервера
            fetchBookInfo(bookId);
        } else {
            // Обработка ошибки: неверный BookID
        }

        prevButton.setOnClickListener(v -> {
            if (pageNumber > 0) {
                showPage(pageNumber - 1);
            }
        });

        nextButton.setOnClickListener(v -> {
            if (pageNumber < pageCount - 1) {
                showPage(pageNumber + 1);
            }
        });

        pdfView.setOnTouchListener((v, event) -> {
            scaleGestureDetector.onTouchEvent(event);
            gestureDetector.onTouchEvent(event);

            // Обновление позиции ImageView при перемещении
            float currentX = event.getX();
            float currentY = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = currentX - pdfView.getTranslationX();
                    startY = currentY - pdfView.getTranslationY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float newX = currentX - startX;
                    float newY = currentY - startY;
                    float imageWidth = pdfView.getWidth() * mScaleFactor;
                    float imageHeight = pdfView.getHeight() * mScaleFactor;
                    float maxX = Math.max(0, pdfView.getWidth() - imageWidth);
                    float maxY = Math.max(0, pdfView.getHeight() - imageHeight);
                    newX = Math.min(maxX, Math.max(0, newX));
                    newY = Math.min(maxY, Math.max(0, newY));
                    animateTranslation(pdfView.getTranslationX(), newX, pdfView.getTranslationY(), newY);
                    break;
                case MotionEvent.ACTION_UP:
                    break;
            }
            return true;
        });
    }

    private void animateTranslation(float startX, float endX, float startY, float endY) {
        ValueAnimator translateAnimatorX = ValueAnimator.ofFloat(currentTranslateX, endX);
        translateAnimatorX.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            pdfView.setTranslationX(value);
        });

        ValueAnimator translateAnimatorY = ValueAnimator.ofFloat(currentTranslateY, endY);
        translateAnimatorY.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            pdfView.setTranslationY(value);
        });

        translateAnimatorX.setDuration(200);
        translateAnimatorY.setDuration(200);

        translateAnimatorX.start();
        translateAnimatorY.start();
    }



    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float scaleFactor = detector.getScaleFactor();
            mScaleFactor *= scaleFactor;
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f));

            // Масштабирование ImageView
            pdfView.setScaleX(mScaleFactor);
            pdfView.setScaleY(mScaleFactor);

            // Устанавливаем новые размеры изображения
            float newWidth = initialImageWidth * mScaleFactor;
            float newHeight = initialImageHeight * mScaleFactor;

            // Ограничиваем размеры изображения, чтобы не превышать размеры pdfView
            newWidth = Math.min(newWidth, pdfView.getWidth());
            newHeight = Math.min(newHeight, pdfView.getHeight());

            // Вычисляем смещение, чтобы изображение оставалось в центре pdfView
            float dx = (pdfView.getWidth() - newWidth) / 2;
            float dy = (pdfView.getHeight() - newHeight) / 2;

            // Применяем масштаб и смещение
            pdfView.setImageMatrix(getMatrix(newWidth, newHeight, dx, dy));

            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            // Перемещение изображения
            pdfView.scrollBy((int) distanceX, (int) distanceY);
            return true;
        }
    }

    private Matrix getMatrix(float width, float height, float dx, float dy) {
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.postTranslate(dx, dy); // Смещение изображения
        matrix.postScale(mScaleFactor, mScaleFactor); // Масштабирование изображения
        return matrix;
    }

    private void fetchBookInfo(int bookId) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<Book> call = apiService.getBookById(bookId);
        call.enqueue(new Callback<Book>() {
            @Override
            public void onResponse(Call<Book> call, Response<Book> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Book book = response.body();
                    String path = book.getPath(); // Получаем путь к PDF из объекта Book
                    downloadPdf(path); // Загружаем PDF файл с сервера, передавая путь
                }
            }

            @Override
            public void onFailure(Call<Book> call, Throwable t) {
                // Обработка ошибки загрузки информации о книге
            }
        });
    }

    private void downloadPdf(String path) {
        // Формируем URL для загрузки PDF файла
        String pdfUrl = path;

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
        File file = new File(getCacheDir(), "temp.pdf");
        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        try {
            byte[] fileReader = new byte[4096];
            long fileSize = body.contentLength();
            long fileSizeDownloaded = 0;

            inputStream = body.byteStream();
            outputStream = new FileOutputStream(file);

            while (true) {
                int read = inputStream.read(fileReader);
                if (read == -1) {
                    break;
                }

                outputStream.write(fileReader, 0, read);
                fileSizeDownloaded += read;
            }

            outputStream.flush();

            openRenderer(file.getAbsolutePath());
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }

            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    private void openRenderer(String filePath) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(new File(filePath), ParcelFileDescriptor.MODE_READ_ONLY);
        pdfRenderer = new PdfRenderer(parcelFileDescriptor);
        pageCount = pdfRenderer.getPageCount();
        showPage(pageNumber);
        setupSeekBar();
    }

    private void showPage(int index) {
        if (pdfRenderer.getPageCount() <= index || index < 0) {
            return;
        }

        if (currentPage != null) {
            currentPage.close();
        }

        currentPage = pdfRenderer.openPage(index);
        pageNumber = index;

        float pdfPageWidth = currentPage.getWidth();
        float pdfPageHeight = currentPage.getHeight();
        float aspectRatio = pdfPageWidth / pdfPageHeight;

        Bitmap bitmap;
        int scaledWidth, scaledHeight;
        int width = pdfView.getWidth();
        int height = pdfView.getHeight();
        if (aspectRatio > 1) {
            scaledWidth = width;
            scaledHeight = (int) (width / aspectRatio);
        } else {
            scaledHeight = height;
            scaledWidth = (int) (height * aspectRatio);
        }

        initialImageWidth = scaledWidth;
        initialImageHeight = scaledHeight;

        // Уменьшаем размер страницы, если необходимо
        bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        pdfView.setImageBitmap(bitmap);
        updatePageInfo();
        updateSeekBar();

        currentTranslateX = pdfView.getTranslationX();
        currentTranslateY = pdfView.getTranslationY();

    }

    private void updateSeekBar() {
        // Установка текущего значения SeekBar в соответствии с текущей страницей
        seekBar.setProgress(pageNumber);
    }

    private void updatePageInfo() {
        pageInfoTextView.setText(getString(R.string.page_info_format, pageNumber + 1, pageCount));
    }

    private void setupSeekBar() {
        seekBar.setMax(pageCount - 1);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                showPage(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Not needed
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Not needed
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentPage != null) {
            currentPage.close();
        }
        if (pdfRenderer != null) {
            pdfRenderer.close();
        }
    }
}
