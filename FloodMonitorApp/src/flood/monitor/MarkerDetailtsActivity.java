package flood.monitor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MarkerDetailtsActivity  extends Activity {

	public String file;
	public Context context = this;
	static final int UPLOADING_DIALOG = 1;
	public ProgressDialog progressDialog;
	public ProgressThread progressThread;
	
	@Override 
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form);
        
        Button buttonLoadImage = (Button)findViewById(R.id.pictureButton);
        buttonLoadImage.setOnClickListener(new Button.OnClickListener(){
        	  @Override
        	  public void onClick(View arg0) {
        	   // TODO Auto-generated method stub
        	   Intent intent = new Intent(Intent.ACTION_PICK,
        	     android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        	   startActivityForResult(intent, 0);
        	  }});
        Button buttonUploadImage = (Button)findViewById(R.id.submitButton);
        buttonUploadImage.setOnClickListener(new Button.OnClickListener(){
        	  @Override
        	  public void onClick(View arg0) {
        		  showDialog(UPLOADING_DIALOG);
        	  }});      
        }
	
	public void onSaveInstanceState(Bundle savedInstanceState) {
		  // Save UI state changes to the savedInstanceState.
		  // This bundle will be passed to onCreate if the process is
		  // killed and restarted.
		  savedInstanceState.putBoolean("MyBoolean", true);
		  savedInstanceState.putDouble("myDouble", 1.9);
		  savedInstanceState.putInt("MyInt", 1);
		  savedInstanceState.putString("MyString", "Welcome back to Android");
		  // etc.
		  super.onSaveInstanceState(savedInstanceState);
		}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
	  super.onRestoreInstanceState(savedInstanceState);
	  // Restore UI state from the savedInstanceState.
	  // This bundle has also been passed to onCreate.
	  boolean myBoolean = savedInstanceState.getBoolean("MyBoolean");
	  double myDouble = savedInstanceState.getDouble("myDouble");
	  int myInt = savedInstanceState.getInt("MyInt");
	  String myString = savedInstanceState.getString("MyString");
	}

	
	protected Dialog onCreateDialog(int id) {
        switch(id) {
        case UPLOADING_DIALOG: {
            ProgressDialog progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Please wait while loading...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            return progressDialog; }
        default: {
            return null; }
        }
	}
	
	@Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch(id) {
        case UPLOADING_DIALOG: {
            progressThread = new ProgressThread(handler);
            progressThread.start();}
        default: {}
        }
    }

    // Define the Handler that receives messages from the thread and update the progress
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            int total = msg.arg1;
            if (total == 1){
                dismissDialog(UPLOADING_DIALOG);
                progressThread.setState(ProgressThread.STATE_DONE);
            }
        }
    };

    /** Nested class that performs progress calculations (counting) */
    private class ProgressThread extends Thread {
        Handler mHandler;
        final static int STATE_DONE = 0;
        final static int STATE_RUNNING = 1;
        int mState;
        int total;
       
        ProgressThread(Handler h) {
            mHandler = h;
        }
       
        public void run() {
            mState = STATE_RUNNING;   
            total = 0;
            UploadPicture(context, file);
            Message msg = mHandler.obtainMessage();
            msg.arg1 = 1;
            mHandler.sendMessage(msg);
        }
        
        /* sets the current state for the thread,
         * used to stop the thread */
        public void setState(int state) {
            mState = state;
        }
    }

	public void UploadPicture(Context context, String file){
    	HttpURLConnection connection = null;
		DataOutputStream outputStream = null;
		DataInputStream inputStream = null;

		String pathToOurFile = file;
		String urlServer = "http://buzz.acm.ndsu.nodak.edu/hosted/cramirez/floodmonitor/plog-mobupload.php";
		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary =  "*****";

		int bytesRead, bytesAvailable, bufferSize;
		byte[] buffer;
		int maxBufferSize = 1*1024*1024;
		try
		{
		FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );

		URL url = new URL(urlServer);
		connection = (HttpURLConnection) url.openConnection();

		// Allow Inputs & Outputs
		connection.setDoInput(true);
		connection.setDoOutput(true);
		connection.setUseCaches(false);

		// Enable POST method
		connection.setRequestMethod("POST");

		connection.setRequestProperty("Connection", "Keep-Alive");
		connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);

		outputStream = new DataOutputStream( connection.getOutputStream() );
		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
		outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + pathToOurFile +"\"" + lineEnd);
		outputStream.writeBytes(lineEnd);

		bytesAvailable = fileInputStream.available();
		bufferSize = Math.min(bytesAvailable, maxBufferSize);
		buffer = new byte[bufferSize];

		// Read file
		bytesRead = fileInputStream.read(buffer, 0, bufferSize);

		while (bytesRead > 0)
		{
		outputStream.write(buffer, 0, bufferSize);
		bytesAvailable = fileInputStream.available();
		bufferSize = Math.min(bytesAvailable, maxBufferSize);
		bytesRead = fileInputStream.read(buffer, 0, bufferSize);
		}

		outputStream.writeBytes(lineEnd);
		outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

		// Responses from the server (code and message)
		int serverResponseCode = connection.getResponseCode();
		String serverResponseMessage = connection.getResponseMessage();

		fileInputStream.close();
		outputStream.flush();
		outputStream.close();
		}
		catch (Exception ex)
		{
			//THIS SHOULD BE LOGGED
			//Toast.makeText(context, "Error Message: " + ex.getMessage(), 5000).show();
		}
		finally
		{

		}
    }
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	 // TODO Auto-generated method stub
	 super.onActivityResult(requestCode, resultCode, data);

	 if (resultCode == RESULT_OK){
	  Uri targetUri = data.getData();
	  file = getRealPathFromURI(targetUri);
	  TextView path = (TextView)findViewById(R.id.pathView);
	  path.setText("File: " + file);
	 }
	}
	
	public String getRealPathFromURI(Uri contentUri) {
		// can post image
		String [] proj={MediaStore.Images.Media.DATA};
		Cursor cursor = managedQuery( contentUri,
		proj, // Which columns to return
		null, // WHERE clause; which rows to return (all rows)
		null, // WHERE clause selection arguments (none)
		null); // Order-by clause (ascending by name)
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	} 
}