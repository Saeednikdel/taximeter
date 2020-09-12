package com.aso.taximeter;


import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.annotation.IntDef;
import android.support.v4.app.ActivityCompat;


public class LocationService extends Service implements LocationListener {
    double loni,lati = 0,speed = 0,b=1,lat22,long22,lat2,long2,lat3,long3;
    double result,result1;
    static double degToRad = Math.PI / 180.0;
    private long startTime = 0L;
    double kiloDouble, timeDouble, enterDouble,priceDouble,baseDouble ;
    private Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    int secs, mini;
    boolean start=true, timeout =true;
    LocationManager locationManager;PowerManager.WakeLock wakeLock;
    public static String str_receiver = "com.aso.location.receiver";
    Intent intent;

    @Override
    public int onStartCommand(Intent intent,int flags, int startId) {
        return START_REDELIVER_INTENT;
    }

    String kiloPrice, timePrice, enterPrice, vibrate, sound, basePrice;
    SharedPreferences shared;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wakeLock.release();
        this.locationManager.removeUpdates(this);
        timeSwapBuff += timeInMilliseconds;
        customHandler.removeCallbacks(updateTimerThread);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        shared = getSharedPreferences("Prefs", Context.MODE_PRIVATE);

        intent = new Intent(str_receiver);
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);

        }
        kiloPrice = shared.getString("kilo", "0");
        timePrice = shared.getString("time", "0");
        enterPrice = shared.getString("enter", "0");
        basePrice = shared.getString("base", "0");
        kiloDouble = Double.parseDouble(kiloPrice);
        baseDouble = Double.parseDouble(basePrice);
        timeDouble = Double.parseDouble(timePrice);
        enterDouble = Double.parseDouble(enterPrice);
        vibrate = shared.getString("vibrate", "0");
        sound = shared.getString("sound", "0");
        PowerManager manager=(PowerManager)getSystemService(POWER_SERVICE);
         wakeLock=manager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"");
        wakeLock.acquire();
    }


    @Override
    public void onLocationChanged(Location location) {

        loni =location.getLongitude();
        lati =location.getLatitude();
        if(start){
            if(Double.parseDouble(vibrate)==1){
                Vibrator v=(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
                v.vibrate(700);
            }

            if(Double.parseDouble(sound)==1){
                MediaPlayer m2 =MediaPlayer.create(getApplicationContext(), R.raw.beep_1sec);
                m2.start();
            }
            startTime = SystemClock.uptimeMillis();
            customHandler.postDelayed(updateTimerThread, 0);
            start=false;
            intent.putExtra("stat","READY");
            sendBroadcast(intent);
        }

        speed = location.getSpeed();
        speed=speed*3.6;
        if(speed>9){
            if(timeout){
                timeSwapBuff += timeInMilliseconds;
                customHandler.removeCallbacks(updateTimerThread);
                timeout =false;
            }
            if (b>1){
                lat22=lat2;
                long22=long2;
            }else{
                lat22=lati;
                long22=loni;
            }

            if (b>0){
                lat2=lati;
                long2=loni;
                b=2;
            }

            lat3=lati;
            long3=loni;
            double phi1 = lat22 * degToRad;
            double phi2 = lat3 * degToRad;
            double lam1 = long22 * degToRad;
            double lam2 = long3 * degToRad;
            result=	6371.01 * Math.acos( Math.sin(phi1) * Math.sin(phi2) + Math.cos(phi1) * Math.cos(phi2) * Math.cos(lam2 - lam1) );
            result1=result1 + result;
            if(result1>baseDouble){
                priceDouble=(result1-baseDouble)*kiloDouble;
                priceDouble=(mini*timeDouble)+priceDouble+enterDouble;
            }else {
                priceDouble=enterDouble+(mini*timeDouble);
            }
            intent.putExtra("speed",String.format("%1.1f", speed));
            intent.putExtra("distance",String.format("%1.3f", result1));
            intent.putExtra("price",String.format("%1.1f", priceDouble));
            sendBroadcast(intent);
        }else{
            if(!timeout){
                startTime = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread, 0);
                timeout =true;
            }
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }


    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private Runnable updateTimerThread = new Runnable() {

        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            secs = (int) (updatedTime / 1000);
            mini = secs / 60;
            secs = secs % 60;
            customHandler.postDelayed(this, 0);
            if(result1>baseDouble){
                priceDouble=(result1-baseDouble)*kiloDouble;
                priceDouble=(mini*timeDouble)+priceDouble+enterDouble;
            }else {
                priceDouble=enterDouble+(mini*timeDouble);
            }
            intent.putExtra("speed",String.format("%1.1f", speed));
            intent.putExtra("distance",String.format("%1.3f", result1));
            intent.putExtra("price",String.format("%1.1f", priceDouble));
            intent.putExtra("time",Integer.toString(mini)+":"+Integer.toString(secs));
            sendBroadcast(intent);
        }
    };

}



