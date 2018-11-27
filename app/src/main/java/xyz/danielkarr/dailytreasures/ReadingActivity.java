package xyz.danielkarr.dailytreasures;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Period;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReadingActivity extends AppCompatActivity {
    private List<String> mLines;
    private LocalDate mStartDate;
    private LocalDate mEndDate;
    private String mStartBook;
    private String mEndBook;
    private int mNumDays;
    private int mTotalVerses;
    private int mVersesPerDay;
    private int mVerseRemainder;
    private ArrayList<String> mBookList;

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDB;

    private static final String TAG = "READINGACTIVITY";

    /* Inner class that defines the master table contents */
    public static class MasterEntry implements BaseColumns {
        public static final String TABLE_NAME = "master";
        public static final String COLUMN_NAME_BOOK = "book";
        public static final String COLUMN_NAME_CHAPTER = "chapter";
        public static final String COLUMN_NAME_NUMVERSES = "numVerses";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);

        mLines = new ArrayList<>();
        String curLine = null;

        try {
            BufferedReader reader = new BufferedReader(new FileReader("/data/data/xyz.danielkarr.dailytreasures/files/schedule"));
            while ((curLine = reader.readLine()) != null) {
                mLines.add(curLine);
            }
            mDbHelper = new DatabaseHelper(this);
            setScheduleVariables();
            calcuateTodaysPortion();
        } catch (Exception e){
            e.printStackTrace();
        }

        // TODO Delete later - debug
        for (String line: mLines) {
            Log.i("READINGACTIVITY", "onCreate: " + line);
        }
    }

    private void setScheduleVariables() {
        String[] splitLine = mLines.get(0).split("/");
//        Log.i(TAG, "setScheduleVariables: " + splitLine[0] + " " +  splitLine[1] + " " + splitLine[2]);
        // Construct LocalDate with year, month, day
        mStartDate = new LocalDate(Integer.parseInt(splitLine[2]),Integer.parseInt(splitLine[0]),Integer.parseInt(splitLine[1]));
        splitLine = mLines.get(1).split("/");
        mEndDate = new LocalDate(Integer.parseInt(splitLine[2]),Integer.parseInt(splitLine[0]),Integer.parseInt(splitLine[1]));
        mStartBook = mLines.get(2);
        mEndBook = mLines.get(3);
        mNumDays = Days.daysBetween(mStartDate,mEndDate).getDays();
        Schedule s = Schedule.getInstance();
        mBookList = s.getBookList();
        Log.i(TAG, "setScheduleVariables: DAYS BETEEN***: " + mNumDays);
    }

    private void calcuateTodaysPortion() {
        // First time accessing DB, initialize it
        mDB = mDbHelper.getReadableDatabase();

        getTotalVerseCount();
        mVersesPerDay = mTotalVerses / mNumDays;
        mVerseRemainder = mTotalVerses % mNumDays;
        Log.i(TAG, "calcuateTodaysPortion: VersesPerDay Remainder " + mVersesPerDay + " " + mVerseRemainder);
    }

    /**
     * Get the total amount of verses in a particular book.
     * @param bookNum the index of the book, it is 1 based not 0 based
     * @return total verses in the book
     */
    private int getTotalBookVerseCount(int bookNum){
        String selectQuery = "SELECT " + MasterEntry.COLUMN_NAME_NUMVERSES + " FROM " + MasterEntry.TABLE_NAME + " WHERE "
                + MasterEntry.COLUMN_NAME_BOOK + " = " + bookNum;
        ArrayList<Integer> numVersesList = new ArrayList<>();
        Cursor c = mDB.rawQuery(selectQuery, null);
        // TODO combine these sections when debugging is no longer necessary
        if (c.moveToFirst()) {
            do {// get row values
                numVersesList.add(c.getInt(0));
            } while (c.moveToNext());
        }

        int bookTotal = 0;
        for (int x:numVersesList                ) {
            Log.i(TAG, "calcuateTodaysPortion: Verses: " + x);
            bookTotal += x;
        }
        return bookTotal;
    }

    private int getTotalVerseCount(){
        int startingIndex = mBookList.indexOf(mStartBook);
        int endingIndex = mBookList.indexOf(mEndBook);
        Log.i(TAG, "getTotalVerseCount: Starting index: " + startingIndex);
        boolean finished = true;
        int totalVerses = 0;
        // Loop through all books until
        for(int i=startingIndex; finished; i++){
            int x = i % mBookList.size();
            totalVerses += getTotalBookVerseCount(x + 1);
            if (x == endingIndex){
                finished = false;
            }
        }
        Log.i(TAG, "getTotalVerseCount: TOTALVERSECOUNT " + totalVerses);
        return totalVerses;
    }
}
