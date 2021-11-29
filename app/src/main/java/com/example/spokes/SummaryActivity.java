package com.example.spokes;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.spokes.ui.main.SectionsPagerAdapter;
import com.example.spokes.databinding.ActivitySummaryBinding;

public class SummaryActivity extends AppCompatActivity {

    private ActivitySummaryBinding binding;

    //Buttons
    private boolean menuOpen;
    private FloatingActionButton mSave;
    private FloatingActionButton mBack;
    private FloatingActionButton mShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySummaryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Grab intent and extras containing all the information

        //Setup of View
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);

        //Buttons setup
        menuOpen = false;
        //Show button
        mShow = binding.show;
        mShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (menuOpen){
                    closeMenu();
                }else{
                    showMenu();
                }
            }
        });

        //Save button --> add to database
        mSave = binding.save;
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Saved to Database", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //Back button
        mBack = binding.back;
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent maps = new Intent(SummaryActivity.this, MapsActivity.class);
                startActivity(maps);
            }
        });
    }

    private void showMenu(){
        menuOpen=true;
        mBack.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        mSave.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
    }

    private void closeMenu(){
        menuOpen=false;
        mBack.animate().translationY(0);
        mSave.animate().translationY(0);
    }
}