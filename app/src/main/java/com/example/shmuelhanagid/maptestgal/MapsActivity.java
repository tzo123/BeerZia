package com.example.shmuelhanagid.maptestgal;

import android.*;
import android.Manifest;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.vision.Frame;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        OnMarkerClickListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnInfoWindowClickListener


{
    private DatabaseReference mDatabase;

    private GoogleMap mMap;
    public static String theVal = "howdy";
    public static boolean pullDown = false;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
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
        LatLng sydney = new LatLng(-34, 151);
        LatLng test = new LatLng(32.0837936, 34.7801019);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            return;
        }


        mMap.setMyLocationEnabled(true);


        PopulateMapFromDB();
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(test));
        mMap.addMarker(new MarkerOptions().position(test).title(theVal));
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnCameraMoveListener(this);


        LocationManager LM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = LM.getLastKnownLocation(LM.getBestProvider(criteria, false));
        if (location != null)
        {
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()),13));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                    .zoom(16)                   // Sets the zoom
                    //.bearing(90)                // Sets the orientation of the camera to east
                    //.tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        else {
            mMap.moveCamera(CameraUpdateFactory.newLatLng(test));
        }


    }

    private void PopulateMapFromDB() {

        mDatabase = FirebaseDatabase.getInstance().getReference();
        //Log.v("A","Start loading Data");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                for (DataSnapshot curEntry : dataSnapshot.child("pubs").getChildren()) {
                    // Pub name is the key of the db entry, and also the name of the marker.
                    String pubName = curEntry.getKey();
                    // pub location (needs to be parsed, save as "Lat,Long" string
                    String pubLoc = curEntry.child("Location").getValue().toString();
                    LatLng curLLA = stringToLatLng(pubLoc);
                    mMap.addMarker(new MarkerOptions()
                            .position(curLLA)
                            .title(pubName));


                }
                ArrayList<String> tmpAL = (ArrayList<String>) dataSnapshot.child("beers").getValue();
                //Log.v("A","finished loading Data");
                Beer.beerBrands = new String[tmpAL.size()];
                tmpAL.toArray(Beer.beerBrands);



                //Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //Log.w(TAG, "Failed to read value.", error.toException());
            }
        });










    }



    private LatLng stringToLatLng(String pubLoc) {
        String[] latlong =  pubLoc.split(",");
        double latitude = Double.parseDouble(latlong[0]);
        double longitude = Double.parseDouble(latlong[1]);
        return new LatLng(latitude,longitude);
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        marker.setTitle(marker.getTitle());
        //marker.showInfoWindow();
        LinearLayout mapWrapperLayout = (LinearLayout) findViewById(R.id.mapWrapper);

        LinearLayout.LayoutParams mapParams = (LinearLayout.LayoutParams) mapWrapperLayout.getLayoutParams();
        LinearLayout infoLayout = (LinearLayout) findViewById(R.id.infoLayout);
        LinearLayout.LayoutParams infoParams = (LinearLayout.LayoutParams) infoLayout.getLayoutParams();






        if (!pullDown) {
            //infoParams.weight = 2;

            pullDown = true;
            FillPubContent(marker);
            mapParams.height = ConvertDPtoPixel(425);
            infoLayout.setVisibility(View.VISIBLE);
            infoParams.height = ConvertDPtoPixel(125);

        } else {
            mapParams.height = ConvertDPtoPixel(550);
            infoLayout.setVisibility(View.GONE);

            //infoParams.weight = 0;
            ClearContentFromLayout();
            pullDown = false;
        }

        mapWrapperLayout.setLayoutParams(mapParams);
        return true;
    }

    private void ClearContentFromLayout() {

        LinearLayout masterContentLayout = (LinearLayout) findViewById(R.id.scrollContentLayout);

        masterContentLayout.removeAllViewsInLayout();


    }

    private void FillPubContent(Marker marker) {
        // This is the master layout of pub content on click event.
        // all content (=linearLayouts, the "grid") will be added to this layout
        getBeerList(marker.getTitle());
        //ArrayList<Beer> curPubBeerList = getBeerList(marker.hashCode());

    }

    private void UpdateGuiWithBeersFromDB(ArrayList<BeerEntry> curPubBeerList, String markerTitle) {
        LinearLayout masterContentLayout = (LinearLayout) findViewById(R.id.scrollContentLayout);
        TextView tvTitle = (TextView) findViewById(R.id.infoText);
        tvTitle.setText(markerTitle);

        for (BeerEntry curBeerEntry : curPubBeerList){
            //Add a new "Line"
            LinearLayout lLineLayout = new LinearLayout(this);

            LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);

            lParams.gravity = Gravity.CENTER_VERTICAL;

            lLineLayout.setOrientation(LinearLayout.HORIZONTAL);
            lLineLayout.setGravity(Gravity.CENTER);
            lLineLayout.setLayoutParams(lParams);

            // Add appropriate columns for this line

            // column 1: beer brand
            LinearLayout lbrandLayout = new LinearLayout(this);

            LinearLayout.LayoutParams lParamsBrand = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);

            lParamsBrand.weight = 1;

            lbrandLayout.setOrientation(LinearLayout.VERTICAL);
            lbrandLayout.setLayoutParams(lParamsBrand);
            TextView brandTV = new TextView(this);
            brandTV.setGravity(Gravity.CENTER_VERTICAL);

            brandTV.setText(curBeerEntry.brand);
            lbrandLayout.addView(brandTV);


            // column 2: third litter logo
            LinearLayout lthirdIconLayout = new LinearLayout(this);

            LinearLayout.LayoutParams lParams2 = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);

            lParams2.weight = 2;
            lthirdIconLayout.setOrientation(LinearLayout.VERTICAL);
            lParams2.gravity = Gravity.FILL | Gravity.CENTER;

            lthirdIconLayout.setLayoutParams(lParams2);
            ImageView thirdImage = new ImageView (this);
            thirdImage.setImageResource(R.mipmap.beerglass);
