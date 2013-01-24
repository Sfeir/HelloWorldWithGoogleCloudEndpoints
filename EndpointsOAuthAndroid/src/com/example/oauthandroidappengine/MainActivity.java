package com.example.oauthandroidappengine;

import java.io.IOException;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.helloworld.Helloworld;
import com.google.api.services.helloworld.model.HelloWorldEntity;

public class MainActivity extends Activity {

	static final String PREF_ACCOUNT_NAME = "accountName";
	static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
	static final int REQUEST_AUTHORIZATION = 1;
	static final int REQUEST_ACCOUNT_PICKER = 2;
	static final String PREF_AUTH_TOKEN = "authToken";

	final String TAG = "DemoEndPoints";
	SharedPreferences settings;
	GoogleAccountCredential credential;
	String accountName;

	boolean signedIn = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final String AUDIENCE = "server:client_id:447535189973.apps.googleusercontent.com";
		credential = GoogleAccountCredential.usingAudience(this, AUDIENCE);
		settings = getSharedPreferences(TAG, 0);
		setAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
		Account account = credential.getSelectedAccount();
		if (credential.getSelectedAccountName() != null) {
			
			onSignIn();
			System.out.println(credential.getScope());
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_AUTHORIZATION:
			if (resultCode == Activity.RESULT_OK) {
				onSignIn();
			} else {
				chooseAccount();
			}
			break;
		case REQUEST_ACCOUNT_PICKER:
			if (data != null && data.getExtras() != null) {
				String accountName = data.getExtras().getString(
						AccountManager.KEY_ACCOUNT_NAME);
				if (accountName != null) {
					setAccountName(accountName);
					SharedPreferences.Editor editor = settings.edit();
					editor.putString(PREF_ACCOUNT_NAME, accountName);
					editor.commit();
					onSignIn();
				}
			}
			break;
		}
	}

	private void onSignIn() {
		this.signedIn = true;
		setSignInEnablement(false);
		setAccountLabel(this.accountName);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void signIn(View v) {
		if (!this.signedIn) {
			chooseAccount();
		} else {
			forgetAccount();
			setSignInEnablement(true);
			setAccountLabel("(not signed in)");
		}
	}

	private void setSignInEnablement(boolean state) {
		Button button = (Button) findViewById(R.id.signin);
		if (state) {
			button.setText("Sign In");
		} else {
			button.setText("Sign Out");
		}
	}

	private void setAccountLabel(String label) {
		TextView userLabel = (TextView) findViewById(R.id.userLabel);
		userLabel.setText(label);
	}

	void chooseAccount() {
		startActivityForResult(credential.newChooseAccountIntent(),
				REQUEST_ACCOUNT_PICKER);
	}

	private void forgetAccount() {
		this.signedIn = false;
		SharedPreferences.Editor editor2 = settings.edit();
		editor2.remove(PREF_AUTH_TOKEN);
		editor2.commit();
	}

	public void onBtnClicked(View v) {
		if (v.getId() == R.id.button1) {
			Toast.makeText(this, "Click !", Toast.LENGTH_SHORT).show();

			if (credential.getSelectedAccountName() != null) {
				Toast.makeText(this, "Signin !", Toast.LENGTH_SHORT).show();
				new SendRequestTask(this).execute("Coucou");
			}
		}
	}

	private void setAccountName(String in_accountName) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(PREF_ACCOUNT_NAME, in_accountName);
		editor.commit();
		credential.setSelectedAccountName(in_accountName);
		accountName = in_accountName;
	}

	private class SendRequestTask extends AsyncTask<String, Void, Void> {
		private Activity activity;

		public SendRequestTask(Activity activity) {
			this.activity = activity;
		}

		protected Void doInBackground(String... input) {
			try {
				Helloworld.Builder builder = new Helloworld.Builder(
						AndroidHttp.newCompatibleTransport(),
						new GsonFactory(), credential);
				Helloworld service = builder.build();
				final HelloWorldEntity result = service.scores().get("coucou").execute();
				runOnUiThread(new Runnable() {
				    public void run() {
						Toast.makeText(activity, "result : " + result.getResult(), Toast.LENGTH_SHORT).show();
				    }
				});
				
				
			} catch (UserRecoverableAuthIOException re) {
				activity.startActivityForResult(re.getIntent(),
						REQUEST_AUTHORIZATION);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(Void unused) {
			Toast.makeText(activity, "Request Done", Toast.LENGTH_SHORT).show();
		}

	}

}
