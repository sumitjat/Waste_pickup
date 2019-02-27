package temperatureconverter.android.vogella.com.olauber;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private String DriverFoundID;
    private static final String TAG = "CustomerMapActivity";
private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLocation;
    LocationRequest mLocationRequest;
    private Button mLogout,mrequest;
    private LatLng pickup;
    private Double latitude,longitude;
    GeoLocation mGeoLocation;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLogout=findViewById(R.id.logout_driver);
        mrequest=findViewById(R.id.Call);

        mrequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               OnDeviceLocation();
               mrequest.setText("Getting YOur Driver");
               getClosestDriver();


            }
        });

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseAuth.getInstance().signOut();
                Intent newintent=new Intent(CustomerMapActivity.this,MainActivity.class);
                startActivity(newintent);
                finish();
                return;
            }
        });

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
                        latitude=currentlocation.getLatitude();
                        longitude=currentlocation.getLongitude();

                        mGeoLocation=new GeoLocation(currentlocation.getLatitude(),currentlocation.getLongitude());
                        if(mGeoLocation==null){
                            Toast.makeText(CustomerMapActivity.this,"what do you want bitch",Toast.LENGTH_LONG).show();
                        }

                        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("customerRequest");

                        GeoFire geoFire=new GeoFire(ref);
                        geoFire.setLocation(userid, new GeoLocation(currentlocation.getLatitude(),currentlocation.getLongitude()), new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {

                            }
                        });


                    } else {

                        Toast.makeText(CustomerMapActivity.this, "unable to get lovation", Toast.LENGTH_LONG).show();
                    }

                }
            });


        } catch (SecurityException e) {
            Toast.makeText(CustomerMapActivity.this, "exceptionf found" + e, Toast.LENGTH_SHORT);
        }


    }

    private  double radius=1;
    private boolean DriverFound=false;

    GeoQuery geoQuery;



        private void getClosestDriver () {
try {
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("DriverAvailable");
    GeoFire geoFire = new GeoFire(reference);
    if (mGeoLocation == null) {
        Toast.makeText(CustomerMapActivity.this, "yoooo", Toast.LENGTH_LONG).show();
    } else {
        geoQuery = geoFire.queryAtLocation(mGeoLocation,radius);

        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {
            @Override
            public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {

                if (!DriverFound) {

                    DriverFound = true;
                    DriverFoundID = dataSnapshot.getKey().toString();

                    DatabaseReference driveref=FirebaseDatabase.getInstance().getReference().child("users").child("Drivers").child(DriverFoundID);
                    String userid=FirebaseAuth.getInstance().getCurrentUser().getUid();
                    HashMap hashMap=new HashMap();
                    hashMap.put("customerrideId",userid);

                    driveref.updateChildren(hashMap);

                    getDriverLocation();

                    Toast.makeText(CustomerMapActivity.this, DriverFoundID, Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onDataExited(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {


                if (!DriverFound) {
                    radius++;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {


            }
        });
    }
}
        catch (Exception e){

            Toast.makeText(CustomerMapActivity.this,"error for you bitch"+e,Toast.LENGTH_LONG).show();
        }
    }

    Marker mDriverMarker;

    private void getDriverLocation() {

            DatabaseReference driverlocationref=FirebaseDatabase.getInstance().getReference().child("DriverWorking").child(DriverFoundID).child("l");
            driverlocationref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists())

                    {
                        List<Object> map = (List<Object>) dataSnapshot.getValue();
                        double locationlng = 0;
                        double locationlat = 0;
                        if (map.get(0) != null ) {
                            locationlat = Double.parseDouble(map.get(0).toString());

                        }

                        if(map.get(1) != null)
                        {
                            locationlng = Double.parseDouble(map.get(1).toString());
                        }

                        LatLng driverLang = new LatLng(locationlat, locationlng);
                        if (mDriverMarker != null) {
                            mDriverMarker.remove();
                        }
                        mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLang).title("your Driver"));
                    }
                }
                    @Override
                    public void onCancelled (@NonNull DatabaseError databaseError){


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
        mMap.setMyLocationEnabled(true);

    }

    private synchronized void buildGoogleApiClient() {

        mGoogleApiClient=new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }



    @Override
    public void onLocationChanged(Location location) {

        mLocation=location;
        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));


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


    }
}


