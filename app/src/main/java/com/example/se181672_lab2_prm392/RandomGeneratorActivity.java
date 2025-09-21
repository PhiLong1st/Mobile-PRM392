package com.example.se181672_lab2_prm392;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.security.SecureRandom;

public class RandomGeneratorActivity extends AppCompatActivity {

    private EditText etMin, etMax;
    private TextView tvResult;
    private Button btnGenerate;

    private SecureRandom rng;

    private static final String KEY_MIN = "KEY_MIN";
    private static final String KEY_MAX = "KEY_MAX";
    private static final String KEY_RES = "KEY_RES";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.random_generator_activity);

        etMin = findViewById(R.id.etMin);
        etMax = findViewById(R.id.etMax);
        tvResult = findViewById(R.id.tvResult);
        btnGenerate = findViewById(R.id.btnGenerate);

        rng = new SecureRandom();

        // Restore state
        if (savedInstanceState != null) {
            etMin.setText(savedInstanceState.getString(KEY_MIN, ""));
            etMax.setText(savedInstanceState.getString(KEY_MAX, ""));
            tvResult.setText(savedInstanceState.getString(KEY_RES, ""));
        }

        // Live validation / enable button
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                etMin.setError(null);
                etMax.setError(null);
                toggleButton();
            }
            @Override public void afterTextChanged(Editable s) {}
        };
        etMin.addTextChangedListener(watcher);
        etMax.addTextChangedListener(watcher);
        toggleButton();

        btnGenerate.setOnClickListener(v -> {
            etMin.setError(null);
            etMax.setError(null);

            Long minVal = parseLong(etMin, true);
            Long maxVal = parseLong(etMax, true);
            if (minVal == null || maxVal == null) {
                tvResult.setText("");
                return;
            }

            long min = minVal;
            long max = maxVal;

            // Option 1: auto-swap to be friendly
            if (min > max) {
                long tmp = min;
                min = max;
                max = tmp;
            }

            // Range length: (max - min + 1) might overflow if not on long
            long bound = safeAdd(safeSub(max, min), 1L);
            if (bound <= 0) {
                // bound must be positive; if not, the range is too large to represent
                etMin.setError("Khoảng quá lớn");
                etMax.setError("Khoảng quá lớn");
                tvResult.setText("");
                return;
            }

            long offset = nextLongBounded(rng, bound); // unbiased
            long result = safeAdd(min, offset);

            tvResult.setText(String.valueOf(result));
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle out) {
        super.onSaveInstanceState(out);
        out.putString(KEY_MIN, etMin.getText().toString());
        out.putString(KEY_MAX, etMax.getText().toString());
        out.putString(KEY_RES, tvResult.getText().toString());
    }

    private void toggleButton() {
        btnGenerate.setEnabled(parseLong(etMin, false) != null && parseLong(etMax, false) != null);
    }

    /**
     * Parses signed integer (long) from EditText.
     * Shows inline error if requireError == true.
     */
    private Long parseLong(EditText et, boolean requireError) {
        String raw = et.getText().toString().trim();
        if (raw.isEmpty()) {
            if (requireError) {
                et.setError("Vui lòng nhập số");
            }
            return null;
        }
        // Only allow optional sign + digits
        if (!raw.matches("^[+-]?\\d+$")) {
            if (requireError) et.setError("Định dạng không hợp lệ");
            return null;
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            if (requireError) et.setError("Số quá lớn/nhỏ");
            return null;
        }
    }

    /** Safe add with overflow check; throws if overflow (caught via caller logic). */
    private long safeAdd(long a, long b) {
        long r = a + b;
        if (((a ^ r) & (b ^ r)) < 0) {
            // overflow
            throw new ArithmeticException("overflow");
        }
        return r;
    }

    /** Safe sub with overflow check; throws if overflow. */
    private long safeSub(long a, long b) {
        long r = a - b;
        if (((a ^ b) & (a ^ r)) < 0) {
            throw new ArithmeticException("overflow");
        }
        return r;
    }

    /**
     * Unbiased random long in [0, bound), bound > 0.
     * Uses rejection sampling (like JDK 17's nextLong(bound)).
     */
    private long nextLongBounded(SecureRandom rnd, long bound) {
        if (bound <= 0) throw new IllegalArgumentException("bound must be positive");
        long r = rnd.nextLong(); // signed
        long m = bound - 1;
        if ((bound & m) == 0L) {
            // power of two: fast path
            return r & m;
        }
        long u = r >>> 1;                   // make positive (0..2^63-1)
        long candidate = u % bound;
        // Rejection threshold to avoid modulo bias
        while (u + m - candidate < 0L) {
            r = rnd.nextLong();
            u = r >>> 1;
            candidate = u % bound;
        }
        return candidate;
    }
}
