package com.example.mytravel;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etPasswordRepeat;
    private Button btnRegister;
    private TextView tvGoLogin;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPasswordRepeat = findViewById(R.id.etPasswordRepeat);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoLogin = findViewById(R.id.tvGoLogin);

        auth = FirebaseAuth.getInstance();

        // schließt die RegisterActivity (Login liegt darunter)
        tvGoLogin.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim(); // später für Firestore
            String email = etEmail.getText().toString().trim().toLowerCase();
            String pw1 = etPassword.getText().toString();
            String pw2 = etPasswordRepeat.getText().toString();

            if (name.isEmpty() || email.isEmpty() || pw1.isEmpty() || pw2.isEmpty()) {
                Toast.makeText(this, "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pw1.equals(pw2)) {
                Toast.makeText(this, "Passwörter stimmen nicht überein", Toast.LENGTH_SHORT).show();
                return;
            }
            if (pw1.length() < 6) {
                Toast.makeText(this, "Passwort muss mindestens 6 Zeichen haben", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, pw1)
                    .addOnSuccessListener(result -> {
                        Toast.makeText(this, "Registrierung erfolgreich!", Toast.LENGTH_SHORT).show();

                        // Nach Registrierung direkt einloggen & weiter
                        startActivity(new Intent(this, HomeActivity.class));
                        finishAffinity();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Registrierung fehlgeschlagen: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });
    }
}