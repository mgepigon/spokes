package com.example.spokes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

public class TripActivity extends AppCompatActivity {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    //Buttons
    private boolean menuOpen;
    private FloatingActionButton mSave;
    private FloatingActionButton mBack;
    private FloatingActionButton mShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip);

        //Setup tabs & page adapter
        mTabLayout = findViewById(R.id.tabs);
        mViewPager = findViewById(R.id.view_pager);
        mTabLayout.setupWithViewPager(mViewPager);

        pageAdapter adapter = new pageAdapter(getSupportFragmentManager(),
                FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.addFragment(new summaryFragment(), "SUMMARY");
        adapter.addFragment(new routeFragment(), "ROUTE");
        adapter.addFragment(new historyFragment(), "HISTORY");
        mViewPager.setAdapter(adapter);


        //Buttons setup
        menuOpen = false;
        //Show button
        mShow = findViewById(R.id.show);
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
        mSave = findViewById(R.id.save);
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Saved to Database", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        //Back button
        mBack = findViewById(R.id.back);
        mBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent maps = new Intent(TripActivity.this, MapsActivity.class);
                startActivity(maps);
            }
        });
    }


    //Menu Animations
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