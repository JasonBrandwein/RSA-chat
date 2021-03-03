package com.release.rsa_20;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.release.rsa_20.Adapters.MessageAdapter;
import com.release.rsa_20.MODELS.Chat;
import com.release.rsa_20.MODELS.Users;
import com.google.firebase.FirebaseError;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessagingActivity extends AppCompatActivity {

    TextView username;
    ImageView imageView;
    TextView pub_key_text_view;


    RecyclerView recyclerViewy;
    EditText msg_editText;
    Button sendBtn;

    FirebaseUser fuser;
    DatabaseReference reference;
    Intent intent;


    MessageAdapter messageAdapter;
    List<Chat> mchat;



    RecyclerView recyclerView;
    String userid;

    String pubkey = "nonnull";
    String pull_pub_key = "nonnull";


    ValueEventListener seenListener;



    //FOR FIREBASE

    public interface OnGetDataListener {
        //this is for callbacks
        void onSuccess(DataSnapshot dataSnapshot);
        void onStart();
        void onFailure();
    }


    public void pull_pubkey(DatabaseReference ref, final OnGetDataListener listener) {
        listener.onStart();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listener.onSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
            public void onCancelled(FirebaseError firebaseError) {
                listener.onFailure();
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //make a context variable for the key.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);

        // Widgets
        username  = findViewById(R.id.usernamey);
        pub_key_text_view = findViewById(R.id.pub_key_text_view);
        sendBtn = findViewById(R.id.btn_send);
        msg_editText = findViewById(R.id.text_send);


        // RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);



        intent = getIntent();
        System.out.println("intent = " + intent);


        userid = intent.getStringExtra("userid");
        System.out.println("userid = " + intent);
        //I pulled from mysers/null. so uderid is fucked?


        pull_pubkey(FirebaseDatabase.getInstance().getReference().child("MyUsers/"+FirebaseAuth.getInstance().getCurrentUser().getUid()), new OnGetDataListener() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                //System.out.println("datasnapshot = " + dataSnapshot);
                pull_pub_key = dataSnapshot.child("pub_key").getValue().toString();
                //System.out.println("pull_pub = " + pull_pub_key);
                //This is able to get the pub key but it's still "too slow"
            }

            @Override
            public void onStart() {
                Log.d("ONSTART", "Started");
            }

            @Override
            public void onFailure() {
                Log.d("onFailure", "Failed");
            }

        });









        reference = FirebaseDatabase.getInstance().getReference("MyUsers").child(String.valueOf(userid));
        System.out.println("ref = " + reference);
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users user  = dataSnapshot.getValue(Users.class);
                //I'm just pulling null objects.
                if(user != null) {
                    System.out.println(user.toString());

                    pubkey = user.getPub_key();
                    //user.getname is bad?
                    username.setText(user.getName());




                    readMessages(fuser.getUid(), userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                String msg = msg_editText.getText().toString();
                if (!msg.equals("")){
                    //I don't think I'm properly grabbing the pubkey of the person we're chatting with. :(
                    System.out.print("pubkey = " + pubkey);
                    //we have pub key so we need to turn it back into a big int.
                    BigInteger pub_key = rsa.base64decoder(pubkey);
                    System.out.println("pub_key = " + pub_key);
                    msg = rsa.send_encrypt(msg, pub_key);
                    System.out.println("fuser = " + fuser) ;
                    sendMessage(fuser.getUid(), userid, msg);
                }

                else
                {
                    Toast.makeText(MessagingActivity.this, "Please send a non empty message", Toast.LENGTH_SHORT).show();
                }

                msg_editText.setText("");
            }
        });


        SeenMessage(userid);
    }


    private void SeenMessage(final String userid){


        reference = FirebaseDatabase.getInstance().getReference("Chats");

        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                for (DataSnapshot snapshot : dataSnapshot.getChildren()){



                    Chat chat = snapshot.getValue(Chat.class);


                    if(fuser.getUid() == null){
                        System.out.println("Fuser is null");
                    }
                    if(chat.getSender().equals(userid)){
                        System.out.println("Chatgetsender is null");
                    }
                    if(chat.getReceiver() == null  ){
                        System.out.println("This is null");
                    }

                    if(chat.getReceiver().equals(fuser.getUid()) && chat.getSender().equals(userid) ){


                        HashMap<String, Object> hashMap = new HashMap<>();

                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);


                    }


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }




    private void sendMessage(String sender, String receiver, String message){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("isseen",false);


        reference.child("Chats").push().setValue(hashMap);


        // Adding User to chat fragment: Latest Chats with contacts
        final DatabaseReference chatRef = FirebaseDatabase.getInstance()
                .getReference("ChatList")
                .child(fuser.getUid())
                .child(userid);


        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }

    Boolean gotkey = false;
    private void readMessages(final String myid, final String userid){
        mchat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mchat.clear();

                //we can pull the pubkey and private key here instead of doing it multiple times.



                for (DataSnapshot snapshot : dataSnapshot.getChildren()){

                    Chat chat = snapshot.getValue(Chat.class);


                    /*
                    commented out to see if my bootleg plan works.
                    if(chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                            chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                        //two cases here. One is when the reciever gets it , and one where the sender gets the message.
                        //If it's the sender, we'll keep it in "encrypted form"
                        //If it's the reciver we'll decrypt it using the private key that's stored locally.
                        mchat.add(chat);
                        System.out.println("reciever = " + chat.getReceiver());
                        System.out.println("sender = " + chat.getSender());

                    }
                     */
                    if(chat.getReceiver().equals(myid) && chat.getSender().equals(userid)){
                        //we decrypt the message
                        rsa key_set_up = new rsa();

                        //step 1 get your private key and public keys.

                        //My grab doesn't work. Fk
                        //todo fix this shit.
                        String privatekey = get_private_key().toString();

                        System.out.println("privkey = " + privatekey);
                        System.out.println("base64 = " + key_set_up.base64decoder(privatekey));


                        System.out.println("pullpubkey = " + pull_pub_key);
                        key_set_up.setPrivatekey(key_set_up.base64decoder(privatekey));
                        System.out.println("keys = " + key_set_up.toString());


                        //step 2 decrypt

                        String message = chat.getMessage();
                        //decode from base64
                        BigInteger big_message = rsa.base64decoder(message);
                        //then modpow and return string

                            message = rsa.send_decrypt(big_message, privatekey, pull_pub_key);



                        //then push the string
                        chat.setMessage(message);
                        mchat.add(chat);
                        //this doesn't add the decrypt message yet.
                    }
                    if(chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                        mchat.add(chat);
                        //we keep the message raw, since we can't decrypt the message at all.
                        //only 1 step and that's adding it to the chat.
                    }

                    messageAdapter = new MessageAdapter(MessagingActivity.this, mchat);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private static final String FILE_NAME = "key.txt";


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


    private void CheckStatus(String status){

        reference  = FirebaseDatabase.getInstance().getReference("MyUsers").child(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }


    @Override
    protected void onResume() {
        super.onResume();
        CheckStatus("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        CheckStatus("Offline");
    }




}
