package com.example.smarthire;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smarthire.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.content.SharedPreferences;
import android.content.Context;

public class Login extends AppCompatActivity {
    Button btConfirm,btRegister;
    DatabaseReference reff;
    String userID;
    private ActivityLoginBinding binding;
    private SignInButton signInButton;
    GoogleSignInClient mGoogleSignInClient;
    private String TAG = "Login";
    private FirebaseAuth mAuth;
    private int RC_SIGN_IN = 1;
    private int loginStatus=0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View v = binding.getRoot();

        setContentView(v);

        /*
        btConfirm = findViewById(R.id.button_login);
        btRegister = findViewById(R.id.button_register);
        final EditText editText_username = findViewById(R.id.editText_username);
        final EditText editText_password = findViewById(R.id.editText_password);

         */


        binding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){


                //reff = FirebaseDatabase.getInstance().getReference().child("User").child("1");
                reff = FirebaseDatabase.getInstance().getReference().child("User");
                //mAuth = FirebaseAuth.getInstance();
                //userID = mAuth.getCurrentUser().getUid();



                reff.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        loginStatus = 0;
                        for (DataSnapshot ds: dataSnapshot.getChildren()){
                            //Toast.makeText(Main4Activity.this,ds.child("userId").getValue().toString(),Toast.LENGTH_LONG).show();
                            if (binding.editTextUsername.getText().toString().equals(ds.child("userId").getValue().toString())
                                    && binding.editTextPassword.getText().toString().equals(ds.child("password").getValue().toString())){
                                loginStatus = 1;
                                SharedPreferences sharedPref = getSharedPreferences("PREF",Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("USER_ID",ds.child("userId").getValue().toString());
                                editor.commit();


                                Intent intent = new Intent(getApplicationContext(), BottomNavigationActivity.class);
                                intent.putExtra("USER_ID",ds.child("userId").getValue().toString());
                                intent.putExtra("USER_NAME",ds.child("name").getValue().toString());
                                intent.putExtra("USER_EMAIL",ds.child("email").getValue().toString());
                                intent.putExtra("USER_CONTACT",ds.child("contact").getValue().toString());
                                intent.putExtra("LOGIN_STATUS",loginStatus);
                                startActivity(intent);
                            }
                        }
                        if (loginStatus!=1){
                            Toast.makeText(Login.this,"Login Failed: Invalid username or password",Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                //

            }

        });

        binding.buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);

            }

        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        binding.buttonGoogleSIgnIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                signInGoogle();
            }

        });
    }

    private void signInGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            SharedPreferences sharedPref = getSharedPreferences("PREF",Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("USER_ID",account.getDisplayName());
            editor.commit();

            Toast.makeText(Login.this,"Welcome" + account.getDisplayName(), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Login.this, BottomNavigationActivity.class);
            startActivity(intent);


        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(Login.this,"Failed: " + e.getStatusCode(), Toast.LENGTH_LONG).show();

        }
    }
}
