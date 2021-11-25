package net.sourov.fdmanagerfinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.sourov.fdmanagerfinal.Model.Users;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class Dashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Toast myToast;
    private FirebaseAuth mAuth;

    CircleImageView ImageOnDashboard;
    TextView nameTextOnDashboard, nameOnDashboard, emailOnDashboard, numberOnDashboard, dateOnDashboard;
    TextView numberOfFriendsOnDashboard;

    DrawerLayout drawerLayout;
    NavigationView navigationView;

    String dateOfBirth, email, name, imageUrl, number;
    String notFound;

    DatabaseReference reference, reference2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);



        mAuth = FirebaseAuth.getInstance(); //initialize firebase auth system
        myToast = Toast.makeText(getApplicationContext(), null, Toast.LENGTH_SHORT);

        notFound = "Data not found on server";

        //hooks for menu layout
        Toolbar toolbar;
        toolbar = findViewById(R.id.toolbarOnDashboard);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout_dashboard);
        navigationView = findViewById(R.id.nav_view_dashboard);
        navigationView.setItemIconTintList(null);

        //navigation toggle
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_deawer_open, R.string.navigation_deawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


        ImageOnDashboard = findViewById(R.id.ImageOnDashboard);
        nameTextOnDashboard = findViewById(R.id.nameTextOnDashboard);
        nameOnDashboard = findViewById(R.id.nameOnDashboard);
        emailOnDashboard = findViewById(R.id.emailOnDashboard);
        numberOnDashboard = findViewById(R.id.numberOnDashboard);
        dateOnDashboard = findViewById(R.id.dateOnDashboard);
        numberOfFriendsOnDashboard = findViewById(R.id.numberOfFriendsOnDashboard);

        getUserDetails();

        getTheNumberOfFriends();

        findViewById(R.id.addFriendsOnDashboard).setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, AddFriends.class));
            finish();
        });
        findViewById(R.id.settingsOnDashboard).setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, SettingsActivity.class));
            finish();
        });
        findViewById(R.id.friendListOnDashboard).setOnClickListener(v -> {
            startActivity(new Intent(Dashboard.this, FriendList.class));
            finish();
        });

        findViewById(R.id.editProfileBtnOnDashboard).setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), EditUserDetails.class));
            finish();
        });

        ImageOnDashboard.setOnClickListener(v -> {
            Intent imagePreview = new Intent(getApplicationContext(), ImageViewer.class);
            imagePreview.putExtra("image_url", imageUrl);
            startActivity(imagePreview);
        });

    }


    private void getUserDetails() {
        reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).child("selfInfo");
        reference.keepSynced(true);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                Users users = snapshot.getValue(Users.class);
                if (users != null) {

                    imageUrl = users.getImageUrl();
                    name = users.getName();
                    email = users.getEmail();
                    number = users.getNumber();
                    dateOfBirth = users.getDateOfBirth();


                    nameTextOnDashboard.setText(name);
                    nameOnDashboard.setText(name);
                    emailOnDashboard.setText(email);
                    numberOnDashboard.setText(number);
                    dateOnDashboard.setText(dateOfBirth);
                    Glide.with(getApplicationContext()).load(imageUrl).placeholder(R.drawable.loading).into(ImageOnDashboard);
                } else {

                    nameTextOnDashboard.setText(notFound);
                    nameOnDashboard.setText(notFound);
                    emailOnDashboard.setText(notFound);
                    numberOnDashboard.setText(notFound);
                    dateOnDashboard.setText(notFound);

                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(Dashboard.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getTheNumberOfFriends() {

        reference2 = FirebaseDatabase.getInstance().getReference("Users").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                .child("Friends");
        reference2.keepSynced(true);
        reference2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @com.google.firebase.database.annotations.NotNull DataSnapshot snapshot) {

                String friendsCount = String.valueOf(snapshot.getChildrenCount());


                numberOfFriendsOnDashboard.setText(friendsCount);

            }

            @Override
            public void onCancelled(@NonNull @com.google.firebase.database.annotations.NotNull DatabaseError error) {

            }
        });

    }


    @Override
    public boolean onNavigationItemSelected(@NonNull @org.jetbrains.annotations.NotNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.home_menu) {
            drawerLayout.closeDrawer(GravityCompat.START);

        } else if (itemId == R.id.fd_list_menu) {
            startActivity(new Intent(getApplicationContext(), FriendList.class));
            finish();

        } else if (itemId == R.id.add_fd_menu) {
            startActivity(new Intent(getApplicationContext(), AddFriends.class));
            finish();
        } else if (itemId == R.id.log_out_menu) {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), SplashhScreen.class));
            finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(Dashboard.this);
            builder.setTitle("Confirm Exit!!!");
            builder.setMessage("Are you sure you want to exit this application?");
            builder.setPositiveButton("Yes", (dialog, which) -> super.onBackPressed());
            builder.setNegativeButton("No", (dialog, which) -> {

            });
            AlertDialog dialog = builder.create();
            dialog.show();

        }

    }
}