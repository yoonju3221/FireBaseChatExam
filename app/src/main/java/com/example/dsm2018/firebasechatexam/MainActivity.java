package com.example.dsm2018.firebasechatexam;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.dsm2018.firebasechatexam.model.ChatMessage;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private static final String MESSAGE_CHILD = "messages";

    private GoogleSignInClient mGoogleApiClient;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;

    private RecyclerView mRecyclerView;
    private EditText mMessageEditText;

    private String mUserName;
    private String mPhotoUrl;

    private String mCurrentUid;


    private FirebaseRecyclerAdapter<ChatMessage, MessageViewHolder> mFirebaseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.mainactivity_recyclerview_message);
        mMessageEditText = findViewById(R.id.mainactivity_edittext_message);

        GoogleSignInOptions gso = new
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = GoogleSignIn.getClient(MainActivity.this, gso);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mCurrentUid = mFirebaseUser.getUid();

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        mUserName = mFirebaseUser.getDisplayName();
        if(mFirebaseUser.getPhotoUrl() != null){
            mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
        }


        findViewById(R.id.mainactivity_button_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ChatMessage chatMessage = new
                        ChatMessage(mMessageEditText.getText().toString(), mUserName,mPhotoUrl, mCurrentUid);
                mDatabaseReference.child("messages").push().setValue(chatMessage);
                mMessageEditText.setText("");
            }
        });


        //쿼리 수행 위치
        Query query = mDatabaseReference.child(MESSAGE_CHILD);

        //옵션 설정
        FirebaseRecyclerOptions<ChatMessage> options =
                new FirebaseRecyclerOptions.Builder<ChatMessage>()
                .setQuery(query, ChatMessage.class)
                .build();

        //어댑터
        mFirebaseAdapter = new FirebaseRecyclerAdapter<ChatMessage, MessageViewHolder>(options) {
            @Override
            protected void onBindViewHolder(MessageViewHolder holder, int position, ChatMessage model) {
                holder.messageTextView.setText(model.getText());

                if((model.getUid() != null) && model.getUid().equals(mCurrentUid)){
                    //내가 전송한 메시지인 경우(현재 접속자가 보낸 메시지인 경우

                    //내 프로필의 사진과 이름은 안 보이게
                    holder.profileimageView.setVisibility(View.GONE);
                    holder.nameTextView.setVisibility(View.GONE);

                    //말풍선 변경
                    holder.messageTextView.setBackgroundResource(R.drawable.rightbubble);

                    holder.linearLayout.setGravity(Gravity.RIGHT);
                }
                else{
                    //남이 보낸 메시지인 경우
                    holder.profileimageView.setVisibility(View.VISIBLE);
                    if(model.getPhotoUrl() == null){
                        holder.profileimageView.setImageDrawable(ContextCompat.getDrawable(MainActivity.this,
                                R.drawable.ic_account_circle_black_24dp));
                    }else{
                        Glide.with(MainActivity.this)
                                .load(model.getPhotoUrl()).into(holder.profileimageView);
                    }

                    //말풍선 변경
                    holder.messageTextView.setBackgroundResource(R.drawable.leftbubble);

                    //이름 화면에 출력
                    holder.nameTextView.setVisibility(View.VISIBLE);
                    holder.nameTextView.setText(model.getName());

                    holder.linearLayout.setGravity(Gravity.LEFT);
                }



            }

            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.item_message,viewGroup, false);
                return new MessageViewHolder(view);
            }
        };

        //리사이클러뷰에 레이아웃 매니저와 어댑터 설정 - 화면에 채팅 보여줌
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mFirebaseAdapter);

        //자동으로 올라가게 해줌
        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int messageCount = mFirebaseAdapter.getItemCount();
                LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                int lastVisiblePosition = layoutManager.findLastCompletelyVisibleItemPosition();

                if(lastVisiblePosition == -1 ||
                        (positionStart >= (messageCount -1) && lastVisiblePosition == (positionStart -1))){
                    mRecyclerView.scrollToPosition(positionStart);
                }
            }
        });
        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener(){
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if(bottom < oldBottom){
                    mRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mRecyclerView.scrollToPosition(mFirebaseAdapter.getItemCount());
                        }
                    }, 100);
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        //실시간 쿼리 시작
        mFirebaseAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFirebaseAdapter.stopListening();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder{
        CircleImageView profileimageView;
        TextView messageTextView;
        TextView nameTextView;
        LinearLayout linearLayout;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            profileimageView = itemView.findViewById(R.id.itemmessage_imageview_profile);
            messageTextView = itemView.findViewById(R.id.itemmessage_textview_message);
            nameTextView = itemView.findViewById(R.id.itemmessage_textview_name);
            linearLayout = itemView.findViewById(R.id.wrapper);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.main_menu_signout :
                mFirebaseAuth.signOut();

                mGoogleApiClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        startActivity(new Intent(MainActivity.this, SigninActivity.class));
                        finish();
                    }
                });
                return true;
                default:
                    return super.onOptionsItemSelected(item);
        }
    }


}
