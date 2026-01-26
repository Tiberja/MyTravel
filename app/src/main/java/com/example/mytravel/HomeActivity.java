package com.example.mytravel;

import android.os.Bundle;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // 🔥 FIRESTORE TEST
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> test = new HashMap<>();
        test.put("msg", "Hallo Firestore");
        test.put("time", System.currentTimeMillis());

        db.collection("test")
                .add(test)
                .addOnSuccessListener(doc -> Log.d("FS", "OK: " + doc.getId()))
                .addOnFailureListener(e -> Log.e("FS", "FAIL", e));

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}