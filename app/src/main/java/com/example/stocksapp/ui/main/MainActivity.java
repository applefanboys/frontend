package com.example.stocksapp.ui.main;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.stocksapp.R;
import com.example.stocksapp.ui.main.home.HomeFragment;
import com.example.stocksapp.ui.main.topics.TopicsFragment;
import com.example.stocksapp.ui.main.mypage.MyPageFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Fragment fragment;
            if (id == R.id.navigation_home) {
                fragment = new HomeFragment();
            } else if (id == R.id.navigation_topics) {
                fragment = new TopicsFragment();
            } else {
                fragment = new MyPageFragment();
            }
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_nav_host, fragment)
                    .commit();
            return true;
        });

        if (savedInstanceState == null) {
            bottomNav.setSelectedItemId(R.id.navigation_home);
        }
    }
}
