package com.example.wittelibrary;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.IOException;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Button;
import android.util.DisplayMetrics;
import android.graphics.Matrix;
import android.animation.ValueAnimator;

public class PDFView_local extends AppCompatActivity {

    private ImageView pdfView;
    private PdfRenderer pdfRenderer;
    private PdfRenderer.Page currentPage;
    private int pageNumber;
    private int pageCount;
    private TextView pageInfoTextView;
    private SeekBar seekBar;
    private Button prevButton;
    private Button nextButton;
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;
    private float mScaleFactor = 1.0f;
    private float startX = 0, startY = 0;

    private float initialImageWidth, initialImageHeight;
    private float screenWidth, screenHeight;
    private float currentTranslateX = 0;
    private float currentTranslateY = 0;

    private Matrix matrix = new Matrix();
    private float[] matrixValues = new float[9];

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

        // Получаем путь к локальному файлу из Intent
        String filePath = getIntent().getStringExtra("file_path");

        // Проверка на получение правильного пути к файлу
        if (filePath != null) {
            // Используем ViewTreeObserver для ожидания полной инициализации pdfView
            pdfView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    pdfView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    try {
                        openRenderer(filePath);
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Обработка ошибки: файл не может быть открыт
                    }
                }
            });
        } else {
            // Обработка ошибки: неверный путь к файлу
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

            pdfView.setScaleX(mScaleFactor);
            pdfView.setScaleY(mScaleFactor);

            float newWidth = initialImageWidth * mScaleFactor;
            float newHeight = initialImageHeight * mScaleFactor;

            newWidth = Math.min(newWidth, pdfView.getWidth());
            newHeight = Math.min(newHeight, pdfView.getHeight());

            float dx = (pdfView.getWidth() - newWidth) / 2;
            float dy = (pdfView.getHeight() - newHeight) / 2;

            pdfView.setImageMatrix(getMatrix(newWidth, newHeight, dx, dy));

            return true;
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            pdfView.scrollBy((int) distanceX, (int) distanceY);
            return true;
        }
    }

    private Matrix getMatrix(float width, float height, float dx, float dy) {
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.postTranslate(dx, dy);
        matrix.postScale(mScaleFactor, mScaleFactor);
        return matrix;
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

        bitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
        currentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        pdfView.setImageBitmap(bitmap);
        updatePageInfo();
        updateSeekBar();

        currentTranslateX = pdfView.getTranslationX();
        currentTranslateY = pdfView.getTranslationY();
    }

    private void updateSeekBar() {
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
                if (fromUser) {
                    showPage(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
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
