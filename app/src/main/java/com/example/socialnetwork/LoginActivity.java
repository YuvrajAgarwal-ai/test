package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity
{
    private Button LoginButton;
    private EditText UserEmail, UserPassword;
    private TextView NeedNewAccountLink, ForgetPasswordLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;

    private ImageView googleSignInButton;
    private GoogleSignInClient mGoogleSignInClient;

    public static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        NeedNewAccountLink = (TextView) findViewById(R.id.register_account_link);
        UserEmail= (EditText) findViewById(R.id.login_email);
        UserPassword= (EditText) findViewById(R.id.login_password);
        LoginButton= (Button) findViewById(R.id.login_button);
        ForgetPasswordLink = (TextView) findViewById(R.id.forget_password_link);
        loadingBar = new ProgressDialog(this);

        googleSignInButton = (ImageView) findViewById(R.id.google_signin_button);

        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                SendUserTpRegisterActivity();
            }
        });

        ForgetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AllowingUserToLogin();
            }
        });

        requestGoogleSignIn();
        googleSignInButton.setOnClickListener(view ->{
            signIn();
        });

    }

    private void requestGoogleSignIn()
    {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            loadingBar.setTitle("Google Sign-In");
            loadingBar.setMessage("Please wait, while you are being logged into your Google Account.");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
                Toast.makeText(this, "Please wait while you are being authenticated.", Toast.LENGTH_SHORT).show();
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Choose an account.", Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            SendUserToMainActivity();
                            loadingBar.dismiss();

                        } else {
                            // If sign in fails, display a message to the user.
                            String message = task.getException().getMessage();
                            SendUserToLoginActivity();
                            Toast.makeText(LoginActivity.this, "Authentication failed: "+message, Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(LoginActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        FirebaseUser currentUser= mAuth.getCurrentUser();

        if(currentUser != null)
        {
            SendUserToMainActivity();
        }
    }

    private void AllowingUserToLogin()
    {
        String email = UserEmail.getText().toString();
        String password = UserPassword.getText().toString();

        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this, "Enter the email", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this, "Enter the password", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Logging In");
            loadingBar.setMessage("Please wait, you are being Logged In");
            loadingBar.show();
            loadingBar.setCanceledOnTouchOutside(true);

            mAuth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if(task.isSuccessful())
                            {
                                SendUserToMainActivity();
                                Toast.makeText(LoginActivity.this, "You are Logged In successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else
                            {
                                String message = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error occurred: " + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }

    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserTpRegisterActivity()
    {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }
}