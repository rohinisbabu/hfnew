package com.healthyfish.healthyfish.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ScrollingView;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.billionbeatsinfotech.mylibrary.Animations.DescriptionAnimation;
import com.billionbeatsinfotech.mylibrary.SliderLayout;
import com.billionbeatsinfotech.mylibrary.SliderTypes.BaseSliderView;
import com.billionbeatsinfotech.mylibrary.SliderTypes.TextSliderView;
import com.healthyfish.healthyfish.R;
import com.healthyfish.healthyfish.adapter.CustomSpinnerAdapter;
import com.healthyfish.healthyfish.adapter.ImageSlideAdapter;
import com.healthyfish.healthyfish.api.Url;
import com.healthyfish.healthyfish.dialogbox.AlertBox;
import com.healthyfish.healthyfish.dialogbox.CustomProgressDialog;
import com.healthyfish.healthyfish.iterface.HealthyFishApi;
import com.healthyfish.healthyfish.iterface.PageIndicator;
import com.healthyfish.healthyfish.manager.ConnectionDetector;
import com.healthyfish.healthyfish.manager.PrefManager;
import com.healthyfish.healthyfish.manager.SetBagdeCount;
import com.healthyfish.healthyfish.model.HomeListItem;
import com.healthyfish.healthyfish.model.Product;
import com.healthyfish.healthyfish.utils.CirclePageIndicator;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;


/**
 * Created by RohiniAjith on 6/8/2016.
 */
