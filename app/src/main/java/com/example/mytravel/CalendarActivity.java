package com.example.mytravel;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mytravel.worldmap.WorldMapActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Source;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity {

    private TextView tvMonth;
    private ImageView btnPrev, btnNext;
    private RecyclerView rvCalendar;

    private CalendarAdapter adapter;
    private Calendar calendar;

    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendar);

        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        tvMonth = findViewById(R.id.tvMonth);
        btnPrev = findViewById(R.id.btnPrev);
        btnNext = findViewById(R.id.btnNext);
        rvCalendar = findViewById(R.id.rvCalendar);

        calendar = Calendar.getInstance();

        adapter = new CalendarAdapter(this::openAddNoteDialog);
        rvCalendar.setAdapter(adapter);

        btnPrev.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, -1);
            renderMonth();
        });

        btnNext.setOnClickListener(v -> {
            calendar.add(Calendar.MONTH, 1);
            renderMonth();
        });

        setupNavigationOverlay();
        setupInsets();

        renderMonth();
    }

    private void setupNavigationOverlay() {
        ImageView navBtn = findViewById(R.id.navigation_btn);
        View navRoot = findViewById(R.id.nav_include);

        // Falls das Overlay mal nicht im Layout ist, soll die App nicht crashen
        if (navBtn == null || navRoot == null) return;

        View backdrop = navRoot.findViewById(R.id.nav_backdrop);

        navBtn.setOnClickListener(v -> {
            navRoot.bringToFront();
            navRoot.setVisibility(navRoot.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        if (backdrop != null) {
            backdrop.setOnClickListener(v -> navRoot.setVisibility(View.GONE));
        }

        View home = navRoot.findViewById(R.id.menu_home);
        if (home != null) home.setOnClickListener(v -> {
            navRoot.setVisibility(View.GONE);
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });

        View calendarMenu = navRoot.findViewById(R.id.menu_calendar);
        if (calendarMenu != null) calendarMenu.setOnClickListener(v -> navRoot.setVisibility(View.GONE));

        View settings = navRoot.findViewById(R.id.menu_settings);
        if (settings != null) settings.setOnClickListener(v -> {
            navRoot.setVisibility(View.GONE);
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
        });

        View newTrip = navRoot.findViewById(R.id.menu_newtrip);
        if (newTrip != null) newTrip.setOnClickListener(v -> {
            navRoot.setVisibility(View.GONE);
            startActivity(new Intent(this, NewTripActivity.class));
            finish();
        });

        View worldMap = navRoot.findViewById(R.id.menu_worldmap);
        if (worldMap != null) worldMap.setOnClickListener(v -> {
            navRoot.setVisibility(View.GONE);
            startActivity(new Intent(this, com.example.mytravel.worldmap.WorldMapActivity.class));
            finish();
        });
    }

    private void setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });
    }

    private void renderMonth() {
        SimpleDateFormat headerFormat = new SimpleDateFormat("MMMM yyyy", Locale.GERMAN);
        tvMonth.setText(headerFormat.format(calendar.getTime()));

        adapter.setCells(buildCalendarCells(calendar));
        loadNotesForMonth(calendar);
        loadTripsForMonth(calendar);
    }

    // Baut immer 42 Zellen, damit das Grid immer gleich groß ist
    private List<String> buildCalendarCells(Calendar cal) {
        List<String> result = new ArrayList<>(42);

        Calendar temp = (Calendar) cal.clone();
        temp.set(Calendar.DAY_OF_MONTH, 1);

        int month = temp.get(Calendar.MONTH);

        int dayOfWeek = temp.get(Calendar.DAY_OF_WEEK);
        int mondayBased = (dayOfWeek == Calendar.SUNDAY) ? 7 : dayOfWeek - 1;
        int emptyBefore = mondayBased - 1;

        for (int i = 0; i < emptyBefore; i++) result.add("");

        SimpleDateFormat keyFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

        while (temp.get(Calendar.MONTH) == month) {
            result.add(keyFormat.format(temp.getTime()));
            temp.add(Calendar.DAY_OF_MONTH, 1);
        }

        while (result.size() < 42) result.add("");

        return result;
    }

    private void openAddNoteDialog(String dateKey) {
        View navRoot = findViewById(R.id.nav_include);
        if (navRoot != null) navRoot.setVisibility(View.GONE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(50, 40, 50, 20);

        EditText input = new EditText(this);
        input.setHint("Notiz eingeben");
        input.setSingleLine(false);
        input.setMaxLines(3);
        root.addView(input);

        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setPadding(0, 30, 0, 0);
        btnRow.setGravity(Gravity.END);

        android.widget.Button btnCancel = new android.widget.Button(this);
        btnCancel.setText("Abbrechen");

        android.widget.Button btnSave = new android.widget.Button(this);
        btnSave.setText("Speichern");

        btnRow.addView(btnCancel);
        btnRow.addView(btnSave);
        root.addView(btnRow);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(dateKey)
                .setView(root)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (text.isEmpty()) {
                Toast.makeText(this, "Bitte etwas eingeben.", Toast.LENGTH_SHORT).show();
                return;
            }
            saveNote(dateKey, text);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void saveNote(String dateKey, String noteText) {
        if (uid == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("date", dateKey);
        data.put("note", noteText);

        db.collection("benutzer")
                .document(uid)
                .collection("kalender")
                .document(dateKey)
                .set(data)
                .addOnSuccessListener(unused -> loadNotesForMonth(calendar));
    }

    private void loadNotesForMonth(Calendar cal) {
        if (uid == null) return;

        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.US);
        String monthPrefix = monthFormat.format(cal.getTime());

        db.collection("benutzer")
                .document(uid)
                .collection("kalender")
                .get(Source.SERVER)
                .addOnSuccessListener(qs -> {
                    Map<String, String> notes = new HashMap<>();

                    qs.getDocuments().forEach(doc -> {
                        CalendarNote note = doc.toObject(CalendarNote.class);
                        if (note != null && note.date != null && note.date.startsWith(monthPrefix)) {
                            notes.put(note.date, note.note == null ? "" : note.note);
                        }
                    });

                    adapter.setNotes(notes);
                });
    }
    private void loadTripsForMonth(Calendar cal) {
        if (uid == null) return;

        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.US);
        String monthPrefix = monthFormat.format(cal.getTime());

        db.collection("benutzer")
                .document(uid)
                .collection("reisen")
                .get(Source.SERVER)
                .addOnSuccessListener(qs -> {
                    Map<String, String> trips = new HashMap<>();

                    for (com.google.firebase.firestore.DocumentSnapshot doc : qs.getDocuments()) {
                        com.google.firebase.Timestamp startTs = doc.getTimestamp("startdatum");
                        com.google.firebase.Timestamp endTs = doc.getTimestamp("enddatum");
                        String ort = doc.getString("ort");

                        if (startTs == null || endTs == null || ort == null) continue;

                        Calendar start = Calendar.getInstance();
                        start.setTime(startTs.toDate());

                        Calendar end = Calendar.getInstance();
                        end.setTime(endTs.toDate());

                        Calendar day = (Calendar) start.clone();

                        while (!day.after(end)) {
                            String key = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(day.getTime());

                            // nur Tage vom aktuellen Monat merken
                            if (key.startsWith(monthPrefix)) {
                                trips.put(key, ort);
                            }

                            day.add(Calendar.DAY_OF_MONTH, 1);
                        }
                    }

                    adapter.setTrips(trips);
                });
    }
}