package xyz.danielkarr.dailytreasures;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.io.File;
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
    private ArrayList<DailyPortion> mDailyList;

    //Refactoring
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDB;
    private int mNumDaysTotal;
    private int mTotalVerses;
    private int mVersesPerDay;
    private int mVerseRemainder;
    private ArrayList<String> mBookList;
    private ArrayList<Integer> mDailyCountList;

    private static final String TAG = "SCHEDULER_ACTIVITY";

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
        setContentView(R.layout.activity_scheduler);
        ButterKnife.bind(this);

        mSchedule = Schedule.getInstance();
        mStartDateButton.setText(dateToString(mSchedule.getStartDate()));
        mEndDateButton.setText(dateToString(mSchedule.getEndDate()));
        mStartBookButton.setText(mSchedule.getStartingBook());
        mEndBookButton.setText(mSchedule.getEndingBook());
        mBookList = mSchedule.getBookList();
        mDailyList = new ArrayList<>();
        mDailyCountList = new ArrayList<>();
        mDbHelper = new DatabaseHelper(this);
        mDB = mDbHelper.getReadableDatabase();
    }

   @Override
   protected void onResume(){
        super.onResume();
        File file = new File("/data/data/xyz.danielkarr.dailytreasures/files/schedule");
        if(file.exists()){
            Toast.makeText(this,"WARNING: Creating a new schedule will override the old one! Hit " +
                    "the back button to cancel.", Toast.LENGTH_LONG).show();
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
            // If start date is before end date, calculate total days and verses and check if verses/day >= 1
            mDB = mDbHelper.getReadableDatabase();
            setNumDaysTotal();
            setTotalVerseCount();
            if(isPortionSizeSufficient()){
                calcuateDailyPortions();
            } else {
                Toast.makeText(this,"You need at least 1 verse per day, right now you have " + mTotalVerses
                        + " verses spread across " + mNumDaysTotal + " days!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this,"Start date must come before end date!", Toast.LENGTH_SHORT).show();
        }


//        TODO delete after refactor
//        if(isDateBeforeDate(mSchedule.getStartDate(),mSchedule.getEndDate())){
//            String filename = "schedule";
//            String fileContents = dateToString(mSchedule.getStartDate()) + "\n" + dateToString(mSchedule.getEndDate())
//                    + "\n" + mSchedule.getStartingBook() + "\n" + mSchedule.getEndingBook();
//            FileOutputStream outputStream;
//
//            try {
//                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
//                outputStream.write(fileContents.getBytes());
//                outputStream.close();
//                finish();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        } else {
//            Toast.makeText(this,"Start date must come before end date!", Toast.LENGTH_SHORT).show();
//        }
    }

    /**
     * Check if date alpha comes before date omega
     * @param alpha
     * @param omega
     * @return
     */
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



    private void calcuateDailyPortions() {
        int sBook = getBookIndex(mSchedule.getStartingBook());
        LocalDate sDate = new LocalDate(mSchedule.getStartDate());
        int numChapters = getNumChaptersInBook(sBook);
        int eBook= mBookList.indexOf(mSchedule.getEndingBook()) + 1;

        // 1. Create portion with starting ref and beginning date
        DailyPortion original = new DailyPortion(sDate, sBook, 1, 1);
        // 2. Pass that portion to getNextXVerses
        DailyPortion currentPortion = getNextXverses(original,0);
        Log.i(TAG, "calcuateDailyPortions: PORTION AFTER CHANGES " + currentPortion.toString());
        Log.i(TAG, "calcuateDailyPortions: DAILYVERSES REMAINDER " + mVersesPerDay + " " + mVerseRemainder);
        Log.i(TAG, "calcuateDailyPortions: TOTALVERSES TOTALDAYS " + mTotalVerses + " " + mNumDaysTotal);
        for(int i=0; i<mDailyCountList.size(); i++){
            Log.i(TAG, "calcuateDailyPortions: V Per Day " + mDailyCountList.get(i));
        }
        // 3. Retrieve completed daily portion and add it to the DailyList
        // 4. Create new portion starting at end of previous and pass it
        // 5. Repeat until done (can use total number of days to check)

    }

    /**
     * Get the reference of mVersesPerDay verses later, assign it to the portion, and return it
     * @param portion the started DailyPortion with start refs already filled
     * @return the completed portion with ending references
     */
    private DailyPortion getNextXverses(DailyPortion portion, int countIndex){
        int sBook = portion.getStartingBookIndex();
        int sChapter = portion.getStartingChapter();
        int sVerse = portion.getStartingVerse();
        int thisDayCount = mDailyCountList.get(countIndex);


        int versesInChap = 0;
        String selectQuery = "SELECT " + MasterEntry.COLUMN_NAME_NUMVERSES + " FROM " + MasterEntry.TABLE_NAME + " WHERE "
                + MasterEntry.COLUMN_NAME_BOOK + " = " + sBook + " AND " + MasterEntry.COLUMN_NAME_CHAPTER + " = " + sChapter;
        Cursor c = mDB.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
            versesInChap = c.getInt(0);
        }
        c.close();

        int unreadVerses = versesInChap - (sVerse - 1);
        // TODO handle remainder
        if(unreadVerses >= thisDayCount){
            int eVerse = thisDayCount - 1 + sVerse;
            portion.setEndingBookIndex(sBook);
            portion.setEndingChapter(sChapter);
            portion.setEndingVerse(eVerse);

        }
        return portion;
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

        // TODO combine these blocks when debugging is no longer necessary
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
        c.close();
        return bookTotal;
    }

    /**
     * Get the number of chapters in a book
     * @param bookNum index of book, 1 based
     * @return number of chapters in the book
     */
    private int getNumChaptersInBook(int bookNum) {
        String selectQuery = "SELECT " + MasterEntry.COLUMN_NAME_CHAPTER + " FROM " + MasterEntry.TABLE_NAME + " WHERE "
                + MasterEntry.COLUMN_NAME_BOOK + " = " + bookNum + " ORDER BY " + MasterEntry.COLUMN_NAME_CHAPTER +  " DESC";
        Cursor c = mDB.rawQuery(selectQuery, null);
        if (c.moveToFirst()) {
            return c.getInt(0);
        } else {
            Log.e(TAG, "getNumChaptersInBook: ERROR GETTING NUMBER OF CHAPTERS");
            return -1;
        }
    }


    /**
     * Iterate from starting book to ending book adding up all the verses
     * Set that value to mTotalVerses
     */
    private void setTotalVerseCount(){
        int startingIndex = mBookList.indexOf(mSchedule.getStartingBook());
        int endingIndex = mBookList.indexOf(mSchedule.getEndingBook());
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
        mTotalVerses = totalVerses;
    }

    /**
     * Calculate number of days in the schedule and set it to mNumDaysTotal
     */
    private void setNumDaysTotal(){
        LocalDate startDate = dateToLocalDate(mSchedule.getStartDate());
        LocalDate endDate = dateToLocalDate(mSchedule.getEndDate());
        mNumDaysTotal = Days.daysBetween(startDate,endDate).getDays();
    }

    /**
     * Initialize mDailyCountList to an arraylist of length TotalDays populated with how many verses per day,
     * including the remainder
     */
    private void setDailyCountList(){
        for(int i=0; i<mNumDaysTotal; i++){
            mDailyCountList.add(mVersesPerDay);
        }
        for(int i=0; i<mVerseRemainder; i++){
            int inc = mDailyCountList.get(i);
            inc += 1;
            mDailyCountList.set(i,inc);
        }
    }

    private LocalDate dateToLocalDate(Date date){
        return new LocalDate(date);
    }

    /**
     * Check to make sure there is at least 1 possible verse per day
     * @return true if the portion is at least 1 verse a day
     */
    private boolean isPortionSizeSufficient(){
        int perDay = mTotalVerses / mNumDaysTotal;
        if(perDay >= 1){
            mVersesPerDay = perDay;
            mVerseRemainder = mTotalVerses % mNumDaysTotal;
            setDailyCountList();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get the index of the book, 1 based
     * @param book String of book name
     * @return the 1 based index of the book
     */
    private int getBookIndex(String book){
        int i = mBookList.indexOf(book) + 1;
        return i;
    }

}
