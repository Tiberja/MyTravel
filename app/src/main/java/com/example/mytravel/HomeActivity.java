package com.example.mytravel;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // NAVIGATION
        ImageView btn = findViewById(R.id.navigation_btn);

// Root vom Overlay ist jetzt nav_include (weil include-ID nav_root überschreibt)
        View navRoot = findViewById(R.id.nav_include);
        View backdrop = navRoot.findViewById(R.id.nav_backdrop);

        btn.setOnClickListener(v -> {
            navRoot.bringToFront();
            navRoot.setVisibility(navRoot.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        backdrop.setOnClickListener(v -> navRoot.setVisibility(View.GONE));


        // FIRESTORE TEST (optional)
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> test = new HashMap<>();
        test.put("msg", "Hallo Firestore");
        test.put("time", System.currentTimeMillis());

        db.collection("test")
                .add(test)
                .addOnSuccessListener(doc -> Log.d("FS", "OK: " + doc.getId()))
                .addOnFailureListener(e -> Log.e("FS", "FAIL", e));

        // INSETS
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
