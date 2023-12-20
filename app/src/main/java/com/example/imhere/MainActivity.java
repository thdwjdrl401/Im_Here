package com.example.imhere;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    public static final int HOW_FAR = 200;
    private GoogleMap mMap;
    int repeat = 0;
    String wheel = "null";
    double pos_dist;
    double selected_pos_dist;
    String gps_net = "";
    Long stop_id;
    Long selected_stop_id;
    double stop_lat;
    double selected_stop_lat;
    double stop_lon;
    double selected_stop_lon;
    double my_lon;
    double my_lat;
    String select_ko_name;
    String selected_ko_name;

    String username;
    String checkid = "네트워크 환경이 원활하지 않습니다.";
    private Button button1;
    private Button button2;
    private Button extend_button;
    private Button extend_button2;
    private LinearLayout linear;
    private LinearLayout linear2;
    private Toolbar toolbar;

    private static String IP_ADDRESS = "ec2-52-193-213-157.ap-northeast-1.compute.amazonaws.com";
    private static String TAG = "phptest";
    private TextView txtResult;
    private TextView current_status;
    private TextView mTextViewResult;
    private TextView mTextViewResult2;

    List<Bus_stop> stopList = new ArrayList<Bus_stop>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.app_name);
        setSupportActionBar(toolbar);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        extend_button = (Button) findViewById(R.id.extend_button);
        extend_button2 = (Button) findViewById(R.id.extend_button2);
        linear = (LinearLayout) findViewById(R.id.linear);
        linear2 = (LinearLayout) findViewById(R.id.linear2);
        current_status = (TextView) findViewById(R.id.current_status);
        mTextViewResult = (TextView) findViewById(R.id.textView_main_result);
        mTextViewResult2 = (TextView) findViewById(R.id.check_pos);
        txtResult = (TextView) findViewById(R.id.txtResult);
        mTextViewResult.setMovementMethod(new ScrollingMovementMethod());
        mTextViewResult2.setMovementMethod(new ScrollingMovementMethod());
        txtResult.setMovementMethod(new ScrollingMovementMethod());
        linear.setLayoutParams(new FrameLayout.LayoutParams(extend_button.getMinimumWidth(), extend_button.getMinHeight()));
        get_my_location();
        Bus_stop defalt = new Bus_stop();
        for (int i = 0; i < 5; i++)
            stopList.add(defalt);
        checkPermission();  // 파일 저장 권한 체크 -----------------------------------------------------------------------------
        String line = null;
        String readStr = "";
        File idFile = new File(getFilesDir() + "/userid.txt"); // 저장 경로
        if (!idFile.exists()) {
            check_id();
            Log.d(TAG, "아이디 체크");

        } else {
            try {
                BufferedReader buf = new BufferedReader(new FileReader(idFile));
                while ((line = buf.readLine()) != null) {
                    readStr += line;
                }
                username = readStr;
                Toast.makeText(getApplicationContext(), readStr, Toast.LENGTH_SHORT).show();
                buf.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        check_with_wheelchair();
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                get_my_location();

                Get_5_bus_stop task = new Get_5_bus_stop();
                task.execute("http://" + IP_ADDRESS + "/dist/index.php?lat=" + my_lat + "&lng=" + my_lon);

            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (repeat == 1) {
                    do_0();

                } else {
                    Toast.makeText(getApplicationContext(), "등록된 정류장이 없습니다.", Toast.LENGTH_SHORT).show();
                }

            }
        });
        extend_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                final FrameLayout.LayoutParams param2 = new FrameLayout.LayoutParams(extend_button.getWidth(),extend_button.getHeight());
                if(linear.getLayoutParams().height == FrameLayout.LayoutParams.WRAP_CONTENT){
                    linear.setLayoutParams(param2);
                }
                else{
                    linear.setLayoutParams(param);
                }
            }
        });
        extend_button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linear2.setLayoutParams(new FrameLayout.LayoutParams(0, 0));
            }
        });

    }

    public void get_my_location() {
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        } else {
            //assert lm != null;
            Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (gps_net.equals("gps")) {
                location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } else {
                location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            Log.d(TAG, "fadsfas " + location.getProvider());

            String provider = location.getProvider();

//                    double my_lon_sub = location.getLongitude();
//                    double my_lat_sub = location.getLatitude();
            my_lon = location.getLongitude();
            my_lat = location.getLatitude();
            double altitude = location.getAltitude();

            txtResult.setText("위치정보 : " + provider + "\n" +
                    "위도 : " + my_lon + "\n" +
                    "경도 : " + my_lat + "\n" +
                    "고도  : " + altitude);
            check_position();
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    1000,
                    1,
                    gpsLocationListener);
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    1000,
                    1,
                    gpsLocationListener);
        }
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(infoWindowClickListener);
        final LatLng SEOUL = new LatLng(my_lat, my_lon);


