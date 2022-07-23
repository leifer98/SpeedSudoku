package leifer.example.speedsoduko.dao;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.HashMap;

import leifer.example.speedsoduko.objects.User;

public class DAOUser {
    private DatabaseReference databaseReference;

    private User tempUser;

    public DAOUser() {
        FirebaseDatabase db  = FirebaseDatabase.getInstance();
        databaseReference = db.getReference(User.class.getSimpleName());
    }
    public void add(User user) {
        databaseReference.push().setValue(user);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                for (DataSnapshot data : snapshot.getChildren()) {
                    if (data.getValue(User.class).getUserID().equals(user.getUserID())) {
                        user.setUserKey(data.getKey());
                        update(user);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    public Task<Void> update(String key,HashMap<String,Object> hashMap) {
        return databaseReference.child(key).updateChildren(hashMap);
    }

    public Task<Void> update(User user) {
        return databaseReference.child(user.getUserKey()).updateChildren(parameters(user));
    }
    public Task<Void> remove(String key) {

        return databaseReference.child(key).removeValue();
    }

    public Query get() {
        return databaseReference;
    }

    public static HashMap<String, Object> parameters(Object obj) {
        HashMap<String, Object> map = new HashMap<>();
        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            try { map.put(field.getName(), field.get(obj)); } catch (Exception e) { }
        }
        return map;
    }






}
