package com.johns4ta.news_app;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class EditNews extends Activity {
	
	final String KEYWORD_SPLITTER = ",-;"; //Split keywords based on rare string occurence (to minimize chance of errors)
	final String WORDSKEY = "com.johns4ta.editnews.words";
	final String SPKEY = "com.johns4ta.editnews";

	SharedPreferences.Editor editPrefs;
	SharedPreferences prefs;
	Button btnSubmit;
	EditText etWords;
	String kwords;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.editnews);

		//Initalize UI and load any information from shared preferences
		initialize();		
		//Pull keywords from shared preferences
		getKeywords();

		final ListView listview = (ListView) findViewById(R.id.lvNewsItems);
		String[] keywords = kwords.split(KEYWORD_SPLITTER);

		//Add keywords to List Array
		final ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < keywords.length; ++i) {
			list.add(keywords[i]);
		}

		//Bind list array with list adapter to fill list object with key words
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list);

		//Set submit button on click listener, adds keywords to user's list of keywords
		btnSubmit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				kwords += etWords.getText().toString() + KEYWORD_SPLITTER;
				list.add(0, etWords.getText().toString());
				adapter.notifyDataSetChanged();
				etWords.setText("");
			}
		});
		listview.setAdapter(adapter);
		
		//Set onclick listener for list object, if item is clicked. open new article pertaining
		//to that list item.
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, final View view,
					int position, long id) {
				final String item = (String) parent.getItemAtPosition(position);
				String word = item + KEYWORD_SPLITTER;
				kwords = kwords.replaceFirst(word.toString(), "");
				view.animate().setDuration(1000).alpha(0)
						.withEndAction(new Runnable() {
							@Override
							public void run() {
								list.remove(item);
								adapter.notifyDataSetChanged();
								view.setAlpha(1);
							}
						});
			}

		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.editnews_actions, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_view:
			intent = new Intent(this, NewsFeed.class);
			startActivity(intent);
			finish();
			break;
		case R.id.action_exit:
			finish();
			break;
		case R.id.action_help:
			displayHelp();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		Intent i = new Intent(this, NewsFeed.class);
		startActivity(i);
		finish();
	}

	//Save shared preferenes when activity stops
	@Override
	protected void onStop() {
		editPrefs.putString(WORDSKEY, kwords);
		editPrefs.commit();
		super.onStop();
	}
	
	//Save shared preferenes when activity pauses
	@Override
	protected void onPause() {
		editPrefs.putString(WORDSKEY, kwords);
		editPrefs.commit();
		super.onPause();
	}

	//Retrieve keywords from shared preferences
	private String getKeywords() {
		kwords = prefs.getString(WORDSKEY, "");
		editPrefs.clear();
		return kwords;
	}

	//Initalize UI
	private void initialize() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
				| ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
		btnSubmit = (Button) findViewById(R.id.btnSubmitKeyword);
		etWords = (EditText) findViewById(R.id.etKeyword);
		prefs = this.getSharedPreferences(SPKEY, Context.MODE_PRIVATE);
		editPrefs = this.getSharedPreferences(SPKEY, Context.MODE_PRIVATE)
				.edit();

	}

	//Display help dialog box to user
	private void displayHelp() {
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
		dlgAlert.setMessage(R.string.edit_news_help);
		dlgAlert.setPositiveButton("OK", null);
		dlgAlert.setCancelable(true);
		dlgAlert.create().show();
	}
}
