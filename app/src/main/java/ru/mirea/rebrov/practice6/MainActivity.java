package ru.mirea.rebrov.practice6;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import ru.mirea.rebrov.practice6.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences sharedPref =
                getSharedPreferences("rebrov_settings", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        if(!sharedPref.getString("party", "unknown").equals("unknown") || !sharedPref.getString("number", "unknown").equals("unknown") || !sharedPref.getString("film", "unknown").equals("unknown"))
        {
            binding.editText.setText(sharedPref.getString("party", "unknown"));
            binding.editText2.setText(sharedPref.getString("number", "unknown"));
            binding.editText3.setText(sharedPref.getString("film", "unknown"));
        }

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("party", binding.editText.getText().toString());
                editor.putString("number", binding.editText2.getText().toString());
                editor.putString("film", binding.editText3.getText().toString());
                editor.apply();
            }
        });
    }
}