package com.johns4ta.news_app;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class NewsFeed extends ListActivity {

	static final String KEY_ITEM = "item"; // parent node
	static final String KEY_TITLE = "title";
	static final String KEY_SOURCE = "source";
	static final String KEY_LINK = "link";
	static final String KEY_DATE = "pubDate";
	final String WORDSKEY = "com.johns4ta.editnews.words";
	final String SPKEY = "com.johns4ta.editnews";
	final String KEYWORD_SPLITTER = ",-;";

	String[] parts = new String[2];
	ArrayList<Article> articles = new ArrayList<Article>();
	int count = 0, length;
	SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		if (networkAvailable()) {
			//Initalize UI and load shared preferences data
			initialize();
			String kwords = prefs.getString(WORDSKEY, null);

			if (kwords != null && kwords != "") {
				String[] queries = kwords.split(KEYWORD_SPLITTER);
				length = queries.length; //Get number of keywords
				
				// Execute xml news requests in parallel
				for (String q : queries) {
					new GetData().executeOnExecutor(
							AsyncTask.THREAD_POOL_EXECUTOR, q);
				}
				ListView lv = getListView();
				
				//If item in list is clicked, load webview activity with url passed as intent
				lv.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Intent i = new Intent(getBaseContext(),
								WebViewActivity.class);
						i.putExtra("URL", articles.get(position).getLink());
						startActivity(i);
						finish();
					}
				});
			} else {
				String value[] = { getString(R.string.no_keywords) };
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
						android.R.layout.simple_list_item_1, value);
				setListAdapter(adapter);
			}
		} else {
			//Let user know there are no networks available
			alertNoNetwork();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.newsfeed_actions, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = null;
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.action_change:
			intent = new Intent(this, EditNews.class);
			break;
		case R.id.action_refresh:
			intent = new Intent(this, NewsFeed.class);
			break;
		case R.id.action_exit:
			finish();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		startActivity(intent);
		finish();
		return true;
	}

	private class GetData extends AsyncTask<String, Void, String> {
		protected String doInBackground(String... urls) {
			String url = "http://news.google.com/news?q=" + urls[0]
					+ "&scoring=d&output=rss";
			url = url.replace(" ", "%20"); // URI encoding for spaces is %20
			XMLParser parser = new XMLParser();
			String xml = parser.getXmlFromUrl(url); // getting XML
			return xml;
		}

		protected void onPostExecute(String xml) {
			ListView lv = getListView();
			lv.setAdapter(null); // Clear contents of list for each query
			Date date;
			XMLParser parser = new XMLParser();
			Document doc = parser.getDomElement(xml); // getting DOM element

			NodeList nl = doc.getElementsByTagName(KEY_ITEM);

			// looping through all item nodes <item>
			for (int i = 0; i < nl.getLength(); i++) {
				Article art = new Article();
				Element e = (Element) nl.item(i);
				parts = parseTitleSource(parser.getValue(e, KEY_TITLE), parts);
				try {
					// Ex. Format: Tue, 24 Dec 2013 20:40:45 GMT
					SimpleDateFormat format = new SimpleDateFormat(
							"EEE, dd MMM yyyy hh:mm:ss zzz");
					date = format.parse(parser.getValue(e, KEY_DATE));
					art.setDateTime(date);

				} catch (ParseException pe) {
					throw new IllegalArgumentException();
				}

				art.setTitle(parts[0]);
				art.setSource(parts[1]);
				art.setLink(parser.getValue(e, KEY_LINK));
				articles.add(art);

			}
			count++;

			if (count >= length) { // All async tasks finished
				count = 0; // reset count incase another refresh is initiated

				Collections.sort(articles); // Sort articles by date

				// Remove possible duplicate articles based off of title
				Map<String, Article> m = new LinkedHashMap<String, Article>();
				for (Article a : articles) {
					m.put(a.getTitle(), a);
				}
				articles.clear();
				articles.addAll(m.values());

				ArrayList<HashMap<String, String>> menuItems = new ArrayList<HashMap<String, String>>();
				// looping through all item nodes <item>
				for (Article a : articles) {
					HashMap<String, String> map = new HashMap<String, String>();
					map.put(KEY_TITLE, a.getTitle());
					map.put(KEY_SOURCE, a.getSource());
					map.put(KEY_DATE, a.getDateTime().toString());

					// adding HashList to ArrayList
					menuItems.add(map);

					// Adding menuItems to ListView
					ListAdapter adapter = new SimpleAdapter(
							getApplicationContext(), menuItems,
							R.layout.list_item, new String[] { KEY_TITLE,
									KEY_DATE, KEY_SOURCE }, new int[] {
									R.id.name, R.id.date, R.id.source });
					setListAdapter(adapter);
				}
			}
		}
	}

	private String[] parseTitleSource(String data, String[] parts) {
		int x;
		String[] mparts = data.split("-");
		// If more than two hypens in string, concatenate parts of string that
		// aren't the source author.
		if (mparts.length > 2) {
			for (x = 0; x < mparts.length - 1; x++) {
				parts[0] = parts[0] + mparts[x];
			}
			parts[1] = mparts[x]; // Name is last part separated from hypen
			// Else, only one hyphen in title
		} else {
			parts[0] = mparts[0].trim();
			parts[1] = mparts[1].trim();
		}
		return parts;
	}

	// Check to make sure data connection is available
	private boolean networkAvailable() {
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager
				.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	//Initalize UI and shared preferences
	private void initialize() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
				| ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
		prefs = this.getSharedPreferences(SPKEY, Context.MODE_PRIVATE);
		Toast.makeText(getApplicationContext(), R.string.updating_list,
				Toast.LENGTH_SHORT).show();

	}

	// Notify user there is no network connection
	private void alertNoNetwork() {
		AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
		dlgAlert.setMessage(R.string.no_network_connection);
		dlgAlert.setPositiveButton("OK", null);
		dlgAlert.setCancelable(true);
		dlgAlert.create().show();
	}
}
