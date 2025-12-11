package com.tfg.charmreader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tfg.charmreader.databinding.ActivityVisorBinding;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;

public class Visor extends AppCompatActivity {

    private ActivityVisorBinding binding;
    private static final int PICK_EPUB_FILE = 42;

    private List<Resource> chapters = new ArrayList<>();
    private int currentChapter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVisorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Abrir selector de EPUB
        openEpubPicker();

        // Botón siguiente capítulo
        binding.btnNext.setOnClickListener(v -> {
            if (currentChapter < chapters.size() - 1) {
                currentChapter++;
                showChapter(currentChapter);
            }
        });

        // Botón anterior capítulo
        binding.btnPrev.setOnClickListener(v -> {
            if (currentChapter > 0) {
                currentChapter--;
                showChapter(currentChapter);
            }
        });
    }

    private void openEpubPicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/epub+zip");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, PICK_EPUB_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_EPUB_FILE && resultCode == RESULT_OK && data != null) {
            Uri epubUri = data.getData();
            readEpub(epubUri);
        }
    }

    private void readEpub(Uri epubUri) {
        try {
            InputStream is = getContentResolver().openInputStream(epubUri);
            EpubReader epubReader = new EpubReader();
            Book book = epubReader.readEpub(is);

            // Mostramos información del libro
            binding.tvTitle.setText(book.getTitle());
            binding.tvAuthor.setText(TextUtils.join(", ", book.getMetadata().getAuthors()));
            binding.tvPublisher.setText(
                    book.getMetadata().getPublishers().isEmpty() ? "Desconocido" : book.getMetadata().getPublishers().get(0)
            );

            // Obtenemos solo los capítulos desde la tabla de contenido
            for (TOCReference tocRef : book.getTableOfContents().getTocReferences()) {
                Resource res = tocRef.getResource();
                if (res != null && res.getMediaType().getName().equals("application/xhtml+xml")) {
                    chapters.add(res);
                }
            }

            // Mostramos el primer capítulo
            if (!chapters.isEmpty()) {
                currentChapter = 0;
                showChapter(currentChapter);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al leer el EPUB", Toast.LENGTH_SHORT).show();
        }
    }

    private void showChapter(int index) {
        if (index < 0 || index >= chapters.size()) return;

        try {
            Resource res = chapters.get(index);
            String chapterHtml = new String(res.getData());

            // Aplicamos CSS personalizado
            String html = "<html><head>"
                    + "<style>"
                    + "body { font-family: sans-serif; font-size:18px; line-height:1.6; padding:16px; margin:0; }"
                    + "h1,h2,h3 { color:#333366; }"
                    + "p { margin-bottom:12px; }"
                    + "</style></head><body>"
                    + chapterHtml
                    + "</body></html>";

            binding.webViewContent.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
