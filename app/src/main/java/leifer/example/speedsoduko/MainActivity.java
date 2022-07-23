package leifer.example.speedsoduko;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import leifer.example.speedsoduko.dao.DAOGame;
import leifer.example.speedsoduko.dao.DAOUser;
import leifer.example.speedsoduko.dao.DaoGameBackend;
import leifer.example.speedsoduko.objects.Friend;
import leifer.example.speedsoduko.objects.Game;
import leifer.example.speedsoduko.objects.GameRequest;
import leifer.example.speedsoduko.objects.Queue;
import leifer.example.speedsoduko.objects.User;

public class MainActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private boolean lookingForGame = false;
    private boolean lookingForFriendGame = false;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseUser firebaseUser;
    private Button btnSign, btnFindGame, btnOffline, btnLeaderboard, btnSearch;
    private TextView txtLabel, txtLabelUserName, txtLabelMmr;
    private DAOUser daoUsers;
    private User gameUser;
    private Handler handler = new Handler();
    private Handler handlerNS = new Handler();
    private int ticks;
    private LinearLayout friends_linearLayout;
    private Friend friendRemoval = null;
    private Friend friend = null;
    private GameRequest gr_main = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        btnSign = findViewById(R.id.btn_sign_in_and_out);
        btnSearch = findViewById(R.id.btn_search);
        btnFindGame = findViewById(R.id.btn_find_game);
        btnOffline = findViewById(R.id.btn_offline_game);
        btnLeaderboard = findViewById(R.id.btn_leaderboard);
        txtLabel = findViewById(R.id.txt_label);
        txtLabelUserName = findViewById(R.id.txt_label_username);
        txtLabelMmr = findViewById(R.id.txt_label_mmr);
        friends_linearLayout = findViewById(R.id.friends_linearLayout);

        daoUsers = new DAOUser();
        getRequest();

        firebaseUser = mAuth.getCurrentUser();
        if (firebaseUser != null) {
            btnSign.setEnabled(false);
            btnSearch.setVisibility(View.INVISIBLE);
            btnSign.setText("signing in...");
            daoUsers.get().addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                    boolean check = true;
                    for (DataSnapshot data : snapshot.getChildren()) {
                        if (data.getValue(User.class).getUserID().equals(firebaseUser.getUid())) {

                            check = false;
                            gameUser = data.getValue(User.class);
                            Toast.makeText(MainActivity.this,"Signed back in",Toast.LENGTH_SHORT).show();

                            DaoGameBackend.removeGarbage(gameUser);
                            DaoGameBackend.removeGames(gameUser);


                            txtLabel.setText("welcome back " + firebaseUser.getDisplayName());
                            txtLabelUserName.setVisibility(View.VISIBLE);
                            txtLabelUserName.setText(gameUser.getUserName());
                            txtLabelMmr.setVisibility(View.VISIBLE);
                            txtLabelMmr.setText("Rating: " + String.valueOf(gameUser.getUserMMR()));
                            btnSign.setText("Sign out");
                            btnSign.setEnabled(true);
                            btnSearch.setVisibility(View.VISIBLE);

                            friends_linearLayout.removeAllViews();
                            for (Friend f : gameUser.getFriends()) {
                                add_friend_to_layout(f);
                            }
                        }
                    }
                    if (check) {
                        onClickSign(null);
                    }
                }

                @Override
                public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {
                }
            });
        }


        mainLoop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacksAndMessages(null);
        if (gameUser != null) {
            System.out.println("onPaused activated");


            DaoGameBackend.removeGarbage(gameUser);
            lookingForGame = false;
            lookingForFriendGame = false;

            btnFindGame.setText("find game");

            btnSign.setEnabled(false);
            btnSearch.setVisibility(View.INVISIBLE);
            btnFindGame.setEnabled(false);
            btnOffline.setEnabled(false);
            btnLeaderboard.setEnabled(false);
            handlerNS.postDelayed(() -> {
                btnSign.setEnabled(true);
                btnSearch.setVisibility(View.VISIBLE);
                btnFindGame.setEnabled(true);
                btnOffline.setEnabled(true);
                btnLeaderboard.setEnabled(true);
            },2000);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.removeCallbacksAndMessages(null);
        mainLoop();
        friend = null;
        gr_main = null;
        lookingForGame = false;
        lookingForFriendGame = false;
        findViewById(R.id.waiting_popup).setVisibility(View.GONE);

    }

    private void mainLoop() {
        if (gameUser != null) {
//            Toast.makeText(this,"hello",Toast.LENGTH_SHORT).show();
            DaoGameBackend.uRef.child(gameUser.getUserKey())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                            gameUser = snapshot.getValue(User.class);
                            txtLabelUserName.setText(gameUser.getUserName());
                            txtLabelMmr.setText("Rating: " + String.valueOf(gameUser.getUserMMR()));

                            friends_linearLayout.removeAllViews();
                            for (Friend f : gameUser.getFriends()) {
                                add_friend_to_layout(f);
                            }
                            View gr_popUp = findViewById(R.id.gr);
                            DaoGameBackend.grRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {


                                    for (DataSnapshot data : snapshot.getChildren()) {
                                        GameRequest gameRequest = data.getValue(GameRequest.class);
                                        if (gameUser.equals(gameRequest.getUserReceived())) {
                                            TextView grPopUp_txt = (TextView) findViewById(R.id.grPopUp_txt);
                                            gr_popUp.setVisibility(View.VISIBLE);
                                            grPopUp_txt.setText(gameRequest.getUserSender().getUserName() + " (" +
                                                    gameRequest.getUserSender().getUserMMR() + ") wants a game");
                                            gr_main = gameRequest;
                                            handler.postDelayed(MainActivity.this::mainLoop,1000);
                                            return;
                                        }
                                    }
                                    gr_popUp.setVisibility(View.GONE);
                                    gr_main = null;
                                    handler.postDelayed(MainActivity.this::mainLoop,1000);
                                }

                                @Override
                                public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

                        }
                    });


        } else handler.postDelayed(MainActivity.this::mainLoop,1000);
    }

    public void add_friend_to_layout(Friend friend) {

        if (friend.getFriendStatus().equals("accepted")) {
            getLayoutInflater().inflate(R.layout.friend_accepted,friends_linearLayout);
        } else if (friend.getFriendStatus().equals("pending")) {
            getLayoutInflater().inflate(R.layout.friend_pending,friends_linearLayout);
        } else {
            getLayoutInflater().inflate(R.layout.friend_sent,friends_linearLayout);
        }
        int i = friends_linearLayout.getChildCount();
        LinearLayout linearLayout = (LinearLayout) friends_linearLayout.getChildAt(i - 1);
        TextView username = (TextView) linearLayout.getChildAt(0);
        TextView mmr = (TextView) linearLayout.getChildAt(1);

        username.setText(friend.getFriendName());
        mmr.setText(String.valueOf(friend.getFriendMMR()));
    }

    public void friend_remove(View view) {
        handler.removeCallbacksAndMessages(null);

        Friend friend = findFriendFromView(view);

        friendRemoval = friend;
        findViewById(R.id.remove_popup).setVisibility(View.VISIBLE);
        TextView textView = (TextView) findViewById(R.id.txt_label_popup);
        switch (friend.getFriendStatus()) {
            case "accepted":
                textView.setText("are you sure you want to unfriend " + friend.getFriendName() + " ?");
                break;
            case "pending":
                textView.setText("are you sure you want to deny " + friend.getFriendName() + " friend request ?");
                break;
            default: //sent
                textView.setText("are you sure you want to cancel " + friend.getFriendName() + " friend request ?");
                break;
        }

    }

    public void friend_add(View view) {
        Friend friend = findFriendFromView(view);

        int i = gameUser.getFriends().indexOf(friend);
        friend.setFriendStatus("accepted");
        gameUser.getFriends().set(i,friend);
        DaoGameBackend.uRef.child(gameUser.getUserKey()).child("friends").setValue(gameUser.getFriends());

        DaoGameBackend.uRef.child(friend.getFriendKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                User friendUser = snapshot.getValue(User.class);
                for (Friend g : friendUser.getFriends()) {
                    if (g.getFriendKey().equals(gameUser.getUserKey())) {
                        int j = friendUser.getFriends().indexOf(g);
                        g.setFriendStatus("accepted");
                        friendUser.getFriends().set(j,g);
                        DaoGameBackend.uRef.child(friendUser.getUserKey()).child("friends").setValue(friendUser.getFriends());
                    }
                }
                Toast.makeText(MainActivity.this,"friend added",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {
            }
        });


    }

    public void cancelClick_waiting(View view) {
//        onclickfindgame
        if (lookingForGame) {
            lookingForGame = false;
            btnFindGame.setEnabled(false);

            handler.postDelayed(() -> {
                btnFindGame.setEnabled(true);
                if (!btnFindGame.getText().toString().equals("searching...")) {
                    lookingForGame = false;
                } else {
                    lookingForGame = true;
                }
            },500);
            return;
        }
        if (lookingForFriendGame) {
            lookingForFriendGame = false;
        } else {
            findViewById(R.id.waiting_popup).setVisibility(View.GONE);
            DaoGameBackend.removeGarbage(gameUser);
        }
    }


    public void friend_play(View view) {
        findViewById(R.id.waiting_popup).setVisibility(View.VISIBLE);

        Friend friendTemp = findFriendFromView(view);
        DaoGameBackend.grRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    GameRequest gameRequest = data.getValue(GameRequest.class);

                    if (gameRequest.getUserReceived().equals(gameUser) && gameRequest.getUserSender().getUserKey().equals(friendTemp.getFriendKey())) {
                        DaoGameBackend.grRef.child(gameRequest.getKey()).child("accepted").setValue(true);
                        moveToGameActivity(gameRequest.getUserSender(),gameRequest.getUserReceived(),false);
                        return;
                    }
                }
                lookingForFriendGame = true;
                friend = friendTemp;
                friend_play_continue();
            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });


    }

    private void friend_play_continue() {
        if (!lookingForFriendGame) {
            DaoGameBackend.removeGarbage(gameUser);
            friend = null;
            cancelClick_waiting(null);
            return;
        }


        DaoGameBackend.uRef.child(friend.getFriendKey()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                User friendUser = snapshot.getValue(User.class);
                createGameRequest(friendUser);
            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });
        {

        }
    }

    private Friend findFriendFromView(View view) {
        LinearLayout friendLayout = (LinearLayout) view.getParent();
        LinearLayout mainLayout = (LinearLayout) friendLayout.getParent();
        return gameUser.getFriends().get(mainLayout.indexOfChild(friendLayout));
    }

    public void cancelClick(View view) {
        friendRemoval = null;
        findViewById(R.id.remove_popup).setVisibility(View.GONE);
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(MainActivity.this::mainLoop,1000);
    }

    public void confirmClick(View view) {
        gameUser.removeFriend(friendRemoval);
        DaoGameBackend.uRef.child(gameUser.getUserKey()).child("friends").setValue(gameUser.getFriends());

        DaoGameBackend.uRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                User friendUser = null;
                for (DataSnapshot data : snapshot.getChildren()) {
                    User user = data.getValue(User.class);
                    if (user.getUserKey().equals(friendRemoval.getFriendKey())) {
                        friendUser = user;
                    }
                }
                if (friendUser != null) {
                    for (Friend f : friendUser.getFriends()) {
                        if (f.getFriendKey().equals(gameUser.getUserKey())) {
                            friendUser.removeFriend(f);
                            DaoGameBackend.uRef.child(friendUser.getUserKey()).child("friends").setValue(friendUser.getFriends());
                        }
                    }
                }
                findViewById(R.id.remove_popup).setVisibility(View.GONE);
                friendRemoval = null;
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(MainActivity.this::mainLoop,1000);
            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });
    }

    public void grPopUp_reject_onClick(View view) {
        View gr_view = findViewById(R.id.gr);
        gr_view.setVisibility(View.GONE);
        DaoGameBackend.grRef.child(gr_main.getKey()).removeValue();
    }

    public void grPopUp_confirm_onClick(View view) {
        View gr_view = findViewById(R.id.gr);
        gr_view.setVisibility(View.GONE);
        DaoGameBackend.grRef.child(gr_main.getKey()).child("accepted").setValue(true);
        moveToGameActivity(gr_main.getUserSender(),gr_main.getUserReceived(),false);
    }

    private void getRequest() {
        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
                Toast.makeText(this,"signed in :)",Toast.LENGTH_SHORT).show();
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this,new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in firebaseUser's information
                            Toast.makeText(MainActivity.this,"signInWithCredential:success",Toast.LENGTH_SHORT).show();
                            firebaseUser = mAuth.getCurrentUser();
                            updateUI();
                        } else {
                            // If sign in fails, display a message to the firebaseUser.
                            Toast.makeText(MainActivity.this,"signInWithCredential:failure",Toast.LENGTH_SHORT).show();
                            updateUI();
                        }
                    }
                });
    }

    private void updateUI() {
        if (firebaseUser != null) {
            GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
            if (googleSignInAccount != null) {


                daoUsers.get().addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        boolean check = true;
                        for (DataSnapshot data : snapshot.getChildren()) {

                            if (data.getValue(User.class).getUserID().equals(firebaseUser.getUid())) {

                                check = false;
                                gameUser = data.getValue(User.class);


                            }
                        }
                        if (check) {
                            gameUser = new User(firebaseUser.getUid(),null,firebaseUser.getDisplayName(),firebaseUser.getEmail(),
                                    700,false);
                            daoUsers.add(gameUser);

                        }

                        txtLabel.setText("hello there " + gameUser.getUserName());
                        txtLabelUserName.setVisibility(View.VISIBLE);
                        txtLabelUserName.setText(gameUser.getUserName());
                        txtLabelMmr.setVisibility(View.VISIBLE);
                        txtLabelMmr.setText("Rating: " + String.valueOf(gameUser.getUserMMR()));
                        btnSign.setText("Sign out");
                        btnSearch.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {
                    }
                });


            }
        }
    }

    public void onClickSign(View view) {
        if (firebaseUser == null) {
            signIn();
        } else {
            if (lookingForGame || lookingForFriendGame) {
                Toast.makeText(this,"Please stop searching for game first",Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.signOut();
            mGoogleSignInClient.signOut();
            firebaseUser = mAuth.getCurrentUser();
            gameUser = null;

            Toast.makeText(this,"signed out",Toast.LENGTH_SHORT).show();
            txtLabel.setText("hello stranger!");
            txtLabelUserName.setVisibility(View.GONE);
            txtLabelMmr.setVisibility(View.GONE);
            btnSign.setText("Sign in");
            btnSearch.setVisibility(View.INVISIBLE);
        }


    }


    public void onClickOfflineGame(View view) {
        if (gameUser != null) {
            if (lookingForGame || lookingForFriendGame) {
                Toast.makeText(this,"Please stop searching for game first",Toast.LENGTH_SHORT).show();
                return;
            }

            handler.removeCallbacksAndMessages(null);
            Intent intent = new Intent(MainActivity.this,OfflineGameActivity.class);
            intent.putExtra("CurrentUser",gameUser);
            startActivity(intent);
        } else {
            Intent intent = new Intent(MainActivity.this,OfflineGameActivity.class);
            intent.putExtra("CurrentUser",new User("userID","userKey","guest","userEmail",101,false));
            startActivity(intent);
        }

    }

    public void onClickFindGame(View view) {
        if (gameUser != null) {
            if (!lookingForGame) {
                findViewById(R.id.waiting_popup).setVisibility(View.VISIBLE);
                btnFindGame.setText("searching...");
                btnFindGame.setEnabled(false);

                handler.postDelayed(() -> {
                    findGame(new Queue(gameUser));
                    btnFindGame.setEnabled(true);
                },500);
            } else {
                lookingForGame = false;
                btnFindGame.setEnabled(false);

                handler.postDelayed(() -> {
                    btnFindGame.setEnabled(true);
                    if (!btnFindGame.getText().toString().equals("searching...")) {
                        lookingForGame = false;
                    } else {
                        lookingForGame = true;
                    }
                },500);
            }

        } else {
            Toast.makeText(this,"Sign in first plz :)",Toast.LENGTH_SHORT).show();
        }
    }

    public void findGame(Queue myQueue) {
        lookingForGame = true;
        DaoGameBackend.removeGarbage(gameUser);
        DaoGameBackend.removeGames(gameUser);
        handler.postDelayed(() -> {
            DaoGameBackend.qRef.push().
                    setValue(myQueue).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    isQueueFirst(myQueue);
                }
            });
        },2000);

    }

    public void isQueueFirst(Queue myQueue) {
        DaoGameBackend.qRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                Queue first = new Queue();
                for (DataSnapshot data : snapshot.getChildren()) {
                    first = data.getValue(Queue.class);
                    if (!myQueue.equals(first)) {
                        break;
                    } else {
                        lookForQ();
                        return;
                    }
                }

                // check if queue exists...
                for (DataSnapshot data : snapshot.getChildren()) {
                    Queue temp = data.getValue(Queue.class);
                    if (myQueue.equals(temp)) {
                        lookForGr(myQueue,first);
                        return;
                    }
                }
                System.out.println("discovered a bug, resets queue");
                findGame(myQueue);
            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });
    }


    public void lookForQ() {
        if (!lookingForGame) {
            DaoGameBackend.removeGarbage(gameUser);
            btnFindGame.setText("find game");
            cancelClick_waiting(null);
            return;
        }

//        System.out.println("looking for queues");

        DaoGameBackend.qRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                boolean check = true;
                for (DataSnapshot data : snapshot.getChildren()) {
                    Queue queue = data.getValue(Queue.class);
                    if (!queue.getUser().equals(gameUser)) {
                        System.out.println(queue.getUser().getUserName() + " added to q");
                        createGameRequest(queue.getUser());
                        check = false;
                        break;
                    }
                }
                if (check) {
                    lookForQ();
                }
            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });

    }

    private void createGameRequest(User rivalUser) {
        GameRequest gameRequest = new GameRequest(gameUser,rivalUser);
        DaoGameBackend.grRef.child(gameRequest.getKey()).setValue(gameRequest);

        ticks = 15;

//        Toast.makeText(this,"passed here _321",Toast.LENGTH_SHORT).show();

        waitForAccept(gameRequest);
    }

    private void waitForAccept(GameRequest gameRequest) {
        ticks--;
        if (ticks == 0) {

            Toast.makeText(MainActivity.this,"passed here _908",Toast.LENGTH_SHORT).show();

            ticks = 15;
            DaoGameBackend.grRef.child(gameRequest.getKey()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    if (friend != null) friend_play_continue();
                    else lookForQ();
                }
            });
            return;
        }

