package com.example.mytravel;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btnLogin;
    TextView tvRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Views mit Layout verbinden
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);

        // Klick auf "Registrieren" -> RegisterActivity öffnen
        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );

        // Echter Login (ohne DB, mit SharedPreferences)
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim().toLowerCase();
            String pw = etPassword.getText().toString();

            if (email.isEmpty() || pw.isEmpty()) {
                Toast.makeText(this, "Bitte E-Mail und Passwort eingeben", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences users = getSharedPreferences("users", MODE_PRIVATE);
            String savedPw = users.getString("pw_" + email, null);

            if (savedPw == null) {
                Toast.makeText(this, "Account existiert nicht. Bitte registrieren.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!pw.equals(savedPw)) {
                Toast.makeText(this, "Falsches Passwort", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Login erfolgreich!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
    }
}
