package com.oanda.dependentseekbars.example;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ExampleListActivity extends ListActivity {

    private static final String[] EXAMPLES_LIST = {
            "Change Progress via EditText",
            "Task Scheduler" };
    private static final Class<?>[] EXAMPLE_ACTIVITIES = {
            EditFieldSampleActivity.class, TaskSchedulerActivity.class };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, EXAMPLES_LIST));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final Intent intent = new Intent(this, EXAMPLE_ACTIVITIES[position]);
        startActivity(intent);
    }

}
