package com.mta.sadna;

import android.app.Activity;
import android.os.Bundle;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public abstract class AbstractMenuActivity extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_activity_layout);

		final ListView listViewMenu = (ListView) this.findViewById(R.id.list_view_menu_items);
		listViewMenu.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, getMenuItems()));
		listViewMenu.setOnItemClickListener(getMenuOnItemClickListener());
		
		initActivity();
	}

	protected abstract String[] getMenuItems();

	protected abstract OnItemClickListener getMenuOnItemClickListener();
	
	protected void initActivity()
	{
		//The extender activity can implement this method
	}
	
	public MainApplication getApplicationContext()
	{
		return (MainApplication) super.getApplicationContext();
	}

}
