package com.job.friendlyeats;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.job.friendlyeats.model.Restaurant;

import java.io.IOException;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

import static android.app.Activity.RESULT_OK;

/**
 * Created by JOB on 3/5/2018.
 */

public class RestaurantDialogueFragment extends DialogFragment {

    public static final String TAG = "RestaurantDialogueFragment";

    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 71;
    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;
    private FirebaseFirestore mFirestore;
    ProgressDialog progressDialog;

    public interface RestaurantListner{
        void onRestaurantAdd();
    }

    public RestaurantListner mRestaurantListner;

    //binding our layout res ids
    @BindView(R.id.floatingActionButton)
    FloatingActionButton floatingActionButton;
    @BindView(R.id.textview_restrant_name)
    EditText restaurantName;
    @BindView(R.id.textView_city)
    AutoCompleteTextView location;
    @BindView(R.id.textView_food_category)
    AutoCompleteTextView foodCategory;
    @BindView(R.id.ratingBar)
    MaterialRatingBar ratingBar;
    @BindView(R.id.button_cancel)
    Button btnCancel;
    @BindView(R.id.button_save)
    Button btnSave;
    @BindView(R.id.imageView)
    ImageView imageView;
    @BindView(R.id.spinner_set_price)
    Spinner priceSpinner;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.restraunt_input,container,false);
        ButterKnife.bind(this, v);

        //set up autocomplete on foodCategory
        String[] foodAdapter = getResources().getStringArray(R.array.categories);
        String[] cityAdapter = getResources().getStringArray(R.array.cities);
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,countries);
        setMyAutoCompletes(foodAdapter,foodCategory);
        setMyAutoCompletes(cityAdapter,location);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 chooseImage();
            }
        });

        //progressDialog = new ProgressDialog(getActivity());

        //firebase init
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        mFirestore = FirebaseFirestore.getInstance();

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);


    }

    private void setMyAutoCompletes(String[] strings, AutoCompleteTextView autoCompleteTextView){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity().getApplication().getApplicationContext(),
                android.R.layout.simple_dropdown_item_1line,strings);
        autoCompleteTextView.setAdapter(adapter);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof RestaurantListner) {
            mRestaurantListner = (RestaurantListner) context;
        }
    }

    private void showError(EditText mEditText) {
        Animation shake = AnimationUtils.loadAnimation(getActivity().getApplication().getApplicationContext(), R.anim.shake);
        mEditText.startAnimation(shake);
    }
    private void showError(ImageView imageView) {
        Animation tremble = AnimationUtils.loadAnimation(getActivity().getApplication().getApplicationContext(), R.anim.tremble);
        imageView.startAnimation(tremble);
        Vibrator v = (Vibrator) getActivity().getApplication().getSystemService(Context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(500);
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @OnClick(R.id.button_save)
    public void saveClick(){
        //create an error on the edit box
        if(TextUtils.isEmpty(restaurantName.getText())) {
            showError(restaurantName);
            restaurantName.setDrawingCacheBackgroundColor(Color.red(1));
            restaurantName.setError("needs a name :)");
            return;
        }
        if(TextUtils.isEmpty(location.getText())) {
            showError(location);
            location.setDrawingCacheBackgroundColor(Color.red(1));
            location.setError("needs a city :)");
            return;
        }
        if(TextUtils.isEmpty(foodCategory.getText())) {
            showError(foodCategory);
            foodCategory.setDrawingCacheBackgroundColor(Color.red(1));
            foodCategory.setError("needs a category :)");
            return;
        }
        if(filePath == null){
            showError(imageView);
            return;
        }

        //upload image and save new restaurant to Db
        onclickSaveToDb();
        dismiss();
    }

    private void onclickSaveToDb() {

        final Restaurant restaurant = new Restaurant();
        restaurant.setName(restaurantName.getText().toString().trim());
        restaurant.setCity(location.getText().toString().trim());
        restaurant.setCategory(foodCategory.getText().toString().trim());
        restaurant.setPrice(getSelectedPrice());
        //restaurant.setPhoto(imageUrl[0]);
        restaurant.setAvgRating(1L);
        restaurant.setNumRatings(ratingBar.getNumStars());



        final Activity activityObj = this.getActivity();
        final String[] imageUrl = new String[1];

        if (filePath != null){
            //image

           /* progressDialog.setTitle("Uploading...");
            progressDialog.show();*/

            StorageReference ref = storageReference.child("images/"+ UUID.randomUUID().toString());

            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //progress finished
                            //progressDialog.dismiss();
                           imageUrl[0] = taskSnapshot.getDownloadUrl().toString();
                            restaurant.setPhoto(taskSnapshot.getDownloadUrl().toString());
                            //saving
                            // Get a reference to the restaurants collection
                            CollectionReference restaurants = mFirestore.collection("restaurants");
                            restaurants.add(restaurant);
                            //imageUrl[0] = takeString("k");
                            Toast.makeText(activityObj, "Uploaded!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            Toast.makeText(activityObj, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = 100.0 * (taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            //progressDialog.setMessage("Uploading... "+progress+"%");
                        }
                    });
        }
    }

    private int getSelectedPrice() {
        String selected = (String) priceSpinner.getSelectedItem();
        if (selected.equals(getString(R.string.price_1))) {
            return 1;
        } else if (selected.equals(getString(R.string.price_2))) {
            return 2;
        } else if (selected.equals(getString(R.string.price_3))) {
            return 3;
        } else {
            return -1;
        }
    }

    @OnClick(R.id.button_cancel)
    public void cancelClick(){
        //new MainActivity().showTodoToast();
        dismiss();
    }
}
