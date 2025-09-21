package com.example.se181672_lab2_prm392;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    private EditText etUser, etPass, etConfirm;
    private Button btnSignUp;
    private TextView tvGoSignIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.signup_activity);

        etUser = findViewById(R.id.etSuUser);
        etPass = findViewById(R.id.etSuPass);
        etConfirm = findViewById(R.id.etSuConfirm);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvGoSignIn = findViewById(R.id.tvGoSignIn);

        tvGoSignIn.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        btnSignUp.setOnClickListener(v -> {
            String u = etUser.getText().toString().trim();
            String p = etPass.getText().toString().trim();
            String c = etConfirm.getText().toString().trim();

            if (!validateSignup(u, p, c)) return;

            if (MockRepo.exists(u)) {
                etUser.setError("Username already exists");
                return;
            }
            MockRepo.save(u, p);
            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private boolean validateSignup(String u, String p, String c) {
        boolean ok = true;
        if (u.isEmpty()) { etUser.setError("Required"); ok = false; }
        if (p.isEmpty()) { etPass.setError("Required"); ok = false; }
        if (c.isEmpty()) { etConfirm.setError("Required"); ok = false; }
        if (!p.isEmpty() && !c.isEmpty() && !p.equals(c)) {
            etConfirm.setError("Passwords must match"); ok = false;
        }
        // simple type rule: username letters/digits only
        if (!u.isEmpty() && !u.matches("^[A-Za-z0-9]{3,}$")) {
            etUser.setError("Letters/digits, ≥3 chars"); ok = false;
        }
        return ok;
    }
}
