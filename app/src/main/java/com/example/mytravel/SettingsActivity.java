package com.example.mytravel;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.mytravel.calendar.CalendarActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        ImageView btn = findViewById(R.id.navigation_btn);

        View navRoot = findViewById(R.id.nav_include);
        View backdrop = navRoot.findViewById(R.id.nav_backdrop);

        // Öffnen / Schließen
        btn.setOnClickListener(v -> {
            navRoot.bringToFront();
            navRoot.setVisibility(
                    navRoot.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
            );
        });

        // Klick neben Menü schließt
        backdrop.setOnClickListener(v -> navRoot.setVisibility(View.GONE));

        View menuHome = navRoot.findViewById(R.id.menu_home);
        View menuCalendar = navRoot.findViewById(R.id.menu_calendar);
        View menuSettings = navRoot.findViewById(R.id.menu_settings);
        View menuNewTrip = navRoot.findViewById(R.id.menu_newtrip);
        View menuWorldMap = navRoot.findViewById(R.id.menu_worldmap);

        menuHome.setOnClickListener(v -> {
            navRoot.setVisibility(View.GONE);
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        menuCalendar.setOnClickListener(v -> {
            navRoot.setVisibility(View.GONE);
            startActivity(new Intent(this, CalendarActivity.class));
            finish();
        });

        menuSettings.setOnClickListener(v -> {
            navRoot.setVisibility(View.GONE);
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
        });

        menuNewTrip.setOnClickListener(v -> {
            navRoot.setVisibility(View.GONE);
            startActivity(new Intent(this, NewTripActivity.class));
            finish();
        });

        menuWorldMap.setOnClickListener(v -> {
            navRoot.setVisibility(View.GONE);
            startActivity(new Intent(this, com.example.mytravel.worldmap.WorldMapActivity.class));
            finish();
        });

        //SETTINGS-AKTIONEN (Logout + Passwort Reset)
        LinearLayout rowLogout = findViewById(R.id.rowLogout);
        LinearLayout rowResetPassword = findViewById(R.id.rowResetPassword);

        rowLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent i = new Intent(SettingsActivity.this, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });

        rowResetPassword.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user == null || user.getEmail() == null) {
                Toast.makeText(this,
                        "Keine E-Mail gefunden. Bitte erneut einloggen.",
                        Toast.LENGTH_LONG).show();
                return;
            }
            // Reset-Mail schicken
            FirebaseAuth.getInstance()
                    .sendPasswordResetEmail(user.getEmail())
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this,
                                    "Reset-Link wurde an " + user.getEmail() + " gesendet.",
                                    Toast.LENGTH_LONG).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Fehler: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show()
                    );
        });

        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}