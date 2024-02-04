package com.example.csproject;

import static com.example.csproject.CommonFunctions.fullscreenSetup;
import static com.example.csproject.CommonFunctions.getUserValues;
import static com.example.csproject.CommonFunctions.mAuth;
import static com.example.csproject.CommonFunctions.systemUiChangeManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class HomePageActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener{

    //drawer views
    //the whole thing
    private DrawerLayout drawer;

    //only the toolbar
    private Toolbar toolbar;

    //only the left of the drawer
    private NavigationView navigationView;
    //end of drawer views

    //the profile icon on top right
    private TextView profileIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        fullscreenSetup(getWindow());
        setContentView(R.layout.activity_home_page);
        systemUiChangeManager(getWindow().getDecorView());

        connectViews();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawerSetup(savedInstanceState);
    }
    //connects the views to the code
    private void connectViews()
    {
        toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        profileIcon =findViewById(R.id.tv_profileIcon_activity_home);
        profileIcon.setOnClickListener(this);
    }

    //signs out and goes to sign in page
    public void signOut()
    {
        Toast.makeText(this, "Logging out", Toast.LENGTH_SHORT).show();
        mAuth.signOut();
        startActivity(new Intent(HomePageActivity.this, MainActivity.class));
    }

    @Override
    public void onClick(View view)
    {
        if(view == profileIcon)
            showPopup(profileIcon);
    }

    //drawer functions
    public void drawerSetup(Bundle savedInstanceState)
    {
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        if (savedInstanceState == null)
        {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_home);
        }
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId()) {
            case R.id.nav_home:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
                break;
            case R.id.nav_stats:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StatsFragment()).commit();
                break;
            case R.id.nav_logout:
                signOut();
                break;
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onBackPressed()
    {
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }
    //end drawer functions

    //menu functions
    public void showPopup(View view)
    {
        PopupMenu popup = new PopupMenu(this, view);
        popup.setForceShowIcon(true);
        popup.inflate(R.menu.profile_menu);

        MenuItem name = popup.getMenu().findItem(R.id.item_name_profile_menu);
        MenuItem email = popup.getMenu().findItem(R.id.item_email_profile_menu);
        MenuItem editProfile = popup.getMenu().findItem(R.id.item_editProfile_profile_menu);

        updateMenuItemsFromUser(name,email);

        setMenuClickListener(popup,editProfile);

        popup.show();
    }
    public void updateMenuItemsFromUser(MenuItem name,MenuItem email)
    {
        getUserValues(userValues ->
        {
            String userEmail = userValues[1];
            String userName = userValues[2];

            email.setTitle(userEmail);
            name.setTitle(userName);
        });
    }
    public void setMenuClickListener(PopupMenu popup,MenuItem editProfile)
    {
        popup.setOnMenuItemClickListener(item ->
        {
            if(item == editProfile)
                startActivity(new Intent(HomePageActivity.this, EditProfileActivity.class));
            return true;
        });
    }
    //end menu functions
}