package xyz.danielkarr.dailytreasures;


import android.content.Context;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SchedulerActivity extends AppCompatActivity implements DatePickerFragment.DateListener {

    @BindView(R.id.start_date_button)
    Button mStartDateButton;

    @BindView(R.id.end_date_button)
    Button mEndDateButton;

    @BindView(R.id.start_book_button)
    Button mStartBookButton;

    @BindView(R.id.end_book_button)
    Button mEndBookButton;

    private Schedule mSchedule;
    private ArrayList<String> mDailyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduler);
        ButterKnife.bind(this);

        mSchedule = Schedule.getInstance();
        mStartDateButton.setText(dateToString(mSchedule.getStartDate()));
        mEndDateButton.setText(dateToString(mSchedule.getEndDate()));
        mStartBookButton.setText(mSchedule.getStartingBook());
        mEndBookButton.setText(mSchedule.getEndingBook());

        mDailyList = new ArrayList<>();
    }

    @OnClick(R.id.start_book_button)
    public void onStartBookButtonClick(){
        ArrayList<String> bookList = mSchedule.getBookList();
        Intent intent = new Intent(this, BookListActivity.class);
        intent.putExtra("booklist", bookList);
        intent.putExtra("requestcode", 1);
        startActivityForResult(intent,1);
    }

    @OnClick(R.id.end_book_button)
    public void onEndBookButtonClick(){
        ArrayList<String> bookList = mSchedule.getBookList();
        Intent intent = new Intent(this, BookListActivity.class);
        intent.putExtra("booklist", bookList);
        intent.putExtra("requestcode", 2);
        startActivityForResult(intent,2);
    }

    @OnClick(R.id.start_date_button)
    public void onStartDateClick(){
        DialogFragment newFragment = new DatePickerFragment();
        ((DatePickerFragment) newFragment).setFragmentType(DatePickerFragment.FragmentType.START_DATE);
        newFragment.show(getSupportFragmentManager(),"Date Picker");
    }

    @OnClick(R.id.end_date_button)
    public void onEndDateCLick(){
        DialogFragment newFragment = new DatePickerFragment();
        ((DatePickerFragment) newFragment).setFragmentType(DatePickerFragment.FragmentType.END_DATE);
        newFragment.show(getSupportFragmentManager(),"Date Picker");
    }

    @OnClick(R.id.submit_button)
    public void onSubmitClick(){
        if(isDateBeforeDate(mSchedule.getStartDate(),mSchedule.getEndDate())){
            String filename = "schedule";
            String fileContents = dateToString(mSchedule.getStartDate()) + "\n" + dateToString(mSchedule.getEndDate())
                    + "\n" + mSchedule.getStartingBook() + "\n" + mSchedule.getEndingBook();
            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(fileContents.getBytes());
                outputStream.close();
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this,"Start date must come before end date!", Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isDateBeforeDate(Date alpha, Date omega){
        LocalDate start = LocalDate.fromDateFields(alpha);
        LocalDate end = LocalDate.fromDateFields(omega);
        int daysbetween = Days.daysBetween(start, end).getDays();
        if(daysbetween >= 1){
            return true;
        } else {
            return false;
        }
    }

    public String dateToString(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return month + "/" + day + "/" + year;
    }

    public void onDatePicked(int month, int day, int year, DatePickerFragment.FragmentType fType){
        Calendar cal = Calendar.getInstance();
        cal.set(year,month,day);
        Date date = cal.getTime();
        if(fType == DatePickerFragment.FragmentType.START_DATE){
            mStartDateButton.setText((month+1) + "/" + day + "/" + year);
            mSchedule.setStartDate(date);
            Log.i("SCHEULDER", "onDatePicked: StartDate" + mSchedule.getStartDate().toString());
        } else {
            mEndDateButton.setText((month+1) + "/" + day + "/" + year);
            mSchedule.setEndDate(date);
            Log.i("SCHEULDER", "onDatePicked: ENDDate" + mSchedule.getEndDate().toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if(requestCode==1)
        {
            String message=data.getStringExtra("ReturnedBook");
            mStartBookButton.setText(message);
            mSchedule.setStartingBook(message);
        } else if (requestCode==2){
            String message=data.getStringExtra("ReturnedBook");
            mEndBookButton.setText(message);
            mSchedule.setEndingBook(message);
        }
    }

}
