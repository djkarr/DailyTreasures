package xyz.danielkarr.dailytreasures;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper mInstance = null;
    private static final int DATABASE_VERSION = 1;
    private static final String DB_NAME = "Bible.db";
    private String DB_PATH;
    private static Context mContext;
    public SQLiteDatabase myDataBase;

    private final String TAG = "DBHELPER";

    public static synchronized DatabaseHelper getInstance(Context ctx) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null) {
            mInstance = new DatabaseHelper(ctx.getApplicationContext());
        }
        return mInstance;
    }

    private DatabaseHelper(Context ctx) {
        super(ctx, DB_NAME, null, DATABASE_VERSION);
        if (android.os.Build.VERSION.SDK_INT >= 17)
            DB_PATH = ctx.getApplicationInfo().dataDir + "/databases/" + DB_NAME;
        else
            DB_PATH = "/data/data/" + ctx.getPackageName() + "/databases/" + DB_NAME;
        this.mContext = ctx;
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

//    public DatabaseHelper(Context context){
//        super(context,DB_NAME,null,1);
//        //TODO SWITCH BACK IF THIS BREAKS
//        //DB_PATH = mContext.getDatabasePath(DB_NAME).getAbsolutePath();
//        if (android.os.Build.VERSION.SDK_INT >= 17)
//            DB_PATH = context.getApplicationInfo().dataDir + "/databases/" + DB_NAME;
//        else
//            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/" + DB_NAME;
//        this.mContext = context;
//        Log.i(TAG, "DatabaseHelper: DBPATH " + DB_PATH);
//        boolean dbexist = checkdatabase();
//        if (dbexist) {
//            System.out.println("Database exists");
//            opendatabase();
//        } else {
//            System.out.println("Database doesn't exist");
//            try {
//                createdatabase();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }




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

    public void createdatabase() throws IOException {
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
        OutputStream myOutput = null;
        int length;
        // Open your local db as the input stream
        InputStream myInput = null;
        try
        {
            myInput = mContext.getAssets().open("databases/Bible.db");
            // transfer bytes from the inputfile to the
            // outputfile
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

    public void opendatabase() throws SQLException {
        //Open the database
        String mypath = DB_PATH;
        //TODO possibly change back to readwrite
        myDataBase = SQLiteDatabase.openDatabase(mypath, null, SQLiteDatabase.OPEN_READONLY);
    }

//    public synchronized void close() {
//        if(myDataBase != null) {
//            myDataBase.close();
//        }
//        super.close();
//    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.disableWriteAheadLogging();
    }

}
