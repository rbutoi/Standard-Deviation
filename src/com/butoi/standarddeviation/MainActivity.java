package com.butoi.standarddeviation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

import com.butoi.standarddeviation.MyArrayAdapter.DeleteButtonCallback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;

public class MainActivity extends Activity implements DeleteButtonCallback {
	
	private AlertDialog about;
	private EditText input;
	private ArrayAdapter<String> aa_results;
	private MyArrayAdapter<Double> aa_data;
	private List<Double> data;
	private SharedPreferences prefs;
	
	// Statistics
	private double mean, median, standardDeviation, variance, min, max;
	private List<Double> mode;
	private List<String> formattedOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mode = new ArrayList<Double>();
        formattedOut = new ArrayList<String>();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        
        // Build about dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name)
               .setMessage(R.string.author);
        about = builder.create();
        
        input = (EditText) findViewById(R.id.inputBox);
        
    	data = new ArrayList<Double>();
        aa_data = new MyArrayAdapter<Double>(this, R.layout.list_item, data);
        aa_data.setCallback(this);
        ListView lv_data = (ListView) findViewById(R.id.dataListView);
        lv_data.setAdapter(aa_data);
        
        aa_results = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, formattedOut);
        GridView gv_results = (GridView) findViewById(R.id.resultsGridView);
        gv_results.setAdapter(aa_results);
        
    	try {
    		JSONArray ja = new JSONArray(prefs.getString("data", "[]"));
    		for(int i = 0; i < ja.length(); i++)
        		data.add(ja.getDouble(i));
    		aa_data.notifyDataSetChanged();
    	} catch (JSONException e) {
    		e.printStackTrace();
    	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
        case R.id.action_clear:
        	aa_data.clear();
        	updateStats();
        	return true;
        case R.id.action_settings:
        	this.startActivity(new Intent(this, SettingsActivity.class));
        	return true;
        case R.id.action_about:
        	about.show();
        	return true;
        default:
        	return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	prefs.edit().putString("data", new JSONArray(data).toString()).apply();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	updateStats();
    }
    
    public void onAdd(View view) {
    	try {
    		aa_data.add(Double.valueOf(input.getText().toString()));
    		input.setText("");
    		updateStats();
    	} catch (NumberFormatException e) {}
    }
    
    protected void updateStats() {
    	formattedOut.clear();
    	
    	// Bail out if empty list
    	if(aa_data.isEmpty()) {
    		aa_results.notifyDataSetChanged();
    		return;
    	}
    	int sz = data.size();
    	
    	List<Double> sortedData = new ArrayList<Double>();
    	mode.clear();
    	min = max = data.get(0);
    	mean = 0;
    	int maxFreq = 1;
    	Map<Double, Integer> freqMap = new HashMap<Double, Integer>();
    	for(Double i : data) {
    		sortedData.add(Double.valueOf(i));
    		mean += i;
    		if(i > max)
    			max = i;
    		if(i < min)
    			min = i;
    		if(freqMap.containsKey(i))
    			freqMap.put(i, freqMap.get(i) + 1);
    		else
    			freqMap.put(i, 1);
    		maxFreq = Math.max(maxFreq, freqMap.get(i));
    	}
    	for(double i : freqMap.keySet())
    		if(freqMap.get(i) == maxFreq)
    			mode.add(i);
    	
    	mean /= sz;
    	
    	Collections.sort(sortedData);
    	if(sz % 2 == 0)
    		median = (sortedData.get(sz / 2) + sortedData.get(sz / 2 - 1)) / 2;
    	else
    		median = sortedData.get(sz / 2);
    	
    	if(prefs.getBoolean("pref_mean", true))
    		formattedOut.add(getResources().getString(R.string.pref_mean)
    				+ ": " + Math.round(mean * 100) / 100.0);
    	if(prefs.getBoolean("pref_median", true))
    		formattedOut.add(getResources().getString(R.string.pref_median)
    				+ ": " + Math.round(median * 100) / 100.0);
    	if(prefs.getBoolean("pref_mode", true)) {
    		StringBuilder sb = new StringBuilder();
    		sb.append(getResources().getString(R.string.pref_mode) + ": ");
    		for(int i = 0; i < mode.size(); i++)
    			if(i == mode.size() - 1)
    				sb.append(mode.get(i).toString());
    			else
    				sb.append(mode.get(i).toString() + ", ");
    		formattedOut.add(sb.toString());
    	}
    	
    	if(sz > 1) {
    		variance = 0;
    		for(double i : data)
    			variance += (mean - i) * (mean - i);
    		variance /= sz - 1;
    		standardDeviation = Math.sqrt(variance);
    		
        	if(prefs.getBoolean("pref_dev", true))
        		formattedOut.add(getResources().getString(R.string.s) + ": "
        				+ Math.round(standardDeviation * 100) / 100.0);
        	if(prefs.getBoolean("pref_var", true))
        		formattedOut.add(getResources().getString(R.string.pref_var)
        				+ ": " + Math.round(variance * 100) / 100.0);
    	}
    	
    	if(prefs.getBoolean("pref_range", true))
    		formattedOut.add(getResources().getString(R.string.pref_range)
    				+ ": " + min + " - " + max);
    	
    	aa_results.notifyDataSetChanged();
    }
    
    public void deletePressed(int position) {
    	data.remove(position);
    	aa_data.notifyDataSetChanged();
    	updateStats();
    }
}
