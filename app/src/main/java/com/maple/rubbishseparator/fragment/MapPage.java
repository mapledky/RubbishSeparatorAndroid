package com.maple.rubbishseparator.fragment;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.android.volley.Request;

import com.maple.rubbishseparator.R;
import com.maple.rubbishseparator.network.HttpHelper;
import com.maple.rubbishseparator.network.ServerCode;
import com.maple.rubbishseparator.network.VollySimpleRequest;
import com.maple.rubbishseparator.util.CanUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapPage extends Fragment implements AMap.OnMyLocationChangeListener {
    private View rootview;
    private MapView mapView;
    private AMap aMap;


    //data
    private ArrayList<CanUtil> candata;
    private String latitude;
    private String longitude;


    public MapPage() {
        // Required empty public constructor
    }

    private Context context;

    public void setParams(Context context) {
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (rootview == null) {
            rootview = inflater.inflate(R.layout.fragment_map_page, container, false);
        }
        mapView = rootview.findViewById(R.id.map_page_map);
        mapView.onCreate(savedInstanceState);
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        //?????????????????????
        initMap();//???????????????
        candata = new ArrayList<>();
        return rootview;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            mapView.setVisibility(View.INVISIBLE);
        } else {
            mapView.setVisibility(View.VISIBLE);
        }
    }


    private void initMap() {
        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();//??????????????????????????????myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????1???1???????????????????????????myLocationType????????????????????????????????????
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW);
        myLocationStyle.interval(10000);
        aMap.setMyLocationStyle(myLocationStyle);//?????????????????????Style
        aMap.getUiSettings().setMyLocationButtonEnabled(true);
        aMap.moveCamera(CameraUpdateFactory.zoomTo(16));
        aMap.setMyLocationEnabled(true);// ?????????true?????????????????????????????????false??????????????????????????????????????????????????????false???
        aMap.setOnMyLocationChangeListener(this);
    }

    /**
     * ??????????????????
     */
    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * ??????????????????
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * ??????????????????
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }


    public void getNearBy() {
        Map<String, String> params = new HashMap<>();
        params.put("requestCode", ServerCode.GETLOCATION);
        params.put("latitude", latitude);
        params.put("longitude", longitude);

        VollySimpleRequest.getInstance(context).sendStringRequest(Request.Method.POST, HttpHelper.MAIN_MOBILE, s -> {
            try {
                JSONArray jsonArray = new JSONArray(s);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    CanUtil canUtil = new CanUtil();

                    canUtil.latitude = jsonObject.getString("latitude");
                    canUtil.longitude = jsonObject.getString("longitude");
                    canUtil.name = jsonObject.getString("name");
                    canUtil.dismiss = jsonObject.getString("dismiss");
                    canUtil.Id = jsonObject.getString("Id");
                    boolean same = false;
                    for (int index = 0; index < candata.size(); index++) {
                        CanUtil can_old = candata.get(index);
                        if (can_old.latitude.equals(canUtil.latitude) && can_old.longitude.equals(canUtil.longitude)) {
                            same = true;
                            break;
                        }
                    }
                    if (!same) {
                        candata.add(canUtil);
                        MarkerOptions markerOption = new MarkerOptions();
                        LatLng latLng = new LatLng(Double.parseDouble(canUtil.latitude), Double.parseDouble(canUtil.longitude));
                        markerOption.position(latLng);
                        markerOption.title(canUtil.name);
                        markerOption.draggable(false);//??????Marker?????????
                        View view = LayoutInflater.from(context).inflate(R.layout.marker, mapView, false);
                        markerOption.icon(BitmapDescriptorFactory.fromView(view));
                        // ???Marker????????????????????????????????????????????????????????????
                        markerOption.setFlat(true);//??????marker??????????????????
                        aMap.addMarker(markerOption);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, volleyError -> {

        }, params);
    }

    @Override
    public void onMyLocationChange(Location location) {
        latitude = String.valueOf(location.getLatitude());
        longitude = String.valueOf(location.getLongitude());
        getNearBy();
    }
}