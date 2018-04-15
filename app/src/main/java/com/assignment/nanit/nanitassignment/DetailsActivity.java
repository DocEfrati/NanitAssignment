package com.assignment.nanit.nanitassignment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class DetailsActivity extends AppCompatActivity {

	private static final String TAG = "DetailsActivity";
	private static final String PREF_NAME_KEY = "PREF_NAME_KEY";
	private static final String PREF_BIRTHDAY_KEY = "PREF_BIRTHDAY_KEY";
	private static final String PREF_PICTURE_KEY = "PREF_PICTURE_KEY";
	private static final String PICTURE_FILE_NAME = "profile.png";

	@BindView(R.id.name_edittext)
	EditText name;

	@BindView(R.id.birthday_edittext)
	EditText birthday;

	@BindView(R.id.picture)
	ImageView picture;

	@BindView(R.id.show_birthday_screen_btn)
	Button showBirthdayScreenBtn;

	private Uri outputFileUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_details);
		ButterKnife.bind(this);
		handlePermissions();
		setUi();

		// TODO: 4/5/2018 check for code correctness
	}

	@OnClick(R.id.picture_btn)
	public void onPictureButtonClick() {
		Log.d(TAG, "onPictureButtonClick: ");
		startActivityForResult(getPickImageChooserIntent(), 200);
	}

	@OnClick(R.id.show_birthday_screen_btn)
	public void onShowBirthdayScreenButtonClick() {
		Log.d(TAG, "onShowBirthdayScreenButtonClick: ");
	}

	/**
	 * Create a chooser intent to select the source to get image from.<br />
	 * The source can be camera's (ACTION_IMAGE_CAPTURE) or gallery's (ACTION_GET_CONTENT).<br />
	 * All possible sources are added to the intent chooser.
	 */
	public Intent getPickImageChooserIntent() {
		// Determine Uri of camera image to save.
		outputFileUri = getCaptureImageOutputUri();

		List<Intent> allIntents = new ArrayList<>();
		PackageManager packageManager = getPackageManager();
		collectAllCameraIntents(allIntents, packageManager);
		collectAllGalleryIntents(allIntents, packageManager);

		// the main intent is the last in the list (ohh android...) so pickup the useless one
		Intent mainIntent = buildMainIntent(allIntents);
		allIntents.remove(mainIntent);

		// Create a chooser from the main intent
		Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");

		// Add all other intents
		chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

		return chooserIntent;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (getPickImageResultUri(data) != null) {
				Uri picUri = getPickImageResultUri(data);
				try {
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), picUri);
//					bitmap = rotateImageIfRequired(myBitmap, picUri); // could even rotate if needed
					picture.setImageBitmap(bitmap);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
//				bitmap = (Bitmap) data.getExtras().get("data"); // if we will want to get only a thumbnail but then
//              we need to remove the intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri); line of code
				setImage(picture);
			}
		}
	}

	private void setUi() {
		SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
		final String nameFromPref = sharedPref.getString(PREF_NAME_KEY, null);
		name.setText(nameFromPref);
		final String birthdayFromPref = sharedPref.getString(PREF_BIRTHDAY_KEY, null);
		birthday.setText(birthdayFromPref);
		final String pictureFilePath = sharedPref.getString(PREF_PICTURE_KEY, null);
		birthday.setText(birthdayFromPref);
		setNameTextChangesHandler();
		setBirthdayOnClickHandler();
		addPictureFromStorageIfExist(pictureFilePath);
		setShowBirthdayScreenButton();

	}

	private void addPictureFromStorageIfExist(String pictureFilePath) {
		if (pictureFilePath != null) {
			ContentResolver cr = this.getContentResolver();
			try {
				File getImage = getExternalFilesDir(null);
				if (getImage != null) {
					Uri fileUri = Uri.fromFile(new File(getImage.getPath(), PICTURE_FILE_NAME));
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(cr, fileUri);
					if (picture != null) {
						picture.setImageBitmap(bitmap);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void setNameTextChangesHandler() {
		// Both not working for all cases
		//name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
		//name.setOnEditorActionListener(new EditText.OnEditorActionListener()

		name.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				// I can also validate the text if we want to.
				// I can do it more sophisticated and performance wise if i will wait some X amount of time and only
				// if the user didn't enter another letter then save it.
				saveToSharedPreferences(PREF_NAME_KEY, s.toString());
				setShowBirthdayScreenButton();
			}
		});
	}

	private void collectAllCameraIntents(List<Intent> allIntents, PackageManager packageManager) {
		// collect all camera intents
		Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
		for (ResolveInfo res : listCam) {
			Intent intent = new Intent(captureIntent);
			intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
			intent.setPackage(res.activityInfo.packageName);
			if (outputFileUri != null) {
				intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
			}
			allIntents.add(intent);
		}
	}

	private void collectAllGalleryIntents(List<Intent> allIntents, PackageManager packageManager) {
		// collect all gallery intents
		Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
		galleryIntent.setType("image/*");
		List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
		for (ResolveInfo res : listGallery) {
			Intent intent = new Intent(galleryIntent);
			intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
			intent.setPackage(res.activityInfo.packageName);
			allIntents.add(intent);
		}
	}

	private Intent buildMainIntent(List<Intent> allIntents) {
		Intent mainIntent = allIntents.get(allIntents.size() - 1);
		for (Intent intent : allIntents) {
			if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
				mainIntent = intent;
				break;
			}
		}
		return mainIntent;
	}

	private void setShowBirthdayScreenButton() {
		// Can make it a one line code but this is more readable.
		if (!TextUtils.isEmpty(name.getText()) && !TextUtils.isEmpty(birthday.getText())) {
			showBirthdayScreenBtn.setEnabled(true);
		} else {
			showBirthdayScreenBtn.setEnabled(false);
		}
	}

	private void saveToSharedPreferences(String key, String value) {
		SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPref.edit();
		editor.putString(key, value);
		editor.apply();
	}

	private void handlePermissions() {
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//			if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
//					!= PackageManager.PERMISSION_GRANTED) {
//
//				// Should we show an explanation?
//				if (shouldShowRequestPermissionRationale(
//						Manifest.permission.READ_EXTERNAL_STORAGE)) {
//					// Explain to the user why we need to read the contacts
//				}
//
//				requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
//						MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
//
//				// MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE is an
//				// app-defined int constant that should be quite unique
//
//				return;
//			}
//		}
	}

	private void setBirthdayOnClickHandler() {
		birthday.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DatePickerDialog.OnDateSetListener datePickerListener = listenToDatePicker();
				Calendar date = Calendar.getInstance();
				DatePickerDialog datePickerDialog = new DatePickerDialog(DetailsActivity.this, datePickerListener,
						date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
				datePickerDialog.show();
			}

			@NonNull
			private DatePickerDialog.OnDateSetListener listenToDatePicker() {
				return new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

						int month = monthOfYear + 1;
						String dateString = dayOfMonth + "/" + month + "/" + year;
						birthday.setText(dateString);
						saveToSharedPreferences(PREF_BIRTHDAY_KEY, dateString);
						setShowBirthdayScreenButton();
					}
				};
			}
		});
	}


	/**
	 * Get URI to image received from capture by camera.
	 */
	private Uri getCaptureImageOutputUri() {
		Uri outputFileUri = null;
		File getImage = getExternalFilesDir(null);
		if (getImage != null) {
			outputFileUri = Uri.fromFile(new File(getImage.getPath(), PICTURE_FILE_NAME));
			saveToSharedPreferences(PREF_PICTURE_KEY, outputFileUri.getPath());
		}
		return outputFileUri;
	}

	private void setImage(ImageView imageView) {
		this.getContentResolver().notifyChange(outputFileUri, null);
		ContentResolver cr = this.getContentResolver();
		try {
			Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, outputFileUri);
//			int valueInPixels = (int) getResources().getDimension(R.dimen.image_max_size);
//			bitmap = getResizedBitmap(bitmap, valueInPixels); // no need in this activity
//			bitmap = rotateImageIfRequired(myBitmap, picUri); // could even rotate if needed

			// Create the RoundedBitmapDrawable.
			RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
			roundDrawable.setCircular(true);

			// Apply it to an ImageView.
			imageView.setImageDrawable(roundDrawable);

		} catch (Exception e) {
			Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
			Log.d(TAG, "Failed to load", e);
		}
	}

	private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
		int width = image.getWidth();
		int height = image.getHeight();

		float bitmapRatio = (float) width / (float) height;
		if (bitmapRatio > 0) {
			width = maxSize;
			height = (int) (width / bitmapRatio);
		} else {
			height = maxSize;
			width = (int) (height * bitmapRatio);
		}
		return Bitmap.createScaledBitmap(image, width, height, true);
	}

	/**
	 * Get the URI of the selected image from getPickImageChooserIntent() which
	 * Will return the correct URI for camera and gallery image.
	 *
	 * @param data the returned data of the activity result
	 */
	private Uri getPickImageResultUri(Intent data) {
		boolean isCamera = true;
		if (data != null) {
			String action = data.getAction();
			isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
		}
		return isCamera ? getCaptureImageOutputUri() : data.getData();
	}

}