public class Home_Details_Fragment extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public  String ROOT_URL = new Url().base_link;
    FragmentActivity activity;
    ImageView imageDisplay;
    TextView title,description,actual_price,sales_price,decrease,quantity,increase,add,recipe_title,recipe_content;
    Spinner quantity_type;
    int increase_i=1;
    String type_gram_id="0",product_id;
    PrefManager prefManager;
    RelativeLayout rece_rel,fdh_rel_nonet;
    ConnectionDetector cd;
    CustomProgressDialog loading;
    ScrollView fdh_scroll;
    int check=0;
    Toolbar tolToolbar;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setContentView(R.layout.fragment_details_home);
        imageDisplay= (ImageView) findViewById(R.id.fragment_details_home_imageview);
        title=(TextView)findViewById(R.id.category_details_title_text);
        description=(TextView)findViewById(R.id.category_details_title_text_des);
        actual_price=(TextView)findViewById(R.id.category_iten_offer_price);
        sales_price=(TextView)findViewById(R.id.category_iten_original_price);
        quantity_type=(Spinner)findViewById(R.id.fragment_details_home_spinner);
        decrease=(TextView)findViewById(R.id.rel_hhrvi_tv_no_decrease);
        quantity=(TextView)findViewById(R.id.rel_hhrvi_tv_no_value);
         rece_rel=(RelativeLayout)findViewById(R.id.category_details_recepies_title);
        increase=(TextView)findViewById(R.id.rel_hhrvi_tv_no_increase);
        add=(TextView)findViewById(R.id.fragment_details_home_add);
        recipe_title=(TextView)findViewById(R.id.category_details_title_text_recepies);
        recipe_content=(TextView)findViewById(R.id.category_details_title_text_recepies_ingradients_des);
        fdh_rel_nonet=(RelativeLayout)findViewById(R.id.fdh_rel_nonet);
        tolToolbar=(Toolbar)findViewById(R.id.lt_toolbar);
        tolToolbar.setTitleTextColor(getResources().getColor(R.color.windowBackground));
        setSupportActionBar(tolToolbar);
        fdh_scroll=(ScrollView)findViewById(R.id.fdh_scroll) ;
        prefManager=new PrefManager(Home_Details_Fragment.this);
        cd= new ConnectionDetector(Home_Details_Fragment.this);
        loading  = new CustomProgressDialog(Home_Details_Fragment.this);

        Intent in=getIntent();
        product_id=in.getStringExtra("prod_id");
        String details_title=in.getStringExtra("title");
        getSupportActionBar().setTitle(details_title);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        quantity_type.setOnItemSelectedListener(this);
        decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (increase_i>0)
                    increase_i--;
                quantity.setText(String.valueOf(increase_i));
            }
        });
        increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increase_i++;
                quantity.setText(String.valueOf(increase_i));
            }
        });



        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (type_gram_id.equals("0"))
                    Toast.makeText(Home_Details_Fragment.this,"Please select the quantiy",Toast.LENGTH_SHORT).show();
                else
                    sendDatatoCart();
            }
        });
        if(cd.isConnectingToInternet()) {

            fdh_rel_nonet.setVisibility(View.GONE);
            fdh_scroll.setVisibility(View.VISIBLE);

            loading.showDialog();
            getHomeDetails();
        }
        else
        {
            fdh_scroll.setVisibility(View.GONE);
            fdh_rel_nonet.setVisibility(View.VISIBLE);

        }

    }


    public void getHomeDetails()
    {
//    loading.dismiss();
        //Creating a rest adapter
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(ROOT_URL)
                .build();

        //Creating an object of our api interface
        HealthyFishApi api = adapter.create(HealthyFishApi.class);

        //Defining the method
        if (Home_Details_Fragment.this!=null) {
            api.categoryDetailsRequest(prefManager.receivedSession(),product_id,  //Creating an anonymous callback
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
                                System.out.print("Result Exp is IO ::" + e);
                            }
                            try {
                                System.out.println("Result is:: " + output);
                                JSONObject jobj = new JSONObject(output);
                                System.out.print("Result from Home JOBJ IS::: " + jobj);
                                if (jobj.getString("response_code").equals("200")&&jobj.getString("response_string").equals("success")) {
                                    JSONArray price_category_details = jobj.getJSONArray("price_category_details");
                                    System.out.println("JSON price_category_details: "+price_category_details);
                                    CustomSpinnerAdapter customAdapter=    setListData(price_category_details);
                                    quantity_type.setAdapter(customAdapter);
                                    customAdapter.notifyDataSetChanged();
                                    String cart=jobj.getString("cart");
                                    new SetBagdeCount().setBadgeCount(cart);
                                    loading.dismissDialog();
                                    JSONArray jsonArraybanner = jobj.getJSONArray("response");
                                        System.out.println("Json array " + jsonArraybanner.getString(0));
                                        JSONObject bannerobj = jsonArraybanner.getJSONObject(0);
                                        product_id = bannerobj.getString("prod_id");
                                        String mobile_title = bannerobj.getString("mobile_title");
                                        String category_id = bannerobj.getString("category_id");
                                        String malayalam_title = bannerobj.getString("malayalam_title");
                                        title.setText(mobile_title+"("+malayalam_title+")");
                                        String descrip = bannerobj.getString("product_des");
                                        description.setText(descrip);
                                        String product_image = bannerobj.getString("product_image");
                                        Picasso.with(Home_Details_Fragment.this)
                                            .load(new Url().home_img_url+product_image)
                                            .placeholder(R.drawable.logo)   // optional
                                            .error(R.drawable.logo)      // optional
                                            // optional
                                            .into(imageDisplay);
                                        String offer_price = bannerobj.getString("offer_price");
                                        actual_price.setText("Rs"+offer_price);

                                        String actual_price = bannerobj.getString("actual_price");
                                        sales_price.setText("Rs"+actual_price);
                                        String rec_title = bannerobj.getString("recipes_title");
                                        if (rec_title.equals("null")) {
                                            rece_rel.setVisibility(View.GONE);
                                            recipe_title.setText("");
                                        }
                                        else
                                        {
                                            rece_rel.setVisibility(View.VISIBLE);
                                            recipe_title.setText(rec_title);
                                        }
                                        String res_des = bannerobj.getString("recipes_description");
                                        if (res_des.equals("null"))
                                        {
                                            rece_rel.setVisibility(View.GONE);
                                            recipe_content.setText("");
                                        }
                                        else
                                        {
                                            rece_rel.setVisibility(View.VISIBLE);
                                            recipe_content.setText(res_des);
                                        }




//                                        String product_image = bannerobj.getStrng("product_image");

                                }




                            } catch (JSONException e) {
                                loading.dismissDialog();
                                System.out.println("Result Exp iss" + e);
                            }
                        }

                        @Override
                        public void failure(RetrofitError error) {
                            loading.dismissDialog();
                            System.out.println("Result Exp iss" + error.getMessage());
                        }
                    }
            );
        }

    }
    public CustomSpinnerAdapter setListData(JSONArray price_category_details) throws JSONException {
        int size=price_category_details.length()+1;
        String cat_name[]=new String[size];
        String cat_ids[] =new String[size];


        // Now i have taken static values by loop.
        // For further inhancement we can take data by webservice / json / xml;

        for (int j=0;j<size;j++)
        {

        if (j==0)
        {
            cat_name[j]="Select";
            cat_ids[j]="0";
        }
        else
        {
            String price_cat_id=price_category_details.getJSONObject(j-1).getString("price_cat_id");
            String price_cat_name=price_category_details.getJSONObject(j-1).getString("price_cat_name");
            cat_name[j]=price_cat_name;
            cat_ids[j]=price_cat_id;
        }


        }

        CustomSpinnerAdapter customAdapter=new CustomSpinnerAdapter(Home_Details_Fragment.this,cat_ids,cat_name);

       return customAdapter;



    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        System.out.println("Type_gram_id check"+check);
        check=check+1;
        if(check>1) {
            System.out.println("Type_gram_id check Inside");
             type_gram_id = ((TextView) view.findViewById(R.id.text_price_cat_id)).getText().toString();

            //String type_gram_id = cat_ids[position];
            System.out.println("Type_gram_id" + type_gram_id);
       }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
    public void sendDatatoCart(){
        System.out.println("Cost Before: "+sales_price.getText().toString());
        String cost=sales_price.getText().toString().substring(2);
        System.out.println("Cost: "+cost);
        final CustomProgressDialog customProgressDialog=  new CustomProgressDialog(Home_Details_Fragment.this);
        customProgressDialog.showCartDialog();
        System.out.println("add");
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(new Url().base_link)
                .build();

        //Creating an object of our api interface
        HealthyFishApi api = adapter.create(HealthyFishApi.class);


        //Defining the method
        api.sendShopCart(prefManager.receivedSession(),prefManager.receivedSession(),product_id,String.valueOf(increase_i) ,type_gram_id,cost,  //Creating an anonymous callback
                new Callback<Response>() {
                    @Override
                    public void success(Response result, Response response) {
                        //On success we will read the server's output using bufferedreader
                        //Creating a bufferedreader object
                        BufferedReader reader = null;

                        //An string to store output from the server
                        String output = "";

                        try {
                            //Initializing buffered reader
                            reader = new BufferedReader(new InputStreamReader(result.getBody().in()));

                            //Reading the output in the string
                            output = reader.readLine();
                        } catch (IOException e) {
                            System.out.println("Result Exp is IO ::" + e);
                        }
                        try {
                            System.out.println("Result is:: " + output);
                            JSONObject jobj = new JSONObject(output);
                            System.out.println("Result JOBJ IS::: " + jobj);
                            String response_code = jobj.getString("response_code");
                            String response_msg = jobj.getString("response_string");
                            customProgressDialog.dismissDialog();
                            if(response_code.equals("200")&&response_msg.equals("success"))
                            {
                                new AlertBox(Home_Details_Fragment.this).alertSuccessShopCart(Home_Details_Fragment.this);
                                String count=jobj.getString("cart");
                                new SetBagdeCount().setBadgeCount(count);

                            }
                            else if(response_code.equals("201")&& response_msg.equals("failed"))
                            {
                                System.out.print("Result Exp");
                            }
                            else if(response_code.equals("202")&& response_msg.equals("not_activated"))

                            {
                                System.out.print("Result Exp ");


                            }
                            else if(response_code.equals("202")&& response_msg.equals("not_suppport_version"))
                            {

                                System.out.print("Result Exp");

                            }
                            else
                            {

                                System.out.print("Result Exp" );
                            }

                        } catch (JSONException e) {
                            System.out.print("Result Exp iss" + e);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if(error.toString().contains("java.io.EOFException")){
                            sendDatatoCart();
                        }
                        else {
                            if (error.getCause().toString().contains("java.io.EOFException")) {
                                sendDatatoCart();
                            } else {
                                customProgressDialog.dismissDialog();
                                System.out.println("Error" + error.getCause());
                                System.out.println("Error" + error.getMessage());
                                System.out.println("Error" + error.getBody());
                                System.out.println("Error" + error.getLocalizedMessage());
                                System.out.print("Result Exp iss" + error);
                            }

                        }


                    }
                }
        );





    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Home_Details_Fragment.this.finish();
                return true;
        }
        return (super.onOptionsItemSelected(menuItem));
    }
}

