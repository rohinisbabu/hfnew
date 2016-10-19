package com.healthyfish.healthyfish.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.healthyfish.healthyfish.R;
import com.healthyfish.healthyfish.api.Url;
import com.healthyfish.healthyfish.dialogbox.CustomProgressDialog;
import com.healthyfish.healthyfish.iterface.HealthyFishApi;
import com.healthyfish.healthyfish.manager.ConnectionDetector;
import com.healthyfish.healthyfish.manager.PrefManager;
import com.healthyfish.healthyfish.manager.SetBagdeCount;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by User on 13-10-2016.
 */
public class DeliveryLocationFragment extends Fragment {
    EditText ed_location;
    Button btn_location;
    RelativeLayout rel_title,rel_content,fdl_rel_no_net,fdl_rel_net;
    TableLayout tbl_disp_location;
    String pin_code;
    TextView tv_pin_disp_title;
    ConnectionDetector cd;
    private PrefManager pref;
    CustomProgressDialog loading;
    public  String ROOT_URL = new Url().base_link;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootview=inflater.inflate(R.layout.fragment_delivery_location,container,false);
        ed_location=(EditText)rootview.findViewById(R.id.fdl_ed_loc_search);
        btn_location=(Button)rootview.findViewById(R.id.fdl_btn_loc_search);
        tv_pin_disp_title=(TextView)rootview.findViewById(R.id.fdl_tv_loc_display_title);
        rel_title=(RelativeLayout)rootview.findViewById(R.id.fdl_rel_loc_search);
        rel_content=(RelativeLayout)rootview.findViewById(R.id.fdl_rel_loc_display);
        tbl_disp_location=(TableLayout)rootview.findViewById(R.id.fdl_tbl_loc_display_content);
        fdl_rel_no_net=(RelativeLayout)rootview.findViewById(R.id.fdl_rel_no_net);
        fdl_rel_net=(RelativeLayout)rootview.findViewById(R.id.fdl_rel_net);
        cd= new ConnectionDetector(getActivity());
        getActivity().setTitle(getArguments().getString("title"));
        loading  = new CustomProgressDialog(getActivity());
        pref=new PrefManager(getActivity());
        btn_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pin_code=ed_location.getText().toString().trim();
                if(cd.isConnectingToInternet()) {
                    fdl_rel_no_net.setVisibility(View.GONE);
                    fdl_rel_net.setVisibility(View.VISIBLE);
                    loading.showDialog();
                    getPincodeLocation(pin_code);
                }
                else
                {
                    fdl_rel_net.setVisibility(View.GONE);
                    fdl_rel_no_net.setVisibility(View.VISIBLE);

                }
            }
        });
        return rootview;
    }
    public void getPincodeLocation(final String pincode)
    {
      String req_id=  pref.receivedSession();
        System.out.println("REQID:"+req_id);
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(ROOT_URL)
                .build();

        //Creating an object of our api interface
        HealthyFishApi api = adapter.create(HealthyFishApi.class);

        //Defining the method
        if (getActivity()!=null) {
            api.pincodeDetailsRequest(req_id,pincode,  //Creating an anonymous callback
                    new Callback<Response>() {
                        @Override
                        public void success(Response result, Response response) {
                            System.out.print("Response got from server::" + response);
                            //On success we will read the server's output using bufferedreader
                            //Creating a bufferedreader object
                            BufferedReader reader = null;
                            //An string to store output from the server
                            String output = "";
                            //loading.dismiss();

                            try {
                                //Initializing buffered reader
                                reader = new BufferedReader(new InputStreamReader(result.getBody().in()));

                                //Reading the output in the string
                                output = reader.readLine();
                            } catch (IOException e) {
                                loading.dismissDialog();
                                System.out.print("Result Exp is IO ::" + e);
                            }

                            try {
                                JSONObject jobj = new JSONObject(output);
                                System.out.print("Result from delivery JOBJ ISssss::: " + jobj);
                                String response_String = jobj.getString("response_string");
                                String response_Code = jobj.getString("response_code");
                                if (response_Code.equals("200") && response_String.equalsIgnoreCase("success")) {
                                  String  shoping_count = jobj.getString("cart");
                                    new SetBagdeCount().setBadgeCount(shoping_count);

                                    JSONObject jobj_response = jobj.getJSONObject("response");
                                    System.out.print("Result response from delivery JOBJ IS::: " + jobj_response);
                                  loading.dismissDialog();
                                    if (jobj_response.has("delivery"))
                                    {
                                        JSONArray jsonArraypin = jobj_response.getJSONArray("delivery");
                                        if (jsonArraypin.length() == 0 || jsonArraypin == null) {
                                            rel_content.setVisibility(View.VISIBLE);
                                            tv_pin_disp_title.setTextColor(Color.RED);
                                            tv_pin_disp_title.setText(getResources().getString(R.string.delivery_location_not_found)+pin_code);
                                            tbl_disp_location.setVisibility(View.GONE);
                                        } else {
                                            tv_pin_disp_title.setTextColor(getResources().getColor(R.color.colorPrimary));
                                            rel_content.setVisibility(View.VISIBLE);
                                            tbl_disp_location.setVisibility(View.VISIBLE);
                                            tv_pin_disp_title.setText(getResources().getString(R.string.delivery_location_found)+pincode);
                                            BuildTable(jsonArraypin.length(),1,jsonArraypin);
                                        }
                                    }
                                    else
                                    {
                                        loading.dismissDialog();
                                        rel_content.setVisibility(View.VISIBLE);
                                        tbl_disp_location.setVisibility(View.GONE);
                                    }
                                }
                                else if (response_Code.equals("201") && response_String.equalsIgnoreCase("empty"))
                                {
                                    loading.dismissDialog();
                                    rel_content.setVisibility(View.VISIBLE);
                                    tv_pin_disp_title.setText(getResources().getString(R.string.delivery_location_not_found)+pin_code);
                                    tv_pin_disp_title.setTextColor(Color.RED);
                                    tbl_disp_location.setVisibility(View.GONE);
                                }
                                else
                                {
                                    loading.dismissDialog();
                                    rel_content.setVisibility(View.GONE);
                                    tbl_disp_location.setVisibility(View.GONE);
                                    Toast.makeText(getActivity(),"Invalid User",Toast.LENGTH_SHORT);
                                }
                            } catch (JSONException e) {
                                loading.dismissDialog();
                                System.out.println("Result delivery Exp issssss" +e);
                            }

                        }


                        @Override
                        public void failure(RetrofitError error) {
                            loading.dismissDialog();
                            if(error.toString().contains("java.io.EOFException")){
                                getPincodeLocation(pincode);
                            }else{
                                if (error.getCause().toString().contains("java.io.EOFException"))
                                {
                                    getPincodeLocation(pincode);}
                                else {
                                    System.out.println("Error" + error.getCause());
                                    System.out.println("Error" + error.getMessage());
                                    System.out.println("Error" + error.getBody());
                                    System.out.println("Error" + error.getLocalizedMessage());
                                }
                            }
                        }
                    }
            );
         }
    }
    private void BuildTable(int rows, int cols,JSONArray jsonArraypin) throws JSONException {
        int ob;
        int lng=jsonArraypin.length();
        System.out.println("JsonArray:"+jsonArraypin);
        System.out.println("JSON ARRAY LENGTH"+lng);
        System.out.println("JSON ARRAY 45 th value: "+jsonArraypin.getJSONObject(44));
        System.out.println("JSON ARRAY 45 th value: "+jsonArraypin.getJSONObject(44).get("delivery_area"));
        // outer for loop
        for (int i = 1; i <= rows; i++) {
            ob=i-1;
            JSONObject jsonObject =jsonArraypin.getJSONObject(ob) ;

            TableRow row = new TableRow(getActivity());
            row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                    TableRow.LayoutParams.WRAP_CONTENT));

            // inner for loop
            for (int j = 1; j <= cols; j++) {

                TextView tv = new TextView(getActivity());
                tv.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
                tv.setPadding(5, 5, 5, 5);
                tv.setText(jsonObject.getString("delivery_area"));

                row.addView(tv);

            }

            tbl_disp_location.addView(row);

        }
    }
}