//            thirdImage.setImageResource(R.drawable.third256);

            lthirdIconLayout.addView(thirdImage);


            // column 3: third litter price
            LinearLayout lthirdPriceLayout = new LinearLayout(this);

            LinearLayout.LayoutParams lParams3 = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);

            lParams3.weight = 2;
            lParams3.gravity = Gravity.FILL | Gravity.CENTER;

            lthirdPriceLayout.setOrientation(LinearLayout.VERTICAL);
            lthirdPriceLayout.setLayoutParams(lParams3);
            TextView tv1 = new TextView(this);
            tv1.setGravity( Gravity.CENTER);
            tv1.setText(String.valueOf(curBeerEntry.priceThird));
            lthirdPriceLayout.addView(tv1);


            // column 4: pint logo

            LinearLayout lPintIconLayout = new LinearLayout(this);

            LinearLayout.LayoutParams lParams4 = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);

            lParams4.weight = 2;
            lPintIconLayout .setOrientation(LinearLayout.VERTICAL);
            lParams4.gravity = Gravity.FILL | Gravity.CENTER;

            lPintIconLayout .setLayoutParams(lParams4);
            ImageView pintImage = new ImageView (this);
            pintImage.setImageResource(R.mipmap.beerglass);
//            pintImage.setImageResource(R.drawable.half512);

            lPintIconLayout.addView(pintImage);

            // column 5: pint price

            LinearLayout lPintPriceLayout = new LinearLayout(this);

            LinearLayout.LayoutParams lParams5 = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);

            lParams5.weight = 2;
            lParams5.gravity = Gravity.BOTTOM;
            lPintPriceLayout.setOrientation(LinearLayout.VERTICAL);
            lPintPriceLayout.setLayoutParams(lParams5);
            TextView tv2 = new TextView(this);
            tv2.setGravity(Gravity.BOTTOM);
            tv2.setText(String.valueOf(curBeerEntry.priceHalf));
            lPintPriceLayout.addView(tv2);
            // column 6: half litter logo

            // column 7: half litter price





            lLineLayout.addView(lbrandLayout);
            lLineLayout.addView(lthirdIconLayout);
            lLineLayout.addView(lthirdPriceLayout);
            lLineLayout.addView(lPintIconLayout);
            lLineLayout.addView(lPintPriceLayout);



            //
            masterContentLayout.addView(lLineLayout);






        }
    }

    private int ConvertDPtoPixel(int yourdpmeasure) {
        Resources r = this.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                yourdpmeasure,
                r.getDisplayMetrics());
    }

    public ArrayList<Beer> beerList = new ArrayList<>();
    private void getBeerList(final String markerTitle) {


        beerList.clear();

        Log.v("A","before add listener");


        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                // Get List of Pub Beers.
                ArrayList<String> tapBeers = (ArrayList<String>) dataSnapshot.child("pubs").child(markerTitle)
                        .child("Tap").getValue();
                for (String beerStr : tapBeers) {
                    Beer curTapBeer = new Beer(beerStr);
                    beerList.add(curTapBeer);

                }
                Log.v("A","Finished inside listener");
                ArrayList<BeerEntry> beerEntriesTmp = consolidateBeerList(beerList);
                UpdateGuiWithBeersFromDB(beerEntriesTmp,markerTitle);
            }

            //Log.d(TAG, "Value is: " + value);
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                //Log.w(TAG, "Failed to read value.", error.toException());
                error.toString();
            }
        });









        /*Beer ex1 = new Beer(eBrandName.Goldstar, new Price(25), new BeerSize(eQuantity.HalfLiter));
        beerList.add(ex1);
        Beer ex2 = new Beer(eBrandName.Goldstar, new Price(22), new BeerSize(eQuantity.ThirdLiter));
        beerList.add(ex2);*/





    }



    private ArrayList<BeerEntry> consolidateBeerList(ArrayList<Beer> beerList) {
        ArrayList<BeerEntry> entriesList = new ArrayList<>();
        BeerEntry tmpEntry;

        for (Beer curBeer : beerList) {
            if (ContainsBrand(entriesList, curBeer.brand)) {
                entriesList = UpdateEntries(entriesList, curBeer);
            } else {
                tmpEntry = new BeerEntry(curBeer);
                entriesList.add(tmpEntry);
            }
        }

        return entriesList;
    }


    private ArrayList<BeerEntry> UpdateEntries(ArrayList<BeerEntry> entriesList, Beer curBeer) {
        ArrayList<BeerEntry> updatedList = new ArrayList<>();
        for (BeerEntry entry : entriesList) {
            if (entry != null && entry.brand.equals(curBeer.brand)) {
                entry.FillEntryUsingBeerObject(curBeer);
            }
            updatedList.add(entry);
        }

        return updatedList;
    }




    private static boolean ContainsBrand(ArrayList<BeerEntry> entriesList, String wantedBrand) {

        for (BeerEntry entry : entriesList) {
            if (entry != null && entry.brand.equals(wantedBrand)) {
                return true;
            }
        }
        return false;

    }







    @Override
    public void onInfoWindowClick(Marker marker) {
        theVal = "Deleted.";

    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.shmuelhanagid.maptestgal/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.shmuelhanagid.maptestgal/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    public void mapSearchBeerClick(View view) {
        Intent myIntent = new Intent(MapsActivity.this,SearchBeer.class);
        MapsActivity.this.startActivity(myIntent);


    }

    @Override
    public void onCameraMove() {
        theVal = mMap.getCameraPosition().toString();

    }
}





