package com.example.android.sulama;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    // Main variables
    public String sulama_mode = "";     // "True" or "False"
    public String timer_start = "";     // "Not set" or "D.M.Y - H:M"
    public String timer_finish = "";    // "Not set" or "D.M.Y - H:M"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase setup
        FirebaseApp.initializeApp(this);
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Database references
        final DatabaseReference sulama_mode_ref = database.getReference("sulama_mode");
        final DatabaseReference timer_start_ref = database.getReference("timer_start");
        final DatabaseReference timer_finish_ref = database.getReference("timer_finish");
        final DatabaseReference cpu_temp = database.getReference("temperature");
        final DatabaseReference shutdown = database.getReference("shutdown");

        // Visual objects
        final Button sulama_button = findViewById(R.id.sulama);
        final Button sulama_timer = findViewById(R.id.sulama_timer);
        final Button cancel_timer_button = findViewById(R.id.cancel_timer_button);
        final Button shutdown_button = findViewById(R.id.shutdown);
        final TextView duration_view = findViewById(R.id.duration_info);
        final TextView timer_info_view = findViewById(R.id.timer_info);
        final TextView heat = findViewById(R.id.cpu_heat);


        // Read initial values from the database and set the text of the green button.
        sulama_mode_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sulama_mode = dataSnapshot.getValue(String.class);
                if(sulama_mode.equals("False")) {
                    sulama_button.setText("Sulama Başlat");
                }else{
                    sulama_button.setText("Sulama Bitir");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("DATABASE_ERROR", "Failed to read value.", error.toException());
            }
        });

        // Read initial values from the database and set the text of timer_start_view.
        timer_start_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                timer_start = dataSnapshot.getValue(String.class);
                if(!timer_start.equals("Not set")) {
                    timer_info_view.setText("Zamanlayıcı Başlangıç:\n"+timer_start);
                }else{
                    timer_info_view.setText("");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("DATABASE_ERROR", "Failed to read value.", error.toException());
            }
        });

        // Read initial values from the database and set the text of timer_finish_view.
        timer_finish_ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                timer_finish = dataSnapshot.getValue(String.class);
                if(!timer_finish.equals("Not set")) {
                    duration_view.setText("Zamanlayıcı Bitiş:\n"+timer_finish);
                }else{
                    duration_view.setText("");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("DATABASE_ERROR", "Failed to read value.", error.toException());
            }
        });

        // Read initial values from the database and set the text of cpu temperature.
        cpu_temp.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                heat.setText("CPU Sıcaklık: " + dataSnapshot.getValue(String.class) + "°C");
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.w("DATABASE_ERROR", "Failed to read value.", error.toException());
            }
        });

        // Shutdown button OnClick
        shutdown_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                shutdown.setValue("True");
            }
        });

        // Sulama button OnClick
        sulama_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(sulama_mode.equals("False")){
                    sulama_mode = "True";
                    sulama_mode_ref.setValue("True");
                    sulama_button.setText("Sulama Bitir");
                }else{
                    sulama_mode = "False";
                    sulama_mode_ref.setValue("False");
                    sulama_button.setText("Sulama Başlat");
                }
            }
        });


        // Cancel button OnClick
        cancel_timer_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                timer_start = "Not set";
                timer_finish = "Not set";
                timer_start_ref.setValue(timer_start);
                timer_finish_ref.setValue(timer_finish);
            }
        });


        // Sulama Timer button OnClick
        sulama_timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                int hour = cldr.get(Calendar.HOUR_OF_DAY);
                int minutes = cldr.get(Calendar.MINUTE);

                // time picker dialog
                final TimePickerDialog picker2 = new TimePickerDialog(MainActivity.this,
                        R.style.TimePickerTheme,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker tp, int eHour, int eMinute) {
                                timer_finish += " - " + eHour + ":" + eMinute;
                                timer_start_ref.setValue(timer_start);
                                timer_finish_ref.setValue(timer_finish);
                            }
                        }, hour, minutes, true);

                final DatePickerDialog datePicker2 = new DatePickerDialog(MainActivity.this,
                        R.style.TimePickerTheme,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker dp, int eYear, int eMonth, int eDay) {
                                timer_finish = eDay + "." + eMonth + "." + eYear;
                                picker2.setTitle("Bitiş");
                                picker2.show();
                            }
                        }, cldr.get(Calendar.YEAR), cldr.get(Calendar.MONTH),
                        cldr.get(Calendar.DAY_OF_MONTH));

                // time picker dialog
                final TimePickerDialog picker = new TimePickerDialog(MainActivity.this,
                        R.style.TimePickerTheme,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker tp, int sHour, int sMinute) {
                                timer_start += " - " + sHour + ":" + sMinute;
                                datePicker2.setTitle("Bitiş");
                                datePicker2.show();
                            }
                        }, hour, minutes, true);

                final DatePickerDialog datePicker = new DatePickerDialog(MainActivity.this,
                        R.style.TimePickerTheme,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker dp, int sYear, int sMonth, int sDay) {
                                timer_start = sDay + "." + sMonth + "." + sYear;
                                picker.setTitle("Başlangıç");
                                picker.show();
                            }
                        }, cldr.get(Calendar.YEAR), cldr.get(Calendar.MONTH),
                        cldr.get(Calendar.DAY_OF_MONTH));

                datePicker.setTitle("Başlangıç");
                datePicker.show();
            }
        });
    }
}
