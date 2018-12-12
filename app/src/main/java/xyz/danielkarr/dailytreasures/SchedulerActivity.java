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
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.Days;
import org.joda.time.LocalDate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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

    private int mScheduleNum;
    private String mFileName;
    private boolean mWarningBeenShown;

    private static final String TAG = "SCHEDULER_ACTIVITY";

    /* Inner class that defines the master table contents */
    static class MasterEntry implements BaseColumns {
        static final String TABLE_NAME = "master";
        static final String COLUMN_NAME_BOOK = "book";
        static final String COLUMN_NAME_CHAPTER = "chapter";
        static final String COLUMN_NAME_NUMVERSES = "numVerses";
    }

    @BindView(R.id.schedule1_info)
    TextView mScheduleOneInfo;

    @BindView(R.id.schedule2_info)
    TextView mScheduleTwoInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheduler);
        ButterKnife.bind(this);

        mSchedule = Schedule.getInstance();
        if(savedInstanceState == null){
            mScheduleNum = getIntent().getIntExtra("scheduleNum",1);
            mWarningBeenShown = false;
        } else {
            mScheduleNum = savedInstanceState.getInt("scheduleNum");
            mSchedule.setStartDate(dateFromString(savedInstanceState.getString("startDate")));
            mSchedule.setEndDate(dateFromString(savedInstanceState.getString("endDate")));
            mSchedule.setStartingBook(savedInstanceState.getString("startingBook"));
            mSchedule.setEndingBook(savedInstanceState.getString("endingBook"));
            mWarningBeenShown = savedInstanceState.getBoolean("warning");
        }

        if(mScheduleNum == 1){
            mFileName = "schedule";
            setTitle("Reading Plan " + 1);
        } else {
            mFileName = "schedule2";
            setTitle("Reading Plan " + 2);
        }

        mStartDateButton.setText(dateToString(mSchedule.getStartDate()));
        mEndDateButton.setText(dateToString(mSchedule.getEndDate()));
        mStartBookButton.setText(mSchedule.getStartingBook());
        mEndBookButton.setText(mSchedule.getEndingBook());
        mBookList = mSchedule.getBookList();
        mDailyList = new ArrayList<>();
        mDailyCountList = new ArrayList<>();
        try{
            mDbHelper = DatabaseHelper.getInstance(this);
            mDB = mDbHelper.getReadableDatabase();
        } catch (Error e){
            e.printStackTrace();
        }
        populateSummary();
    }

   @Override
   protected void onResume(){
        super.onResume();
        File file = new File(getApplicationContext().getFilesDir().getPath() + "/" + mFileName);
        if(file.exists() && !mWarningBeenShown){
            Toast.makeText(this,"WARNING: Creating a new schedule will override the old one! Hit " +
                    "the back button to cancel.", Toast.LENGTH_LONG).show();
            mWarningBeenShown = true;
        }
   }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null){
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
            setNumDaysTotal();
            setTotalVerseCount();
            if(isPortionSizeSufficient()){
                calcuateDailyPortions();
                mDbHelper.close();
                mDB.close();
                deleteScrollFile();
                writeToFile();
            } else {
                Toast.makeText(this,"You need at least 1 verse per day, right now you have " + mTotalVerses
                        + " verses spread across " + mNumDaysTotal + " days!", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this,"Start date must come before end date!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Deletes appropriate scroll file when creating a new schedule.
     */
    private void deleteScrollFile(){
        String filename;
        if(mScheduleNum == 1){
            filename = "scrollOne";
        } else {
            filename = "scrollTwo";
        }
        File file = new File(getApplicationContext().getFilesDir().getPath() + "/" + filename);
        if(file.exists()) {
            file.delete();
        }

    }

    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString("startingBook", mSchedule.getStartingBook());
        outState.putString("endingBook", mSchedule.getEndingBook());
        outState.putString("startDate", dateToString(mSchedule.getStartDate()));
        outState.putString("endDate", dateToString(mSchedule.getEndDate()));
        outState.putInt("scheduleNum", mScheduleNum);
        outState.putBoolean("warning",mWarningBeenShown);
    }

    /**
     * Populates schedule info views.
     */
    private void populateSummary(){
        File fileOne = new File(getApplicationContext().getFilesDir().getPath() + "/" + "schedule");
        String scheduleOne, scheduleTwo;
        if(fileOne.exists()){
            scheduleOne = getSummaryLine("schedule");
            scheduleOne = parseSummaryLine(scheduleOne,1);
            mScheduleOneInfo.setText(scheduleOne);
        }else {
            mScheduleOneInfo.setText("Reading Plan 1\n\nDoesn't Exist");
        }
        File fileTwo = new File(getApplicationContext().getFilesDir().getPath() + "/" + "schedule2");
        if(fileTwo.exists()){
            scheduleTwo = getSummaryLine("schedule2");
            scheduleTwo = parseSummaryLine(scheduleTwo,2);
            mScheduleTwoInfo.setText(scheduleTwo);
        } else {
            mScheduleTwoInfo.setText("Reading Plan 2\n\nDoesn't Exist");
        }
    }

    /**
     * Parses out the summary line and constructs string for displaying schedule info.
     * @param summaryLine string to be parsed.
     * @param num the schedule number.
     * @return string to display for schedule summary.
     */
    private String parseSummaryLine(String summaryLine, int num){
        String[] s = summaryLine.split(" ");
        if(s.length == 4){
            return  "Reading Plan " + num + "\n\n\n" + "Start Date: " + s[0] + "\n\n" + "End Date: " + s[1] + "\n\n" + "Starting Book: " + s[2] +
                    "\n\n" + "Ending Book: " + s[3];
        } else {
            return  "Reading Plan " + num + "\n\n\n" + "Start Date: " + s[0] + "\n\n" + "End Date: " + s[1] + "\n\n" + "Starting Book: " + s[2] +
                    "\n\n" + "Ending Book: " + s[3] + " " + s[4];
        }
    }

    /**
     * Returns the first line, AKA summary line from the appropriate file.
     * @param file to read from.
     * @return string of summary line.
     */
    private String getSummaryLine(String file){
        String schedule = "";
        try{
            FileInputStream fis = this.openFileInput(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader bufferedReader = new BufferedReader(isr);
            schedule = bufferedReader.readLine();
            bufferedReader.close();
        } catch (IOException e){
            e.printStackTrace();
        }
        return schedule;
    }

    /**
     * Write mDailyList out to the appropriate file.
     */
    private void writeToFile(){
        String filename = mFileName;
        FileOutputStream outputStream;
        String summary = dateToString(mSchedule.getStartDate()) + " " + dateToString(mSchedule.getEndDate()) + " " +
                mSchedule.getStartingBook() + " " + mSchedule.getEndingBook() + "\n";

        try {
            outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(summary.getBytes());
            for(int i=0; i<mDailyList.size(); i++){
                String fileContents = mDailyList.get(i).toString() +"\n";
                outputStream.write(fileContents.getBytes());
            }
            outputStream.close();
            finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Clears the schedule ArrayLists.
     */
    private void clearSchedule(){
        mDailyList.clear();
        mDailyCountList.clear();
    }

    /**
     * Check if date alpha comes before date omega
     * @param alpha first date
     * @param omega end date
     * @return true if alpha comes before omega
     */
    private boolean isDateBeforeDate(Date alpha, Date omega){
        LocalDate start = LocalDate.fromDateFields(alpha);
        LocalDate end = LocalDate.fromDateFields(omega);
        int daysbetween = Days.daysBetween(start, end).getDays();
        return daysbetween >= 1;
    }

    /**
     * Constructs string representation of date (MM/(d)d/yyyy)
     * @param date to turn into string.
     * @return string of date.
     */
    private String dateToString(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        return month + "/" + day + "/" + year;
    }

    /**
     * Creates Date object from string representation of date.
     * @param dateString to turn into Date object.
     * @return Date object.
     */
    private Date dateFromString(String dateString){
        String[] line = dateString.split("/");
        Calendar c = GregorianCalendar.getInstance();
        int month = Integer.parseInt(line[0]) -1;
        int day = Integer.parseInt(line[1]);
        int year = Integer.parseInt(line[2]);
        c.set(year,month,day);
        return c.getTime();
    }

    /**
     * When user selects a start/end date, sets it to schedule object and assigns text of
     * respective button.
     * @param month int month.
     * @param day int day.
     * @param year int year.
     * @param fType DatePicker fragment.
     */
    public void onDatePicked(int month, int day, int year, DatePickerFragment.FragmentType fType){
        Calendar cal = Calendar.getInstance();
        cal.set(year,month,day);
        Date date = cal.getTime();
        if(fType == DatePickerFragment.FragmentType.START_DATE){
            mStartDateButton.setText(String.format("%d/%d/%d", month + 1, day, year));
            mSchedule.setStartDate(date);
        } else {
            mEndDateButton.setText(String.format("%d/%d/%d", month + 1, day, year));
            mSchedule.setEndDate(date);
        }
    }


    /**
     * Calculates the daily portion schedule using DailyPortion objects and populates mDailyList
     * with the daily portions.
     */
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
    }

    /**
     * Get the reference of mVersesPerDay verses later, assign it to the portion, and return it
     * @param incompletePortion the started DailyPortion with start refs already filled
     * @return the completed portion with ending references
     */
    private DailyPortion getNextXverses(DailyPortion incompletePortion, int countIndex){
        int sBook = incompletePortion.getStartingBookIndex();
        int sChapter = incompletePortion.getStartingChapter();
        int sVerse = incompletePortion.getStartingVerse();
        int thisDayCount = mDailyCountList.get(countIndex);

        do {
            int chapsInBook = getNumChaptersInBook(sBook);
            int versesInChap = getNumVersesInChapter(sBook,sChapter);
            int unreadVerses = versesInChap - (sVerse - 1);

            // If there are enough verses left in current chapter
            if(unreadVerses >= thisDayCount){
                int eVerse = thisDayCount - 1 + sVerse;
                incompletePortion.setEndingBookIndex(sBook);
                incompletePortion.setEndingChapter(sChapter);
                incompletePortion.setEndingVerse(eVerse);
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

        return incompletePortion;
    }

    /**
     * Takes a finished daily portion, and uses its ending values to create the next day's portion.
     * @param endPortion the completed portion to be used as the start of the next portion.
     * @return new portion with starting values assigned.
     */
    private DailyPortion getNewStartingPortion(DailyPortion endPortion) {
        int eVerse = endPortion.getEndingVerse() + 1;
        int eChap = endPortion.getEndingChapter();
        int eBook = endPortion.getEndingBookIndex();
        LocalDate sDate = endPortion.getDate();
        sDate = sDate.plusDays(1);
        int versesInChap = getNumVersesInChapter(eBook,eChap);
        int chapsInBook = getNumChaptersInBook(eBook);
        // Not the last verse in the chapter
        if(eVerse <= versesInChap){
            return new DailyPortion(sDate,eBook,eChap,eVerse);
        } else if(eChap+1 <= chapsInBook){ // End of chapter but not end of book
            return new DailyPortion(sDate,eBook,eChap+1,1);
        } else {        // End of book
            eBook++;
            if(eBook > 66) { eBook = 1; }
            return new DailyPortion(sDate,eBook,1,1);
        }

    }

    /**
     * Get the number of verses in a particular chapter.
     * @param bookNum book index.
     * @param chapter chapter index.
     * @return int number of verses in a chapter.
     */
    private int getNumVersesInChapter(int bookNum, int chapter){
        String selectQuery = "SELECT " + MasterEntry.COLUMN_NAME_NUMVERSES + " FROM " + MasterEntry.TABLE_NAME + " WHERE "
                + MasterEntry.COLUMN_NAME_BOOK + " = " + bookNum + " AND " + MasterEntry.COLUMN_NAME_CHAPTER + " = " + chapter;
        Cursor c = mDB.rawQuery(selectQuery, null);
        int verses;
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
        for (int x:numVersesList) {
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
        return mBookList.indexOf(book) + 1;
    }
}
