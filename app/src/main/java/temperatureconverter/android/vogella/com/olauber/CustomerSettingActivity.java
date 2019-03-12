package temperatureconverter.android.vogella.com.olauber;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Struct;
import java.util.HashMap;
import java.util.Map;

public class CustomerSettingActivity extends AppCompatActivity {


    private Button mconfirm,mback;
    private EditText mname,mphone;

    private FirebaseAuth mAuth;
    private DatabaseReference mcustomerdatabaseref;

    private String userID,Name,Phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_setting);

        mconfirm=findViewById(R.id.confirmbttn);
        mback=findViewById(R.id.backbttn);

        mname=findViewById(R.id.Name);
        mphone=findViewById(R.id.phone);

        mAuth=FirebaseAuth.getInstance();
        userID=mAuth.getCurrentUser().getUid();

        mcustomerdatabaseref= FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);
        getUser();


        mconfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveUserInformation();
            }
        });

        mback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                return;
            }
        });
    }

    private void saveUserInformation() {

        Name=mname.getText().toString();
        Phone=mphone.getText().toString();

        Map userinfo=new HashMap();
        userinfo.put("name",Name);
        userinfo.put("phone",Phone);
        mcustomerdatabaseref.updateChildren(userinfo);

        finish();
    }



    private void getUser() {

        mcustomerdatabaseref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0)
                {
                    Map<String,Object> map=(Map<String,Object>)dataSnapshot.getValue();
                    if (map.get("name")!=null){

                        Name=map.get("name").toString();
                        mname.setText(Name);

                    }

                    if (map.get("phone")!=null){

                        Phone=map.get("phone").toString();
                        mphone.setText(Phone);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