//        MarkerOptions markerOptions = new MarkerOptions();
////        markerOptions.position(SEOUL);
////        markerOptions.title("서울");
////        markerOptions.snippet("한국의 수도");
////        mMap.addMarker(markerOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL, 15));

        Log.d(TAG, "dsa " + mMap.getCameraPosition());
        // mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        Log.d(TAG, "dsa " + mMap.getCameraPosition());

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                Context context = getApplicationContext(); //or getActivity(), YourActivity.this, etc.

                LinearLayout info = new LinearLayout(context);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(context);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(context);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });
        CameraUpdate center = CameraUpdateFactory.newLatLng(marker.getPosition());
        mMap.animateCamera(center);
        mTextViewResult.setText(marker.getTitle()+" "+marker.getSnippet().substring(0, marker.getSnippet().length()-8));
        selected_stop_id = Long.parseLong(marker.getSnippet().substring(0, marker.getSnippet().length()-8));
        selected_ko_name = marker.getTitle();

        selected_stop_lat = marker.getPosition().latitude;
        selected_stop_lon = marker.getPosition().longitude;
        check_selected_position();
        check_position();
        marker.showInfoWindow();

        FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        param.gravity = Gravity.BOTTOM|Gravity.RIGHT;
        linear2.setLayoutParams(param);
        set_mTextViewResult2();
        //changeSelectedMarker(marker);

        return true;
    }

    GoogleMap.OnInfoWindowClickListener infoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            mTextViewResult.setText(marker.getTitle()+" "+marker.getSnippet().substring(0, marker.getSnippet().length()-8));
            stop_id = selected_stop_id;
            select_ko_name = selected_ko_name;

            stop_lat = selected_stop_lat;
            stop_lon = selected_stop_lon;
            Log.d(TAG, "dist test1 - stop_lon - " + stop_lon);
            Log.d(TAG, "dist test1 - stop_lat - " + stop_lat);
            Log.d(TAG, "dist test1 - my_lon - " + selected_stop_id);
            Log.d(TAG, "dist test1 - my_lat - " + selected_ko_name);
//            String markerId = marker.getId();
//            Toast.makeText(MainActivity.this, "정보창 클릭 Marker ID : "+markerId, Toast.LENGTH_SHORT).show();

            id_push();

        }
    };


    final LocationListener gpsLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            Log.d(TAG, "fadsfas -1 " + location.getProvider());

            String provider = location.getProvider();
            gps_net = location.getProvider();
            my_lon = location.getLongitude();
            my_lat = location.getLatitude();
            double altitude = location.getAltitude();
            //Toast.makeText(getApplicationContext(), Double.toString(my_lat) + " "+ my_lon, Toast.LENGTH_LONG).show();

            txtResult.setText("위치정보 : " + provider + "\n" +
                    "위도 : " + my_lon + "\n" +
                    "경도 : " + my_lat + "\n" +
                    "고도  : " + altitude);
            check_position();

        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };


    class Get_5_bus_stop extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected String doInBackground(String... params) {

            //String name = (String)params[1];
            //String country = (String)params[2];

            String serverURL = (String) params[0];
            //String postParameters = "name=" + name + "&country=" + country;
            Log.d(TAG, "어디까지 왔나 0");


            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                Log.d(TAG, "어디까지 왔나 1");

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();
                Log.d(TAG, "어디까지 왔나 2");


                /*
                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();*/


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code --- " + responseStatusCode);

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK || responseStatusCode == HttpURLConnection.HTTP_CREATED) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }


                bufferedReader.close();


                return sb.toString();


            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);

                return new String("Error: " + e.getMessage());
            }

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray stopArray = jsonObject.getJSONArray("distance");
                Log.d(TAG, "asdfsadf " + jsonObject.getString("distance"));

                for (int i = 0; i < stopArray.length(); i++) {
                    JSONObject stopObject = stopArray.getJSONObject(i);
                    Log.d(TAG, "POST response  stopArray- " + stopArray.getString(i));
                    Bus_stop stop = new Bus_stop();

                    stop.setKo_name(stopObject.getString("ko_name"));
                    stop.setStop_id(stopObject.getString("stop_id"));
                    stop.setLatitude(stopObject.getString("latitude"));
                    stop.setLongitude(stopObject.getString("longitude"));
                    stop.setDist(stopObject.getString("dist"));
                    Log.d(TAG, "POST response  0- ");
                    stopList.set(i, stop);
                }
                select_stop();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            progressDialog.dismiss();

