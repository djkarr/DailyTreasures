package xyz.danielkarr.dailytreasures;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import net.danlew.android.joda.JodaTimeAndroid;

import java.io.IOException;
import java.io.InputStream;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        JodaTimeAndroid.init(this);

//        try {
//            String assets = getAssets().list("").toString();
//            Log.i("HOME", "onCreate: " + assets);
//            InputStream in = getApplicationContext().getAssets().open("databases/Bible.db");
//        } catch (IOException e){
//            e.printStackTrace();
//        }


    }

    @OnClick(R.id.schedule_button)
    public void onScheduleClick(){
//        Toast.makeText(this, "Schedule Clicked!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, SchedulerActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.read_button)
    public void onReadClick(){
        Intent intent = new Intent(this, ReadingActivity.class);
        startActivity(intent);
    }
}
