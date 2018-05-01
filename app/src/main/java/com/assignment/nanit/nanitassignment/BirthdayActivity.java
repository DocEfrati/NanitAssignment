package com.assignment.nanit.nanitassignment;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.assignment.nanit.nanitassignment.DetailsActivity.CAMERA_REQUEST_CODE;
import static com.assignment.nanit.nanitassignment.DetailsActivity.EXTRA_BIRTHDAY;
import static com.assignment.nanit.nanitassignment.DetailsActivity.EXTRA_NAME;
import static com.assignment.nanit.nanitassignment.DetailsActivity.EXTRA_PICTURE_FILE_PATH;

public class BirthdayActivity extends AppCompatActivity {

	private static final String POPUP = "popup";
	private static final String PLACE_HOLDER = "placeHolder";
	private static final String CAMERA = "camera";
	private static final String TAG = "BirthdayActivity";

	@BindView(R.id.name)
	TextView name;

	@BindView(R.id.age_old_text)
	TextView ageOldText;

	@BindView(R.id.age_first_digit)
	ImageView ageFirstDigit;

	@BindView(R.id.age_second_digit)
	ImageView ageSecondDigit;

	@BindView(R.id.popup)
	ImageView popup;

	@BindView(R.id.place_holder)
	ImageView placeHolder;

	@BindView(R.id.picture_btn)
	ImageView pictureBtn;

