package xyz.danielkarr.dailytreasures;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class Schedule {

    private Date mStartDate;
    private Date mEndDate;
    private String mStartingBook;
    private String mEndingBook;
    private ArrayList<String> mBookList;



    private static final Schedule ourInstance = new Schedule();

    public static Schedule getInstance() {
        return ourInstance;
    }

    private Schedule() {
        mBookList = new ArrayList<>(Arrays.asList(
                "Genesis",
                "Exodus",
                "Leviticus",
                "Numbers",
                "Deuteronomy",
                "Joshua",
                "Judges",
                "Ruth",
                "1 Samuel",
                "2 Samuel",
                "1 Kings",
                "2 Kings",
                "1 Chronicles",
                "2 Chronicles",
                "Ezra",
                "Nehemiah",
                "Esther",
                "Job",
                "Psalms",
                "Proverbs",
                "Ecclesiastes",
                "Song of Songs",
                "Isaiah",
                "Jeremiah",
                "Lamentations",
                "Ezekiel",
                "Daniel",
                "Hosea",
                "Joel",
                "Amos",
                "Obadiah",
                "Jonah",
                "Micah",
                "Nahum",
                "Habakkuk",
                "Zephaniah",
                "Haggai",
                "Zechariah",
                "Malachi",
                "Matthew",
                "Mark",
                "Luke",
                "John",
                "Acts",
                "Romans",
                "1 Corinthians",
                "2 Corinthians",
                "Galatians",
                "Ephesians",
                "Philippians",
                "Colossians",
                "1 Thessalonians",
                "2 Thessalonians",
                "1 Timothy",
                "2 Timothy",
                "Titus",
                "Philemon",
                "Hebrews",
                "James",
                "1 Peter",
                "2 Peter",
                "1 John",
                "2 John",
                "3 John",
                "Jude",
                "Revelation"
        ));
        mStartingBook = getBookName(0);
        mEndingBook = getBookName(65);
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(new Date());
        mStartDate = cal.getTime();
        Log.i("SCHEDULE", "Schedule start date: " + mStartDate.toString());
        cal.add(Calendar.DAY_OF_YEAR, +365);
        mEndDate = cal.getTime();
        Log.i("SCHEDULE", "Schedule start date: " + mEndDate.toString());
    }

    public Date getStartDate() {
        return mStartDate;
    }

    public void setStartDate(Date startDate) {
        mStartDate = startDate;
    }

    public Date getEndDate() {
        return mEndDate;
    }

    public void setEndDate(Date endDate) {
        mEndDate = endDate;
    }

    public String getBookName(int bookNum){
        return mBookList.get(bookNum);
    }

    public ArrayList<String> getBookList(){
        return mBookList;
    }

    public String getStartingBook() {
        return mStartingBook;
    }

    public void setStartingBook(String startingBook) {
        mStartingBook = startingBook;
    }

    public String getEndingBook() {
        return mEndingBook;
    }

    public void setEndingBook(String endingBook) {
        mEndingBook = endingBook;
    }
}


