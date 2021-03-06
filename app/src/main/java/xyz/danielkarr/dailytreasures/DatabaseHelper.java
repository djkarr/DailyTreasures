package xyz.danielkarr.dailytreasures;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper mInstance = null;
    private static final int DATABASE_VERSION = 1;
    private static final String DB_NAME = "Bible.db";
    private final String DB_PATH;
    private static Context mContext;
    private SQLiteDatabase myDataBase;

    private final String TAG = "DBHELPER";

    static synchronized DatabaseHelper getInstance(Context ctx) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (mInstance == null) {
            mInstance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    private DatabaseHelper(Context ctx) {
        super(ctx, DB_NAME, null, DATABASE_VERSION);
        DB_PATH = ctx.getApplicationInfo().dataDir + "/databases/" + DB_NAME;
        mContext = ctx;
        Log.i(TAG, "DatabaseHelper: DBPATH " + DB_PATH);
        boolean dbexist = checkdatabase();
        if (dbexist) {
            System.out.println("Database exists");
            opendatabase();
        } else {
            System.out.println("Database doesn't exist");
            try {
                createdatabase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(newVersion>oldVersion){
            try {
                copydatabase();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void createdatabase() throws IOException{
        boolean dbexist = checkdatabase();
        if(dbexist) {
            System.out.println(" Database exists.");
        } else {
            this.getReadableDatabase();
            this.close();
            try {
                copydatabase();
            } catch(IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    private boolean checkdatabase() {
        boolean checkdb = false;
        try{
            String myPath = DB_PATH;
            File dbfile = new File(myPath);
            checkdb = dbfile.exists();
            Log.i(TAG, "checkdatabase: IFDBFILEEXISTS " + checkdb);
        } catch(SQLiteException e) {
            System.out.println("Database doesn't exist");
        }
        return checkdb;
    }

    private void copydatabase() throws IOException {
        Log.i("Database",
                "New database is being copied to device!");
        byte[] buffer = new byte[1024];
        OutputStream myOutput;
        int length;
        // Open your local db as the input stream
        InputStream myInput;
        try
        {
            myInput = mContext.getAssets().open("databases/Bible.db");
            // transfer bytes from the inputfile to the outputfile
            myOutput =new FileOutputStream(DB_PATH);
            while((length = myInput.read(buffer)) > 0)
            {
                myOutput.write(buffer, 0, length);
            }
            myOutput.close();
            myOutput.flush();
            myInput.close();
            Log.i("Database",
                    "New database has been copied to device!");
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    private void opendatabase() throws SQLException {
        //Open the database
        String mypath = DB_PATH;
        myDataBase = SQLiteDatabase.openDatabase(mypath, null, SQLiteDatabase.OPEN_READONLY);
    }
}
