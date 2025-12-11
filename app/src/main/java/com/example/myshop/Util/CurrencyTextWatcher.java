package com.example.myshop.Util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyTextWatcher implements TextWatcher {

    private final EditText editText;
    private String current = "";

    public CurrencyTextWatcher(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (!s.toString().equals(current)) {
            editText.removeTextChangedListener(this);

            String cleanString = s.toString().replaceAll("[^\\d]", ""); // Chỉ giữ lại số

            if (!cleanString.isEmpty()) {
                try {
                    double parsed = Double.parseDouble(cleanString);
                    // Định dạng số: 20000000 -> 20.000.000
                    DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.GERMANY);
                    formatter.applyPattern("#,###");
                    String formatted = formatter.format(parsed);

                    current = formatted;
                    editText.setText(formatted);
                    editText.setSelection(formatted.length()); // Đưa con trỏ về cuối
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            } else {
                current = "";
                editText.setText("");
            }

            editText.addTextChangedListener(this);
        }
    }

    // Hàm tiện ích để lấy giá trị số thực từ EditText đã format
    public static double getDoubleValue(EditText editText) {
        String original = editText.getText().toString().replaceAll("[^\\d]", "");
        if (original.isEmpty()) return 0;
        return Double.parseDouble(original);
    }
}
