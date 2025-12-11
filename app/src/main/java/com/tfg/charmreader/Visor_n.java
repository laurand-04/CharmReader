package com.tfg.charmreader;

import android.adservices.ondevicepersonalization.OnDevicePersonalizationException;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;

public class Visor_n extends AppCompatActivity {

    private WebView webViewContent;
    private List<Resource> chapters = new ArrayList<>();
    private int currentChapter = 0;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visor_n);

        webViewContent = findViewById(R.id.webViewContent);
        webViewContent.getSettings().setJavaScriptEnabled(true);

        // Configurar gestos para avanzar/retroceder capítulos
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                float x = e.getX();
                float width = webViewContent.getWidth();
                if (x > width / 2) nextChapter();
                else prevChapter();
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX < 0) nextChapter();
                    else prevChapter();
                    return true;
                }
                return false;
            }
        });

        webViewContent.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        // Leer EPUB pasado por Intent
        Uri epubUri = Uri.parse(getIntent().getStringExtra("URL_LIBRO"));
        if (epubUri != null) readEpub(epubUri);
        else Toast.makeText(this, "No se proporcionó EPUB", Toast.LENGTH_SHORT).show();
    }

    private void nextChapter() {
        if (currentChapter < chapters.size() - 1) {
            currentChapter++;
            showChapter(currentChapter);
        }
    }

    private void prevChapter() {
        if (currentChapter > 0) {
            currentChapter--;
            showChapter(currentChapter);
        }
    }

    private void readEpub(Uri epubUri) {
        try {
            InputStream is = getContentResolver().openInputStream(epubUri);
            nl.siegmann.epublib.domain.Book book = new EpubReader().readEpub(is);

            chapters.clear();
            for (TOCReference tocRef : book.getTableOfContents().getTocReferences()) {
                if (tocRef.getResource() != null) chapters.add(tocRef.getResource());
            }

            if (!chapters.isEmpty()) {
                currentChapter = 0;
                showChapter(currentChapter);
            } else {
                Toast.makeText(this, "No se encontraron capítulos", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al leer EPUB", Toast.LENGTH_SHORT).show();
        }
    }

    private void showChapter(int index) {
        if (index < 0 || index >= chapters.size()) return;

        try {
            Resource res = chapters.get(index);
            String chapterHtml = new String(res.getData());

            String html = "<html><head>"
                    + "<style>"
                    + "body { font-family: sans-serif; font-size:18px; line-height:1.6; padding:16px; margin:0; }"
                    + "h1,h2,h3 { color:#333366; }"
                    + "p { margin-bottom:12px; }"
                    + "</style></head><body>"
                    + chapterHtml
                    + "</body></html>";

            webViewContent.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


