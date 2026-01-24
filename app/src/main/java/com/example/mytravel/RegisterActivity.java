package com.example.mytravel;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etPasswordRepeat;
    private Button btnRegister;
    private TextView tvGoLogin;

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

        // Klick auf "Schon ein Konto? Anmelden" -> zurück zum Login
        tvGoLogin.setOnClickListener(v -> finish());

        btnRegister.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim().toLowerCase();
            String pw1 = etPassword.getText().toString();
            String pw2 = etPasswordRepeat.getText().toString();

            // Validierung
            if (name.isEmpty() || email.isEmpty() || pw1.isEmpty() || pw2.isEmpty()) {
                Toast.makeText(this, "Bitte alle Felder ausfüllen", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!pw1.equals(pw2)) {
                Toast.makeText(this, "Passwörter stimmen nicht überein", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences users = getSharedPreferences("users", MODE_PRIVATE);

            // Existiert schon?
            if (users.getString("pw_" + email, null) != null) {
                Toast.makeText(this, "E-Mail ist schon registriert", Toast.LENGTH_SHORT).show();
                return;
            }

            // Speichern
            users.edit()
                    .putString("name_" + email, name)
                    .putString("pw_" + email, pw1)
                    .apply();

            Toast.makeText(this, "Registriert! Bitte einloggen.", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}