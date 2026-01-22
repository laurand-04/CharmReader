package com.tfg.charmreader.menu.tusLibros;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tfg.charmreader.R;
import com.tfg.charmreader.Utilidades;
import com.tfg.charmreader.objetosBD.LibrosDeUsuario;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.TOCReference;
import nl.siegmann.epublib.epub.EpubReader;
import retrofit2.Response;

public class Visor_n extends AppCompatActivity {

    private WebView webViewContent;
    private List<Resource> chapters = new ArrayList<>();
    private int currentChapter = 0;
    private float scrollGuardado = 0f;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visor_n);

        webViewContent = findViewById(R.id.webViewContent);
        webViewContent.getSettings().setJavaScriptEnabled(true);

        webViewContent.setWebViewClient(new android.webkit.WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Solo aplicamos el scroll si hay algo guardado
                if (scrollGuardado > 0f) {
                    aplicarScroll();
                }
            }
        });

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
        if (epubUri != null) {
            cargarProgresoInicial(); // aquí puedes dejar execute()
            readEpub(epubUri);
        }
        else Toast.makeText(this, "No se proporcionó EPUB", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        guardarSeguimiento();
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

            // Si la API ya respondió antes de que termináramos de leer el EPUB,
            // cargamos el capítulo ahora.
            if (!chapters.isEmpty() && scrollGuardado >= 0) {
                runOnUiThread(() -> showChapter(currentChapter));
            }

        } catch (Exception e) {
            Log.e("EPUB", "Error al leer EPUB", e);
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
                    + "</style></head><body>"
                    + chapterHtml
                    + "</body></html>";

            webViewContent.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void guardarSeguimiento() {
        // 1. CAPTURAMOS EL SCROLL AQUÍ (Hilo Principal)
        final float porcentajeScroll = obtenerPorcentajeScroll();
        final int capituloActual = currentChapter;

        new Thread(() -> {
            try {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) return;

                String correo = user.getEmail();
                int idUsuario = Utilidades.obtenerIdUsuarioDesdeAPI();
                int idLibro = getIntent().getIntExtra("idL", -1);

                if (idUsuario == -1 || idLibro == -1) return;

                // 2. OBTENEMOS EL OBJETO ACTUAL
                Response<LibrosDeUsuario> response = Utilidades.apiLibrosDeUsuario
                        .getLibrodeUsuario(idUsuario, idLibro).execute();

                if (response.isSuccessful() && response.body() != null) {
                    LibrosDeUsuario progreso = response.body();

                    // 3. ASIGNAMOS LOS VALORES CAPTURADOS
                    progreso.setCapitulo(capituloActual);
                    progreso.setScroll(porcentajeScroll);

                    // 4. GUARDAMOS
                    Utilidades.apiLibrosDeUsuario.guardarProgreso(progreso).execute();
                    Log.d("PROGRESO", "Guardado capítulo: " + capituloActual + " y scroll: " + porcentajeScroll);
                }
            } catch (IOException e) {
                Log.e("PROGRESO", "Error de red al guardar", e);
            }
        }).start();
    }

    private float obtenerPorcentajeScroll() {
        try {
            int scrollY = webViewContent.getScrollY();
            // Altura real del contenido dibujado
            int contentHeight = (int) Math.floor(webViewContent.getContentHeight() * webViewContent.getScale());
            int viewHeight = webViewContent.getHeight();

            int maxScrollableHeight = contentHeight - viewHeight;

            if (maxScrollableHeight <= 0) return 0f;

            float porcentaje = (float) scrollY / maxScrollableHeight;

            // Limitar entre 0 y 1
            return Math.max(0f, Math.min(porcentaje, 1f));

        } catch (Exception e) {
            return 0f;
        }
    }

    private void cargarProgresoInicial() {
        new Thread(() -> {
            try {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) return;

                int idLibro = getIntent().getIntExtra("idL", -1);
                int idUsuario = Utilidades.obtenerIdUsuarioDesdeAPI();
                if(idUsuario == -1 || idLibro == -1){
                    Log.e("ERROR en Visor_n", "Error con idusuario o idLibro en cargarProgresoInicial");
                    return;
                }

                Response<LibrosDeUsuario> response = Utilidades.apiLibrosDeUsuario
                        .getLibrodeUsuario(idUsuario, idLibro).execute();

                if (response.isSuccessful() && response.body() != null) {
                    currentChapter = response.body().getCapitulo();
                    scrollGuardado = response.body().getScroll();

                    Log.d("VISOR", "Datos cargados de API: Cap " + currentChapter + " Scroll " + scrollGuardado);

                    runOnUiThread(() -> {
                        // Solo cargamos el capítulo si la lista 'chapters' ya fue llenada por readEpub
                        if (!chapters.isEmpty()) {
                            showChapter(currentChapter);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e("ERROR_API", "Error al cargar progreso: " + e.getMessage());
            }
        }).start();
    }

    private void aplicarScroll() {
        // Usamos un delay un poco mayor para asegurar el renderizado
        webViewContent.postDelayed(() -> {
            float scale = webViewContent.getScale();
            int contentHeight = (int) (webViewContent.getContentHeight() * scale);
            int viewHeight = webViewContent.getHeight();
            int maxScroll = contentHeight - viewHeight;

            if (maxScroll > 0) {
                int scrollY = (int) (maxScroll * scrollGuardado);
                webViewContent.scrollTo(0, scrollY);
                Log.d("SCROLL_SUCCESS", "Scroll realizado a: " + scrollY + " de un máximo de " + maxScroll);

                // OJO: No pongas scrollGuardado = 0 aquí todavía,
                // pruébalo primero comentando esa línea para ver si funciona.
            } else {
                // Si entra aquí, es que el WebView cree que el contenido mide 0
                Log.e("SCROLL_FAIL", "No se pudo scroll: contentHeight=" + contentHeight + " viewHeight=" + viewHeight);
            }
        }, 500);
    }

}


