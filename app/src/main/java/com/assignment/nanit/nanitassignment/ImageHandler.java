package com.assignment.nanit.nanitassignment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Thelegendery on 4/15/2018.
 */
public class ImageHandler {

	private static final String TAG = "ImageHandler";
	static final String PICTURE_FILE_NAME = "profile.png";
	static final String PREF_PICTURE_KEY = "PREF_PICTURE_KEY";

	private static Uri outputFileUri;

	static void handleImage(Activity activityContext, Intent data, ImageView image, boolean withResize) {
		final Uri pickImageResultUri = getPickImageResultUri(activityContext, data);
		if (pickImageResultUri != null) {
			try {
				Bitmap bitmap = MediaStore.Images.Media.getBitmap(activityContext.getContentResolver(), pickImageResultUri);
//					bitmap = rotateImageIfRequired(myBitmap, picUri); // could even rotate if needed
				image.setImageBitmap(bitmap);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
//				bitmap = (Bitmap) data.getExtras().get("data"); // if we will want to get only a thumbnail but then
//              we need to remove the intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri); line of code
			setImage(activityContext, image, withResize);
		}
	}

	/**
	 * Create a chooser intent to select the source to get image from.<br />
	 * The source can be camera's (ACTION_IMAGE_CAPTURE) or gallery's (ACTION_GET_CONTENT).<br />
	 * All possible sources are added to the intent chooser.
	 */
	public static Intent getPickImageChooserIntent(Activity activityContext) {
		// Determine Uri of camera image to save.
		outputFileUri = getCaptureImageOutputUri(activityContext);

		List<Intent> allIntents = new ArrayList<>();
		PackageManager packageManager = activityContext.getPackageManager();
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

	/**
	 * Get URI to image received from capture by camera.
	 */
	static Uri getCaptureImageOutputUri(Activity activityContext) {
		Uri outputFileUri = null;
		File getImage = activityContext.getExternalFilesDir(null);
		if (getImage != null) {
			outputFileUri = Uri.fromFile(new File(getImage.getPath(), PICTURE_FILE_NAME));
			SharedPreferenceHandler.saveToSharedPreferences(activityContext, PREF_PICTURE_KEY, outputFileUri.getPath());
		}
		return outputFileUri;
	}

	private static void collectAllCameraIntents(List<Intent> allIntents, PackageManager packageManager) {
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

	private static void collectAllGalleryIntents(List<Intent> allIntents, PackageManager packageManager) {
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

	private static Intent buildMainIntent(List<Intent> allIntents) {
		Intent mainIntent = allIntents.get(allIntents.size() - 1);
		for (Intent intent : allIntents) {
			if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
				mainIntent = intent;
				break;
			}
		}
		return mainIntent;
	}

	static void setImage(Context context, ImageView imageView, boolean withResize) {
		context.getContentResolver().notifyChange(ImageHandler.outputFileUri, null);
		ContentResolver cr = context.getContentResolver();
		try {
			Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, ImageHandler.outputFileUri);
			int valueInPixels = (int) context.getResources().getDimension(R.dimen.image_max_size);
			if (withResize) {
				bitmap = getResizedBitmap(bitmap, valueInPixels); // no need in this activity
			}
//			bitmap = rotateImageIfRequired(myBitmap, picUri); // could even rotate if needed

			// Create the RoundedBitmapDrawable.
			RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
			roundDrawable.setCircular(true);

			// Apply it to an ImageView.
			imageView.setImageDrawable(roundDrawable);

		} catch (Exception e) {
			Toast.makeText(context, "Failed to load", Toast.LENGTH_SHORT).show();
			Log.d(TAG, "Failed to load", e);
		}
	}

	private static Bitmap getResizedBitmap(Bitmap image, int maxSize) {
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
	private static Uri getPickImageResultUri(Activity activityContext, Intent data) {
		boolean isCamera = true;
		if (data != null) {
			String action = data.getAction();
			isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
		}
		return isCamera ? getCaptureImageOutputUri(activityContext) : data.getData();
	}

	static boolean setPictureFromStorageIfExist(Context context, ImageView picture, String pictureFilePath) {
		boolean success = false;
		if (pictureFilePath != null) {
			ContentResolver cr = context.getContentResolver();
			try {
				File getImage = context.getExternalFilesDir(null);
				if (getImage != null) {
					Uri fileUri = Uri.fromFile(new File(getImage.getPath(), ImageHandler.PICTURE_FILE_NAME));
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(cr, fileUri);
					if (picture != null) {
						RoundedBitmapDrawable roundDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), bitmap);
						roundDrawable.setCircular(true);
						picture.setImageDrawable(roundDrawable);
						success = true;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return success;
	}


}
