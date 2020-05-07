package com.somesh.apps.upipay;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    EditText amount,upiid,name,message;
    Button pay;
    final int UPI_PAYMENT=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        amount=(EditText) findViewById(R.id.amount);
        upiid=(EditText) findViewById(R.id.upiid);
        name=(EditText) findViewById(R.id.name);
        message=(EditText) findViewById(R.id.message);

        pay=(Button)findViewById(R.id.pay);

        pay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String amt=amount.getText().toString();
                String UID=upiid.getText().toString();
                String cname=name.getText().toString();
                String note=message.getText().toString();
                payUsingUPI(amt,UID,cname,note);
            }
        });

    }

    private void payUsingUPI(String amt, String uid, String cname, String note) {
        Uri uri=Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa",uid)     //personal account
                .appendQueryParameter("pn",cname)   //personal name
                .appendQueryParameter("tn",note)    //transaction note
                .appendQueryParameter("am",amt)     //amount
                .appendQueryParameter("cu","INR")   //currency unit
                .build();


        Intent upiPayIntent=new Intent(Intent.ACTION_VIEW);
        upiPayIntent.setData(uri);

        //check for all UPI supported apps
        Intent chooser=Intent.createChooser(upiPayIntent,"Pay with");

        //check if upi app found or not
        if(chooser.resolveActivity(getPackageManager())!=null)
        {
            startActivityForResult(chooser,UPI_PAYMENT);
        }
        else
        {
            Toast.makeText(MainActivity.this, "NO UPI App Found !!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==UPI_PAYMENT && resultCode==RESULT_OK)
        {
            if(data!=null)
            {
                String text=data.getStringExtra("response");
                Log.d("UPI", "onActivityResult: "+text);
                Toast.makeText(MainActivity.this, "HINT: "+text, Toast.LENGTH_LONG).show();
                ArrayList<String> dataList=new ArrayList<>();
                dataList.add(text);
                //upiPaymentDataOperation(dataList);
            }
            else
            {
                Log.d("UPI", "onActivityResult: "+"return data null");
                ArrayList<String> dataList=new ArrayList<>();
                Toast.makeText(MainActivity.this, "NULL DATA RECEIVED ", Toast.LENGTH_LONG).show();

                dataList.add("nothing");
                //upiPaymentDataOperation(dataList);
            }
        }
        else
        {
            Log.d("UPI", "onActivityResult: "+"back without transaction");
            ArrayList<String> dataList=new ArrayList<>();
            dataList.add("nothing");
            Toast.makeText(MainActivity.this, "NOTHING RECEIVED "+data, Toast.LENGTH_LONG).show();
            //upiPaymentDataOperation(dataList);
        }
    }

    private void upiPaymentDataOperation(ArrayList<String> dataList)
    {
        //this method only validate google pay
        String str=dataList.get(0);
        Log.d("UPI_Pay", "upiPaymentDataOperation: "+str);
        String paymentCancel="";
        if(str==null) str="discard";
        String status="";
        String ApprovalRefNo="";
        String response[]=str.split("&");
        for(int i=0;i<response.length;i++)
        {
            String equalStr[]=response[i].split("=");
            if(equalStr.length>=2)
            {
                if(equalStr[0].toLowerCase().equals("Status".toLowerCase())){
                    status=equalStr[1].toLowerCase();
                }
                else if(equalStr[0].toLowerCase().equals("ApprovalRefNo".toLowerCase())||equalStr[0].toLowerCase().equals("txnref".toLowerCase())){
                    ApprovalRefNo=equalStr[1];
                }
            }
            else{
                paymentCancel="payment cancel by user";
            }
        }
        if(status.equals("success")){
            Toast.makeText(MainActivity.this, "Transaction successful", Toast.LENGTH_SHORT).show();
        }
        else if(paymentCancel.equals("payment cancel by user")){
            Toast.makeText(MainActivity.this, "payment cancelled by user", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(MainActivity.this, "Transaction failed", Toast.LENGTH_SHORT).show();
        }
    }
}