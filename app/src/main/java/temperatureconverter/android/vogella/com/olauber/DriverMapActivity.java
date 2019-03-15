package temperatureconverter.android.vogella.com.olauber;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class DriverMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLocation;
    LocationRequest mLocationRequest;
    private Button mLogout;

    private LinearLayout mcustomerinfo;
    private ImageView mcustomerprofile;
    private TextView mcustomername,mcustomerphone;

    DatabaseReference mcustomerdatabaseref;
    private static final String TAG = "DriverMapActivity";

    private String CustomerID="";

    private FusedLocationProviderClient mFusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mcustomername=findViewById(R.id.customername);
        mcustomerphone=findViewById(R.id.customerphone);
        mcustomerinfo=findViewById(R.id.customerinfo);

        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        String userID=mAuth.getCurrentUser().getUid();

        mcustomerdatabaseref= FirebaseDatabase.getInstance().getReference().child("users").child("Customers").child(userID);

        mcustomerprofile=findViewById(R.id.customerprofileimage);

        mLogout = findViewById(R.id.logout_driver);

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().signOut();
                Intent newintent = new Intent(DriverMapActivity.this, MainActivity.class);
                startActivity(newintent);
                finish();
                return;
            }
        });

        getAssignCustomer();

    }

    private void getAssignCustomer() {

        String userid=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assigncustref=FirebaseDatabase.getInstance().getReference().child("users").child("Drivers").child(userid).child("customerrideId");
        assigncustref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    CustomerID=dataSnapshot.getKey().toString();
                    getassignPickUpLocation();
                    getcustomerinfo();

                }

                }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getcustomerinfo() {

        mcustomerinfo.setVisibility(View.VISIBLE);

        mcustomerdatabaseref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0)
                {
                    Map<String,Object> map=(Map<String,Object>)dataSnapshot.getValue();
                    if (map.get("name")!=null){


                        mcustomername.setText(map.get("name").toString());

                    }

                    if (map.get("phone")!=null){


                        mcustomerphone.setText(map.get("phone").toString());

                    }

                    if (map.get("profileimageurl")!=null)
                    {

                        Glide.with(getApplication()).load(map.get("profileimageurl").toString()).into(mcustomerprofile);


                    }                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getassignPickUpLocation() {
        String userid=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference refofdriver=FirebaseDatabase.getInstance().getReference().child("users").child("Drivers").child(userid).child("customerrideId");
        String key1=refofdriver.getKey();
        DatabaseReference assigncustpickref=FirebaseDatabase.getInstance().getReference().child("customerRequest").child(key1).child("l");
        assigncustpickref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists())
                {
                    List<Object> map=(List<Object>) dataSnapshot.getValue();
                    double locationlng=0;
                    double locationlat=0;

                    if (map.get(0) != null ) {
                        locationlat = Double.parseDouble(map.get(0).toString());

                    }

                    if(map.get(1) != null)
                    {
                        locationlng = Double.parseDouble(map.get(1).toString());
                    }

                    LatLng driverLang=new LatLng(locationlat,locationlng);
                    mMap.addMarker(new MarkerOptions().position(driverLang).title("Customer for you"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        buildGoogleApiClient();
        OnDeviceLocation();
        mMap.setMyLocationEnabled(true);

    }

    private void OnDeviceLocation() {

        Log.d(TAG, "ondevicelocation() is running ");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            Task location1 = mFusedLocationProviderClient.getLastLocation();
                location1.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {

                        if (task.isSuccessful() && task.getResult() != null) {
                            Log.d(TAG, "oncomplete found lcoation");

                            Location currentlocation = (Location) task.getResult();
                            String userid= FirebaseAuth.getInstance().getCurrentUser().getUid();
                            DatabaseReference ref= FirebaseDatabase.getInstance().getReference("DriverAvailable");

                            GeoFire geoFire=new GeoFire(ref);
                            geoFire.setLocation(userid, new GeoLocation(currentlocation.getLatitude(), currentlocation.getLongitude()), new GeoFire.CompletionListener() {
                                @Override
                                public void onComplete(String key, DatabaseError error) {

                                }
                            });


                        } else {

                            Toast.makeText(DriverMapActivity.this, "unable to get lovation", Toast.LENGTH_LONG).show();
                        }

                    }
                });


        } catch (SecurityException e) {
            Toast.makeText(DriverMapActivity.this, "exceptionf found" + e, Toast.LENGTH_SHORT);
        }
    }

    private synchronized void buildGoogleApiClient() {

        mGoogleApiClient=new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {


        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));

        String userid=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference refavl=FirebaseDatabase.getInstance().getReference().child("DriverAvailable");
        DatabaseReference refworking=FirebaseDatabase.getInstance().getReference().child("DriverWorking");
        GeoFire geoFire=new GeoFire(refworking);
        GeoFire geoworking=new GeoFire(refavl);

        switch (CustomerID)
        {
            case "":
                geoworking.removeLocation(userid);
                geoworking.setLocation(userid,new GeoLocation(location.getLatitude(),location.getLongitude()));
                break;

             default:
                 geoFire.removeLocation(userid);
                 geoFire.setLocation(userid,new GeoLocation(location.getLatitude(),location.getLongitude()));
                 break;
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

    //when map is connected we
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocationRequest=new LocationRequest();
        mLocationRequest.setInterval(100);
        mLocationRequest.setFastestInterval(100);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.getFusedLocationProviderClient(this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {


    }

    @Override
    protected void onStop() {
        super.onStop();

       /* String userid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("DriverAvailable");

        GeoFire geoFire=new GeoFire(ref);
        geoFire.removeLocation(userid, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {


            }
        });*/
    }
    }


