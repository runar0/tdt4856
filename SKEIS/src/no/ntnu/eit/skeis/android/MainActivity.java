package no.ntnu.eit.skeis.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(savedInstanceState != null && savedInstanceState.containsKey("alias")) {
			((TextView) findViewById(R.id.aliasText)).setText(savedInstanceState.getCharArray("alias").toString());
		}
		
		setContentView(R.layout.activity_main);

		final Button button = (Button) findViewById(R.id.start);
		final Context context = this;
		
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				TextView alias = (TextView) findViewById(R.id.aliasText);
				
				Intent intent = new Intent(context, BackgroundService.class);
				intent.putExtra("alias", alias.getText().toString());
				startService(intent);
				Toast.makeText(context, "Starting scanner", Toast.LENGTH_LONG).show();
			}
		});
		
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putCharSequence("alias", ((TextView) findViewById(R.id.aliasText)).getText());
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onRestoreInstanceState(savedInstanceState);

		if(savedInstanceState != null && savedInstanceState.containsKey("alias")) {
			((TextView) findViewById(R.id.aliasText)).setText(savedInstanceState.getCharArray("alias").toString());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
