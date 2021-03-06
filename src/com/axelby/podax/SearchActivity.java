package com.axelby.podax;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ResourceCursorTreeAdapter;
import android.widget.TextView;

public class SearchActivity extends Activity {
	ExpandableListView epView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);

	    setContentView(R.layout.search);

	    epView = (ExpandableListView) findViewById(R.id.results);
	    
	    MatrixCursor groupCursor = new MatrixCursor(new String[] {
	    		"_id", "title"
	    }, 2);
	    String[] groups = getResources().getStringArray(R.array.search_groups);
	    for (int i = 0; i < groups.length; ++i)
	    	groupCursor.addRow(new Object[] { i, groups[i] });
	    
	    // Get the intent, verify the action and get the query
	    Intent intent = getIntent();
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	      String query = intent.getStringExtra(SearchManager.QUERY);
	      SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
	    		  SearchSuggestionProvider.AUTHORITY, SearchSuggestionProvider.MODE);
	      suggestions.saveRecentQuery(query, null);
		  epView.setAdapter(new SearchResultsAdapter(this, groupCursor, query));
	    }
	    else
		    epView.setAdapter(new SearchResultsAdapter(this, groupCursor, ""));
	    epView.expandGroup(0);
	    epView.expandGroup(1);
	}	

    public class SearchResultsAdapter extends ResourceCursorTreeAdapter {
    	private Context _context;
    	private String _query;

    	public SearchResultsAdapter(Context context, Cursor cursor, String query)
    	{
    		super(context, cursor, R.layout.list_item, R.layout.list_item);
    		_context = context;
    		_query = query;
    	}

		@Override
		protected void bindChildView(View view, Context context, Cursor cursor,
				boolean isLastChild) {
			TextView textView = (TextView) view;
			// assumes podcast and subscription provider have the same column name for title
			int titleColumn = cursor.getColumnIndexOrThrow(SubscriptionProvider.COLUMN_TITLE);
			String title = cursor.getString(titleColumn);
			textView.setText(title);
		}

		@Override
		protected void bindGroupView(View view, Context context, Cursor cursor,
				boolean isExpanded) {
			TextView textView = (TextView) view;
			view.setPadding(60, 0, 0, 0);
			textView.setText(cursor.getString(1));
		}

		@Override
		protected Cursor getChildrenCursor(Cursor groupCursor) {
			String groupTitle = groupCursor.getString(1);
			if (groupTitle.equals("Subscriptions")) {
				Uri searchUri = Uri.withAppendedPath(SubscriptionProvider.URI, "search");
				String[] projection = {
						SubscriptionProvider.COLUMN_ID,
						SubscriptionProvider.COLUMN_TITLE,
				};
				return _context.getContentResolver().query(searchUri, projection,
						null, new String[] { _query },
						SubscriptionProvider.COLUMN_TITLE);
			} else if (groupTitle.equals("Podcasts")) {
				Uri searchUri = Uri.withAppendedPath(PodcastProvider.URI, "search");
				String[] projection = {
						PodcastProvider.COLUMN_ID,
						PodcastProvider.COLUMN_TITLE,
				};
				return _context.getContentResolver().query(searchUri, projection,
						null, new String[] { _query },
						PodcastProvider.COLUMN_PUB_DATE + " DESC");
			} else
				throw new IllegalArgumentException("Invalid search group");
		}
	}
}