package com.eilifint.ravimal.helloauction;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.eilifint.ravimal.helloauction.data.HelloAuctionContract.ItemEntry;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

public class AddItemActivity extends AppCompatActivity {

    /**
     * EditText field to enter the  name
     */
    private EditText mName;

    /**
     * EditText field to enter Description
     */
    private EditText mDescription;

    /**
     * EditText field to enter starting price
     */
    private EditText mStartingPrice;

    /**
     * EditText field duration
     */
    private EditText mDuration;

    /**
     * Image ID
     */

    private static final int SELECTED_IMAGE = 1;
    /**
     * Image views for loading image
     */
    ImageView mImage, mBackgroundImg;

    /**
     * User id
     */
    private int mUserId;

    /**
     * Default image size value
     */
    final static int IMAGE_SIZE = 350;

    /**
     * Default value for shared preferences
     */
    private final static int DEFAULT_VALUE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        //set title to action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle(this.getString(R.string.title_home));

        //adding up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Find all relevant views that we will need to read
        mName = (EditText) findViewById(R.id.name_edit_text);
        mDuration = (EditText) findViewById(R.id.duration_edit_text);
        mDescription = (EditText) findViewById(R.id.description_edit_text);
        mStartingPrice = (EditText) findViewById(R.id.price_edit_text);
        mImage = (ImageView) findViewById(R.id.product_image_view);
        mBackgroundImg = (ImageView) findViewById(R.id.image_view);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUserId = preferences.getInt(this.getString(R.string.user), DEFAULT_VALUE);

        //image on click listener to store image
        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //start mediaStore to select image
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, SELECTED_IMAGE);
                // //set background imageView to invisible if image is selected
                mBackgroundImg.setVisibility(View.INVISIBLE);

            }
        });
    }

    /**
     * onActivityResult is used to get request code,uri a for the image
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //data uri if requestCode and resultCode are equal to given ones
        if (requestCode == SELECTED_IMAGE && resultCode == RESULT_OK) {
            Uri targetUri = data.getData();

            //set image
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetUri));
                mImage.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {

                e.printStackTrace();
            }
        } else {
            //set background imageView to visible if no image is selected
            mBackgroundImg.setVisibility(View.VISIBLE);
        }

    }

    /**
     * actionbar item selection
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save item to database
                insertItem();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Helper method to insert  item data into the database.
     */
    private void insertItem() {

        byte[] image = null;
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mName.getText().toString().trim();
        String descriptionString = mDescription.getText().toString().trim();
        String startPriceString = mStartingPrice.getText().toString().trim();
        String durationString = mDuration.getText().toString().trim();


        //if image is selected convert it to byte array
        if (mImage.getDrawable() != null) {
            Bitmap bitmap = ((BitmapDrawable) mImage.getDrawable()).getBitmap();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            image = bos.toByteArray();

        }

        // Create a ContentValues object where column names are the keys,
        // and  item attributes are the values.
        if (!TextUtils.isEmpty(nameString) &&
                !TextUtils.isEmpty(descriptionString) &&
                !TextUtils.isEmpty(startPriceString) &&
                !TextUtils.isEmpty(durationString) &&
                image != null) {
            //check image size is acceptable to load before store in to database

            if ((image.length) / 1024 < IMAGE_SIZE) {
                if (Integer.parseInt(durationString) <= 60) {

                    //convert start price
                    double price = Double.parseDouble(startPriceString);
                    //Since item hasn't won by a winner and the auction
                    // is not ended by the time item adding
                    final int IS_END = 0;
                    final int WINNER_ID = -1;
                    final long MINUTE = 60000;
                    int NO_OF_MINUTES = Integer.parseInt(durationString);
                    final long AUCTION_DURATION = NO_OF_MINUTES * MINUTE;
                    long startTime = System.currentTimeMillis();
                    long endTime = System.currentTimeMillis() + AUCTION_DURATION;

                    //content value to store key value pair
                    ContentValues values = new ContentValues();
                    values.put(ItemEntry.COLUMN_ITEM_NAME, nameString);
                    values.put(ItemEntry.COLUMN_ITEM_DESCRIPTION, descriptionString);
                    values.put(ItemEntry.COLUMN_ITEM_START_PRICE, price);
                    values.put(ItemEntry.COLUMN_ITEM_IMAGE, image);
                    values.put(ItemEntry.COLUMN_ITEM_IS_END, IS_END);
                    values.put(ItemEntry.COLUMN_ITEM_WINNER_ID, WINNER_ID);
                    values.put(ItemEntry.COLUMN_ITEM_USER_ID, mUserId);
                    values.put(ItemEntry.COLUMN_ITEM_START_TIME, Long.toString(startTime));
                    values.put(ItemEntry.COLUMN_ITEM_END_TIME, Long.toString(endTime));

                    // Insert a new row for Toto in the database, returning the ID of that new row.
                    Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);
                    // Show a toast message depending on whether or not the insertion was successful
                    if (newUri == null) {
                        // If the new content URI is null, then there was an error with insertion.
                        Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Otherwise, the insertion was successful and we can display a toast.
                        Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                                Toast.LENGTH_SHORT).show();
                        // Exit activity
                        finish();
                    }
                } else
                    Toast.makeText(this, getString(R.string.enter_correct_duration), Toast.LENGTH_SHORT).show();
            } else
                Toast.makeText(this, getString(R.string.image_too_large), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.dont_miss),
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //hide delete menu item
        MenuItem menuItem = menu.findItem(R.id.action_delete);
        menuItem.setVisible(false);

        return true;
    }

}
