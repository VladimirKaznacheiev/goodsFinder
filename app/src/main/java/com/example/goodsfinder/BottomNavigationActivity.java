package com.example.goodsfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.goodsfinder.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class BottomNavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DialogFragment dlg;
    DialogFragment dlg2;

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    NavigationView navigationView;

    SharedPreferences mSettings;

    FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

    HomeFragment homeFragment = new HomeFragment();
    FavouriteFragment favouriteFragment = new FavouriteFragment();

    FrameLayout fragmentLayout;
    FrameLayout fragmentLayout2;




    private void myClickItem(MenuItem item){
        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragmentLayout.setVisibility(View.VISIBLE);
                fragmentLayout2.setVisibility(View.INVISIBLE);
                break;

            case R.id.navigation_dashboard:
                fragmentLayout.setVisibility(View.INVISIBLE);
                fragmentLayout2.setVisibility(View.VISIBLE);
                break;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation);

        fragmentLayout = findViewById(R.id.fragment_layout);
        fragmentLayout2 = findViewById(R.id.fragment_layout2);

        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_layout, homeFragment);
        transaction.replace(R.id.fragment_layout2, favouriteFragment);
        transaction.commit();

        fragmentLayout.setVisibility(View.VISIBLE);
        fragmentLayout2.setVisibility(View.INVISIBLE);
        String APP_PREFERENCES = "searches";


        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);


        dlg = new Dialog();
        dlg2 = new Dialog2();

        toolbar = findViewById(R.id.toolbar);
        //((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.navigationView);
        navigationView.bringToFront();
        navigationView.setNavigationItemSelectedListener(this);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        actionBarDrawerToggle.syncState();

        BottomNavigationView bottomNavigationView =  findViewById(R.id.nav_view);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            myClickItem(item); //call here
            return true;
        });

        View view = bottomNavigationView.findViewById(R.id.navigation_home);
        view.performClick();

        //findViewById(R.id.navigation_dashboard).setSelected(true);
        //findViewById(R.id.navigation_dashboard).setSelected(false);

//        Intent intent = getIntent();
//        if(intent!=null) {
//            getSupportActionBar().setTitle(String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getEmail()));
//        }

/*        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);*/
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.logout){
            Log.d("TEST", "LOGOUT");
            FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(BottomNavigationActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();

        } else if (menuItem.getItemId() == R.id.theme_dark){
            if(mSettings.getString("theme", "").equals("light")){
                dlg.show(getFragmentManager(), "dlg");
            }
        } else if (menuItem.getItemId() == R.id.theme_light){
            if(mSettings.getString("theme", "").equals("dark")){
                dlg2.show(getFragmentManager(), "dlg2");
            }



        }

        Log.d("THEME", "SETTED");

        return true;
    }
}