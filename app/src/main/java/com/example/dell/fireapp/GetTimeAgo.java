package com.example.dell.fireapp;

import android.app.Application;
import android.content.Context;

/**
 * Created by DELL on 10/30/2017.
 */

public class GetTimeAgo extends Application {

    private static final int SECOND_MILIS=1000;
    private static final int MINUTES_MILIS=SECOND_MILIS*60;
    private static final int HOUR_MILIS=MINUTES_MILIS*60;
    private static final int DAY_MILIS=HOUR_MILIS*24;

    public static String getTimeAgo(long time, Context ctx){
        if (time<10000000000000L){
            time*=100;
        }
        long now=System.currentTimeMillis();

        if (time>now | time<=0){
            return null;
        }

        final long dif=now-time;
        if (dif<MINUTES_MILIS){
            return "just now";
        }else if (dif<2*MINUTES_MILIS){
            return "a minutes ago";
        }else if (dif<50*MINUTES_MILIS){
            return dif/MINUTES_MILIS +" minutes ago";
        }else if (dif<90*MINUTES_MILIS){
            return "an hour ago";
        }else if (dif<24*HOUR_MILIS){
            return dif/HOUR_MILIS +" hours ago";
        }else if (dif<48*HOUR_MILIS){
            return "yesturday";
        }else {
            return dif/DAY_MILIS +" days ago";
        }
    }

}
