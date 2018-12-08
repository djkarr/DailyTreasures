package xyz.danielkarr.dailytreasures;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

class Schedule {

    private Date mStartDate;
    private Date mEndDate;
    private String mStartingBook;
    private String mEndingBook;
    private final ArrayList<String> mBookList;

    private static final Schedule ourInstance = new Schedule();

    static Schedule getInstance() {
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

    Date getStartDate() {
        return mStartDate;
    }

    void setStartDate(Date startDate) {
        mStartDate = startDate;
    }

    Date getEndDate() {
        return mEndDate;
    }

    void setEndDate(Date endDate) {
        mEndDate = endDate;
    }

    private String getBookName(int bookNum){
        return mBookList.get(bookNum);
    }

    ArrayList<String> getBookList(){
        return mBookList;
    }

    String getStartingBook() {
        return mStartingBook;
    }

    void setStartingBook(String startingBook) {
        mStartingBook = startingBook;
    }

    String getEndingBook() {
        return mEndingBook;
    }

    void setEndingBook(String endingBook) {
        mEndingBook = endingBook;
    }
}


