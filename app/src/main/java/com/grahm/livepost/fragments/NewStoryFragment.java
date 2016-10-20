package com.grahm.livepost.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.grahm.livepost.R;
import com.grahm.livepost.activities.MainActivity;
import com.grahm.livepost.asynctask.CreateStoryTask;
import com.grahm.livepost.interfaces.OnFragmentInteractionListener;
import com.grahm.livepost.interfaces.OnPutImageListener;
import com.grahm.livepost.objects.MultipartFormField;
import com.grahm.livepost.objects.Story;
import com.grahm.livepost.objects.User;
import com.grahm.livepost.util.GV;
import com.grahm.livepost.util.Utilities;
import com.objectlife.statelayout.StateLayout;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class NewStoryFragment extends Fragment implements OnPutImageListener {
    public static final String SESSION_KEY = "session";
    public static final String PAGE_KEY = "page";
    public static final String URI_KEY = "uri";
    public static final String FILE_KEY = "file";
    private DatabaseReference mFirebaseRef;
    private static final String TAG = "NewStoryFragment";
    private CreateStoryTask mStoryTask = null;
    private Story mStory;
    private Uri mUri;
    private File mFile;
    private int mCurrentItem;
    private User mUser;
    private NewSessionViewsManager mNewSessionViewsManager;

    // UI references.
    @BindView(R.id.new_story_pager)
    public ViewPager mViewPager;
    @BindView(R.id.new_story_toolbar)
    public Toolbar mToolbar;
    @BindView(R.id.new_story_progress)
    public ProgressBar mProgressBarView;
    @BindView(R.id.new_story_progress_str)
    public TextView mProgressBarTextView;
    @BindView(R.id.new_story_btn_next)
    public Button mBtnNext;

    private OnFragmentInteractionListener mListener;

    public NewStoryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment NewStoryFragment.
     */
    public static NewStoryFragment newInstance(Bundle args) {
        NewStoryFragment fragment = new NewStoryFragment();
        if (args != null)
            fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mStory = (Story) savedInstanceState.getSerializable(SESSION_KEY);
            mCurrentItem = savedInstanceState.getInt(PAGE_KEY, 0);
            String uriString = savedInstanceState.getString(URI_KEY);
            mUri = uriString == null ? null : Uri.parse(uriString);
            mFile = (File)savedInstanceState.getSerializable(FILE_KEY);
        }
        mUser = Utilities.getUser(mFirebaseRef, getActivity(), savedInstanceState);
        mStory = mStory == null ? new Story() : mStory;
        mFirebaseRef = FirebaseDatabase.getInstance().getReference();
        mNewSessionViewsManager = new NewSessionViewsManager();
        setHasOptionsMenu(true);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_settings);
        item.setVisible(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        View view = inflater.inflate(R.layout.fragment_new_session, container, false);
        ButterKnife.bind(this, view);
        toolbarSetup();
        EasyImage.configuration(activity)
                .setImagesFolderName("images")
                .saveInAppExternalFilesDir()
                .setCopyExistingPicturesToPublicLocation(true);
        mViewPager.setAdapter(new NewSessionPagerAdapter(inflater));
        switchMainActivityView(StateLayout.VIEW_CONTENT);

        updateProgressViews();
        return view;
    }

    private void toolbarSetup() {
//        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_36dp);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentItem = mViewPager.getCurrentItem();
                if (currentItem == 0) {
                    getActivity().onBackPressed();
                } else {
                    mViewPager.setCurrentItem(Math.max(currentItem - 1, 0), true);
                    updateProgressViews();
                }
            }
        });

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    private void updateProgressViews() {
        MultipartFormField r = mNewSessionViewsManager.list.get(mViewPager.getCurrentItem());
        mToolbar.setTitle(getString(r.getTitle()));

        int vCount = mViewPager.getAdapter().getCount();
        int current = mViewPager.getCurrentItem() + 1;
        mProgressBarTextView.setText(current + "/" + vCount);
        mProgressBarView.setProgress(current * 100 / vCount);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PAGE_KEY, mViewPager.getCurrentItem());
        outState.putSerializable(SESSION_KEY, mStory);
        if (mUri != null)
            outState.putString(URI_KEY, mUri.toString());
        if(mFile != null)
            outState.putSerializable(FILE_KEY,mFile);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //Listener to avoid transfering clicks to the views below this fragment.
    @OnClick(R.id.new_story_background)
    public void backgroundEmptyClick() {
        //Leave Empty
    }

    @OnClick(R.id.new_story_btn_next)
    public void nextButton(View v) {
        if(mViewPager != null && mViewPager.getAdapter() != null){
            int vCount = mViewPager.getAdapter().getCount();
            int current = mViewPager.getCurrentItem();
            MultipartFormField r = mNewSessionViewsManager.list.get(current);
            if (r.onValidate()) {
                if (current + 1 == vCount) {//Send Registration Form
                    attemptSessionCreation();
                }
                mViewPager.setCurrentItem(current + 1, true);
                updateProgressViews();
                v.setVisibility(current == 0 ? View.INVISIBLE : View.VISIBLE);
            }
        }
    }

    public void attemptSessionCreation() {
        if (mStoryTask != null) return;

        mStory.setAuthor(mUser.getUserKey());
        mStory.setAuthor_name(mUser.getName());
        mStoryTask = new CreateStoryTask(mStory, mFirebaseRef.child("posts"), getActivity(), this, true);
        if (mUri != null) {
            Log.e(TAG,"Executing: "+mUri);
            //mStoryTask.execute(mUri);
            mStoryTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mUri);
        }else{
            Log.e(TAG,"Null Uri: "+mUri);
        }

    }
    private void switchMainActivityView(int state){
        Bundle b = new Bundle();
        b.putInt(MainActivity.STATE_KEY, state);
        ((OnFragmentInteractionListener)getActivity()).onFragmentInteraction(MainActivity.VIEW_INTERACTIONS,b);
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */


    public class NewSessionViewsManager {
        private NewSessionErrors mNewSessionErrors;
        public List<MultipartFormField> list;

        public NewSessionViewsManager() {
            mNewSessionErrors = new NewSessionErrors();
            list = new ArrayList<MultipartFormField>();
            list.add(new StoryNameField());
            list.add(new CategoryField());
            list.add(new ImageField());
        }

        public class NewSessionErrors {
            // String binding
            public final String ERROR_TITLE;
            public final String ERROR_NO_IMG;

            NewSessionErrors() {
                ERROR_TITLE = getString(R.string.ns_story_name_error);
                ERROR_NO_IMG = getString(R.string.ns_story_no_img_error);
            }
        }

        public class StoryNameField extends MultipartFormField {
            public int getTitle() {
                return R.string.ns_story_name;
            }

            public int getLayout() {
                return R.layout.new_session_storyname;
            }

            public boolean onValidate() {
                final TextView storyNameView = ButterKnife.findById(mViewPager, R.id.new_session_story_name);
                String s = storyNameView.getText().toString().trim();
                if (TextUtils.isEmpty(s) || s.length() < 2) {
                    storyNameView.setError(mNewSessionErrors.ERROR_TITLE);
                    return false;
                }
                mStory.setTitle(s);
                return true;
            }

            public void onSetup(ViewGroup layout) {
                ButterKnife.findById(layout, R.id.new_session_story_name).requestFocus();
            }
        }

        public class CategoryField extends MultipartFormField {
            private Spinner mSpinner;
            private ListView mList;

            public int getTitle() {
                return R.string.ns_category;
            }

            public int getLayout() {
                return R.layout.new_session_category;
            }

            public boolean onValidate() {
                return mStory.getCategory() != null;
            }

            public void onSetup(ViewGroup layout) {

                ArrayAdapter adapter = new ArrayAdapter(getActivity().getApplicationContext(), R.layout.item_category,
                        R.id.txt_category, getActivity().getResources().getStringArray(R.array.categories));
                mList = ButterKnife.findById(layout, R.id.list_categories);
                mList.setAdapter(adapter);
                mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Object listItem = mList.getItemAtPosition(position);
                        mStory.setCategory(listItem.toString());
                        view.setBackgroundColor(Color.rgb(234, 234, 234));
                        nextButton(mBtnNext);
                    }
                });
            }

        }

        public class ImageField extends MultipartFormField {

            public int getTitle() {
                return R.string.ns_image;
            }

            public int getLayout() {
                return R.layout.new_session_image;
            }

            public void onSetup(ViewGroup layout) {
                ImageButton imageButton = (ImageButton) layout.findViewById(R.id.new_session_avatar);
                if (mUri != null) {
                    ButterKnife.findById(layout, R.id.new_session_avatar_layout).setVisibility(View.GONE);
                    ImageView resultView = (ImageView) layout.findViewById(R.id.new_session_selected_img);
                    resultView.setVisibility(View.VISIBLE);
//                    resultView.setImageResource(R.drawable.ic_add_a_photo_black_48dp);
                    Bitmap media = BitmapFactory.decodeFile(mFile.getPath());
                    resultView.setImageBitmap(media);
//                    resultView.setImageURI(mUri);


                    resultView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EasyImage.openChooserWithDocuments(NewStoryFragment.this, mStory.getTitle(), 1);
                        }
                    });
                }
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EasyImage.openChooserWithDocuments(NewStoryFragment.this, mStory.getTitle(), 1);
                    }
                });


            }

            public boolean onValidate() {
//                if (mUri == null) {
//                    TextInputLayout t = ButterKnife.findById(mViewPager, R.id.new_session_avatar_input);
//                    if (t != null)
//                        t.setError(mNewSessionErrors.ERROR_NO_IMG);
//                    return false;
//                }
//                return true;
                return mUri != null;
            }
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EasyImage.handleActivityResult(requestCode, resultCode, data, getActivity(), new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                TextInputLayout t = ButterKnife.findById(mViewPager, R.id.new_session_avatar_input);
                if (t != null)
                    t.setError(e.getMessage());
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                //Handle the image
                onPhotoReturned(imageFile);
            }
        });

    }

    private void onPhotoReturned(File imageFile) {
        mFile = imageFile;
        mUri = Uri.fromFile(imageFile);
        ButterKnife.findById(mViewPager, R.id.new_session_avatar_layout).setVisibility(View.GONE);
        ImageView resultView = ButterKnife.findById(mViewPager, R.id.new_session_selected_img);
        resultView.setVisibility(View.VISIBLE);
        resultView.setImageResource(R.drawable.ic_add_a_photo_black_48dp);
        resultView.setImageURI(mUri);
        resultView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyImage.openChooserWithDocuments(NewStoryFragment.this, mStory.getTitle(), 1);
            }
        });
        Log.e(TAG, mUri.toString());
        mStory.setPosts_picture(mUri.toString());
    }

    @Override
    public void onSuccess(String url) {
        mListener.onFragmentInteraction(MainActivity.HOME_IDX, null);
    }

    private class NewSessionPagerAdapter extends PagerAdapter {
        LayoutInflater mInflater;

        public NewSessionPagerAdapter(LayoutInflater inflater) {
            mInflater = inflater;
        }

        public Object instantiateItem(ViewGroup collection, int position) {
            MultipartFormField r = mNewSessionViewsManager.list.get(position);
            ViewGroup layout = (ViewGroup) mInflater.inflate(r.getLayout(), collection, false);
            r.onSetup(layout);
            collection.addView(layout);
            updateProgressViews();
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
            return mNewSessionViewsManager.list.size();
        }
    }

}