	private SparseArray<Map<String,Integer>> resourcesMaps;
	private SparseIntArray numbersMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_birthday);
		ButterKnife.bind(this);
		initResourcesMaps();
		initNumbersMap();
		setUi();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == CAMERA_REQUEST_CODE) {
				ImageHandler.handleImage(this, data, placeHolder, true, placeHolder.getHeight());
				setResult(Activity.RESULT_OK);
			}
		}
	}

	@OnClick(R.id.close_button)
	public void onCloseButtonClick() {
		onBackPressed();
	}

	@OnClick(R.id.picture_btn)
	public void onPictureButtonClick() {
		Log.d(TAG, "onPictureButtonClick: ");
		startActivityForResult(ImageHandler.getPickImageChooserIntent(this), CAMERA_REQUEST_CODE);
	}

	private void initResourcesMaps() {
		resourcesMaps = new SparseArray<>();
		final Map<String, Integer> yellowResourcesMap = buildResourceMap(R.drawable.android_elephant_popup,
				R.drawable.camera_icon_yellow, R.drawable.default_place_holder_yellow);
		resourcesMaps.put(0, yellowResourcesMap);
		final Map<String, Integer> blueResourcesMap = buildResourceMap(R.drawable.android_pelican_popup,
				R.drawable.camera_icon_blue, R.drawable.default_place_holder_blue);
		resourcesMaps.put(1, blueResourcesMap);
		final Map<String, Integer>greenResourcesMap = buildResourceMap(R.drawable.android_fox_popup,
				R.drawable.camera_icon_green, R.drawable.default_place_holder_green);
		resourcesMaps.put(2, greenResourcesMap);
	}

	private Map<String, Integer> buildResourceMap(int popupResId, int cameraResId, int placeHolderResId) {
		Map<String,Integer> resourcesMap = new HashMap<>();
		resourcesMap.put(POPUP, popupResId);
		resourcesMap.put(CAMERA, cameraResId);
		resourcesMap.put(PLACE_HOLDER, placeHolderResId);
		return resourcesMap;
	}

	private void initNumbersMap() {
		numbersMap = new SparseIntArray();
		numbersMap.put(0, R.drawable.zero);
		numbersMap.put(1, R.drawable.one);
		numbersMap.put(2, R.drawable.two);
		numbersMap.put(3, R.drawable.three);
		numbersMap.put(4, R.drawable.four);
		numbersMap.put(5, R.drawable.five);
		numbersMap.put(6, R.drawable.six);
		numbersMap.put(7, R.drawable.seven);
		numbersMap.put(8, R.drawable.eight);
		numbersMap.put(9, R.drawable.nine);
		numbersMap.put(10, R.drawable.ten);
		numbersMap.put(11, R.drawable.eleven);
		numbersMap.put(12, R.drawable.twelve);
	}

	private void setUi() {
		final Intent intent = getIntent();
		final String nameText = intent.getStringExtra(EXTRA_NAME);
		final String birthday = intent.getStringExtra(EXTRA_BIRTHDAY);
		final String pictureFilePath = intent.getStringExtra(EXTRA_PICTURE_FILE_PATH);
		name.setText(getString(R.string.today_birthday, nameText));
		Random random = new Random();
		final int randomNum = random.nextInt(3);
		final Map<String, Integer> resourcesMap = resourcesMaps.get(randomNum);

		boolean setImageFromStorageSuccessfully = false;
		if (pictureFilePath != null) {
			setImageFromStorageSuccessfully = ImageHandler.setPictureFromStorageIfExist(this, placeHolder,
					pictureFilePath, false, getPlaceHolderDefaultSize());
		}
		if (!setImageFromStorageSuccessfully) {
			placeHolder.setImageResource(resourcesMap.get(PLACE_HOLDER));
		}
		popup.setImageResource(resourcesMap.get(POPUP));
		pictureBtn.setImageResource(resourcesMap.get(CAMERA));

		setAge(birthday);
	}

	private int getPlaceHolderDefaultSize() {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int screenWidth = displaymetrics.widthPixels;
		int screenHeight = displaymetrics.heightPixels;
		placeHolder.measure(screenWidth, screenHeight);
		return placeHolder.getMeasuredHeight();
	}

	/**
	 * Method to extract the user's age from the entered Date of Birth.
	 *
	 */
	private void setAge(String birthday){
		Calendar dob = Calendar.getInstance();
		Calendar today = Calendar.getInstance();
		setToday(birthday, dob);

		final int todayYear = today.get(Calendar.YEAR);
		final int dobYear = dob.get(Calendar.YEAR);
		final int todayMonth = today.get(Calendar.MONTH);
		final int dobMonth = dob.get(Calendar.MONTH);
		final int todayDayOfMonth = today.get(Calendar.DAY_OF_MONTH);
		final int dobDayOfMonth = dob.get(Calendar.DAY_OF_MONTH);

		// Check if if the age is less then 12 month
		if (todayYear == dobYear || ( todayYear - dobYear == 1 &&
				(todayMonth < dobMonth || (todayMonth == dobMonth && dobDayOfMonth < todayDayOfMonth) ) ) ) {
			setAgeInMonth(dob, today);
		} else {
			setAgeInYears(dob, today, todayYear, dobYear);
		}
	}

	private void setToday(String birthday, Calendar dob) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
		try {
			dob.setTime(sdf.parse(birthday));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private void setAgeInYears(Calendar dob, Calendar today, int todayYear, int dobYear) {
		int age = todayYear - dobYear;

		if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)){
			age--;
		}
		int firstDigit = age;
		if (age > 12) {
			ageSecondDigit.setImageResource(numbersMap.get(age % 10));
			ageSecondDigit.setVisibility(View.VISIBLE);
			firstDigit = age / 10;
		}
		ageFirstDigit.setImageResource(numbersMap.get(firstDigit));
		ageOldText.setText(R.string.year);
	}

	private void setAgeInMonth(Calendar dob, Calendar today) {
		int monthsBetween = 0;
		int dateDiff = today.get(Calendar.DAY_OF_MONTH) - dob.get(Calendar.DAY_OF_MONTH);

		if (dateDiff < 0) {
			int borrow = today.getActualMaximum(Calendar.DAY_OF_MONTH);
			dateDiff = (today.get(Calendar.DAY_OF_MONTH) + borrow) - dob.get(Calendar.DAY_OF_MONTH);
			monthsBetween--;

			if (dateDiff > 0) {
				monthsBetween++;
			}
		}

		monthsBetween += today.get(Calendar.MONTH) - dob.get(Calendar.MONTH);
		monthsBetween += (today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)) * 12;
		ageFirstDigit.setImageResource(numbersMap.get(monthsBetween));
		ageOldText.setText(R.string.month);
	}

}
