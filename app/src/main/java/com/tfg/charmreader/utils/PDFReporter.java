package com.tfg.charmreader.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.content.Intent;
import androidx.core.content.FileProvider;

import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import com.tfg.charmreader.R;
import com.tfg.charmreader.data.model.Libro;
import com.tfg.charmreader.data.model.LibrosDeUsuario;
import com.tfg.charmreader.data.model.Usuario;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PDFReporter {

    private final Context context;
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public PDFReporter(Context context) {
        this.context = context;
    }

    public void generarInformeLectura(Usuario usuario, List<LibrosDeUsuario> relaciones, Map<Integer, Libro> libroMap, Date inicio, Date fin) {
        PdfDocument document = new PdfDocument();
        // Tamaño A4 (595 x 842 puntos)
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // 1. Cabecera y Logo
        dibujarCabecera(canvas, paint, usuario, inicio, fin);

        // 2. Listado de Libros
        int y = 280; // Posición inicial vertical
        for (LibrosDeUsuario ldu : relaciones) {
            if (ldu.getFechaFin() != null) {
                // Filtrado por fecha
                if (inicio != null && fin != null) {
                    if (ldu.getFechaFin().before(inicio) || ldu.getFechaFin().after(fin)) continue;
                }

                Libro detalle = libroMap.get(ldu.getId().getIdL());
                y = dibujarItemLibro(canvas, paint, ldu, detalle, y);

                // Control de fin de página (simplificado)
                if (y > 750) break;
            }
        }

        document.finishPage(page);

        // 3. Guardado y Compartir
        guardarYCompartir(document);
    }

    private void dibujarCabecera(Canvas canvas, Paint paint, Usuario usuario, Date inicio, Date fin) {

        int margenInferiorLogo = 40; // Espacio extra que queremos debajo del logo
        int yActual = 50; // Punto de inicio

        try {
            Bitmap bitmap = getBitmapFromVectorDrawable(R.mipmap.ic_launcher);
            if (bitmap != null) {
                int logoWidth = 100;
                int logoHeight = 100;
                Bitmap scaledLogo = Bitmap.createScaledBitmap(bitmap, logoWidth, logoHeight, true);
                float centerX = (595 - logoWidth) / 2f;

                canvas.drawBitmap(scaledLogo, centerX, yActual, paint);

                // Actualizamos yActual: inicio (50) + alto logo (100) + margen (40) = 190
                yActual += logoHeight + margenInferiorLogo;
            }
        } catch (Exception e) {
            Log.e("PDF_LOGO", "Error con vector: " + e.getMessage());
            yActual = 100; // Si falla el logo, empezamos un poco más arriba
        }

        // Título Principal
        paint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
        paint.setTextSize(28);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("CharmReader", 595 / 2f, yActual, paint);

        // Subtítulo (Nombre Usuario) - Bajamos 30 puntos
        yActual += 30;
        paint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL));
        paint.setTextSize(12);
        paint.setColor(Color.DKGRAY);
        canvas.drawText("Informe de Lectura: " + (usuario.getNombre() != null ? usuario.getNombre() : "Usuario"), 595 / 2f, yActual, paint);

        // Periodo - Bajamos 20 puntos
        yActual += 20;
        String periodo = (inicio == null) ? "Historial Completo" : sdf.format(inicio) + " - " + sdf.format(fin);
        canvas.drawText("Periodo: " + periodo, 595 / 2f, yActual, paint);
    }

    private Bitmap getBitmapFromVectorDrawable(int drawableId) {
        android.graphics.drawable.Drawable drawable = androidx.core.content.ContextCompat.getDrawable(context, drawableId);
        if (drawable != null) {
            // Creamos un bitmap con un tamaño base alto (500x500) para asegurar nitidez absoluta
            Bitmap bitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
        return null;
    }

    private int dibujarItemLibro(Canvas canvas, Paint paint, LibrosDeUsuario ldu, Libro detalle, int y) {
        String titulo = (detalle != null) ? detalle.getNombre() : "Libro Desconocido";

        // 1. Título del libro
        paint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
        paint.setTextSize(14);
        paint.setColor(Color.BLACK);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("📖 " + titulo, 70, y, paint);

        // 2. Metadatos (Fecha y Nota)
        String valStr = (ldu.getValoracion() > 0) ? String.format(Locale.getDefault(), "%.1f/5 ⭐", ldu.getValoracion()) : "Sin valorar";
        paint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL));
        paint.setTextSize(11);
        paint.setColor(Color.GRAY);
        canvas.drawText("Finalizado: " + sdf.format(ldu.getFechaFin()) + "  |  Puntuación: " + valStr, 90, y + 20, paint);

        // 3. Reseña con SALTO DE LÍNEA AUTOMÁTICO
        String desc = (ldu.getDescripcion() != null && !ldu.getDescripcion().isEmpty()) ? ldu.getDescripcion() : "Sin comentario.";

        TextPaint textPaint = new TextPaint(paint); // Necesitamos TextPaint para el Layout
        int anchoPermitido = 430; // Ancho máximo del texto (595 - márgenes)

        // Creamos el Layout que gestiona los saltos de línea
        StaticLayout staticLayout = StaticLayout.Builder.obtain("Reseña: " + desc, 0, ("Reseña: " + desc).length(), textPaint, anchoPermitido)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(2, 1) // 2 de espacio extra entre líneas
                .setIncludePad(false)
                .build();

        canvas.save();
        canvas.translate(90, y + 28); // Movemos el canvas a la posición de la reseña
        staticLayout.draw(canvas);
        canvas.restore();

        // Retornamos Y + espacio ocupado por el texto + margen
        // El alto del texto es staticLayout.getHeight()
        return y + 35 + staticLayout.getHeight() + 20;
    }

    private void guardarYCompartir(PdfDocument document) {
        File file = new File(context.getExternalFilesDir(null), "Informe_CharmReader.pdf");
        try {
            document.writeTo(new FileOutputStream(file));
            document.close();

            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(Intent.createChooser(intent, "Compartir informe"));

        } catch (Exception e) {
            Log.e("PDF_ERROR", "Error al guardar PDF: " + e.getMessage());
        }
    }
}