package com.example.socialnetwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyPostsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView myPostsList;
    private FirebaseAuth mAuth;
    private DatabaseReference PostsRef;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        mToolbar = (Toolbar) findViewById(R.id.my_posts_bar_layout);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("My Posts");

        myPostsList = (RecyclerView) findViewById(R.id.my_all_posts_list);
        myPostsList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myPostsList.setLayoutManager(linearLayoutManager);
        
        DisplayMyAllPosts();
    }

    private void DisplayMyAllPosts()
    {
        Query myPostsQuery = PostsRef.orderByChild("uid")
                .startAt(currentUserID).endAt(currentUserID + "\uf8ff");

        FirebaseRecyclerOptions<Posts> options =
                new FirebaseRecyclerOptions.Builder<Posts>()
                        .setQuery(myPostsQuery, Posts.class)
                        .build();

        FirebaseRecyclerAdapter<Posts, MyPostsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Posts, MyPostsViewHolder>(options)
        {
            @Override
            protected void onBindViewHolder(MyPostsActivity.MyPostsViewHolder holder, int position, Posts model)
            {
                holder.setFullname(model.getFullname());
                holder.setTime(model.getTime());
                holder.setDate(model.getDate());
                holder.setDescription(model.getDescription());
                holder.setProfileimage(model.getProfileimage());
                holder.setPostimage(model.getPostimage());
            }

            @Override
            public MyPostsViewHolder onCreateViewHolder( ViewGroup parent, int viewType)
            {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.all_posts_layout, parent, false);
                return new MyPostsViewHolder(view);
            }
        };
        firebaseRecyclerAdapter.startListening();
        myPostsList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class MyPostsViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public MyPostsViewHolder( View itemView)
        {
            super(itemView);

            mView = itemView;
        }
        public void setFullname(String fullname)
        {
            TextView username = (TextView) mView.findViewById(R.id.post_user_name);
            username.setText(fullname);
        }
        public void setProfileimage( String profileimage)
        {
            CircleImageView image = (CircleImageView) mView.findViewById(R.id.post_profile_image);
            Picasso.get().load(profileimage).into(image);
        }
        public void setTime(String time)
        {
            TextView PostTime = (TextView) mView.findViewById(R.id.post_time);
            PostTime.setText("    " + time);
        }

        public void setDate(String date)
        {
            TextView PostDate = (TextView) mView.findViewById(R.id.post_date);
            PostDate.setText("    " + date);
        }

        public void setDescription(String description)
        {
            TextView PostDescription = (TextView) mView.findViewById(R.id.post_description);
            PostDescription.setText(description);
        }

        public void setPostimage(String postimage)
        {
            ImageView PostImage = (ImageView) mView.findViewById(R.id.post_image);
            Picasso.get().load(postimage).placeholder(R.drawable.profile).into(PostImage);
        }
    }
}