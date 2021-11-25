package net.sourov.fdmanagerfinal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;

import net.sourov.fdmanagerfinal.auth.AuthActivity;

public class SplashhScreen extends AppCompatActivity {
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mAuth.getCurrentUser() != null) {
                    startActivity(new Intent(SplashhScreen.this, Dashboard.class));
                    finish();
                }else {
                    startActivity(new Intent(SplashhScreen.this, AuthActivity.class));
                    finish();
                }
            }
        },2000);
    }




}