//        System.out.println("waiting for accept");
        DaoGameBackend.grRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                boolean check = true;
                boolean checkForGr = false;
                for (DataSnapshot data : snapshot.getChildren()) {
                    GameRequest temp = data.getValue(GameRequest.class);
                    if (temp.getKey().equals(gameRequest.getKey())) {
                        if (temp.isAccepted()) {
                            moveToGameActivity(gameRequest.getUserSender(),gameRequest.getUserReceived(),true);
                            return;
                        }
                    } else {
                        if (temp.getUserSender().equals(gameRequest.getUserReceived()) &&
                                temp.getUserReceived().equals(gameRequest.getUserSender())) {
                            checkForGr = true;
                        }
                    }
                }
                waitForAccept(gameRequest);


            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });
    }

    private void checkForGr(GameRequest gameRequest) {
    }

    public void lookForGr(Queue myQueue,Queue firstQueue) {
//        System.out.println("looking for game requests");
        DaoGameBackend.grRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @org.jetbrains.annotations.NotNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    GameRequest gameRequest = data.getValue(GameRequest.class);
                    if (gameRequest.getUserReceived().equals(myQueue.getUser()) &&
                            gameRequest.getUserSender().equals(firstQueue.getUser())) {
                        DaoGameBackend.grRef.child(gameRequest.getKey()).child("accepted").setValue(true);
                        moveToGameActivity(gameRequest.getUserSender(),gameRequest.getUserReceived(),false);
                        return;
                    }
                }
                isQueueFirst(myQueue);

            }

            @Override
            public void onCancelled(@NonNull @org.jetbrains.annotations.NotNull DatabaseError error) {

            }
        });

    }

    private void moveToGameActivity(User userSender,User userReceived,boolean isPlayer1) {
        Intent intent = new Intent(MainActivity.this,OnlineGameActivity.class);

        if (isPlayer1) {
            DaoGameBackend.removeGarbage(userSender);
            DaoGameBackend.removeGarbage(userReceived);
            intent.putExtra("CurrentUser",userSender);
            intent.putExtra("OtherUser",userReceived);

            Game game = new Game(userSender,userReceived);
            Object[] obj = DaoGameBackend.sudokoGenerator();
            game.setBoard(game.convertToList((int[][]) obj[0]));
            game.setSolvedBoard(game.convertToList((int[][]) obj[1]));

            DaoGameBackend.gRef.child(game.getGameKey()).setValue(game);
        } else {
            intent.putExtra("CurrentUser",userReceived);
            intent.putExtra("OtherUser",userSender);
        }

//        System.out.println("ready for game, isPlayer1:" + String.valueOf(isPlayer1));
        startActivity(intent);
    }

    public void onClickLeaderboard(View view) {
        Intent intent = new Intent(MainActivity.this,LeaderboardActivity.class);
        startActivity(intent);
    }

    public void onClickSearch(View view) {
        Intent intent = new Intent(MainActivity.this,SearchFriendActivity.class);

        intent.putExtra("CurrentUser",gameUser);
        startActivity(intent);
    }
}