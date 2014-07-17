package com.holandago.wbamanager.ordersmanager;

import android.os.SystemClock;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by maestro on 06/07/14.
 */


public class SecureDate {


    //Date sync with server
    private Date mServerDate;

    //Number of millisecond since boot
    private long mElapsedRealtime;

    //Singleton instance
    private static final SecureDate INSTANCE = new SecureDate();

    /**
     * Get the singleton instance of this class
     * @return the unique instance of this class
     */
    public static SecureDate getInstance() {
        return INSTANCE;
    }

    public long getTime(){
        long current = 0L;
        if(mServerDate == null){
            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Europe/Berlin"),Locale.GERMANY);
            current = now.getTimeInMillis();
        } else {
            mServerDate.setTime(mServerDate.getTime()
                    + (SystemClock.elapsedRealtime() - mElapsedRealtime));
            current = mServerDate.getTime();
        }
        return current;
    }
    /**
     * Method used to obtain the real date based on the server date
     * if the server date was sync
     * @return the actual secure time
     */
    public Date getDate() {
        Date current = mServerDate;
        if (current == null) {
            current = Calendar.getInstance(Locale.GERMAN).getTime();
        } else {
            current.setTime(current.getTime()
                    + (SystemClock.elapsedRealtime() - mElapsedRealtime));
        }
        return current;
    }

    /**
     * Method used to know if the date is sync with the server
     * @return true if the serverDate is sync false otherwise
     */
    public boolean isSyncDate(){
        return mServerDate != null;
    }

    /**
     * Method used to init the server date
     * @param pServerDate the sync server date
     */
    public void initServerDate(final Date pServerDate) {
        mElapsedRealtime = SystemClock.elapsedRealtime();
        mServerDate = pServerDate;
    }

}

