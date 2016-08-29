package com.grahm.livepost.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.grahm.livepost.asynctask.RegisterUserTask;
import com.grahm.livepost.interfaces.OnPutImageListener;
import com.grahm.livepost.R;
import com.grahm.livepost.objects.FirebaseActivity;
import com.grahm.livepost.objects.MultipartFormField;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.util.GV;
import com.grahm.livepost.util.Utilities;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.*;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.aprilapps.easyphotopicker.EasyImage;


/**
 * A login screen that offers login via email/password.
 */
public class RegistrationActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>, OnPutImageListener{
    public static final String USER_KEY = "user";
    public static final String PWD_KEY = "pwd";
    private Firebase mFirebaseRef;
    protected FirebaseError mFirebaseError;
    private static final String TAG = "LoginActivity";
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private RegisterUserTask mAuthTask = null;
    private User mUser;
    private Uri mUri;
    private String mPassword;
    private RegistrationViewsManager mRegistrationViewsManager;


    // UI references.
    @BindView(R.id.reg_pager) public ViewPager mViewPager;
    @BindView(R.id.toolbar) public Toolbar mToolbar;
    @BindView(R.id.reg_progress) public ProgressBar mProgressBarView;
    @BindView(R.id.reg_progress_str) public TextView mProgressBarTextView;
    @BindView(R.id.reg_progress_circle) public View mProgressView;
    @BindView(R.id.reg_form) public View mLoginFormView;


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(USER_KEY, mUser);
        outState.putString("pwd", mPassword);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mUser = (User) savedInstanceState.getSerializable(USER_KEY);
        mPassword = savedInstanceState.getString(PWD_KEY);
        updateProgressViews();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                int currentItem = mViewPager.getCurrentItem();
                if (currentItem == 0) {
                    onBackPressed();
                    return true;
                }
                mViewPager.setCurrentItem(Math.max(currentItem - 1, 0), true);
                updateProgressViews();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateProgressViews() {
        MultipartFormField r = mRegistrationViewsManager.list.get(mViewPager.getCurrentItem());
        mToolbar.setTitle(r.getTitle());
        int vCount = mViewPager.getAdapter().getCount();
        int current = mViewPager.getCurrentItem() + 1;
        mProgressBarTextView.setText(current + "/" + vCount);
        mProgressBarView.setProgress(current * 100 / vCount);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseRef = new Firebase(getString(R.string.firebase_url));
        mUser = Utilities.getUser(mFirebaseRef,this,savedInstanceState);
        mUser = mUser==null?new User():mUser;
        mRegistrationViewsManager = new RegistrationViewsManager();
        setContentView(R.layout.activity_registration);
        ButterKnife.bind(this);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_36dp);
        EasyImage.configuration(this)
                .setImagesFolderName("images")
                .saveInAppExternalFilesDir()
                .setCopyExistingPicturesToPublicLocation(true);
        setSupportActionBar(mToolbar);
        mViewPager.setAdapter(new LoginPagerAdapter());
        updateProgressViews();
        populateAutoComplete();
    }

    @OnClick(R.id.reg_next_button)
    public void nextButton() {
        int vCount = mViewPager.getAdapter().getCount();
        int current = mViewPager.getCurrentItem();
        MultipartFormField r = mRegistrationViewsManager.list.get(current);
        if (r.onValidate()) {
            if (current + 1 == vCount) {//Send Registration Form
                Log.d(TAG,"Attempting registration");
                attemptRegistration(mUri);
            }
            mViewPager.setCurrentItem(current + 1, true);
            updateProgressViews();
        }
    }


    private void populateAutoComplete() {
        getLoaderManager().initLoader(0, null, this);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegistration(Uri pictureUri) {
        if (mAuthTask != null) {
            return;
        }
        AmazonS3Client amazonS3Client = new AmazonS3Client(new BasicAWSCredentials(GV.ACCESS_KEY_ID, GV.SECRET_KEY));
        mAuthTask = new RegisterUserTask(mUser, mPassword,mFirebaseRef, FirebaseAuth.getInstance(),this,amazonS3Client,this,true);
        mAuthTask.execute(pictureUri);

    }

    private static boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
            mLoginFormView.setVisibility(show ? View.INVISIBLE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    @Override
    public void onSuccess(String url) {
        Intent mainIntent = new Intent(RegistrationActivity.this, MainActivity.class);
        RegistrationActivity.this.startActivity(mainIntent);
        RegistrationActivity.this.finish();
    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(RegistrationActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

    }



    protected void pickImageForCropping() {
        Crop.pickImage(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(result.getData());
        } else if (requestCode == Crop.REQUEST_CROP) {
            handleCrop(resultCode, result);
        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        TextInputLayout err = ButterKnife.findById(this, R.id.reg_avatar_input);

        if (resultCode == RESULT_OK) {
            ImageButton resultView = ButterKnife.findById(this, R.id.reg_avatar);
            Uri cropUri = Crop.getOutput(result);
            resultView.setImageResource(R.drawable.ic_add_a_photo_black_48dp);
            resultView.setImageURI(cropUri);
            mUri = cropUri;
            mUser.setProfile_picture(cropUri.toString());
        } else if (resultCode == Crop.RESULT_ERROR) {
            err.setError(Crop.getError(result).getMessage());
        }
    }

    private static boolean isEmailValid(String email) {
        if (email == null) {
            return false;
        } else {
            return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
        }
    }


    public class RegistrationViewsManager {
        private RegistrationErrors mRegistrationErrors;
        public List<MultipartFormField> list;

        public RegistrationViewsManager() {
            mRegistrationErrors = new RegistrationErrors();
            list = new ArrayList<MultipartFormField>();
            list.add(new UsernameField());
            list.add(new PasswordField());
            list.add(new FullnameField());
            list.add(new AvatarField());
        }

        public class RegistrationErrors {
            // String binding
            public final String ERROR_FIELD_REQ;
            public final String ERROR_INVALID_EMAIL;
            public final String ERROR_INVALID_PASSWORD;
            public final String ERROR_TAKEN_EMAIL;

            RegistrationErrors() {
                ERROR_FIELD_REQ = getString(R.string.error_field_required);
                ERROR_INVALID_EMAIL = getString(R.string.error_invalid_email);
                ERROR_INVALID_PASSWORD = getString(R.string.error_invalid_password);
                ERROR_TAKEN_EMAIL = getString(R.string.error_taken_email);
            }
        }



        public class UsernameField extends MultipartFormField {
            public int getTitle() {
                return R.string.reg_email;
            }

            public int getLayout() {
                return R.layout.registration_email;
            }

            public boolean mEmailAvailable;

            protected void setEmailAvailable(boolean b) {
                mEmailAvailable = b;
            }

            protected boolean getEmailAvailable() {
                return mEmailAvailable;
            }

            public boolean onValidate() {
                final Object lock = new Object();
                final TextView emailView = ButterKnife.findById(mViewPager, R.id.reg_email_field);
                emailView.setError(null);
                String email = emailView.getText().toString();
                // Check for a valid email address.
                if (TextUtils.isEmpty(email)) {
                    emailView.setError(mRegistrationErrors.ERROR_FIELD_REQ);
                    return false;
                } else if (!isEmailValid(email)) {
                    emailView.setError(mRegistrationErrors.ERROR_INVALID_EMAIL);
                    return false;
                } else {//Check if username exists in DB
                    mUser.setEmail(email);
                    return true;
                }
            }
        }

        public class PasswordField extends MultipartFormField {
            public int getTitle() {
                return R.string.reg_password;
            }

            public int getLayout() {
                return R.layout.registration_password;
            }

            public boolean onValidate() {
                TextView passwordView = ButterKnife.findById(mViewPager, R.id.reg_password);
                passwordView.setError(null);
                String password = passwordView.getText().toString();
                //Validate Password
                if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
                    passwordView.setError(mRegistrationErrors.ERROR_INVALID_PASSWORD);
                    return false;
                }
                mPassword = password;
                return true;
            }
        }

        public class FullnameField extends MultipartFormField {
            public int getTitle() {
                return R.string.reg_fullname;
            }

            public int getLayout() {
                return R.layout.registration_fullname;
            }

            public boolean onValidate() {
                TextView firstNameView = ButterKnife.findById(mViewPager, R.id.reg_first_name);
                TextView lastNameView = ButterKnife.findById(mViewPager, R.id.reg_last_name);
                String fname = firstNameView.getText().toString().trim();
                String lname = lastNameView.getText().toString().trim();
                mUser.setName(fname+" "+lname);
                return true;
            }
        }

        public class AvatarField extends MultipartFormField {
            public int getTitle() {
                return R.string.reg_avatar;
            }

            public int getLayout() {
                return R.layout.registration_avatar;
            }

            public void onSetup(ViewGroup layout) {
                ImageButton imageButton = (ImageButton) layout.findViewById(R.id.reg_avatar);
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pickImageForCropping();
                    }
                });
            }

            public boolean onValidate() {
                return true;
            }
        }
    }


    private class LoginPagerAdapter extends PagerAdapter {


        public LoginPagerAdapter() {

        }

        public Object instantiateItem(ViewGroup collection, int position) {
            MultipartFormField r = mRegistrationViewsManager.list.get(position);
            ViewGroup layout = (ViewGroup) getLayoutInflater().inflate(r.getLayout(), collection, false);
            r.onSetup(layout);
            collection.addView(layout);
            return layout;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return ((View) object) == view;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return super.getPageTitle(position);
        }

        @Override
        public int getCount() {
            return mRegistrationViewsManager.list.size();
        }
    }
}

