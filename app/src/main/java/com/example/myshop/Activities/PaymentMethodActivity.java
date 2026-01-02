package com.example.myshop.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myshop.R;

public class PaymentMethodActivity extends AppCompatActivity {

    private RadioGroup radioGroupPayment;
    private Button btnConfirm;
    private String selectedMethod = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_method);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        radioGroupPayment = findViewById(R.id.radioGroupPayment);
        btnConfirm = findViewById(R.id.btnConfirmPayment);

        // Lấy phương thức hiện tại được truyền từ Checkout (để check sẵn)
        String currentMethod = getIntent().getStringExtra("CURRENT_METHOD");
        if (currentMethod != null) {
            if (currentMethod.equals("Google Pay")) {
                radioGroupPayment.check(R.id.rbGooglePay);
            } else if (currentMethod.equals("Banking")) {
                radioGroupPayment.check(R.id.rbBanking);
            } else {
                radioGroupPayment.check(R.id.rbCOD);
            }
        }

        btnConfirm.setOnClickListener(v -> {
            int selectedId = radioGroupPayment.getCheckedRadioButtonId();

            if (selectedId == -1) {
                Toast.makeText(this, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedId == R.id.rbCOD) {
                selectedMethod = "Thanh toán khi nhận hàng (COD)";
            } else if (selectedId == R.id.rbGooglePay) {
                selectedMethod = "Google Pay";
            } else if (selectedId == R.id.rbBanking) {
                selectedMethod = "Banking";
            }

            Intent returnIntent = new Intent();
            returnIntent.putExtra("SELECTED_PAYMENT_METHOD", selectedMethod);
            setResult(RESULT_OK, returnIntent);
            finish();
        });
    }
}
