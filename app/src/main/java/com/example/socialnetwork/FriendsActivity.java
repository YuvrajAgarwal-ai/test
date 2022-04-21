package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsActivity extends AppCompatActivity
{
    private RecyclerView myFriendList;
    private DatabaseReference FriendsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String online_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        myFriendList = (RecyclerView) findViewById(R.id.friend_list);
        myFriendList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        DisplayAllFriends();
    }

    private void DisplayAllFriends()
    {
        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>()
                .setQuery(FriendsRef, Friends.class)
                .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {

            @NonNull

            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_users_display_layout, parent, false);
                return new FriendsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull FriendsActivity.FriendsViewHolder holder, int position, @NonNull Friends model)
            {
                holder.setDate(model.getDate());
                final String usersIDs = getRef(position).getKey();

                UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot)
                    {
                        final String userName = snapshot.child("fullname").getValue().toString();
                        final String profileImage = snapshot.child("profileimage").getValue().toString();

                        holder.setFullname(userName);
                        holder.setProfileimage(profileImage);

                        holder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                CharSequence option[]= new CharSequence[]
                                        {
                                            userName + "'s Profile",
                                            "Send Message"
                                        };

                                AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                builder.setTitle("Select Option");

                                builder.setItems(option, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        if (which == 0)
                                        {
                                            Intent profileintent = new Intent(FriendsActivity.this, PersonProfileActivity.class);
                                            profileintent.putExtra("visit_user_id", usersIDs);
                                            startActivity(profileintent);
                                        }
                                        if (which == 1)
                                        {
                                            Intent chatintent = new Intent(FriendsActivity.this, ChatActivity.class);
                                            chatintent.putExtra("visit_user_id", usersIDs);
                                            chatintent.putExtra("userName", userName);
                                            startActivity(chatintent);
                                        }
                                    }
                                });
                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        };
        adapter.startListening();
        myFriendList.setAdapter(adapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;
        public FriendsViewHolder(View itemView)
        {
            super(itemView);

            mView = itemView;
        }
        public void setProfileimage(String profileimage)
        {
            CircleImageView myImage = (CircleImageView) mView.findViewById(R.id.all_users_profile_image);
            Picasso.get().load(profileimage).placeholder(R.drawable.profile).into(myImage);
        }
        public void setFullname(String fullname)
        {
            TextView myName = (TextView) mView.findViewById(R.id.all_users_profile_full_name);
            myName.setText(fullname);
        }
        public void setDate(String date)
        {
            TextView friendsDate = (TextView) mView.findViewById(R.id.all_users_status);
            friendsDate.setText("Friends since: " + date);
        }
    }
}