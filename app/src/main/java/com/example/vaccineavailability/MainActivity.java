package com.example.vaccineavailability;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    private EditText mPincode;
    private Button mSearchBtn;
    RecyclerView recyclerRV;
    ArrayList<CenterModel> centerModelList;
    CenterAdapter centerAdapter;
    ProgressBar loadingPB;
    LinearLayoutManager layoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPincode = findViewById(R.id.idEdtPinCode);
        mSearchBtn = findViewById(R.id.idBtnSearch);
        loadingPB = findViewById(R.id.idPBLoading);
        recyclerRV = findViewById(R.id.centersRV);
        centerModelList = new ArrayList<CenterModel>();

        mSearchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pincode = mPincode.getText().toString();
                if(pincode.length() != 6 ){
                    Toast.makeText(MainActivity.this, "Please Enter a Vaild Pincode", Toast.LENGTH_SHORT).show();
                }
                else{
                    centerModelList.clear();

                    final Calendar c = Calendar.getInstance();
                    int mYear = c.get(Calendar.YEAR);
                    int mMonth = c.get(Calendar.MONTH);
                    int mDay = c.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                            new DatePickerDialog.OnDateSetListener() {

                                @Override
                                public void onDateSet(DatePicker view, int year,
                                                      int monthOfYear, int dayOfMonth) {
                                        loadingPB.setVisibility(View.VISIBLE);
                                        String dateStr = dayOfMonth+"-"+(monthOfYear+1)+"-"+year;
                                        getAppointement(pincode,dateStr);
                                        Log.v("something",dateStr);
                                }
                            }, mYear, mMonth, mDay);
                    datePickerDialog.show();
                }
            }
        });
    }


    private void getAppointement(String pinCode,String date){
        String url = "https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByPin?pincode=" +pinCode+ "&date=" + date;
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    loadingPB.setVisibility(View.GONE);
                    JSONArray centreArray = response.getJSONArray("centers");
                    Log.v("something", String.valueOf(centreArray.length()));
                    if(centreArray.length() == 0) {
                        Toast.makeText(MainActivity.this, "No vaccination available", Toast.LENGTH_SHORT).show();
                    }
                    Log.v("something", String.valueOf(centreArray));
                    for(int i=0;i<centreArray.length();i++) {
                        JSONObject centreObj = centreArray.getJSONObject(i);
                        String centreName = centreObj.getString("name");
                        String fee_type = centreObj.getString("fee_type");
                        JSONObject sessionObj = centreObj.getJSONArray("sessions").getJSONObject(0);
                        int availableCapacity = sessionObj.getInt("available_capacity");
                        int ageLimit = sessionObj.getInt("min_age_limit");
                        String vaccineName = sessionObj.getString("vaccine");

                        Log.v("something", String.valueOf(pinCode));
                        CenterModel center = new CenterModel(
                                centreName,
                                fee_type,
                                ageLimit,
                                vaccineName,
                                availableCapacity
                        );
                        centerModelList.add(center);

                    }

                    layoutManager = new LinearLayoutManager(MainActivity.this);
                    layoutManager.setOrientation(RecyclerView.VERTICAL);
                    recyclerRV.setLayoutManager(layoutManager);
                    centerAdapter = new CenterAdapter(centerModelList,MainActivity.this);
                    recyclerRV.setAdapter(centerAdapter);
                    centerAdapter.notifyDataSetChanged();

                }catch (Exception e) {
                    loadingPB.setVisibility(View.GONE);
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loadingPB.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Unable to Fetch Data\nCheck your wifi connection", Toast.LENGTH_LONG).show();
            }
        });
        queue.add(jsonObjectRequest);
    }

}