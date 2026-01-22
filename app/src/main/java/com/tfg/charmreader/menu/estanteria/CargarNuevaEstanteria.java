package com.tfg.charmreader.menu.estanteria;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tfg.charmreader.Utilidades;
import com.tfg.charmreader.databinding.ActivityNuevaEstanteriaBinding;
import com.tfg.charmreader.interfacesAPI.I_ApiEstanteria;
import com.tfg.charmreader.objetosBD.API;
import com.tfg.charmreader.objetosBD.Estanteria;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CargarNuevaEstanteria extends AppCompatActivity {
    //private FirebaseAuth mAuth;
    private ActivityNuevaEstanteriaBinding binding;
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    private final I_ApiEstanteria apiEstanteria = API.getInstancia().create(I_ApiEstanteria.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNuevaEstanteriaBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //mAuth = FirebaseAuth.getInstance();
        if (firebaseUser == null) return;

        binding.Guardar.setOnClickListener(v -> guardar());
    }

    private void guardar() {
        String titulo = binding.Titulo.getText().toString().trim();
        if (titulo.isEmpty()){
            mostrarAlerta("Alerta", "Debe introducir un título");
            return;
        }
        /*int idUsuario = Utilidades.obtenerIdUsuarioDesdeAPI();
        if (idUsuario != -1) {
            ejecutarGuardadoEstanteria(idUsuario, titulo);
        } else {
            mostrarAlerta("Error", "No se pudo obtener el ID del usuario");
        }*/
        // CREAMOS UN HILO PARA LA OPERACIÓN DE RED
        new Thread(() -> {
            int idUsuario = Utilidades.obtenerIdUsuarioDesdeAPI();

            // VOLVEMOS AL HILO PRINCIPAL PARA TOCAR LA UI O LANZAR EL GUARDADO
            runOnUiThread(() -> {
                if (idUsuario != -1) {
                    ejecutarGuardadoEstanteria(idUsuario, titulo);
                } else {
                    mostrarAlerta("Error", "No se pudo obtener el ID del usuario");
                }
            });
        }).start();
    }

    private void ejecutarGuardadoEstanteria(int id, String titulo) {
        Estanteria nueva = new Estanteria(id, titulo);
        apiEstanteria.anadirEstanteria(nueva).enqueue(new Callback<Estanteria>() {
            @Override
            public void onResponse(Call<Estanteria> call, Response<Estanteria> response) {
                if (response.isSuccessful()) {
                    Log.d("API_SUCCESS", "Guardado correctamente: " + response.body().toString());
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    // Esto te dirá por qué el servidor rechazó la petición (Error 400, 404, 500...)
                    try {
                        Log.e("API_ERROR", "Cuerpo del error: " + response.errorBody().string());
                    } catch (Exception e) { e.printStackTrace(); }
                    mostrarAlerta("Error " + response.code(), "El servidor rechazó los datos");
                }
            }

            @Override
            public void onFailure(Call<Estanteria> call, Throwable t) {
                // Esto te dirá si ni siquiera pudo llegar al servidor (Error de red, URL mal escrita, etc.)
                Log.e("API_FAILURE", "Error de conexión: " + t.getMessage());
                mostrarAlerta("Error de Red", "No se pudo conectar con el servidor");
            }
        });
    }

    public void mostrarAlerta(String titulo, String contenido) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titulo);
        builder.setMessage(contenido);

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
