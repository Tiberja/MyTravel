package com.example.mytravel;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class NewTripActivity extends AppCompatActivity {

    private EditText etOrt, etStartDate, etEndDate;
    private ImageView ivPreview;
    private Uri selectedImageUri = null;

    private FirebaseFirestore db;

    private ActivityResultLauncher<String[]> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_trip);

        // Firebase
        db = FirebaseFirestore.getInstance();

        // Views
        etOrt = findViewById(R.id.destination);
        etStartDate = findViewById(R.id.start_date);
        etEndDate = findViewById(R.id.end_date);
        ivPreview = findViewById(R.id.imageView6);

        Button btnAbbrechen = findViewById(R.id.btn_abbrechen);
        Button btnFertig = findViewById(R.id.btn_fertig);


        // Bild aus Galerie wählen
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );

                        selectedImageUri = uri;
                        ivPreview.setImageURI(uri);
                    }
                }
        );

        findViewById(R.id.bildHochladen).setOnClickListener(v -> {
            pickImageLauncher.launch(new String[]{"image/*"});
        });



        // Abbrechen -> Home

        btnAbbrechen.setOnClickListener(v -> goHome());


        // Fertig -> Speichern

        btnFertig.setOnClickListener(v -> saveTrip());
    }

    private void saveTrip() {
        String ort = etOrt.getText().toString().trim();
        String startdatum = etStartDate.getText().toString().trim();
        String enddatum = etEndDate.getText().toString().trim();

        if (ort.isEmpty()) {
            etOrt.setError("Bitte Reiseziel eingeben");
            return;
        }

        if (startdatum.isEmpty()) {
            etStartDate.setError("Bitte Startdatum eingeben");
            return;
        }

        if (enddatum.isEmpty()) {
            etEndDate.setError("Bitte Enddatum eingeben");
            return;
        }

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (uid == null) {
            Toast.makeText(this, "Nicht eingeloggt.", Toast.LENGTH_LONG).show();
            return;
        }

        // Firestore-Daten
        Map<String, Object> reise = new HashMap<>();
        reise.put("ort", ort);
        reise.put("startdatum", startdatum);
        reise.put("enddatum", enddatum);

        // Bild lokal (Uri als String)
        if (selectedImageUri != null) {
            reise.put("bild", selectedImageUri.toString());
        } else {
            reise.put("bild", null);
        }

        // users/{uid}/reisen/{autoId}
        db.collection("benutzer")
                .document(uid)
                .collection("reisen")
                .add(reise)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Reise gespeichert!", Toast.LENGTH_SHORT).show();
                    goHome();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Fehler: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void goHome() {
        // ⚠️ anpassen, falls deine Home-Activity anders heißt
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