//            Bus_stop bus = stopList.get(0);
//            Log.d(TAG, "POST response  0- " + stopList.size());
//            Log.d(TAG, "POST response  1- " + stopList.get(1));
//            Log.d(TAG, "POST response  2- " + stopList.get(2));
//            Log.d(TAG, "POST response  3- " + stopList.get(3));
//            Log.d(TAG, "POST response  4- " + stopList.get(4));
//            //Log.d(TAG, "POST response  5- " + stopList.get(5));
//            //Log.d(TAG, "POST response  6- " + stopList.get(6));
//            //Log.d(TAG, "POST response  7- " + stopList.get(7));

        }

    }


    public class Bus_stop {
        private String ko_name = "네트워크가 원활하지 않습니다.";
        private String stop_id = "네트워크가 원활하지 않습니다.";
        private String latitude = "네트워크가 원활하지 않습니다.";
        private String longitude = "네트워크가 원활하지 않습니다.";
        private String dist = "네트워크가 원활하지 않습니다.";

        public String getKo_name() {
            return ko_name;
        }

        public String getStop_id() {
            return stop_id;
        }

        public String getLatitude() {
            return latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public String getDist() {
            return dist;
        }

        public void setKo_name(String ko_name) {
            this.ko_name = ko_name;
        }

        public void setStop_id(String stop_id) {
            this.stop_id = stop_id;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }

        public void setDist(String dist) {
            this.dist = dist;
        }

    }

    void select_stop() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("정류장을 선택하세요");
        Log.d(TAG, "stop response code stop1- " + stopList.get(0).getDist());
        Log.d(TAG, "stop response code stop2- " + stopList.get(1).getDist());
        Log.d(TAG, "stop response code stop3- " + stopList.get(2).getDist());
        Log.d(TAG, "stop response code stop4- " + stopList.get(3).getDist());
        Log.d(TAG, "stop response code stop5- " + stopList.get(4).getDist());

        for (int i = 0; i < 5; i++) {

            LatLng stop_point = new LatLng(Double.valueOf(stopList.get(i).getLatitude()), Double.valueOf(stopList.get(i).getLongitude()));

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(stop_point);
            markerOptions.title(stopList.get(i).getKo_name());
            markerOptions.snippet(stopList.get(i).getStop_id()+"\n대기열에 등록");
            mMap.addMarker(markerOptions);
        }


        final CharSequence[] items = {stopList.get(0).getKo_name() + " " + stopList.get(0).getStop_id(), stopList.get(1).getKo_name() + " " + stopList.get(1).getStop_id(),
                stopList.get(2).getKo_name() + " " + stopList.get(2).getStop_id(), stopList.get(3).getKo_name() + " " + stopList.get(3).getStop_id(), stopList.get(4).getKo_name() + " " + stopList.get(4).getStop_id()};

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int pos) {
                mTextViewResult.setText(stopList.get(pos).getKo_name());
                stop_id = Long.parseLong(stopList.get(pos).getStop_id());
                Log.d(TAG, "marker -" + stopList.get(pos).getStop_id() + "-" + stop_id);

                select_ko_name = stopList.get(pos).getKo_name();
                stop_lat = Double.parseDouble(stopList.get(pos).getLatitude());
                stop_lon = Double.parseDouble(stopList.get(pos).getLongitude());
                id_push();
            }
        });

        AlertDialog alertDialog = builder.create();
        //builder.show();
    }

    void check_with_wheelchair() {
        final List<String> ListItems = new ArrayList<>();
        ListItems.add("휠체어 사용");
        final CharSequence[] items = ListItems.toArray(new String[ListItems.size()]);

        final List SelectedItems = new ArrayList();
        int defaultItem = 0;
        SelectedItems.add(defaultItem);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("휠체어를 사용중이십니까?");

        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String msg = "";

                        if (!SelectedItems.isEmpty()) {
                            int index = (int) SelectedItems.get(0);
                            msg = ListItems.get(index);
                        }
                        wheel = "1";
                        Toast.makeText(getApplicationContext(),
                                "Items Selected.\n" + msg, Toast.LENGTH_LONG)
                                .show();
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    void check_id() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("id를 만드세요");
        final EditText nametetetet = new EditText(this);
        builder.setView(nametetetet);
        //builder.setMessage("Test for preventing dialog close");
        builder.setPositiveButton("중복체크",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Do nothing here because we override this button later to change the close behaviour.
                        //However, we still need this because on older versions of Android unless we
                        //pass a handler the button doesn't get instantiated
                    }
                });
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = nametetetet.getText().toString();
                Check_Id_In_DB task = new Check_Id_In_DB();
                Log.i(TAG, "task.getStatus() = " + task.getStatus());


                task.execute("http://ec2-52-193-213-157.ap-northeast-1.compute.amazonaws.com/index.php?id=%27" + username + "%27");
                Toast.makeText(getApplicationContext(), username, Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (checkid.equals("중복되지 않은 id\n"))
                            dialog.dismiss();


                    }
                }, 1500);
                Log.i(TAG, "task.getStatus() = " + task.getStatus());

            }
        });


    }

    public void checkPermission() {
        int permissionInfo = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionInfo == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "SDCard 쓰기 권한 있음", Toast.LENGTH_SHORT).show();

        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                Toast.makeText(getApplicationContext(), "권한의 필요성 설명", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    void set_mTextViewResult2() {
        if ((selected_pos_dist > HOW_FAR) && Double.compare(selected_stop_lon, 0) != 0) {
            mTextViewResult2.setText(selected_pos_dist + "m\n떨어져 정류장에 충분히 가깝지 않음");

        }
        if ((selected_pos_dist <= HOW_FAR) && Double.compare(selected_stop_lon, 0) != 0) {
            mTextViewResult2.setText(selected_pos_dist + "m\n떨어져 정류장에 충분히 가까움");
        }
    }


    class Check_Id_In_DB extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(MainActivity.this,
                    "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i(TAG, "checkid = " + checkid.length());
            if (checkid.equals("중복되지 않은 id\n")) { // 중복체크 된 아이디를 파일로 저장-----------------------------------------------------------------------

                try {
                    FileOutputStream outFs = openFileOutput("userid.txt", Context.MODE_PRIVATE);
                    String str = username;
                    outFs.write(str.getBytes());
                    outFs.close();
                } catch (IOException e) {
                }
                Toast.makeText(getApplicationContext(), "userid.txt가 생성됨", Toast.LENGTH_SHORT).show();//--------------------------------------------------

//                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "id 생성 성공", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "중복된 id", Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
            Log.d(TAG, "POST response  - " + result);


        }


        @Override
        protected String doInBackground(String... params) {

            //String name = (String)params[1];
            //String country = (String)params[2];

            String serverURL = (String) params[0];
            //String postParameters = "name=" + name + "&country=" + country;


            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();


                /*
                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();*/


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK || responseStatusCode == HttpURLConnection.HTTP_CREATED) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }


                bufferedReader.close();

                checkid = sb.toString();

                return sb.toString();


            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);

                return new String("Error: " + e.getMessage());
            }

        }
    }

    void id_push() {
        Set_Utilizing_Table task = new Set_Utilizing_Table();
        check_position();
        if ((pos_dist > HOW_FAR)) {

            do_0();

            Toast.makeText(MainActivity.this, "대기열 등록 실패", Toast.LENGTH_SHORT).show();

        } else if (Long.compare(stop_id, 0) != 0 && stop_lat != 0) {
            if (Integer.compare(repeat, 0) == 0) {
                task.execute("http://ec2-52-193-213-157.ap-northeast-1.compute.amazonaws.com/util/index.php?username="
                        + username + "&stop_id=" + stop_id + "&ko_name=" + select_ko_name + "&with_wheel=" + wheel + "&do_what=1");
                current_status.setText("-현재 등록된 bus_stop\n" + select_ko_name + " " + stop_id);
//                Toast.makeText(MainActivity.this, "대기열 등록 성공", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "POST response  - execute " + repeat);

            }

        }
        set_mTextViewResult2();
    }

    void do_0() {
        if (Integer.compare(repeat, 1) == 0) {
            Set_Utilizing_Table task = new Set_Utilizing_Table();

            task.execute("http://ec2-52-193-213-157.ap-northeast-1.compute.amazonaws.com/util/index.php?username=" + username + "&do_what=0");
            current_status.setText("-현재 등록된 bus_stop\n" + "없음");
        }

    }

    void check_selected_position() {
        double X_dist;
        double Y_dist;
        X_dist = (Math.cos(stop_lat) * 6400 * 2 * Math.PI / 360) * Math.abs(selected_stop_lon - my_lon);
        Y_dist = 111 * Math.abs(selected_stop_lat - my_lat);
        selected_pos_dist = Math.sqrt(Math.pow(X_dist, 2) + Math.pow(Y_dist, 2)) * 1000;
    }

    void check_position() {
        check_selected_position();
        double X_dist;
        double Y_dist;
        X_dist = (Math.cos(stop_lat) * 6400 * 2 * Math.PI / 360) * Math.abs(stop_lon - my_lon);
        Y_dist = 111 * Math.abs(stop_lat - my_lat);
        pos_dist = Math.sqrt(Math.pow(X_dist, 2) + Math.pow(Y_dist, 2)) * 1000;
        if ((pos_dist > HOW_FAR) && Integer.compare(repeat, 1) == 0) {
            do_0();
            Toast.makeText(MainActivity.this, "대기열 추방", Toast.LENGTH_SHORT).show();

        }
        set_mTextViewResult2();


        Log.d(TAG, "dist test - stop_lon - " + stop_lon);
        Log.d(TAG, "dist test - stop_lat - " + stop_lat);
        Log.d(TAG, "dist test - my_lon - " + my_lon);
        Log.d(TAG, "dist test - my_lat - " + my_lat);

        Log.d(TAG, "dist test - X_dist - " + X_dist);
        Log.d(TAG, "dist test - Y_dist - " + Y_dist);
        Log.d(TAG, "dist test - pos_dist - " + pos_dist);
        Log.d(TAG, "lat test - stop_lat - " + stop_lat);

    }


    class Set_Utilizing_Table extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(MainActivity.this,
                    "데이터베이스 접근중", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            if(!result.equals("정상 처리되었습니다.")) {
                if (Integer.compare(repeat, 0) == 0) {
                    Toast.makeText(MainActivity.this, "대기열 등록 성공", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "POST response  - if " + repeat);
                    repeat = 1;

                }
                else {
                    Toast.makeText(MainActivity.this, "대기열 취소 성공", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "POST response  - else " + repeat);
                    repeat = 0;

                }
                Log.d(TAG, "POST response  - ㅑㄹ " + repeat);

            }
            else{
                Toast.makeText(MainActivity.this, "처리에 실패하였습니다.", Toast.LENGTH_SHORT).show();

            }

            Log.d(TAG, "POST response  - " + result);
        }


        @Override
        protected String doInBackground(String... params) {

            //String name = (String)params[1];
            //String country = (String)params[2];

            String serverURL = (String) params[0];
            //String postParameters = "name=" + name + "&country=" + country;


            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();


                /*
                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();*/


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK || responseStatusCode == HttpURLConnection.HTTP_CREATED) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }


                bufferedReader.close();

                Log.d(TAG, "sb란무엇인가 "+sb.toString());

                return sb.toString();


            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);

                return new String("Error: " + e.getMessage());
            }

        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch (item.getItemId())
        {
            case R.id.search :

                if(wheel.equals("null")){

                    toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary2));
                    toolbar.getMenu().findItem(R.id.search).setIcon(getResources().getDrawable(R.mipmap.ywheel_foreground));


                    wheel = "1";
                    Toast.makeText(getApplicationContext(), "휠체어 모드", Toast.LENGTH_SHORT).show();

                }
                else{
                    toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                    toolbar.getMenu().findItem(R.id.search).setIcon(getResources().getDrawable(R.mipmap.bwheel2_foreground));

                    wheel = "null";
                    Toast.makeText(getApplicationContext(), "일반 모드", Toast.LENGTH_SHORT).show();
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}