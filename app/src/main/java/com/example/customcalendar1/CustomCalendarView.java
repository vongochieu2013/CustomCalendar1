package com.example.customcalendar1;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.EventLog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CustomCalendarView extends LinearLayout {

  ImageButton nextButton, previousButton;
  TextView CurrentDate;
  GridView gridView;
  private static final int MAX_CALENDAR_DAYS = 42;
  Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
  Context context;
  SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
  SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", Locale.ENGLISH);
  SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.ENGLISH);
  SimpleDateFormat eventDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

  MyGridAdapter myGridAdapter;

  AlertDialog alertDialog;
  List<Date> dates = new ArrayList();
  List<Events> eventsList = new ArrayList();


  public CustomCalendarView(Context context) {
    super(context);
  }

  public CustomCalendarView(final Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    this.context = context;
    InitializeLayout();
    SetUpCalendar();

    previousButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        calendar.add(Calendar.MONTH, -1);
        SetUpCalendar();
      }
    });

    nextButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        calendar.add(Calendar.MONTH, 1);
        SetUpCalendar();
      }
    });

    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        final View addView = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_newevent_layout, null);
        final EditText EventName = addView.findViewById(R.id.event_name);
        final TextView EventTime = addView.findViewById(R.id.eventtime);
        ImageButton SetTime = addView.findViewById(R.id.set_event_time);
        final Button addEvent = addView.findViewById(R.id.add_event);
        SetTime.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            Calendar calendar = Calendar.getInstance();
            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(addView.getContext(), R.style.Theme_AppCompat_Dialog, new TimePickerDialog.OnTimeSetListener() {
              @Override
              public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                Calendar c = Calendar.getInstance();
                c.set(Calendar.HOUR_OF_DAY, hourOfDay);
                c.set(Calendar.MINUTE, minute);
                c.setTimeZone(TimeZone.getDefault());
                SimpleDateFormat hformate = new SimpleDateFormat("K:mm a", Locale.ENGLISH);
                String event_Time = hformate.format(c.getTime());
                EventTime.setText(event_Time);
              }
            }, hours, minutes, false);
            timePickerDialog.show();
          }
        });
        final String date = eventDateFormat.format(dates.get(position));
        final String month = monthFormat.format(dates.get(position));
        final String year = yearFormat.format(dates.get(position));

        addEvent.setOnClickListener(new OnClickListener() {
          @Override
          public void onClick(View v) {
            SaveEvent(EventName.getText().toString(), EventTime.getText().toString(), date, month, year);
            SetUpCalendar();
            alertDialog.dismiss();
          }
        });

        builder.setView(addView);
        alertDialog = builder.create();
        alertDialog.show();
      }
    });

    gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
      @Override
      public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        String date = eventDateFormat.format(dates.get(position));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        View showView = LayoutInflater.from(parent.getContext()).inflate(R.layout.show_events_layout, null);
        RecyclerView recyclerView = showView.findViewById(R.id.EventsRV);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(showView.getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        EventRecyclerAdapter eventRecyclerAdapter = new EventRecyclerAdapter(showView.getContext(), CollectEventByDate(date));
        recyclerView.setAdapter(eventRecyclerAdapter);
        eventRecyclerAdapter.notifyDataSetChanged();

        builder.setView(showView);
        alertDialog = builder.create();
        alertDialog.show();
        alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog) {
            SetUpCalendar();
          }
        });
        return true;
      }
    });

  }

  private ArrayList<Events> CollectEventByDate(String date) {
    ArrayList<Events> arrayList = new ArrayList<>();
    DBOpenHelper dbOpenHelper = new DBOpenHelper(context);
    SQLiteDatabase database = dbOpenHelper.getReadableDatabase();
    Cursor cursor = dbOpenHelper.ReadEvents(date, database);
    while (cursor.moveToNext()) {
      String event = cursor.getString(cursor.getColumnIndex(DBStructure.EVENT));
      String time = cursor.getString(cursor.getColumnIndex(DBStructure.TIME));
      String Date = cursor.getString(cursor.getColumnIndex(DBStructure.DATE));
      String month = cursor.getString(cursor.getColumnIndex(DBStructure.MONTH));
      String Year = cursor.getString(cursor.getColumnIndex(DBStructure.YEAR));
      Events events = new Events(event, time, Date, month, Year);
      arrayList.add(events);
    }
    cursor.close();
    dbOpenHelper.close();

    return arrayList;
  }

  public CustomCalendarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

  }

  private void SaveEvent(String event, String time, String date, String month, String year) {

    DBOpenHelper dbOpenHelper = new DBOpenHelper(context);
    SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
    dbOpenHelper.SaveEvent(event, time, date, month, year, database);
    dbOpenHelper.close();
    Toast.makeText(context, "Event Saved", Toast.LENGTH_SHORT).show();
    
  }

  private void InitializeLayout() {
    LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view = inflater.inflate(R.layout.calendar_layout, this);
    nextButton = view.findViewById(R.id.nextBtn);
    previousButton = view.findViewById(R.id.previousBtn);
    CurrentDate = view.findViewById(R.id.current_date);
    gridView = view.findViewById(R.id.gridView);
  }

  private void SetUpCalendar() {
    String currentDate = dateFormat.format(calendar.getTime());
    CurrentDate.setText(currentDate);
    dates.clear();
    Calendar monthCalendar = (Calendar)calendar.clone();
    monthCalendar.set(Calendar.DAY_OF_MONTH, 1);
    int firstDayOfMonth = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1;
    monthCalendar.add(Calendar.DAY_OF_MONTH, -firstDayOfMonth);
    CollectEventsPerMonth(monthFormat.format(calendar.getTime()), yearFormat.format(calendar.getTime()));

    while (dates.size() < MAX_CALENDAR_DAYS) {
      dates.add(monthCalendar.getTime());
      monthCalendar.add(Calendar.DAY_OF_MONTH, 1);
    }

    myGridAdapter = new MyGridAdapter(context, dates, calendar, eventsList);
    gridView.setAdapter(myGridAdapter);
  }

  private void CollectEventsPerMonth(String Month, String year) {
    eventsList.clear();
    DBOpenHelper dbOpenHelper = new DBOpenHelper(context);
    SQLiteDatabase database = dbOpenHelper.getReadableDatabase();
    Cursor cursor = dbOpenHelper.ReadEventsperMonth(Month, year, database);
    while (cursor.moveToNext()) {
      String event = cursor.getString(cursor.getColumnIndex(DBStructure.EVENT));
      String time = cursor.getString(cursor.getColumnIndex(DBStructure.TIME));
      String date = cursor.getString(cursor.getColumnIndex(DBStructure.DATE));
      String month = cursor.getString(cursor.getColumnIndex(DBStructure.MONTH));
      String Year = cursor.getString(cursor.getColumnIndex(DBStructure.YEAR));
      Events events = new Events(event, time, date, month, Year);
      eventsList.add(events);
    }
    cursor.close();
    dbOpenHelper.close();
  }
}
