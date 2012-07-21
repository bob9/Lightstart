package my.light;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class LightstartActivity extends Activity {
	/** Called when the activity is first created. */

	public static Vibrator vibrate;
	public Lights Lights;

	protected InitTask _initTask;

	public static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	private void updateStatus(Activity activity, Lights newLights) {
		LightstartActivity.lock.writeLock().lock();
		try {
			this.Lights = newLights;
		} finally {
			LightstartActivity.lock.writeLock().unlock();
		}

		LightstartActivity.lock.readLock().lock();
		try {
			if (Lights != null && Lights.Data != null
					&& Lights.Data.Switches != null) {

				// Update Temperature
				DecimalFormat frmt = new DecimalFormat("0.000 Celsius");

				TextView tv = (TextView) activity
						.findViewById(R.id.textViewTemperature);
				//tv.setText(frmt.format(Lights.Data.Temp));

				for (Switch s : Lights.Data.Switches) {

					// setUpStatus(R.id.Button01On, R.id.Button01Off, 2, s,
					// activity);
					// setUpStatus(R.id.Button03On, R.id.Button03Off, 3, s,
					// activity);
					// setUpStatus(R.id.Button02On, R.id.Button02Off, 4, s,
					// activity);
					setUpStatus(R.id.Button04On, R.id.Button04Off, 8, s,
							activity);
					// setUpStatus(R.id.Button05On, R.id.Button05Off, 5, s,
					// activity);
					setUpStatus(R.id.Button06On, R.id.Button06Off, 6, s,
							activity);
					setUpStatus(R.id.Button07On, R.id.Button07Off, 7, s,
							activity);
					// setUpStatus(R.id.Button09On, R.id.Button09Off, 49, s,
					// activity);

				}
			}
		} finally {
			LightstartActivity.lock.readLock().unlock();
		}

	}

	private static void setUpStatus(int button01on, int button01off, int i,
			Switch s, Activity activity) {

		if (s.P == i) {

			Button btnOn = (Button) activity.findViewById(button01on);
			Button btnOff = (Button) activity.findViewById(button01off);
			if (s.Value()) {
				btnOn.setBackgroundColor(Color.GREEN); // GREEN
				btnOff.setBackgroundColor(Color.WHITE);
			} else {
				btnOn.setBackgroundColor(Color.WHITE);
				btnOff.setBackgroundColor(Color.RED);

			}
		}

	}

	public void setupbutton(int onButtonId, int offButtonId, int pin) {
		Button btn = (Button) findViewById(onButtonId);
		click on = new click(pin, 1);
		btn.setOnClickListener(on);

		btn = (Button) findViewById(offButtonId);
		click off = new click(pin, 0);
		btn.setOnClickListener(off);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		vibrate = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		// setupbutton(R.id.Button01On, R.id.Button01Off, 2);
		// setupbutton(R.id.Button03On, R.id.Button03Off, 3);
		// setupbutton(R.id.Button02On, R.id.Button02Off, 4);
		setupbutton(R.id.Button04On, R.id.Button04Off, 8);
		// setupbutton(R.id.Button05On, R.id.Button05Off, 5);
		setupbutton(R.id.Button06On, R.id.Button06Off, 6);
		setupbutton(R.id.Button07On, R.id.Button07Off, 7);
		// setupbutton(R.id.Button09On, R.id.Button09Off, 49);

		Button btn = (Button) findViewById(R.id.buttonRefresh);
		btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// refresh();
				refresh();
			}
		});
		refresh();
	}

	public void refresh() {
		try {
			InitTask task = new InitTask(
					"http://brisbane.selfip.com/lights.html", "Refreshing");
			task.execute();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(Thread.currentThread().getStackTrace()[1]
					.getMethodName(), e.toString() + ". Trace: "
					+ e.getStackTrace());
		}
	}

	public class InitTask extends AsyncTask<Context, Integer, String> {
		private ProgressDialog progressDialog;

		private ProgressDialog pd;
		String url;
		String popupMessage;

		public Lights Lights;

		public InitTask(String url, String popupMessage) {
			this.url = url;
			this.popupMessage = popupMessage;
		}

		// -- run intensive processes here
		// -- notice that the datatype of the first param in the class
		// definition matches the param passed to this method
		// -- and that the datatype of the last param in the class definition
		// matches the return type of this mehtod
		@Override
		protected String doInBackground(Context... params) {
			BufferedReader in = null;
			StringBuffer page = new StringBuffer();
			try {
				DefaultHttpClient client = new DefaultHttpClient();
				
				HttpGet request = new HttpGet();

				String username = "admin";
				String password = "Pokker8000";

				UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
						username, password);
				request.addHeader(new BasicScheme()
						.authenticate(creds, request));

				request.setURI(new URI(url));
				HttpResponse response = client.execute(request);
				in = new BufferedReader(new InputStreamReader(response
						.getEntity().getContent()));
				String line = "";
				String NL = System.getProperty("line.separator");
				while ((line = in.readLine()) != null) {
					page.append(line + NL);
				}
				in.close();

				Gson gson = new GsonBuilder().create();
				this.Lights = gson.fromJson(page.toString(), Lights.class);
			} catch (Exception ex) {
				Log.e(Thread.currentThread().getStackTrace()[1]
						.getMethodName(), ex.toString() + ". Trace: "
						+ ex.getStackTrace() + " Page:" + page.toString());
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						Log.e(Thread.currentThread().getStackTrace()[1]
								.getMethodName(), e.toString() + ". Trace: "
								+ e.getStackTrace());
					}
				}
			}

			return "COMPLETE!";
		}

		// -- gets called just before thread begins
		@Override
		protected void onPreExecute() {

			try {
				Log.i("makemachine", "onPreExecute()");
				super.onPreExecute();
				progressDialog = ProgressDialog.show(LightstartActivity.this,
						"", popupMessage);
				progressDialog.show();
			} catch (Exception ex) {
				Log.e(Thread.currentThread().getStackTrace()[1]
						.getMethodName(), ex.toString() + ". Trace: "
						+ ex.getStackTrace());
			}

		}

		// -- called from the publish progress
		// -- notice that the datatype of the second param gets passed to this
		// method
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);

		}

		// -- called if the cancel button is pressed
		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		// -- called as soon as doInBackground method completes
		// -- notice that the third param gets passed to this method
		@Override
		protected void onPostExecute(String result) {
			try {
				super.onPostExecute(result);
				progressDialog.dismiss();
				updateStatus(LightstartActivity.this, this.Lights);
			} catch (Exception ex) {
				Log.e(Thread.currentThread().getStackTrace()[1]
						.getMethodName(), ex.toString() + ". Trace: "
						+ ex.getStackTrace());
			}

		}
	}

	public class click implements OnClickListener {
		int pin = 0;
		int OnOrOff = 0;

		public click(int pin, int OnOrOff) {
			this.pin = pin;
			this.OnOrOff = OnOrOff;
		}

		@Override
		public void onClick(View v) {
			try {
				LightstartActivity.vibrate.cancel();
				LightstartActivity.vibrate.vibrate(40);

				InitTask task = new InitTask(
						"http://brisbane.selfip.com/lights.html?pinD" + pin
								+ "=" + OnOrOff, "Flicking Light");
				task.execute();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				Log.e(Thread.currentThread().getStackTrace()[1]
						.getMethodName(), e.toString() + ". Trace: "
						+ e.getStackTrace());
			}

		}

		/**
		 * sub-class of AsyncTask
		 */

	}
}