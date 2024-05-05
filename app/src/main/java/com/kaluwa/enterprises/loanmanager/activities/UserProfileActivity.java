package com.kaluwa.enterprises.loanmanager.activities;

import static com.kaluwa.enterprises.loanmanager.constants.DatabaseReferences.USER_REFERENCE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.kaluwa.enterprises.loanmanager.MainActivity;
import com.kaluwa.enterprises.loanmanager.R;
import com.kaluwa.enterprises.loanmanager.models.User;
import com.kaluwa.enterprises.loanmanager.utils.CircleTransform;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class UserProfileActivity extends AppCompatActivity {

    private static final String TAG = "UserProfileActivityExceptions";
    private TextView tvWelcome;
    private EditText etFName, etLName, etEmail, etMobile, etDob;
    private ImageView ivDp;
    private RadioGroup rbgGender;
    private RadioButton rbSelectedGender;
    private ProgressBar progressBar;

    private FirebaseAuth authProfile;
    private FirebaseUser firebaseUser;
    private SwipeRefreshLayout swipeContainer;
    private User user = new User();

    @SuppressLint("LongLogTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // set title
        getSupportActionBar().setTitle("My Profile");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        swipeToRefresh();

        // assign findViewByIds
        assignViewsById();

        authProfile = FirebaseAuth.getInstance();
        firebaseUser = authProfile.getCurrentUser();

        if (firebaseUser == null) {
            Toast.makeText(this, "Something went wrong!, User's details are not available at the moment.", Toast.LENGTH_LONG).show();
        } else {
            // read and assign user details
            readUserDetails();
        }

        // set onclick listener for edit/save button
        Button editButton = findViewById(R.id.up_img_btn);
        editButton.setOnClickListener(v -> {
            etFName.setHint("First Name");
            etLName.setHint("Last Name");
            etMobile.setHint("Mobile Number");

            Button b = (Button)v;
            if (b.getText().toString().equalsIgnoreCase("edit")) {
                v.setBackgroundColor(getResources().getColor(R.color.green));
                ((Button) v).setText("save");

                etFName.setEnabled(true);
                etLName.setEnabled(true);
                etMobile.setEnabled(true);
            } else {
                String textFName, textLName, textMobile;

                textFName = etFName.getText().toString();
                textLName = etLName.getText().toString();
                textMobile = etMobile.getText().toString();

                // validation
                if (TextUtils.isEmpty(textFName)) {
                    Toast.makeText(this, "Please enter your first name!", Toast.LENGTH_LONG).show();
                    etFName.setError("First name is required.");
                    etFName.requestFocus();
                } else if (TextUtils.isEmpty(textLName)) {
                    Toast.makeText(this, "Please enter your last name!", Toast.LENGTH_LONG).show();
                    etLName.setError("Last name is required.");
                    etLName.requestFocus();
                } else if (TextUtils.isEmpty(textMobile)) {
                    Toast.makeText(this, "Please enter your mobile number!", Toast.LENGTH_LONG).show();
                    etMobile.setError("Mobile number is required.");
                    etMobile.requestFocus();
                } else if (textMobile.length() != 10) {
                    Toast.makeText(this, "Please enter a valid mobile number!", Toast.LENGTH_LONG).show();
                    etMobile.setError("Mobile No. should be 10 digits.");
                    etMobile.requestFocus();
                } else {
                    v.setBackgroundColor(getResources().getColor(R.color.royalBlue));
                    ((Button) v).setText("edit");

                    etFName.setEnabled(false);
                    etLName.setEnabled(false);
                    etMobile.setEnabled(false);

                    // save changes
                    saveChanges(textFName, textLName, textMobile);
                }
            }
        });

        // set onclick listener for profile picture
        ivDp.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, ivDp);
            popupMenu.inflate(R.menu.option_on_dp);
            if (firebaseUser.getPhotoUrl() != null) {
                Picasso.get().load(firebaseUser.getPhotoUrl()).fit().transform(new CircleTransform()).centerCrop(Gravity.TOP)
                        .placeholder(R.drawable.dp_loaing_sketch) // Use default image as a placeholder
                        .error(R.drawable.dp_loaing_sketch)
                        .into(ivDp, new Callback() {
                    @Override
                    public void onSuccess() {
                        // Image loaded successfully
                        // Do something here
                        System.out.println("success");
                    }

                    @Override
                    public void onError(Exception e) {
                        // Error occurred while loading image
                        // Do something here, like loading a default image
                        System.out.println("error");
                        popupMenu.getMenu().findItem(R.id.remove_dp).setEnabled(false);
                    }
                });
            }
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.change_dp) {
                    Intent intent = new Intent(this, UploadProfilePictureActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    AlertDialog.Builder removeDpDialog = new AlertDialog.Builder(this);
                    removeDpDialog.setTitle("Remove Profile Picture");
                    removeDpDialog.setMessage("Do you want to remove your profile picture? This action can't be undo.");
                    removeDpDialog.setPositiveButton("Remove", (dialog, which) -> {
                        progressBar.setVisibility(View.VISIBLE);
                        // Delete Profile Picture
                        if (firebaseUser.getPhotoUrl() != null) {
                            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                            StorageReference storageReference = firebaseStorage.getReferenceFromUrl(firebaseUser.getPhotoUrl().toString());
                            storageReference.delete().addOnSuccessListener(unused -> {
                                progressBar.setVisibility(View.GONE);
                                Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.dp_loaing_sketch);
                                Picasso.get().load(uri).fit().transform(new CircleTransform()).centerCrop(Gravity.TOP)
                                        .placeholder(R.drawable.dp_loaing_sketch) // Use default image as a placeholder
                                        .error(R.drawable.dp_loaing_sketch) // Use default image if an error occurs
                                        .into(ivDp);
                                Log.d(TAG, "OnSuccess: "+firebaseUser.getUid()+" user's photo deleted.");
                            }).addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Log.d(TAG, "OnFailure: "+e.getMessage());
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                        } else {
                            progressBar.setVisibility(View.GONE);
                        }
                    });

                    removeDpDialog.setNegativeButton("Cancel", (dialog, which) -> {
                        dialog.cancel();
                    });
                    // Create the Alert dialog
                    AlertDialog alertDialog = removeDpDialog.create();
                    // Change the continue button color
                    alertDialog.setOnShowListener(dialog -> {
                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
                    });
                    // Show the Alert Dialog box
                    alertDialog.show();
                }
                return false;
            });
            popupMenu.show();
        });
    }

    @SuppressLint("LongLogTag")
    private void saveChanges(String textFName, String textLName, String textMobile) {
        progressBar.setVisibility(View.VISIBLE);

        user.setFirstName(textFName);
        user.setLastName(textLName);
        user.setMobile(textMobile);

        // Extract database reference
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference(USER_REFERENCE);
        String userID = firebaseUser.getUid();

        referenceProfile.child(userID).setValue(user).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                // Update new display name
                UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(user.getFirstName() + " " + user.getLastName()).build();
                firebaseUser.updateProfile(profileChangeRequest);

                Toast.makeText(this, "User Profile Updated!", Toast.LENGTH_LONG).show();
                // refresh activity
                startActivity(getIntent());
                finish();
                overridePendingTransition(0, 0);
            } else {
                try {
                    throw task.getException();
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                    Toast.makeText(this, "Profile Update Error: "+e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
            progressBar.setVisibility(View.GONE);
        });
    }

    private void readUserDetails() {
        progressBar.setVisibility(View.VISIBLE);
        // extract uid
        String userId = firebaseUser.getUid();

        // extracting user reference from database for "Users"
        DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference(USER_REFERENCE);
        referenceProfile.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                if (user == null) {
                    Toast.makeText(UserProfileActivity.this, "Retrieved null data!", Toast.LENGTH_SHORT).show();
                } else {
                    tvWelcome.setText("Welcome " + user.getTitle() + " " + user.getFirstName() + "!");
                    etFName.setText(user.getFirstName());
                    etLName.setText(user.getLastName());
                    etEmail.setText(user.getEmail());
                    etMobile.setText(user.getMobile());
                    etDob.setText(user.getDob());


                    for (int i = 0; i < rbgGender.getChildCount(); i++) {
                        if (user.getGender().equalsIgnoreCase(((RadioButton) rbgGender.getChildAt(i)).getText().toString())) {
                            int radioId = ((RadioButton) rbgGender.getChildAt(i)).getId();
                            rbSelectedGender = findViewById(radioId);
                            rbSelectedGender.setChecked(true);
                            break;
                        }
                    }

                    // Load user image from Firebase
                    Uri userImageUri = firebaseUser.getPhotoUrl();
                    Picasso.get().load(userImageUri).fit().transform(new CircleTransform()).centerCrop(Gravity.TOP)
                            .placeholder(R.drawable.dp_loaing_sketch) // Use default image as a placeholder
                            .error(R.drawable.dp_loaing_sketch) // Use default image if an error occurs
                            .into(ivDp);
                }
                progressBar.setVisibility(View.GONE);
            }

            @SuppressLint("LongLogTag")
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, error.getDetails());
                Toast.makeText(UserProfileActivity.this, "Data retriving error: "+error.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void assignViewsById() {
        tvWelcome = findViewById(R.id.up_tv_welcome);
        etFName = findViewById(R.id.up_et_firstname);
        etLName = findViewById(R.id.up_et_lastname);
        etEmail = findViewById(R.id.up_et_email);
        etMobile = findViewById(R.id.up_et_mobile);
        etDob = findViewById(R.id.up_et_dob);
        ivDp = findViewById(R.id.up_dp);
        progressBar = findViewById(R.id.up_progressBar);

        // radio buttons for gender
        rbgGender = findViewById(R.id.rbg_up_gender);
        rbgGender.clearCheck();
    }

    @Override
    protected void onStart() {
        super.onStart();
        etFName.setEnabled(false);
        etLName.setEnabled(false);
        etMobile.setEnabled(false);
        etEmail.setEnabled(false);
        etDob.setEnabled(false);
    }

    // Creating ActionBar Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu items
        getMenuInflater().inflate(R.menu.common_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
            return true;
        } else if (id == R.id.menu_dashboard) {
            // open dashboard
            Intent intent = new Intent(this, DashboardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_my_profile) {
            // open profile
            Intent intent = new Intent(this, UserProfileActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menu_change_pwd) {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_delete_account) {
            Intent intent = new Intent(this, DeleteAccountActivity.class);
            startActivity(intent);
        } else {
            // logout
            authProfile.signOut();
            Toast.makeText(this, "User successfully logout.", Toast.LENGTH_SHORT).show();
            // Open main activity after successful registration
            Intent intent = new Intent(this, MainActivity.class);
            // To prevent user from returning back to register on pressing back button.
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void swipeToRefresh() {
        // Look up for the swipe container
        swipeContainer = findViewById(R.id.swipeContainer);

        // Setup Refresh Listener which triggers new data loading
        swipeContainer.setOnRefreshListener(() -> {
            // Code to refresh goes here. Make sure to call swipeContainer.setRefresh(false) once the refreshed.
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
            swipeContainer.setRefreshing(false);
        });

        // Configure refresh colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright, android.R.color.holo_green_light, android.R.color.holo_orange_light, android.R.color.holo_red_light);
    }
}