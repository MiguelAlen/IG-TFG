package org.altbeacon.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import org.altbeacon.adapters.ChatAdapter;
import org.altbeacon.beaconreference.databinding.ActivityTaskBinding;
import org.altbeacon.models.ChatMessage;
import org.altbeacon.models.User;
import org.altbeacon.utilities.PreferenceManager;

import java.util.List;

public class TaskActivity extends AppCompatActivity {
    private ActivityTaskBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTaskBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

    }

}