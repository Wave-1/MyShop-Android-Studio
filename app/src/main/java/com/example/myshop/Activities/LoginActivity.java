package com.example.myshop.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myshop.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private EditText edtEmail, edtPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private ImageButton btnGoogleLogin;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // --- Ánh xạ View ---
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegister = findViewById(R.id.tvRegister);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);

        // --- Khởi tạo Firebase và Google Sign-In ---
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        configureGoogleSignIn();

        // --- Cấu hình Launcher cho Google Sign-In ---
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        try {
                            GoogleSignInAccount account = task.getResult(ApiException.class);
                            firebaseAuthWithGoogle(account);
                        } catch (ApiException e) {
                            Log.w(TAG, "Google sign in failed", e);
                            Toast.makeText(this, "Đăng nhập Google thất bại: " + e.getStatusCode(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        setupClickListeners();

        String preEmail = getIntent().getStringExtra("email");
        if (preEmail != null) {
            edtEmail.setText(preEmail);
        }
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> loginWithEmail());
        btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginWithEmail() {
        String email = edtEmail.getText().toString().trim();
        String pass = edtPassword.getText().toString().trim();

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(authResult -> {
                    handleSuccessfulLogin(authResult.getUser());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Sai tài khoản hoặc mật khẩu!", Toast.LENGTH_SHORT).show();
                });
    }

    private void configureGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnSuccessListener(this, authResult -> {
                    handleSuccessfulLogin(authResult.getUser());
                })
                .addOnFailureListener(this, e -> {
                    Toast.makeText(LoginActivity.this, "Xác thực Firebase thất bại.", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "signInWithCredential failed", e);
                });
    }

    private void handleSuccessfulLogin(FirebaseUser firebaseUser) {
        if (firebaseUser == null) return;

        db.collection("users")
                .document(firebaseUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        saveToPrefsAndNavigate(role, firebaseUser.getEmail());
                    } else {
                        createNewUserInFirestore(firebaseUser);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error getting user document", e);
                });
    }

    private void createNewUserInFirestore(FirebaseUser firebaseUser) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", firebaseUser.getUid());
        userData.put("name", firebaseUser.getDisplayName());
        userData.put("email", firebaseUser.getEmail());
        userData.put("profileImage", firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "");
        userData.put("role", "user");

        db.collection("users")
                .document(firebaseUser.getUid())
                .set(userData, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User profile created in Firestore.");
                    saveToPrefsAndNavigate("user", firebaseUser.getEmail());
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error creating user profile", e);
                    Toast.makeText(this, "Lỗi tạo hồ sơ người dùng.", Toast.LENGTH_SHORT).show();
                });
    }

    private void saveToPrefsAndNavigate(String role, String email) {
        SharedPreferences prefs = getSharedPreferences("MyShop", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("userRole", role);
        editor.putBoolean("isLoggedIn", true);
        editor.putString("email", email);
        editor.apply();

        if ("admin".equals(role)) {
            Toast.makeText(this, "Xin chào Admin!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ManageProductsActivity.class));
        } else {
            Toast.makeText(this, "Xin chào: " + email, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, HomeActivity.class));
        }
        finishAffinity();
    }
}
