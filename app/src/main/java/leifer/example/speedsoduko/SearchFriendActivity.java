package leifer.example.speedsoduko;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import leifer.example.speedsoduko.dao.DaoGameBackend;
import leifer.example.speedsoduko.objects.Friend;
import leifer.example.speedsoduko.objects.User;

public class SearchFriendActivity extends AppCompatActivity {
    private EditText searchText;
    private User gameUser;
    private Handler handler;
    private LinearLayout friends_linearLayout;
    private List<User> results = new ArrayList<User>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friend);
        handler = new Handler();
        searchText = findViewById(R.id.text_search);
        friends_linearLayout = findViewById(R.id.friends_linearLayout);
        
        if (getIntent().getExtras() != null) {
            gameUser = (User) getIntent().getExtras().getSerializable("CurrentUser");
            mainLoop();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
    }

    private void mainLoop() {
        if (gameUser != null) {
//            Toast.makeText(this,"hello",Toast.LENGTH_SHORT).show();
            DaoGameBackend.uRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                            friends_linearLayout.removeAllViews();
                            results.clear();

                            for (DataSnapshot data : snapshot.getChildren()) {
                                User user = data.getValue(User.class);
                                if (!gameUser.equals(user)) {
                                    if (user.getUserName().contains(searchText.getText().toString())) {
                                        add_user_to_layout(user);
                                        results.add(user);
                                    }
                                } else {
                                    gameUser = user;
                                }

                            }
                            handler.postDelayed(SearchFriendActivity.this::mainLoop,1000);
                        }

                        @Override
                        public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

                        }
                    });
        } else handler.postDelayed(SearchFriendActivity.this::mainLoop,1000);
    }

    public void add_user_to_layout(User friend) {
        getLayoutInflater().inflate(R.layout.friend_pending,friends_linearLayout);
        int i = friends_linearLayout.getChildCount();
        LinearLayout linearLayout = (LinearLayout) friends_linearLayout.getChildAt(i - 1);
        TextView username = (TextView) linearLayout.getChildAt(0);
        TextView mmr = (TextView) linearLayout.getChildAt(1);

        username.setText(friend.getUserName());
        mmr.setText(String.valueOf(friend.getUserMMR()));

        linearLayout.getChildAt(2).setVisibility(View.INVISIBLE);
    }

    public void friend_add(View view) {
        User friend = findUserFromView(view);

        for (Friend f :gameUser.getFriends()) {
            if (friend.getUserKey().equals(f.getFriendKey())) {
                switch (f.getFriendStatus()) {
                    case "accepted":
                        Toast.makeText(this,"already friends",Toast.LENGTH_SHORT).show();
                        break;
                    case "pending":
                        int i = gameUser.getFriends().indexOf(f);
                        f.setFriendStatus("accepted");
                        gameUser.getFriends().set(i,f);
                        DaoGameBackend.uRef.child(gameUser.getUserKey()).child("friends").setValue(gameUser.getFriends());

                        for (Friend g : friend.getFriends()) {
                            if (g.getFriendKey().equals(gameUser.getUserKey())) {
                                int j = friend.getFriends().indexOf(g);
                                g.setFriendStatus("accepted");
                                friend.getFriends().set(j,g);
                                DaoGameBackend.uRef.child(friend.getUserKey()).child("friends").setValue(friend.getFriends());
                            }
                        }
                        Toast.makeText(this,"friend added",Toast.LENGTH_SHORT).show();
                        break;
                    default: //sent
                        Toast.makeText(this,"already sent friend request",Toast.LENGTH_SHORT).show();
                        break;
                }
                return;
            }
        }

        gameUser.addFriend(new Friend(friend,"sent"));
        DaoGameBackend.uRef.child(gameUser.getUserKey()).child("friends").setValue(gameUser.getFriends());

        friend.addFriend(new Friend(gameUser,"pending"));
        DaoGameBackend.uRef.child(friend.getUserKey()).child("friends").setValue(friend.getFriends());
    }

    private User findUserFromView(View view) {
        LinearLayout friendLayout = (LinearLayout) view.getParent();
        LinearLayout mainLayout = (LinearLayout) friendLayout.getParent();
        return results.get(mainLayout.indexOfChild(friendLayout));
    }

    public void onClickBack(View view) {
        finish();
    }
}