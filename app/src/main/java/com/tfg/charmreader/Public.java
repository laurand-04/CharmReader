package com.tfg.charmreader;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.tfg.charmreader.R;

public class Public extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public); // Vinculación con el XML

        MaterialButton btnVolver = findViewById(R.id.btnVolverPrivado);
        btnVolver.setOnClickListener(v -> finish()); // Regresa a MainActivity
    }
}
