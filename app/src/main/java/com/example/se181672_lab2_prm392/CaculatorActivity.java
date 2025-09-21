package com.example.se181672_lab2_prm392;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class CaculatorActivity extends AppCompatActivity {
    private static final String KEY_A = "KEY_A";
    private static final String KEY_B = "KEY_B";
    private static final String KEY_RESULT = "KEY_RESULT";

    EditText editTextNumber1, editTextNumber2;
    TextView resultTextView;
    Button addButton, subtractButton, multiplyButton, divideButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.caculator_activity);

        editTextNumber1 = findViewById(R.id.editTextNumber1);
        editTextNumber2 = findViewById(R.id.editTextNumber2);
        resultTextView  = findViewById(R.id.resultText);

        addButton      = findViewById(R.id.add_button);
        subtractButton = findViewById(R.id.subtract_button);
        multiplyButton = findViewById(R.id.multiply_button);
        divideButton   = findViewById(R.id.divide_button);

        // Clear inline errors as the user types
        TextWatcher clearErrorWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                editTextNumber1.setError(null);
                editTextNumber2.setError(null);
            }
            @Override public void afterTextChanged(Editable s) {
                toggleDivideEnabled();
            }
        };
        editTextNumber1.addTextChangedListener(clearErrorWatcher);
        editTextNumber2.addTextChangedListener(clearErrorWatcher);

        addButton.setOnClickListener(v -> calculate('+'));
        subtractButton.setOnClickListener(v -> calculate('-'));
        multiplyButton.setOnClickListener(v -> calculate('*'));
        divideButton.setOnClickListener(v -> calculate('/'));

        // Restore state on rotation
        if (savedInstanceState != null) {
            editTextNumber1.setText(savedInstanceState.getString(KEY_A, ""));
            editTextNumber2.setText(savedInstanceState.getString(KEY_B, ""));
            resultTextView.setText(savedInstanceState.getString(KEY_RESULT, "Kết quả:"));
        }

        toggleDivideEnabled();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_A, editTextNumber1.getText().toString());
        outState.putString(KEY_B, editTextNumber2.getText().toString());
        outState.putString(KEY_RESULT, resultTextView.getText().toString());
    }

    private void toggleDivideEnabled() {
        BigDecimal b = safeParse(editTextNumber2, false);
        boolean canDivide = b != null && b.compareTo(BigDecimal.ZERO) != 0;
        divideButton.setEnabled(canDivide);
    }

    private void calculate(char op) {
        // Parse & validate both operands
        BigDecimal a = safeParse(editTextNumber1, true);
        BigDecimal b = safeParse(editTextNumber2, true);

        if (a == null || b == null) {
            resultTextView.setText("Kết quả:");
            return;
        }

        BigDecimal res;
        switch (op) {
            case '+':
                res = a.add(b);
                break;
            case '-':
                res = a.subtract(b);
                break;
            case '*':
                res = a.multiply(b);
                break;
            case '/':
                if (b.compareTo(BigDecimal.ZERO) == 0) {
                    editTextNumber2.setError("Không thể chia cho 0");
                    editTextNumber2.requestFocus();
                    resultTextView.setText("Kết quả:");
                    return;
                }
                // Choose a sensible scale for division
                res = a.divide(b, 12, RoundingMode.HALF_UP);
                break;
            default:
                resultTextView.setText("Kết quả:");
                return;
        }

        resultTextView.setText("Kết quả: " + formatResult(res));
    }

    /**
     * Parses the edit text into BigDecimal safely.
     * Accepts both "." and "," as decimal separators.
     * Returns null (and sets error) when invalid and requireError == true.
     */
    private BigDecimal safeParse(EditText et, boolean requireError) {
        String raw = et.getText().toString().trim();
        if (raw.isEmpty()) {
            if (requireError) {
                et.setError("Vui lòng nhập số");
                et.requestFocus();
            }
            return null;
        }

        // Normalize decimal comma to dot, allow leading +/-
        String normalized = raw.replace(',', '.');

        // Reject partial tokens like "-", "+", ".", "+.", "-."
        if (normalized.equals("+") || normalized.equals("-") || normalized.equals(".")
                || normalized.equals("+.") || normalized.equals("-.")) {
            if (requireError) {
                et.setError("Giá trị không hợp lệ");
                et.requestFocus();
            }
            return null;
        }

        // Validate numeric pattern: optional sign, digits, optional .digits
        if (!normalized.matches("^[+-]?\\d*(?:\\.\\d+)?$")) {
            if (requireError) {
                et.setError("Vui lòng nhập đúng định dạng số");
                et.requestFocus();
            }
            return null;
        }

        try {
            // BigDecimal handles big/small values precisely
            return new BigDecimal(normalized);
        } catch (NumberFormatException ex) {
            if (requireError) {
                et.setError("Số quá lớn/nhỏ hoặc không hợp lệ");
                et.requestFocus();
            }
            return null;
        }
    }

    /**
     * Formats BigDecimal without scientific notation, trims trailing zeros,
     * and falls back to compact scientific only if extremely long.
     */
    private String formatResult(BigDecimal x) {
        BigDecimal cleaned = x.stripTrailingZeros();
        String plain = cleaned.toPlainString();

        // If too long for UI, fall back to scientific with up to 12 decimals
        if (plain.length() > 30) {
            DecimalFormat sci = new DecimalFormat("0.############E0");
            sci.setRoundingMode(RoundingMode.HALF_UP);
            return sci.format(cleaned);
        }
        return plain;
    }
}
