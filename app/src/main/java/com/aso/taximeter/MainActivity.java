package com.aso.taximeter;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetSequence;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.aso.taximeter.R.drawable.marker;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
            ,OnMapReadyCallback  ,SensorEventListener ,LocationListener {
    LocationManager locationManager;
    boolean network_enabled,gps_enabled;
    Location gpsLocation, nwLocation;
    GoogleMap mMap;double lati=0,loni=0; float degree,tilt=0;
    boolean working=false,tilted=false,routed=false,mapLoaded=false; String string_orient,string_map,string_firstRun;
    private SensorManager mSensorManager;
    AppCompatButton start,status;Bitmap steelmaker;
    TextView time,price,dis,speed;MarkerOptions Origin;
    ;Marker markerOrigin=null, markerDest =null;Polyline path=null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("on create","start");

        ViewFlipper vf=(ViewFlipper)findViewById(R.id.flip);
        Log.d("shared ","");

        SharedPreferences shared = getSharedPreferences("Prefs", MODE_PRIVATE);
        string_orient = shared.getString("orientation", "1");
        string_firstRun = shared.getString("firstRun", "0");
        string_map = shared.getString("map", "1");


        if(string_map.equals("1")){
            Log.d("map load","");

            vf.setDisplayedChild(2);
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment);
            mapFragment.getMapAsync(this);
            Log.d("sensor manager ","start");

            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            Log.d("sensor manager ","done");

        }else {vf.setDisplayedChild(1);}

        BitmapDrawable bitmap =(BitmapDrawable)getResources().getDrawable(marker);
        Bitmap b=bitmap.getBitmap();
        steelmaker =Bitmap.createScaledBitmap(b,80,80,false);
        Origin =new MarkerOptions().flat(true)
                .icon(BitmapDescriptorFactory
                        .fromBitmap(steelmaker))
                .anchor(0.5f,0.5f);

        if(string_orient.equals("0")){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        time=(TextView)findViewById(R.id.textTime);
        dis=(TextView)findViewById(R.id.textView16);
        price=(TextView)findViewById(R.id.textView2);
        speed=(TextView)findViewById(R.id.textView9);

        start=(AppCompatButton)findViewById(R.id.button4);
        status=(AppCompatButton)findViewById(R.id.button2);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LocationManager locationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
                boolean isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                if (!isGPSEnable){
                    Toast.makeText(getApplicationContext(), R.string.gpsOff, Toast.LENGTH_LONG).show();

                }else{
                    int locationPermission = ContextCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_FINE_LOCATION);
                    if (locationPermission != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this ,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                                ,99);
                    }else {

                        if(!working){
                            status.setText(R.string.wait);
                            Intent intent = new Intent(MainActivity.this, LocationService.class);
                            startService(intent);
                            start.setText(R.string.stop);
                            working=true;
                            if(string_map.equals("1")){
                                mMap.getUiSettings().setAllGesturesEnabled(false);}

                        }else{
                            Intent intent = new Intent(MainActivity.this, LocationService.class);
                            stopService(intent);
                            start.setText(R.string.start);
                            working=false;
                            status.setText(" ");
                            if(string_map.equals("1")){
                                mMap.getUiSettings().setAllGesturesEnabled(true);}

                        }
                    }

                }
            }
        });
        FloatingActionButton delete =(FloatingActionButton)findViewById(R.id.floatingActionButton);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(markerDest !=null){
                markerDest.remove();}
                if(path!=null){
                path.remove();}
                routed=false;
            }
        });
