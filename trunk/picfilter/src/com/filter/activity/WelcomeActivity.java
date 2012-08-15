package com.filter.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.filter.R;

public class WelcomeActivity extends Activity {
	private DataLoaderTask dataLoader;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.startup);
		
		dataLoader = new DataLoaderTask();
		dataLoader.execute();
		
	}
	class DataLoaderTask extends AsyncTask<Void, Long, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Integer result) {
			Intent intent = new Intent();
			intent.setClass(WelcomeActivity.this, PictureTakeActivity.class);
			startActivity(intent);
			finish();
			dataLoader = null;
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}
		
	}
}

