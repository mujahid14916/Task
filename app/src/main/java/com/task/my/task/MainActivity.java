package com.task.my.task;

import android.content.Intent;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.task.my.task.database.DBOpenHelper;
import com.task.my.task.database.TableFields;
import com.task.my.task.fragment.DaysPagerAdapter;
import com.task.my.task.fragment.FragmentDays;
import com.task.my.task.fragment.FragmentYear;
import com.task.my.task.fragment.TextDialog;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements
        FragmentYear.yInterface, FragmentDays.DaysInterface, TextDialog.dialogInterface {

    public static List<Integer> yearList;

    public static String[] monthList;
    public static int year, month, day;
    private static Calendar cal;
    public static int startYear;

    //TODO: Create Constants
    private byte state;
    public static final int MONTH_STATE = 1;
    public static final int YEAR_STATE = 0;
    private TextView header;
    private FragmentManager manager;
    private List<FragmentDays> fragmentList;
    private TabLayout tabLayout;
    private ViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getWindow().setBackgroundDrawableResource(R.drawable.background);

        yearList = new ArrayList<>();
        monthList = new String[] {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        header = (TextView) findViewById(R.id.header);
        cal = Calendar.getInstance();
        startYear = (cal.get(Calendar.YEAR) / 20) * 20 + 1;
        state = MONTH_STATE;
        year = 0;

        if (savedInstanceState != null) {
            startYear = savedInstanceState.getInt("startYear");
            state = savedInstanceState.getByte("state");
            header.setText(savedInstanceState.getString("headerText"));
            year = savedInstanceState.getInt("year");
            findViewById(R.id.tabLayout).setVisibility(View.GONE);
        }

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        manager = getSupportFragmentManager();

        showFragment();

        Calendar cal = Calendar.getInstance();

        TextView cDate = (TextView) findViewById(R.id.currentDate);
        cDate.setText("Date: " + cal.get(Calendar.DATE) + "/" + (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.YEAR));


    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putByte("state", state);
        outState.putString("headerText", header.getText().toString());
        outState.putInt("startYear", startYear);
        outState.putInt("year", year);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_list) {


            Intent intent = new Intent(this, EventListActivity.class);
            startActivity(intent);


            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void showFragment() {

        if (state == MONTH_STATE) {

            if (year == 0) {
                year = cal.get(Calendar.YEAR);
            }
            month = cal.get(Calendar.MONTH) + 1;
            day = cal.get(Calendar.DATE);

            header.setText(String.valueOf(year));

            addPager();

        }
    }

    private void initializeList() {

        fragmentList = new ArrayList<>(12);

        Bundle b;
        FragmentDays fragment;

        for (int i = 0; i < 12; i++) {

            b = new Bundle(1);
            fragment = new FragmentDays();
            b.putInt(FragmentDays.MONTH_KEY, i + 1);
            b.putInt(FragmentDays.YEAR_KEY, year);
            fragment.setArguments(b);
            fragmentList.add(fragment);

        }

    }

    private void addPager() {

        initializeList();

        DaysPagerAdapter adapter = new DaysPagerAdapter(getSupportFragmentManager(), fragmentList);

        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);

        viewPager.setCurrentItem(month - 1);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //Required to query events
                month = position + 1;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayout.setVisibility(View.VISIBLE);


        //Adding tab styles with one of three state - win, loss, neutral
        LayoutInflater inflater = getLayoutInflater();
        TextView tv;

        DBOpenHelper helper = new DBOpenHelper(this);
        SQLiteDatabase db = helper.getReadableDatabase();
        int win, rows;

        for (int i = 0; i < 12; i++) {

            win = (int) DatabaseUtils.queryNumEntries(db,
                    TableFields.DateEntry.TABLE_NAME, TableFields.DateEntry.DAYS_DATE + " LIKE ? AND " +
                            TableFields.DateEntry.DAYS_WIN + " = " + TableFields.DateEntry.CHECKED,
                    new String[] {year + "-" + (i+1) + "%"});

            rows = (int) DatabaseUtils.queryNumEntries(db,
                    TableFields.DateEntry.TABLE_NAME, TableFields.DateEntry.DAYS_DATE + " LIKE ?",
                    new String[]{year + "-" + (i+1) + "%"});

            tv = (TextView) inflater.inflate(R.layout.month_tab, null);

            tv.setText(monthList[i]);

            if (win >= (rows/2) + 1) {
                tv.setTextColor(Color.GREEN);
            } else if (rows > 0) {
                tv.setTextColor(Color.RED);
            }

            tabLayout.getTabAt(i).setCustomView(tv);

        }

    }


    private void removePager() {


        tabLayout.removeAllTabs();

        viewPager.removeAllViewsInLayout();

        tabLayout.setVisibility(GONE);


    }


    @Override
    public void yearDone() {


        header.setText(String.valueOf(year));

        if (manager.findFragmentById(R.id.fragment_container) != null) {
            manager
                    .beginTransaction()
                    .remove(manager.findFragmentById(R.id.fragment_container))
                    .commit();
        }

        findViewById(R.id.fragment_container).setVisibility(GONE);

        addPager();

        state = MONTH_STATE;

    }


    @Override
    public void daysDone() {

        TextDialog dialog = new TextDialog();
        dialog.setCancelable(false);
        dialog.show(getSupportFragmentManager(), null);


    }

    public void goLeft(View view) {


        //TODO: Check for transition
        Fragment oldFragment, newFragment;
        if (state == YEAR_STATE) {
            startYear = yearList.get(0) - yearList.size();
            oldFragment = manager.findFragmentById(R.id.fragment_container);
            newFragment = new FragmentYear();

            manager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.custom_slide_in_right, R.anim.custom_slide_out_right)
                    .remove(oldFragment)
                    .add(R.id.fragment_container, newFragment)
                    .commit();

        } else {

            year = year - 1;

            header.setText(String.valueOf(year));

            viewPager.removeAllViewsInLayout();
            addPager();

        }
    }

    public void goRight(View view) {

        Fragment oldFragment, newFragment;
        if (state == YEAR_STATE) {
            startYear = yearList.get(yearList.size() - 1) + 1;
            oldFragment = manager.findFragmentById(R.id.fragment_container);
            newFragment = new FragmentYear();


            manager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.custom_slide_in_left, R.anim.custom_slide_out_left)
                    .remove(oldFragment)
                    .add(R.id.fragment_container, newFragment)
                    .commit();
        } else {
            year = year + 1;

            header.setText(String.valueOf(year));

            viewPager.removeAllViewsInLayout();
            addPager();

        }


    }

    public void goBack(View view) {


        if (state == YEAR_STATE) {

            int currentYear = (cal.get(Calendar.YEAR) / 20) * 20 + 1;

            if (startYear > currentYear) {

                startYear = currentYear;
                manager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.custom_slide_in_right, R.anim.custom_slide_out_right)
                        .remove(manager.findFragmentById(R.id.fragment_container))
                        .add(R.id.fragment_container, new FragmentYear())
                        .commit();
            } else if (startYear < currentYear) {
                startYear = currentYear;
                manager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.custom_slide_in_left, R.anim.custom_slide_out_left)
                        .remove(manager.findFragmentById(R.id.fragment_container))
                        .add(R.id.fragment_container, new FragmentYear())
                        .commit();
            } else {
                //TODO: Custom Toast
                Toast.makeText(this, "Not Reactive", Toast.LENGTH_SHORT).show();
            }

        } else {


            removePager();

            manager
                    .beginTransaction()
                    .setCustomAnimations(R.anim.custom_entry_zoom_out, 0)
                    .add(R.id.fragment_container, new FragmentYear())
                    .commit();

            findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);

            state = YEAR_STATE;

            header.setText(R.string.year_text);
        }

    }

    @Override
    public void restartPager() {

        removePager();
        addPager();


    }
}
