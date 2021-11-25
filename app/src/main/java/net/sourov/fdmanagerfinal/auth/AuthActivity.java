package net.sourov.fdmanagerfinal.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import net.sourov.fdmanagerfinal.Dashboard;
import net.sourov.fdmanagerfinal.R;
import net.sourov.fdmanagerfinal.Sourov;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class AuthActivity extends AppCompatActivity {

    CountryCodePicker ccp;
    TextView countryCodeTextOnAuth;
    EditText inputNumberOnAuth, inputCodeOnVerify;
    CardView verifyContainer;

    FirebaseAuth mAuth;
    private String mVerificationId;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 4;
    Sourov sourov;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        sourov = new Sourov(AuthActivity.this);

        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) //ignore this error
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        ccp = findViewById(R.id.countryCodePickerOnAuth);
        countryCodeTextOnAuth = findViewById(R.id.countryCodeTextOnAuth);
        inputNumberOnAuth = findViewById(R.id.inputNumberOnAuth);
        verifyContainer = findViewById(R.id.verifyContainer);
        inputCodeOnVerify = findViewById(R.id.inputCodeOnVerify);


        ccp.registerCarrierNumberEditText(inputNumberOnAuth);
        ccp.setPhoneNumberValidityChangeListener(isValidNumber -> {
            String str = ccp.getSelectedCountryCodeWithPlus();


            countryCodeTextOnAuth.setText(str);

        });

        findViewById(R.id.verifyBtnOnAuth).setOnClickListener(v -> {
            sourov.spinner().show();
            sendCodeToUser(ccp.getFullNumberWithPlus());

        });

        findViewById(R.id.googleImageOnAuth).setOnClickListener(v -> {
            sourov.spinner().show();
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            sourov.spinner().dismiss();
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        sourov.spinner().show();
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    sourov.spinner().dismiss();
                    if (task.isSuccessful()) {
                        if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                            Toast.makeText(AuthActivity.this, "Account Created with the name " + mAuth.getCurrentUser().getDisplayName(), Toast.LENGTH_SHORT).show();
                        }
                        startActivity(new Intent(AuthActivity.this, Dashboard.class));
                        finish();


                    } else {
                        Toast.makeText(this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
    }


    private void sendCodeToUser(String fullNumber) {

        PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onCodeSent(@NonNull @NotNull String s, @NonNull @NotNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                createPopupDialog();
                sourov.spinner().dismiss();
                mVerificationId = s;
                createPopupDialog();


            }

            @Override
            public void onVerificationCompleted(@NonNull @NotNull PhoneAuthCredential phoneAuthCredential) {

                final String code = phoneAuthCredential.getSmsCode();
                if (code != null) {
                    inputCodeOnVerify.setText(code);
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                    signInWithPhoneAuthCredential(credential);
                }

            }

            @Override
            public void onVerificationFailed(@NonNull @NotNull FirebaseException e) {

                Toast.makeText(AuthActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(fullNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void createPopupDialog() {
        verifyContainer.setVisibility(View.VISIBLE);
        Animation sgAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in_up);
        verifyContainer.setAnimation(sgAnimation);

        findViewById(R.id.cancelPopupBtnOnVerify).setOnClickListener(v -> {
            Animation slide_out_up = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out_up);
            verifyContainer.setAnimation(slide_out_up);
            verifyContainer.setVisibility(View.GONE);
        });
        findViewById(R.id.buttonOnVerify).setOnClickListener(v -> {
            String codeByUser = inputCodeOnVerify.getText().toString().trim();
            if (codeByUser.length() == 6) {
                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, codeByUser);
                signInWithPhoneAuthCredential(credential);
            } else {
                Toast.makeText(this, "Code must be 6 digit long", Toast.LENGTH_SHORT).show();
            }

        });


    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        sourov.spinner().show();

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    sourov.spinner().dismiss();
                    if (task.isSuccessful()) {

                        startActivity(new Intent(AuthActivity.this, Dashboard.class));
                        finish();


                    } else {
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Toast.makeText(AuthActivity.this, "code invalid", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(AuthActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    }

                });
    }


    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(AuthActivity.this, Dashboard.class));
            finish();
        }
    }
}