package com.example.customcalendar1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import org.naishadhparmar.zcustomcalendar.CustomCalendar;

public class MainActivity extends AppCompatActivity {

  CustomCalendarView customCalendarView;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    customCalendarView = (CustomCalendarView)findViewById(R.id.custom_calendar_view);
  }
}