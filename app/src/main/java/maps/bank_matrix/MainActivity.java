package maps.bank_matrix;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;


public class MainActivity extends AppCompatActivity {
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Executor executor = ContextCompat.getMainExecutor(this);

        biometricPrompt = new BiometricPrompt(MainActivity.this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Toast.makeText(getApplicationContext(), "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                        advanceToMatrix();
                    }

                });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Matrix Biometric login")
                .setSubtitle("Log in using your biometric login or default phone credentials")
                .setDeviceCredentialAllowed(true)
                .build();

        // Login either with app open or click shield
        ImageView fingerprintIcon = findViewById(R.id.fingerprintIcon);
        fingerprintIcon.setOnClickListener(view -> biometricPrompt.authenticate(promptInfo));
        biometricPrompt.authenticate(promptInfo);
    }

    public void advanceToMatrix() {
        Intent intentMatrix = new Intent(this, MatrixList.class);
        startActivity(intentMatrix);
    }

}

