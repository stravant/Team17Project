package com.ualberta.team17.view;

import android.app.Activity;
<<<<<<< HEAD
import android.content.Intent;
=======
>>>>>>> 971b134baeafd827bc4eef6b87ce0e2afce7e6a9
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ualberta.team17.R;
import com.ualberta.team17.controller.QAController;
<<<<<<< HEAD
import com.ualberta.team17.datamanager.UserContext;
=======
>>>>>>> 971b134baeafd827bc4eef6b87ce0e2afce7e6a9

public class LoginActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		
		TextView tv = (TextView) findViewById(R.id.loginWarning);
		if (tv != null) {
			tv.setVisibility(View.INVISIBLE);
		}
		
		Button loginButton = (Button) findViewById(R.id.signInButton);
		if (loginButton != null) {
			loginButton.setOnClickListener( new OnClickListener() {

	            @Override
	            public void onClick(View v) {
	                LoginActivity.this.login();
	            }
	        });
		}
	}
	
	private void login() {
		String username;
		
		EditText usernameET = (EditText) findViewById(R.id.usernameText);
		if (usernameET != null) {
			username = usernameET.getText().toString();
			
<<<<<<< HEAD
			if (username.length() >= 4 && username.length() <= 20 && !username.contains(" ")) {
				// Create the user context.
				QAController.getInstance().login(new UserContext(username));
				Intent intent = new Intent(LoginActivity.this, QuestionTaxonomyActivity.class);
				startActivity(intent);
=======
			if (username.length() >= 6 && username.length() <= 20 && !username.contains(" ")) {
				// Create the user context.
				//QAController.getInstance().
>>>>>>> 971b134baeafd827bc4eef6b87ce0e2afce7e6a9
			}
			else {
				TextView tv = (TextView) findViewById(R.id.loginWarning);
				if (tv != null) {
					tv.setVisibility(View.VISIBLE);
				}
			}
		}
	}
}
