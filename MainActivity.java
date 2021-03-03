package com.release.rsa_20;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.transition.Scene;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
//generate the private/public key  done
// // storage and retrieval of the keys now. The pub key should be uploaded to the db and now the private key is stored locally in base64url.
//Then be able to recognize if we're logged in and send to the main screen. - 1-4 hrs
//Be able to import contacts, and people to "Text" - 12-24hrs of work
//Then work out the messaging. :p

public class MainActivity extends AppCompatActivity {
    //where to save the privatekey
    private static final String FILE_NAME = "key.txt";

    //firebase check
    private FirebaseAuth Auth;
    private FirebaseUser firebaseUser;
    //database ref
    public DatabaseReference myRef;

    //declare scenese here.
    private Scene register = null;
    private Scene explore = null;
    private Scene message = null;
    private Scene splash = null;

    //to check if code has even been sent.
    boolean sendverf = false;

    //login ui
    public EditText editTextPhone, editTextCode, editTextName;
    String codeSent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registerpage);
        Auth = FirebaseAuth.getInstance();


        editTextCode = findViewById(R.id.editTextCode);
        editTextPhone = findViewById(R.id.editTextPhone);
        editTextName = findViewById(R.id.editTextName);

        //This is causing a false flag sometimes. So maybe we can fix it somehow?
        FirebaseUser firebasecheck = FirebaseAuth.getInstance().getCurrentUser();
        System.out.println("firebasecheck = " + firebasecheck);


        if(firebasecheck != null){
            System.out.println("User exists");
            Intent i = new Intent(MainActivity.this, messagingUI.class);
            startActivity(i);
            finish();
        }
        else{
            System.out.println("User doesn't exist");
        }



        findViewById(R.id.buttonGetVerificationCode).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println(editTextPhone.getText().toString());
                //For now jsut print numbers in the text field
                sendVerificationCode();
            }
        });


        findViewById(R.id.buttonSignIn).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                System.out.println(editTextCode.getText().toString());
                //For now just print the fucking numbers.
                if(sendverf) {
                    verifySignInCode();
                }
                else{
                    Toast.makeText(getApplicationContext(),
                            "Verification code not sent yet.", Toast.LENGTH_LONG).show();
                }
            }
        });

        //if exists then load message.class?
        //otherwise, keep on registration page.


    }



    //    public void onclick_changescene(View view) {


    public void verifySignInCode(){
        editTextCode = findViewById(R.id.editTextCode);
        String code = editTextCode.getText().toString();
        System.out.println("code = " + code);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeSent, code);
        System.out.println("Credential = " + credential);
        signInWithPhoneAuthCredential(credential);
    }
    public void sendVerificationCode(){
       
        editTextPhone = findViewById(R.id.editTextPhone);

        String phone = editTextPhone.getText().toString();
        

        System.out.println(phone);
        System.out.println("Phone number = " + phone);
        phone = "+1" + phone;
        System.out.println("Updated phone = " + phone);

        if(phone.isEmpty()){
            editTextPhone.setError("Phone number is required");
            editTextPhone.requestFocus();
            return;
        }

        if(phone.length() < 10 ){
            editTextPhone.setError("Please enter a valid phone number");
            editTextPhone.requestFocus();
            return;
        }
        System.out.println("Sending to veryphone function");
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phone,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }


    //https://www.youtube.com/watch?v=JZ8hwzBKsMM
    //Last 5 minutes for saving the login and change the scene to the "Search" area to see who I can go and message :)
    //https://www.youtube.com/watch?v=VVGuTDjsgcw
    //This is a good resource for literally everything lol
    //Then we can test by signing out, and creating another "dummy" account with a diff number. We'll generate the primes and message away. :)))



    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        Auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String public_key = null;
                            try {
                                public_key = keygen_and_return_public_key();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            editTextName = findViewById(R.id.editTextName);
                            Auth = FirebaseAuth.getInstance();
                            FirebaseUser firebaseUser = Auth.getCurrentUser();
                            String userid = firebaseUser.getUid();
                            System.out.println("auth = " + Auth.getCurrentUser());
                            System.out.println("userid = " + userid);
                            myRef = FirebaseDatabase.getInstance().getReference("MyUsers")
                                    .child(userid);

                            //get all the data needed.
                            HashMap<String, String> hashMap = new HashMap<>();
                            hashMap.put("id", userid);
                            if(editTextName.getText().toString() != null){
                                hashMap.put("name", editTextName.getText().toString());
                            }
                            else{
                                hashMap.put("name", "John Doe");
                            }
                            hashMap.put("status", "offline");
                            //pubkey is at a default -1 that will need to be updated when the keys are generated once signup is complete.
                            hashMap.put("pub_key", public_key);



                            //upload to firebase
                            myRef.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){

                                        Intent i = new Intent(MainActivity.this, messagingUI.class);
                                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(i);
                                        finish();

                                        System.out.println("Should be in db");
                                    }

                                }
                            });
                            Toast.makeText(getApplicationContext(),
                                    "Login Successfull", Toast.LENGTH_LONG).show();
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(getApplicationContext(),
                                        "Incorrect Verification Code ", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public String keygen_and_return_public_key() throws IOException {
    //uploads a base64 version of the private key here.
        System.out.println("On_upload started.. Generating Keys...");
        rsa key = new rsa();
        key.genkeys();
    String string_pubkey = key.get_publickey();
    String string_key = key.get_privatekey();
        FileOutputStream fos =null;
        try {
            fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
            fos.write(string_key.getBytes());
            System.out.println("Sucessfully saved key");
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            if(fos!=null){
                try {
                    fos.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        return key.get_publickey();
    }


    //load the private key.
    public void on_load(View v){
        FileInputStream fis = null;
        try{
            fis = openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;
            while((text = br.readLine()) != null){
                sb.append(text).append("\n");
            }

            if(sb != null)
            System.out.println("loaded key = " + sb.toString());
            else {
                System.out.println("Key doesn't exist :(");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(fis != null){
                try {
                    {
                        fis.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public String get_private_key(){
        FileInputStream fis = null;
        try {
            fis = openFileInput(FILE_NAME);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String text;
            while ((text = br.readLine()) != null) {
                sb.append(text).append("\n");
            }

            if (sb != null){
                System.out.println("loaded key = " + sb.toString());
                return sb.toString();
            }
            else {
                System.out.println("Key doesn't exist :(");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(fis != null){
                try {
                    {
                        fis.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("String not found, this is a big sad");
        return "string not found :(";
    }




    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            Toast.makeText(getApplicationContext(),
                    "Verification completed", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(getApplicationContext(),
                    "Verification failure, please try again.    ", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            sendverf = true;
            super.onCodeSent(s, forceResendingToken);
            codeSent = s;
        }
    };

    public void sendMessage(View view) {

        //send the ecnrypted message to the db, along with your public key so they can de-crypt.exe
        EditText myEditText = findViewById(R.id.editText);
        //mydatabase.push().setValue(myEditText.getText().toString());
        myEditText.setText("");
        //if the key isn't known then we'll just send it in plain text with an appended wanring message that this wasn't encrypted because the databse isn't good/free.

    }

    ViewGroup rootContainer;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onclick_changescene(View view) {
        //change scene
        rootContainer = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
        splash = Scene.getSceneForLayout(rootContainer, R.layout.signup, this);
        splash.enter();
        //splash constructor  This constructor is deprecated. use Scene(android.view.ViewGroup, android.view.View).
        //TransitionManager.go(splash);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onclick_changeToLogin(View view) {
        rootContainer = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
        //Login custom page? I'm not sure.
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onclick_changeToRegister(View view) {
        rootContainer = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
        register = Scene.getSceneForLayout(rootContainer, R.layout.registerpage, this);
        register.enter();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void onclick_changeToMessage(View view) {
        rootContainer = (ViewGroup) ((ViewGroup) this.findViewById(android.R.id.content)).getChildAt(0);
        message = Scene.getSceneForLayout(rootContainer, R.layout.messanger, this);
        message.enter();

    }
}