package net.sourov.fdmanagerfinal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.yalantis.ucrop.UCrop;

import net.sourov.fdmanagerfinal.Model.Users;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class EditUserDetails extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Toast myToast;
    private FirebaseAuth mAuth;
    DatabaseReference reference;
    StorageReference storageReference;

    String name, number, dateOfBirth, email, image_url;

    CircleImageView ImageOnEditUserDetails;
    TextView nameTextOnEditUserDetails;
    EditText nameOnEditUserDetails, dateOnEditUserDetailsVariable, numberOnEditUserDetails, emailOnEditUserDetails;

    DrawerLayout drawerLayout;
    NavigationView navigationView;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private Uri photoURI = null;
    private Uri croppedPhotoUri;
    public static final int CAMERA_CODE = 200;
    private static final int PICK_IMAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_details);

        mAuth = FirebaseAuth.getInstance(); //initialize firebase auth system
        storageReference = FirebaseStorage.getInstance().getReference();
        myToast = Toast.makeText(getApplicationContext(), null, Toast.LENGTH_SHORT);


        //hooks for menu layout
        Toolbar toolbar;
        toolbar = findViewById(R.id.toolbarOnEditUserDetails);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Update your details");
        drawerLayout = findViewById(R.id.drawer_layout_EditUserDetails);
        navigationView = findViewById(R.id.nav_view_EditUserDetails);
        navigationView.setItemIconTintList(null);

        //navigation toggle
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_deawer_open, R.string.navigation_deawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


        ImageOnEditUserDetails = findViewById(R.id.ImageOnEditUserDetails);
        nameTextOnEditUserDetails = findViewById(R.id.nameTextOnEditUserDetails);

        nameOnEditUserDetails = findViewById(R.id.nameOnEditUserDetails);
        dateOnEditUserDetailsVariable = findViewById(R.id.dateOnEditUserDetails);
        numberOnEditUserDetails = findViewById(R.id.numberOnEditUserDetails);
        emailOnEditUserDetails = findViewById(R.id.emailOnEditUserDetails);

        dateOnEditUserDetailsVariable.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH);
            int day = cal.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(
                    EditUserDetails.this,
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    mDateSetListener,
                    year, month, day);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });

        mDateSetListener = (datePicker, year, month, day) -> {
            month = month + 1;
            String date = day + "/" + month + "/" + year;
            dateOnEditUserDetailsVariable.setText(date);
        };
        getDetails();

        findViewById(R.id.openGalleryOnEditUserDetails).setOnClickListener(v -> openGalleryIntent());
        findViewById(R.id.openCameraOnEditUserDetails).setOnClickListener(v -> openCameraIntent());

        findViewById(R.id.updateProfileBtnOnEditUserDetails).setOnClickListener(v -> {
            name = nameOnEditUserDetails.getText().toString().trim();
            number = numberOnEditUserDetails.getText().toString().trim();
            dateOfBirth = dateOnEditUserDetailsVariable.getText().toString().trim();
            email = emailOnEditUserDetails.getText().toString().trim();

            if (croppedPhotoUri == null) {
                sendData();
            } else {
                try {
                    checkImageBeforeUpload();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void getDetails() {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users")
                .child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid()).child("selfInfo");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                Users users = snapshot.getValue(Users.class);
                if (users != null) {

                    image_url = users.getImageUrl();
                    name = users.getName();
                    email = users.getEmail();
                    number = users.getNumber();
                    dateOfBirth = users.getDateOfBirth();

                    setDetails();

                }

            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {
                Toast.makeText(EditUserDetails.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void openGalleryIntent() {
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");


        Intent pickIntent = new Intent(Intent.ACTION_PICK);
        pickIntent.setDataAndType(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});


        try {
            startActivityForResult(pickIntent, PICK_IMAGE);

        } catch (Exception e) {
            e.printStackTrace();
            startActivityForResult(chooserIntent, PICK_IMAGE);
        }
    }

    private void openCameraIntent() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE, "temp_title");
        contentValues.put(MediaStore.Images.Media.TITLE, "temp_desc");
        photoURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        startActivityForResult(intent, CAMERA_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_CODE) {
                if (photoURI == null) {
                    myToast.setText("The selected image appears to be blank");

                } else {
                    cropImage(photoURI);
                }

            } else if (requestCode == UCrop.REQUEST_CROP) {
                croppedPhotoUri = UCrop.getOutput(data);
                ImageOnEditUserDetails.setImageURI(croppedPhotoUri);
            } else if (requestCode == PICK_IMAGE) {
                photoURI = data.getData();
                cropImage(photoURI);
            }
        }
    }


    private void cropImage(Uri photoURI) {
        UCrop.of(photoURI, Uri.fromFile(new File(getCacheDir(), UUID.randomUUID() + ".jpg")))
                .withAspectRatio(1, 1)
                .start(EditUserDetails.this);
    }


    private void setDetails() {

        Glide.with(getApplicationContext()).load(image_url).placeholder(R.drawable.loading).into(ImageOnEditUserDetails);
        nameTextOnEditUserDetails.setText(name);

        nameOnEditUserDetails.setText(name);
        dateOnEditUserDetailsVariable.setText(dateOfBirth);
        numberOnEditUserDetails.setText(number);
        emailOnEditUserDetails.setText(email);


    }

    private void checkImageBeforeUpload() throws IOException {
        File f = new File(croppedPhotoUri.getPath());
        long sizeInByte = f.length();
        if (sizeInByte < 51200) {
            uploadImage(f);
        } else {
            File compressedImageFile = new Compressor(this)
                    .setMaxWidth(600)
                    .setMaxHeight(600)
                    .setCompressFormat(Bitmap.CompressFormat.WEBP)
                    .compressToFile(f);
            uploadImage(compressedImageFile);
        }

    }

    private void uploadImage(File imageFile) {
        ProgressDialog progressDialog
                = new ProgressDialog(this);
        progressDialog.setTitle("Uploading...");
        progressDialog.show();
        StorageReference ref
                = FirebaseStorage.getInstance().getReference()
                .child("users/" + mAuth.getCurrentUser().getUid()).child("profilePic/").child(UUID.randomUUID().toString() + ".webp");
        ref.putFile(Uri.fromFile(imageFile)).addOnSuccessListener(taskSnapshot -> {
            Task<Uri> task1 = taskSnapshot.getStorage().getDownloadUrl();
            task1.addOnSuccessListener(uri -> {
                image_url = uri.toString();
                sendData();
            });

            progressDialog.dismiss();
            myToast.setText("Image Uploaded!!");
            myToast.show();

        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            myToast.setText("Failed " + e.getMessage());
            myToast.show();
        }).addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
            progressDialog.setMessage("Uploaded " + (int) progress + "%");
        });

    }

    private void sendData() {
        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser()
                .getUid()).child("selfInfo");
        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("name", name);
        hashMap.put("email", email);
        hashMap.put("number", number);
        hashMap.put("imageUrl", image_url);
        hashMap.put("dateOfBirth", dateOfBirth);


        reference.updateChildren(hashMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                myToast.setText("data sent to database");
                myToast.show();
                backToaDashboard();
            }

        });
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull @org.jetbrains.annotations.NotNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.home_menu) {
            startActivity(new Intent(getApplicationContext(), Dashboard.class));
            finish();
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
        backToaDashboard();
    }

    private void backToaDashboard() {
        startActivity(new Intent(EditUserDetails.this, Dashboard.class));
        finish();
    }
}