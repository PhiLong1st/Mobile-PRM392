package com.example.se181672_lab2_prm392;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etUser, etPass;
    private Button btnSignIn;
    private TextView tvGoSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.login_activity);

        etUser = findViewById(R.id.etUser);
        etPass = findViewById(R.id.etPass);
        btnSignIn = findViewById(R.id.btnSignIn);
        tvGoSignUp = findViewById(R.id.tvGoSignUp);

        tvGoSignUp.setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class)));

        btnSignIn.setOnClickListener(v -> {
            String u = etUser.getText().toString().trim();
            String p = etPass.getText().toString().trim();

            if (!validateLogin(u, p)) return;

            if (!MockRepo.exists(u)) {
                etUser.setError("Account not found");
                return;
            }
            if (!MockRepo.verify(u, p)) {
                etPass.setError("Wrong password");
                return;
            }
            Toast.makeText(this, "Sign in success!", Toast.LENGTH_SHORT).show();
        });
    }

    private boolean validateLogin(String u, String p) {
        boolean ok = true;
        if (u.isEmpty()) { etUser.setError("Required"); ok = false; }
        if (p.isEmpty()) { etPass.setError("Required"); ok = false; }
        return ok;
    }
}
