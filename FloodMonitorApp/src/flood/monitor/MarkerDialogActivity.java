package flood.monitor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

import flood.monitor.modules.Connector;
import flood.monitor.modules.kmlparser.Marker;

public class MarkerDialogActivity extends Activity {

	// ===========================================================
	// Constants
	// ===========================================================

	// ===========================================================
	// Fields
	// ===========================================================
	private int mode;
	private boolean upload;
	private String localImage;
	private Marker marker;
	private MarkerDialogActivity activity;

	// ===========================================================
	// Methods from Activity
	// ===========================================================
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Activity is first created.
		setContentView(R.layout.markerdialog);
		activity = this;
		upload = false;
		Bundle markerData = getIntent().getExtras();
		if (markerData != null) {
			marker = new Marker(markerData.getInt("id"), new GeoPoint(
					(int) (markerData.getDouble("latitude") * 1000000),
					(int) (markerData.getDouble("longitude") * 1000000)),
					markerData.getString("title"),
					markerData.getString("desc"),
					markerData.getString("image"),
					markerData.getInt("severity"));
			marker.setRegionId(markerData.getInt("regionId"));
			marker.setEventId(markerData.getInt("eventId"));

			mode = markerData.getInt("mode");
			upload = markerData.getBoolean("upload");
			TextView titleView;
			TextView descView;
			TextView imageView;
			ProgressBar imageCircle;
			TextView latView;
			TextView lonView;
			TextView addressView;
			ProgressBar circle;

			switch (mode) {
			case MapViewActivity.MARKER_LOCATION:
				titleView = (TextView) findViewById(R.id.textViewTitle);
				descView = (TextView) findViewById(R.id.textViewDescription);
				latView = (TextView) findViewById(R.id.textViewLatitude);
				lonView = (TextView) findViewById(R.id.textViewLongitude);
				imageView = (TextView) findViewById(R.id.textViewImageLoading);
				imageCircle = (ProgressBar) findViewById(R.id.progressBarImageLoading);
				addressView = (TextView) findViewById(R.id.textViewAddress);
				circle = (ProgressBar) findViewById(R.id.progressBarAddress);

				latView.setText(activity.getResources().getString(
						R.string.text_Latitude)
						+ ": "
						+ Double.toString(marker.getPoint().getLatitudeE6() / 1000000f));
				lonView.setText(activity.getResources().getString(
						R.string.text_Longitude)
						+ ": "
						+ Double.toString(marker.getPoint().getLongitudeE6() / 1000000f));

				titleView.setVisibility(View.GONE);
				descView.setVisibility(View.GONE);
				latView.setVisibility(View.VISIBLE);
				lonView.setVisibility(View.VISIBLE);
				addressView.setVisibility(View.VISIBLE);
				circle.setVisibility(View.GONE);
				imageView.setVisibility(View.GONE);
				imageCircle.setVisibility(View.GONE);

				new GetAddressTask().execute();
				break;
			case MapViewActivity.MARKER_UPLOAD:
				titleView = (TextView) findViewById(R.id.textViewTitle);
				descView = (TextView) findViewById(R.id.textViewDescription);
				imageView = (TextView) findViewById(R.id.textViewImageLoading);
				imageCircle = (ProgressBar) findViewById(R.id.progressBarImageLoading);
				latView = (TextView) findViewById(R.id.textViewLatitude);
				lonView = (TextView) findViewById(R.id.textViewLongitude);
				addressView = (TextView) findViewById(R.id.textViewAddress);
				circle = (ProgressBar) findViewById(R.id.progressBarAddress);

				titleView.setText(activity.getResources().getString(
						R.string.text_ObservationDateTime)
						+ ": " + marker.getObservationTime());
				descView.setText(activity.getResources().getString(
						R.string.text_Description)
						+ ": " + marker.getUserComment());
				latView.setText(activity.getResources().getString(
						R.string.text_Latitude)
						+ ": " + Double.toString(marker.getLatitude()));
				lonView.setText(activity.getResources().getString(
						R.string.text_Longitude)
						+ ": " + Double.toString(marker.getLongitude()));

				titleView.setVisibility(View.VISIBLE);
				descView.setVisibility(View.VISIBLE);
				latView.setVisibility(View.VISIBLE);
				lonView.setVisibility(View.VISIBLE);
				addressView.setVisibility(View.VISIBLE);
				circle.setVisibility(View.VISIBLE);
				imageView.setVisibility(View.VISIBLE);
				imageCircle.setVisibility(View.VISIBLE);
				new GetAddressTask().execute();
				new DownloadImageTask().execute();
				break;
			default:
				break;
			}
		}
		Button buttonCancel = (Button) findViewById(R.id.markerCancelButton);
		buttonCancel.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		Button buttonUpload = (Button) findViewById(R.id.markerSubmitButton);
		buttonUpload.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(activity, UploadFormActivity.class);
				intent.putExtra("latitude",
						marker.getPoint().getLatitudeE6() / 1000000f);
				intent.putExtra("longitude",
						marker.getPoint().getLongitudeE6() / 1000000f);
				startActivityForResult(intent, MapViewActivity.UPLOAD_INTENT);
			}
		});
		if (upload) {
			buttonUpload.setVisibility(View.VISIBLE);
		} else {
			buttonUpload.setVisibility(View.GONE);
		}

		setResult(RESULT_CANCELED);
	}

	@Override
	protected void onStart() {
		super.onStart();
		// The activity is about to become visible.
	}

	@Override
	protected void onResume() {
		super.onResume();
		// The activity has become visible (it is now "resumed").
	}

	@Override
	protected void onPause() {
		super.onPause();
		// Another activity is taking focus (this activity is about to be
		// "paused").
	}

	@Override
	protected void onStop() {
		super.onStop();
		// The activity is no longer visible (it is now "stopped")
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		// The activity is about to be destroyed.
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// The activity is been brought back to the front.
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		// Save UI state changes to the savedInstanceState.
		// This bundle will be passed to onCreate if the process is
		// killed and restarted.
		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		// Restore UI state from the savedInstanceState.
		// This bundle has also been passed to onCreate.
	}

	private class GetAddressTask extends AsyncTask<Void, Void, Void> {
		protected boolean taskCompleted = false;
		private String address;

		@Override
		protected void onPreExecute() {
			((ProgressBar) findViewById(R.id.progressBarAddress))
					.setVisibility(View.VISIBLE);
		}

		@Override
		protected Void doInBackground(Void... params) {
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isConnected()) {
				Geocoder geocoder = new Geocoder(activity, Locale.getDefault());
				List<Address> addressList;
				try {
					addressList = geocoder.getFromLocation(
							marker.getLatitude(), marker.getLongitude(), 1);
					if (addressList.size() == 0) {
						return null;
					}
					StringBuffer sb = new StringBuffer(50);
					String[] items = new String[addressList.size()];
					for (int i = 0; i < addressList.size(); i++) {
						int addSize = addressList.get(i)
								.getMaxAddressLineIndex();
						for (int j = 0; j < addSize; j++) {
							sb.append(addressList.get(i).getAddressLine(j));
							if (j < addSize - 1)
								sb.append(", ");
						}
					}
					items[0] = sb.toString();
					address = items[0];
				} catch (IOException e) {
					e.printStackTrace();
				}
				taskCompleted = true;
			} else {
				address = "Could not be determined";
				activity.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(activity,
								"We could not contact the server.",
								Toast.LENGTH_LONG).show();
					}
				});
				taskCompleted = true;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void none) {
			if (taskCompleted) {
				((TextView) findViewById(R.id.textViewAddress))
						.setText(activity.getResources().getString(
								R.string.text_Address)
								+ ": " + address);
				((ProgressBar) findViewById(R.id.progressBarAddress))
						.setVisibility(View.GONE);

			}
		}
	}

	private class DownloadImageTask extends AsyncTask<Void, Void, Void> {
		protected boolean taskCompleted = false;
		protected boolean imageLoaded = false;
		private String message;

		@Override
		protected void onPreExecute() {
		}

		@Override
		protected Void doInBackground(Void... params) {
			ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
			if (marker.getImage().equals("")) {
				message = "No picture";
				return null;
			}
			if (networkInfo != null && networkInfo.isConnected()) {
				File image = Connector.downloadPicture(marker);
				if (image == null) {
					imageLoaded = false;
					message = "Image could not be downloaded";
				} else {
					imageLoaded = true;
					localImage = image.getAbsolutePath();
				}
				taskCompleted = true;
			} else {
				File image = new File(
						Environment.getExternalStorageDirectory(),
						Connector.PUBLIC_DIR + File.separator
								+ Connector.DOWNLOAD_DIR + File.separator
								+ marker.getRegionId() + File.separator
								+ marker.getEventId() + marker.getBoundaryId()
								+ File.separator + marker.getId() + ".jpg");
				activity.runOnUiThread(new Runnable() {
					public void run() {
						Toast.makeText(activity,
								"We could not contact the server.",
								Toast.LENGTH_LONG).show();
					}
				});
				if (!image.exists()) {
					imageLoaded = false;
					message = "No network connection";
				} else {
					localImage = image.getAbsolutePath();
					imageLoaded = true;

				}
				taskCompleted = true;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void none) {
			if (taskCompleted) {
				if (imageLoaded) {
					((TextView) findViewById(R.id.textViewImageLoading))
							.setVisibility(View.GONE);
					ImageView myImage = (ImageView) findViewById(R.id.imageViewPictureUpload);
					Bitmap myBitmap = BitmapFactory.decodeFile(localImage);

					WindowManager mWinMgr = (WindowManager) activity
							.getSystemService(Context.WINDOW_SERVICE);
					int displayWidth = mWinMgr.getDefaultDisplay().getWidth();
					int displayHeight = mWinMgr.getDefaultDisplay().getHeight();
					if (myBitmap.getWidth() > ((float) displayWidth * 0.90f)
							|| myBitmap.getHeight() > ((float) displayHeight * 0.90f)) {
						Log.i("MarkerDialogActivity", "Image is been processed");
						myImage.setImageBitmap(decodeSampledBitmapFromFile(
								localImage, (int) (displayWidth * 0.90)));
					} else {
						Log.i("MarkerDialogActivity",
								"Image is loaded without been processed");
						myImage.setImageBitmap(myBitmap);
					}

					myImage.setVisibility(View.VISIBLE);
				} else {
					((TextView) findViewById(R.id.textViewImageLoading))
							.setVisibility(View.VISIBLE);
				}
			}
			((TextView) findViewById(R.id.textViewImageLoading))
					.setText(activity.getResources().getString(
							R.string.text_Picture)
							+ ": " + message);
			((ProgressBar) findViewById(R.id.progressBarImageLoading))
					.setVisibility(View.GONE);
		}

		public Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth) {

			// First decode with inJustDecodeBounds=true to check dimensions
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			Bitmap sample = BitmapFactory.decodeFile(filePath);
			int width = sample.getWidth();
			int height = sample.getHeight();
			float ratio = (float) width / (float) height;
			// Calculate inSampleSize
			options.inSampleSize = calculateInSampleSize(sample.getWidth(),
					sample.getWidth(), reqWidth,
					(int) ((float) reqWidth / (float) ratio));

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			return BitmapFactory.decodeFile(filePath, options);
		}

		public int calculateInSampleSize(int realWidth, int realHeight,
				int reqWidth, int reqHeight) {
			// Raw height and width of image
			final int height = realHeight;
			final int width = realWidth;
			int inSampleSize = 2;

			if (height > reqHeight || width > reqWidth) {
				if (width > height) {
					inSampleSize = Math.round((float) height
							/ (float) reqHeight);
				} else {
					inSampleSize = Math.round((float) width / (float) reqWidth);
				}
			}
			return inSampleSize;
		}
	}

}
