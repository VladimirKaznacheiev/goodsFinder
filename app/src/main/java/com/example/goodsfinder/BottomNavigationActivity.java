package com.example.goodsfinder;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.goodsfinder.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.skydoves.balloon.ArrowOrientation;
import com.skydoves.balloon.Balloon;
import com.skydoves.balloon.BalloonAnimation;
import com.skydoves.balloon.BalloonSizeSpec;
import com.skydoves.balloon.OnBalloonDismissListener;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.io.IOException;

import static java.security.AccessController.getContext;

public class BottomNavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    DialogFragment dlg;
    DialogFragment dlg2;

    Balloon balloon4;

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

    private final Handler uiHandler = new Handler();

    Balloon balloon8;
    Balloon balloon9;

    View view;
    View view1;




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
        toolbar = findViewById(R.id.toolbar);
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
        SharedPreferences.Editor editor = mSettings.edit();


        dlg = new Dialog();
        dlg2 = new Dialog2();


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

        view = bottomNavigationView.findViewById(R.id.navigation_home);
        view1 = bottomNavigationView.findViewById(R.id.navigation_dashboard);

        view.performClick();
        view.setEnabled(false);
        view1.setEnabled(false);



        toolbar.findViewById(R.id.help_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                view = bottomNavigationView.findViewById(R.id.navigation_home);
                view.performClick();
                FragmentManager fm = getSupportFragmentManager();
                HomeFragment f = (HomeFragment) fm.findFragmentById(R.id.fragment_layout);

                f.balloon1.showAlignBottom(f.mySearchView);


            }
        });

        balloon4 = new Balloon.Builder(this)
                .setArrowSize(10)
                .setArrowOrientation(ArrowOrientation.TOP)
                .setArrowPosition(0.75f)
                .setWidth(BalloonSizeSpec.WRAP)
                .setHeight(65)
                .setTextSize(15f)
                .setCornerRadius(4f)
                .setText(this.getResources().getString((R.string.balloon4)))
                .setTextColor(ContextCompat.getColor(this, R.color.white))
                .setTextIsHtml(true)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.bg_learn))
                .setBalloonAnimation(BalloonAnimation.OVERSHOOT)
                .setArrowAlignAnchorPadding(10)
                .setOnBalloonDismissListener(new OnBalloonDismissListener() {
                    @Override
                    public void onBalloonDismiss() {

                        view1.performClick();

                        //запустить 5 шарик надо


                        FragmentManager fm = getSupportFragmentManager();
                        FavouriteFragment f = (FavouriteFragment) fm.findFragmentById(R.id.fragment_layout2);
                        f.learning1();
                    }
                })
                .build();






                      // balloon4.showAlignBottom(favFrag.getView().findViewById(R.id.spinner));

        //ЭТО УКАЖЕТ НА 3 ПОЛОСКИ И ВИДЖЕТ ОБУЧЕНИЯ

        balloon8 = new Balloon.Builder(this)
                .setArrowSize(10)
                .setArrowOrientation(ArrowOrientation.LEFT)
                .setArrowPosition(0.5f)
                .setWidth(BalloonSizeSpec.WRAP)
                .setHeight(40)
                .setTextSize(15f)
                .setCornerRadius(4f)
                .setText(this.getResources().getString((R.string.balloon8)))
                .setTextColor(ContextCompat.getColor(this, R.color.white))
                .setTextIsHtml(true)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.bg_learn))
                .setBalloonAnimation(BalloonAnimation.OVERSHOOT)
                .setArrowAlignAnchorPadding(10)
                .setOnBalloonDismissListener(new OnBalloonDismissListener() {
                    @Override
                    public void onBalloonDismiss() {
                        balloon9.showAlignBottom(toolbar.findViewById(R.id.help_btn));
                    }
                })
                .build();

        balloon9 = new Balloon.Builder(this)
                .setArrowSize(10)
                .setArrowOrientation(ArrowOrientation.TOP)
                .setArrowPosition(0.95f)
                .setWidth(BalloonSizeSpec.WRAP)
                .setHeight(65)
                .setTextSize(15f)
                .setCornerRadius(4f)
                .setText(this.getResources().getString((R.string.balloon9)))
                .setTextColor(ContextCompat.getColor(this, R.color.white))
                .setTextIsHtml(true)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.bg_learn))
                .setBalloonAnimation(BalloonAnimation.OVERSHOOT)
                .setArrowAlignAnchorPadding(10)
                .setOnBalloonDismissListener(new OnBalloonDismissListener() {
                    @Override
                    public void onBalloonDismiss() {
                        //тут я запишу, что юзер обучился в шаред

                        view.performClick();

                        editor.putString("isTrained","1");
                        editor.apply();
                        editor.commit();

                    }
                })
                .build();






        uiHandler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        boolean tmp = true;
                        ///while (tmp){
                            if(mSettings.getString("give_enter1", "").equals("1")){
                                Log.d("ENTER", "ENTER");
                                tmp = false;
                            }
                        //}
                    }
                });





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
                view.performClick();
                dlg.show(getFragmentManager(), "dlg");
            }
        } else if (menuItem.getItemId() == R.id.theme_light){
            if(mSettings.getString("theme", "").equals("dark")){
                view.performClick();
                dlg2.show(getFragmentManager(), "dlg2");
            }
        }else if(menuItem.getItemId() == R.id.about){
                Uri address = Uri.parse("http://forbuy.cf/main.html");
                Intent openlinkIntent = new Intent(Intent.ACTION_VIEW, address);
                startActivity(openlinkIntent);


        }

        Log.d("THEME", "SETTED");

        return true;
    }

    public void onClickUpdate(){
        FragmentManager fm = getSupportFragmentManager();
        FavouriteFragment f = (FavouriteFragment) fm.findFragmentById(R.id.fragment_layout2);
        f.reload();
    }


}