/*        FloatingActionButton current =(FloatingActionButton)findViewById(R.id.floatingActionButton3);
        current.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!working){
                if(lati>0){
                    getLocation();
                    LatLng search = new LatLng(lati, loni );
                    CameraPosition updateCamera = new CameraPosition.Builder()
                            .target(search).zoom(13f).build();
                    CameraUpdate loc= CameraUpdateFactory.newCameraPosition(updateCamera);
                    mMap.animateCamera(loc);}}
            }
        });*/
        final FloatingActionButton view =(FloatingActionButton)findViewById(R.id.floatingActionButton2);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lati>0){
                if(tilted){
                    tilted=false;
                    tilt=0;
                    view.setImageResource(R.drawable.gps);
                    if(!working){

                        getLocation();
                    LatLng search = new LatLng(lati, loni );
                    CameraPosition updateCamera = new CameraPosition.Builder()
                            .target(search).tilt(tilt).zoom(14f).build();
                    CameraUpdate loc= CameraUpdateFactory.newCameraPosition(updateCamera);
                    mMap.animateCamera(loc);}
                }else{

                    tilted=true;
                    tilt=65;
                    view.setImageResource(R.drawable.compass);
                    if(!working){
                    getLocation();
                    LatLng search = new LatLng(lati, loni );
                    CameraPosition updateCamera = new CameraPosition.Builder()
                            .target(search).tilt(tilt).zoom(14f).build();
                    CameraUpdate loc= CameraUpdateFactory.newCameraPosition(updateCamera);
                    mMap.animateCamera(loc);}
                }}
            }
        });

         final TapTargetSequence sequence = new TapTargetSequence(this)
                .targets(TapTarget.forToolbarNavigationIcon(toolbar,
                        getResources().getString(R.string.menu),
                        getResources().getString(R.string.menuDetail)).id(1)
                                .outerCircleColor(R.color.blue)
                                .cancelable(false)
                                .transparentTarget(true)
                                .dimColor(R.color.black)
                        , TapTarget.forView(findViewById(R.id.floatingActionButton),
                                getResources().getString(R.string.del),
                                getResources().getString(R.string.delDetail))
                                .outerCircleColor(R.color.blue)
                                .cancelable(false)
                                .transparentTarget(true)
                                .dimColor(R.color.black)
                        , TapTarget.forView(findViewById(R.id.floatingActionButton2),
                                getResources().getString(R.string.view),
                                getResources().getString(R.string.viewDetail))
                                .outerCircleColor(R.color.blue)
                                .cancelable(false)
                                .transparentTarget(true)
                                .dimColor(R.color.black)
                        , TapTarget.forView(findViewById(R.id.button4),
                                getResources().getString(R.string.startStop),
                                getResources().getString(R.string.startStopDetail))
                                .outerCircleColor(R.color.blue)
                                .cancelable(false)
                                .transparentTarget(true)
                                .dimColor(R.color.black)
                ) ;
        if(string_firstRun.equals("0")){
            SharedPreferences shared1 = getSharedPreferences("Prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = shared1.edit();
            editor.putString("firstRun","1");
            editor.apply();
            sequence.start();
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("map ready","");

        mMap = googleMap;
        int locationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                    ,99);
        }else{
            if(lati>0){
                getLocation();
                if(lati>0){
            LatLng search = new LatLng(lati, loni );
                Origin.position(search);
                markerOrigin=mMap.addMarker(Origin);
            CameraPosition updateCamera = new CameraPosition.Builder()
                    .target(search).zoom(14f).build();
            CameraUpdate loc= CameraUpdateFactory.newCameraPosition(updateCamera);
            mMap.moveCamera(loc);}}
        }
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                if(!routed){
                    routed=true;
                    getLocation();
                LatLng dest = new LatLng(point.latitude ,  point.longitude);
                MarkerOptions marker =new MarkerOptions().position(dest);
                markerDest =mMap.addMarker(marker);

                LatLng origin = new LatLng(lati , loni);

                String url = getDirectionsUrl(origin, dest);
                if (isNetworkOnline()){
                DownloadTask downloadTask = new DownloadTask();
                downloadTask.execute(url);
                }
                }
            }
        });
        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            public void onMapLoaded() {
                mapLoaded=true;
            }
        });
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            backButtonHandler();
        }
    }
    @Override
    protected void onResume() {
        Log.d("resume","");

        super.onResume();

        registerReceiver(broadcastReceiver, new IntentFilter(LocationService.str_receiver));
        if(string_map.equals("1")){
            int locationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (locationPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                        ,99);
            }else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
            }
            getLocation();
            mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(string_map.equals("1")){
            Log.d("pause","");

            locationManager.removeUpdates(MainActivity.this);
            mSensorManager.unregisterListener(MainActivity.this);}
        unregisterReceiver(broadcastReceiver);
    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("bradcast","");

            if(intent.getStringExtra("speed")!=null){speed.setText(intent.getStringExtra("speed"));}
            if(intent.getStringExtra("distance")!=null){dis.setText(intent.getStringExtra("distance"));}
            if(intent.getStringExtra("time")!=null){ time.setText(intent.getStringExtra("time"));}
            if(intent.getStringExtra("stat")!=null){ status.setText(R.string.ready);}
            if(intent.getStringExtra("price")!=null){ price.setText(intent.getStringExtra("price"));}

        }

    };
    @Override
    public boolean onNavigationItemSelected( MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_gps:
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                break;
            case R.id.nav_manage:
                startActivity(new Intent(MainActivity.this,Setting.class));
                break;
            case R.id.nav_help:
                displayAboutDialog();
                break;
            case R.id.nav_otherapp:
                Intent intent1 = new Intent(Intent.ACTION_VIEW);
             //   intent1.setData(Uri.parse("bazaar://collection?slug=by_author&aid=" + "saeed_nikdel"));
             //   intent1.setPackage("com.farsitel.bazaar");
                	intent1.setData(Uri.parse("http://myket.ir/DeveloperApps.aspx?Packagename=com.aso.taximeter"));
                	intent1.setPackage("ir.mservices.market");
                startActivity(intent1);
                break;
            case R.id.nav_send:
                Intent email=new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{"1991saeed@gmail.com"});
                email.putExtra(Intent.EXTRA_SUBJECT, "TaxiMeter");
                email.setType("message/rfc822");
                startActivity(Intent.createChooser(email, " ارسال ایمیل با : "));
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    public boolean isNetworkOnline(){
        Log.d("network online","");

        ConnectivityManager cm=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info=cm.getActiveNetworkInfo();
        return info != null && info.isConnectedOrConnecting();
    };
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            Intent intent = new Intent(MainActivity.this, LocationService.class);
            stopService(intent);
            dis.setText("0.0");
            time.setText("0.0");
            price.setText("0.0");
            speed.setText("0.0");
            status.setText("");
            start.setText(R.string.start);
            working=false;
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void backButtonHandler() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                MainActivity.this);
        alertDialog.setMessage(R.string.exitMessage);
        alertDialog.setPositiveButton(R.string.exit,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(MainActivity.this, LocationService.class);
                        stopService(intent);
                        if(string_map.equals("1")){
                        mSensorManager.unregisterListener(MainActivity.this);
                        locationManager.removeUpdates(MainActivity.this);}
                        finish();
                    }
                });
        alertDialog.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        alertDialog.show();
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d("sensor change","");
        degree = Math.round(event.values[0]);

        if(mapLoaded){

          //  getLocation();
          if(string_orient.equals("0")){
              degree=degree+90;
          }
          if(working){
              if(lati>0){  LatLng search = new LatLng(lati, loni );
                  if(markerOrigin!=null){
                      markerOrigin.setPosition(search);
                      markerOrigin.setRotation(degree);
                  }
                  CameraPosition updateCamera = new CameraPosition.Builder()
                          .target(search).tilt(tilt).bearing(degree).zoom(15f).build();
                  CameraUpdate loc= CameraUpdateFactory.newCameraPosition(updateCamera);
                  mMap.moveCamera(loc);}
          }else{
              if(lati>0){
                  LatLng search = new LatLng(lati, loni );
                  if(markerOrigin!=null){
                      markerOrigin.setPosition(search);
                      markerOrigin.setRotation(degree);
                  }else {
                      markerOrigin=mMap.addMarker(Origin.position(search).rotation(degree));
                  }
              }

          }
      }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("location change","");

        lati=location.getLatitude();
        loni=location.getLongitude();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int locationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (locationPermission == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);

        }
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();


            parserTask.execute(result);

        }
    }


    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points;
            PolylineOptions lineOptions = new PolylineOptions();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();

                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.RED);
                lineOptions.geodesic(true);

            }

// Drawing polyline in the Google Map for the i-th route
            path=mMap.addPolyline(lineOptions);
        }
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;

        // Output format
        String output = "json";

        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;


        return url;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
    private void getLocation(){

        int locationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}
                    ,99);
        }else {

            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (network_enabled) {
                nwLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (nwLocation != null) {
                    lati = nwLocation.getLatitude();
                    loni = nwLocation.getLongitude();
                }
            }
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (gps_enabled) {
                gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (gpsLocation != null) {
                    lati = gpsLocation.getLatitude();
                    loni = gpsLocation.getLongitude();
                }
            }
        }
    }
    private void displayAboutDialog()
    {
        final LayoutInflater inflator = LayoutInflater.from(this);
        final View settingsview = inflator.inflate(R.layout.aboat, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setView(settingsview);
        builder.setPositiveButton("باشه", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.create().show();
    }
}
