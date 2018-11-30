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
        try{
            mDbHelper = new DatabaseHelper(this);
            mDB = mDbHelper.getReadableDatabase();
            Log.i(TAG, "onCreate: GOT READABLE DB");
        } catch (Error e){
            e.printStackTrace();
        }

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
        clearSchedule();
        ArrayList<String> bookList = mSchedule.getBookList();
        Intent intent = new Intent(this, BookListActivity.class);
        intent.putExtra("booklist", bookList);
        intent.putExtra("requestcode", 1);
        startActivityForResult(intent,1);
    }

    @OnClick(R.id.end_book_button)
    public void onEndBookButtonClick(){
        clearSchedule();
        ArrayList<String> bookList = mSchedule.getBookList();
        Intent intent = new Intent(this, BookListActivity.class);
        intent.putExtra("booklist", bookList);
        intent.putExtra("requestcode", 2);
        startActivityForResult(intent,2);
    }

    @OnClick(R.id.start_date_button)
    public void onStartDateClick(){
        clearSchedule();
        DialogFragment newFragment = new DatePickerFragment();
        ((DatePickerFragment) newFragment).setFragmentType(DatePickerFragment.FragmentType.START_DATE);
        newFragment.show(getSupportFragmentManager(),"Date Picker");
    }

    @OnClick(R.id.end_date_button)
    public void onEndDateCLick(){
        clearSchedule();
        DialogFragment newFragment = new DatePickerFragment();
        ((DatePickerFragment) newFragment).setFragmentType(DatePickerFragment.FragmentType.END_DATE);
        newFragment.show(getSupportFragmentManager(),"Date Picker");
    }

    @OnClick(R.id.submit_button)
    public void onSubmitClick(){
        if(isDateBeforeDate(mSchedule.getStartDate(),mSchedule.getEndDate())){
            // If start date is before end date, calculate total days and verses and check if verses/day >= 1
//            mDB = mDbHelper.getReadableDatabase();
            setNumDaysTotal();
            setTotalVerseCount();
            if(isPortionSizeSufficient()){
                calcuateDailyPortions();
                writeToFile();
//                for(int i=0; i<mDailyList.size(); i++){
//                    Log.i(TAG, "onSubmitClick: " + mDailyList.get(i).toString());
//                }
            } else {
                Toast.makeText(this,"You need at least 1 verse per day, right now you have " + mTotalVerses
                        + " verses spread across " + mNumDaysTotal + " days!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this,"Start date must come before end date!", Toast.LENGTH_SHORT).show();
        }
    }

    public void writeToFile(){
        String filename = "schedule";
        FileOutputStream outputStream;

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            for(int i=0; i<mDailyList.size(); i++){
                String fileContents = mDailyList.get(i).toString() +"\n";
                outputStream.write(fileContents.getBytes());
            }
            outputStream.close();
            String[] list = fileList();
            Log.i(TAG, "writeToFile: LIST LENGTH " + list.length);
            for (String s:list
                    ) {
                Log.i(TAG, "writeToFile: FILELIST " + s);
            }
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clearSchedule(){
        mDailyList.clear();
        mDailyCountList.clear();
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
//            Log.i("SCHEULDER", "onDatePicked: StartDate" + mSchedule.getStartDate().toString());
        } else {
            mEndDateButton.setText((month+1) + "/" + day + "/" + year);
            mSchedule.setEndDate(date);
//            Log.i("SCHEULDER", "onDatePicked: ENDDate" + mSchedule.getEndDate().toString());
        }
    }



    private void calcuateDailyPortions() {
        int sBook = getBookIndex(mSchedule.getStartingBook());
        LocalDate sDate = new LocalDate(mSchedule.getStartDate());

        // 1. Create portion with starting ref and beginning date
        DailyPortion original = new DailyPortion(sDate, sBook, 1, 1);

        for(int i=0; i<mDailyCountList.size(); i++){
            // 2. Pass that portion to getNextXVerses
            DailyPortion currentPortion = getNextXverses(original,i);
            mDailyList.add(currentPortion);
            original = getNewStartingPortion(currentPortion);
        }


//        Log.i(TAG, "calcuateDailyPortions: PORTION AFTER CHANGES " + currentPortion.toString());
//        Log.i(TAG, "calcuateDailyPortions: DAILYVERSES REMAINDER " + mVersesPerDay + " " + mVerseRemainder);
//        Log.i(TAG, "calcuateDailyPortions: TOTALVERSES TOTALDAYS " + mTotalVerses + " " + mNumDaysTotal);
//        for(int i=0; i<mDailyCountList.size(); i++){
//            Log.i(TAG, "calcuateDailyPortions: V Per Day " + mDailyCountList.get(i));
//        }
        // 3. Retrieve completed daily portion and add it to the DailyList
        // 4. Create new portion starting at end of previous and pass it
        // 5. Repeat until done (can use total number of days to check)

    }

    /**
     * Get the reference of mVersesPerDay verses later, assign it to the portion, and return it
     * @param incompletePortion the started DailyPortion with start refs already filled
     * @return the completed portion with ending references
     */
    private DailyPortion getNextXverses(DailyPortion incompletePortion, int countIndex){
        DailyPortion portion = incompletePortion;
        int sBook = portion.getStartingBookIndex();
        int sChapter = portion.getStartingChapter();
        int sVerse = portion.getStartingVerse();
        int thisDayCount = mDailyCountList.get(countIndex);

        do {
            int chapsInBook = getNumChaptersInBook(sBook);
            int versesInChap = getNumVersesInChapter(sBook,sChapter);
            int unreadVerses = versesInChap - (sVerse - 1);
//            Log.i(TAG, "getNextXverses: BOOK CHAP CHAPSINBOOK " + sBook + " " + sChapter + " " + chapsInBook);

            // If there are enough verses left in current chapter
            if(unreadVerses >= thisDayCount){
                int eVerse = thisDayCount - 1 + sVerse;
                portion.setEndingBookIndex(sBook);
                portion.setEndingChapter(sChapter);
                portion.setEndingVerse(eVerse);
                thisDayCount = 0;
            } else {
                thisDayCount -= unreadVerses;
                sVerse = 1;
                //Go to next chapter
                sChapter += 1;
                //If at end of book go to next book
                if(sChapter > chapsInBook){
                    sChapter = 1;
                    sBook += 1;
                    //If at last book go to first
                    if(sBook > 66){
                        sBook = 1;
                    }
                }
            }
        } while (thisDayCount > 0);

        return portion;
    }

    /**
     * Takes a daily portion, increments verse, possibly to new chapter or book, and assigns date 1 day later
     * @param endPortion
     * @return
     */
    private DailyPortion getNewStartingPortion(DailyPortion endPortion) {
        int eVerse = endPortion.getEndingVerse() + 1;
        int eChap = endPortion.getEndingChapter();
        int eBook = endPortion.getEndingBookIndex();
        LocalDate sDate = endPortion.getDate();
        sDate = sDate.plusDays(1);
//        Log.i(TAG, "getNewStartingPortion: book chap " + eBook + " " + eChap);
        int versesInChap = getNumVersesInChapter(eBook,eChap);
        int chapsInBook = getNumChaptersInBook(eBook);
        // Not the last verse in the chapter
        if(eVerse <= versesInChap){
            return new DailyPortion(sDate,eBook,eChap,eVerse);
        } else if(eChap+1 <= chapsInBook){ // End of chapter but not end of book
//            Log.i(TAG, "getNewStartingPortion: BOOK CHAP " + eBook + " " + eChap+1);
            return new DailyPortion(sDate,eBook,eChap+1,1);
        } else {        // End of book
            eBook++;
            if(eBook > 66) { eBook = 1; }
            return new DailyPortion(sDate,eBook,1,1);
        }

    }

    /**
     * Get the number of verses in a particular chapter.
     * @param bookNum
     * @param chapter
     * @return
     */
    private int getNumVersesInChapter(int bookNum, int chapter){
//        Log.i(TAG, "getNumVersesInChapter: BOOKNUM CHAPTER " + bookNum + " " + chapter);
        String selectQuery = "SELECT " + MasterEntry.COLUMN_NAME_NUMVERSES + " FROM " + MasterEntry.TABLE_NAME + " WHERE "
                + MasterEntry.COLUMN_NAME_BOOK + " = " + bookNum + " AND " + MasterEntry.COLUMN_NAME_CHAPTER + " = " + chapter;
        Cursor c = mDB.rawQuery(selectQuery, null);
        int verses = 0;
        if (c.moveToFirst()) {
            verses = c.getInt(0);
            c.close();
            return verses;
        } else {
            Log.e(TAG, "getNumVersesInChapter: CURSOR ERROR");
            c.close();
            return -1;
        }
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
        Cursor c = mDB.rawQuery(selectQuery,null);

        // TODO combine these blocks when debugging is no longer necessary
        if (c.moveToFirst()) {
            do {// get row values
                numVersesList.add(c.getInt(0));
            } while (c.moveToNext());
        }

        int bookTotal = 0;
        for (int x:numVersesList                ) {
//            Log.i(TAG, "calcuateTodaysPortion: Verses: " + x);
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
            int a = c.getInt(0);
            c.close();
            return a;
        } else {
            Log.e(TAG, "getNumChaptersInBook: ERROR GETTING NUMBER OF CHAPTERS");
            c.close();
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
//        Log.i(TAG, "getTotalVerseCount: Starting index: " + startingIndex);
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
//        Log.i(TAG, "getTotalVerseCount: TOTALVERSECOUNT " + totalVerses);
        mTotalVerses = totalVerses;
    }

    /**
     * Calculate number of days in the schedule and set it to mNumDaysTotal
     */
    private void setNumDaysTotal(){
        LocalDate startDate = dateToLocalDate(mSchedule.getStartDate());
        LocalDate endDate = dateToLocalDate(mSchedule.getEndDate());
        mNumDaysTotal = Days.daysBetween(startDate,endDate).getDays() + 1;
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
