package xyz.danielkarr.dailytreasures;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import net.danlew.android.joda.JodaTimeAndroid;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class HomeActivity extends AppCompatActivity {

    private boolean mScheduleOneExists;
    private boolean mScheduleTwoExists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        JodaTimeAndroid.init(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        checkSchedulesExist();
    }

    private void checkSchedulesExist(){
        File fileOne = new File(getApplicationContext().getFilesDir().getPath() + "/schedule");
        File fileTwo = new File(getApplicationContext().getFilesDir().getPath() + "/schedule2");
        mScheduleOneExists = fileOne.exists();
        mScheduleTwoExists = fileTwo.exists();
    }

    @OnClick(R.id.schedule_button)
    public void onScheduleClick(){
        Intent intent = new Intent(this, SchedulerActivity.class);
        intent.putExtra("scheduleNum", 1);
        startActivity(intent);
    }

    @OnClick(R.id.schedule_button_two)
    public void onScheduleTwoClick(){
        Intent intent = new Intent(this, SchedulerActivity.class);
        intent.putExtra("scheduleNum", 2);
        startActivity(intent);
    }

    @OnClick(R.id.read_button)
    public void onReadClick(){
        if(mScheduleOneExists){
            Intent intent = new Intent(this, ReadingActivity.class);
            intent.putExtra("scheduleNum", 1);
            startActivity(intent);
        } else {
            Toast.makeText(this,"Schedule #1 has not been created!",Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.read_button_two)
    public void onReadTwoClick(){
        if(mScheduleTwoExists){
            Intent intent = new Intent(this, ReadingActivity.class);
            intent.putExtra("scheduleNum", 2);
            startActivity(intent);
        } else {
            Toast.makeText(this,"Schedule #2 has not been created!",Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.emanna_button)
    public void onEmannaClick(){
        Intent intent = new Intent(this, EMannaActivity.class);
        startActivity(intent);
    }
}
