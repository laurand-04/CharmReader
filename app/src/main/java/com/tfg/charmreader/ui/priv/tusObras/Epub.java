package com.tfg.charmreader.ui.priv.tusObras;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.tfg.charmreader.data.model.Obras;
import com.tfg.charmreader.data.network.API.CloudinaryClient;
import com.tfg.charmreader.data.repository.priv.tusObras.ObrasRepository;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Date;

import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Identifier;
import nl.siegmann.epublib.domain.Metadata;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.epub.EpubReader;
import nl.siegmann.epublib.epub.EpubWriter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Epub {

    private final ObrasRepository obrasRepository = new ObrasRepository();
    private final Context context;

    public interface CrearEpubCallback {
        void onSuccess();
        void onError(String mensajeError);
    }

    public Epub(Context context) {
        this.context = context.getApplicationContext();
    }

    // --- GENERAR DESDE CERO ---
    /*public void generarObraDesdeCero(String titulo, String autor, String descripcion, Uri uriPortada, int idUsuario, CrearEpubCallback callback) {
        new Thread(() -> {
            try {
                Book miLibro = new Book();
                Metadata metadata = miLibro.getMetadata();
                metadata.addIdentifier(new Identifier("UUID", java.util.UUID.randomUUID().toString()));

                metadata.addTitle(titulo);
                metadata.addAuthor(new Author(autor));

                // ✅ SINOPSIS compatible con Play Libros
                if (descripcion != null && !descripcion.isEmpty()) {
                    metadata.setDescriptions(Collections.singletonList(descripcion));
                }

                byte[] coverBytes = null;

                if (uriPortada != null) {
                    coverBytes = leerBytesDesdeUri(uriPortada);

                    if (coverBytes != null) {

                        // ✅ PORTADA CORRECTA
                        Resource coverResource = new Resource(coverBytes, "cover.jpg");
                        coverResource.setId("cover");

                        miLibro.setCoverImage(coverResource);
                    }
                }

                File archivoEpub = new File(context.getFilesDir(), "obra_creada_" + System.currentTimeMillis() + ".epub");

                try (FileOutputStream out = new FileOutputStream(archivoEpub)) {
                    new EpubWriter().write(miLibro, out);
                }

                String rutaAbsoluta = archivoEpub.getAbsolutePath();
                Log.d("CREAR_OBRA", "1 idUsuario: " + idUsuario);

                if (coverBytes != null) {
                    CloudinaryClient.subirImagenCloudinary(coverBytes, new CloudinaryClient.CloudinaryCallback() {
                        @Override
                        public void onUrl(String urlPortada) {
                            registrarNuevaObraApi(idUsuario, titulo, autor, descripcion, urlPortada, rutaAbsoluta, callback);
                        }

                        @Override
                        public void onError(String e) {
                            registrarNuevaObraApi(idUsuario, titulo, autor, descripcion, "", rutaAbsoluta, callback);
                        }
                    });
                } else {
                    registrarNuevaObraApi(idUsuario, titulo, autor, descripcion, "", rutaAbsoluta, callback);
                }

            } catch (Exception e) {
                Log.e("EPUB", "Error creando EPUB", e);
                callback.onError("Error al fabricar el archivo.");
            }
        }).start();
    }*/
    public void generarObraDesdeCero(String titulo, String autor, String descripcion, Uri uriPortada, int idUsuario, CrearEpubCallback callback) {
        new Thread(() -> {
            try {
                Book miLibro = new Book();
                Metadata metadata = miLibro.getMetadata();
                metadata.addIdentifier(new Identifier("UUID", java.util.UUID.randomUUID().toString()));
                metadata.addTitle(titulo);
                metadata.addAuthor(new Author(autor));

                if (descripcion != null && !descripcion.isEmpty()) {
                    metadata.setDescriptions(Collections.singletonList(descripcion));
                }

                byte[] coverBytes = null;

                // ✅ LEER PORTADA BIEN
                if (uriPortada != null) {
                    coverBytes = leerBytesDesdeUri(uriPortada);
                }

                // ✅ 1. CREAR CAPÍTULO 1 (SIEMPRE PRIMERO)
                String tituloCap1 = "Capítulo 1";
                String contenidoHtml =
                        "<html><head><title>" + tituloCap1 + "</title></head>" +
                                "<body><h2>" + tituloCap1 + "</h2><div></div></body></html>";

                miLibro.addSection(tituloCap1,
                        new Resource(contenidoHtml.getBytes("UTF-8"), "capitulo_1.html"));

                // ✅ 2. AÑADIR PORTADA SIN ROMPER SPINE
                if (coverBytes != null) {

                    Resource coverImage = new Resource(coverBytes, "cover.jpg");
                    coverImage.setId("cover-image");
                    miLibro.setCoverImage(coverImage);

                    String portadaHtml =
                            "<html><body style='margin:0;padding:0;text-align:center;'>" +
                                    "<img src='cover.jpg' style='max-width:100%;height:auto;'/>" +
                                    "</body></html>";

                    Resource portada = new Resource(portadaHtml.getBytes("UTF-8"), "cover.html");

                    miLibro.getResources().add(portada);

                    // 🔥 IMPORTANTE: NO usar addSection aquí
                    miLibro.getSpine().getSpineReferences().add(0, new SpineReference(portada));
                }

                // 3. GUARDADO
                File archivoEpub = new File(context.getFilesDir(), "obra_" + System.currentTimeMillis() + ".epub");
                try (FileOutputStream out = new FileOutputStream(archivoEpub)) {
                    new EpubWriter().write(miLibro, out);
                }

                String rutaAbsoluta = archivoEpub.getAbsolutePath();

                // 4. CLOUDINARY
                if (coverBytes != null) {
                    byte[] finalCoverBytes = coverBytes;
                    CloudinaryClient.subirImagenCloudinary(finalCoverBytes, new CloudinaryClient.CloudinaryCallback() {
                        @Override
                        public void onUrl(String url) {
                            registrarNuevaObraApi(idUsuario, titulo, autor, descripcion, url, rutaAbsoluta, callback);
                        }

                        @Override
                        public void onError(String e) {
                            registrarNuevaObraApi(idUsuario, titulo, autor, descripcion, "", rutaAbsoluta, callback);
                        }
                    });
                } else {
                    registrarNuevaObraApi(idUsuario, titulo, autor, descripcion, "", rutaAbsoluta, callback);
                }

            } catch (Exception e) {
                Log.e("EPUB", "Error creando EPUB", e);
                callback.onError("Error al fabricar el archivo.");
            }
        }).start();
    }

    // --- MODIFICAR METADATOS ---
    public void modificarMetadatosEpub(Obras obra, String nuevoTitulo, String nuevoAutor, String nuevaDesc, Uri nuevaPortadaUri, CrearEpubCallback callback) {
        new Thread(() -> {
            try {

                String rutaLimpia = obra.getRuta();
                if (rutaLimpia.startsWith("file://")) {
                    rutaLimpia = rutaLimpia.substring(7);
                }

                File archivoOriginal = new File(rutaLimpia);

                if (!archivoOriginal.exists()) {
                    callback.onError("No se encuentra el archivo de la obra.");
                    return;
                }

                Book miLibro = new EpubReader().readEpub(new FileInputStream(archivoOriginal));
                Metadata metadata = miLibro.getMetadata();

                // ✅ TÍTULO Y AUTOR
                metadata.getTitles().clear();
                metadata.addTitle(nuevoTitulo);

                metadata.getAuthors().clear();
                metadata.addAuthor(new Author(nuevoAutor));

                // ✅ SINOPSIS FIX
                if (nuevaDesc != null) {
                    metadata.setDescriptions(Collections.singletonList(nuevaDesc));
                }

                // ✅ PORTADA NUEVA
                if (nuevaPortadaUri != null) {
                    byte[] coverBytes = leerBytesDesdeUri(nuevaPortadaUri);

                    if (coverBytes != null) {

                        Resource coverResource = new Resource(coverBytes, "cover.jpg");
                        coverResource.setId("cover");

                        miLibro.setCoverImage(coverResource);

                        // HTML portada
                        String portadaHtml = "<html><body><img src='cover.jpg'/></body></html>";
                        Resource portadaHtmlRes = new Resource(portadaHtml.getBytes("UTF-8"), "cover.html");

                        miLibro.addSection("Portada", portadaHtmlRes);
                        miLibro.getSpine().getSpineReferences().add(0, new SpineReference(portadaHtmlRes));

                        CloudinaryClient.subirImagenCloudinary(coverBytes, new CloudinaryClient.CloudinaryCallback() {
                            @Override
                            public void onUrl(String url) {
                                guardarEpubFisico(miLibro, archivoOriginal);
                                actualizarObraApi(obra, nuevoTitulo, nuevoAutor, nuevaDesc, url, callback);
                            }

                            @Override
                            public void onError(String e) {
                                callback.onError("Error al subir imagen: " + e);
                            }
                        });

                        return;
                    }
                }

                guardarEpubFisico(miLibro, archivoOriginal);
                actualizarObraApi(obra, nuevoTitulo, nuevoAutor, nuevaDesc, obra.getUrl_imagen(), callback);

            } catch (Exception e) {
                Log.e("Epub", "Error al modificar metadatos", e);
                callback.onError("Error al modificar el archivo de la obra.");
            }
        }).start();
    }

    private void guardarEpubFisico(Book libro, File destino) {
        try {
            new EpubWriter().write(libro, new FileOutputStream(destino));
        } catch (Exception e) {
            Log.e("Epub", "Error guardando EPUB", e);
        }
    }

    private void registrarNuevaObraApi(int idU, String t, String a, String s, String url, String ruta, CrearEpubCallback cb) {
        Obras n = new Obras();
        Log.d("CREAR_OBRA", "2 idUsuario: " + idU);

        n.setIdUsuario(idU);
        n.setNombre(t);
        n.setAutor(a);
        n.setSinopsis(s);
        n.setUrl_imagen(url);
        n.setRuta(ruta);
        n.setFecha_ultima_modificacion(new Date());
        n.setFinalizado(false);

        obrasRepository.guardarObra(n, new Callback<Obras>() {
            @Override
            public void onResponse(Call<Obras> c, Response<Obras> r) {
                if (r.isSuccessful()) cb.onSuccess();
                else cb.onError("Error API");
            }

            @Override
            public void onFailure(Call<Obras> c, Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    private void actualizarObraApi(Obras o, String t, String a, String s, String url, CrearEpubCallback cb) {
        o.setNombre(t);
        o.setAutor(a);
        o.setSinopsis(s);
        o.setUrl_imagen(url);
        o.setFecha_ultima_modificacion(new Date());

        obrasRepository.guardarObra(o, new Callback<Obras>() {
            @Override
            public void onResponse(Call<Obras> c, Response<Obras> r) {
                if (r.isSuccessful()) cb.onSuccess();
                else cb.onError("Error API");
            }

            @Override
            public void onFailure(Call<Obras> c, Throwable t) {
                cb.onError(t.getMessage());
            }
        });
    }

    private byte[] leerBytesDesdeUri(Uri uri) {
        try (InputStream is = context.getContentResolver().openInputStream(uri);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            byte[] buf = new byte[1024];
            int len;

            while ((len = is.read(buf)) != -1) {
                bos.write(buf, 0, len);
            }

            return bos.toByteArray();

        } catch (Exception e) {
            return null;
        }
    }
}