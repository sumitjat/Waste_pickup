package temperatureconverter.android.vogella.com.olauber;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Struct;
import java.util.HashMap;
import java.util.Map;

public class CustomerSettingActivity extends AppCompatActivity {


    private Button mconfirm,mback;
    private EditText mname,mphone;
    private ImageView imageView;
    private Uri resulturi;
    private String profileimageurl;

    private FirebaseAuth mAuth;
    private DatabaseReference mcustomerdatabaseref;

    private String userID,Name,Phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_setting);

        mconfirm=findViewById(R.id.confirmbttn);
        mback=findViewById(R.id.backbttn);
        imageView=findViewById(R.id.profileImage);

        mname=findViewById(R.id.Name);
        mphone=findViewById(R.id.phone);

        mAuth=FirebaseAuth.getInstance();
        userID=mAuth.getCurrentUser().getUid();

        mcustomerdatabaseref= FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);
        getUser();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent,1);
            }
        });
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

        if(resulturi!=null){

            StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("profile_images").child(userID);
            Bitmap bitmap=null;

            try {
                bitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),resulturi);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteArrayOutputStream boas=new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG,20,boas);
            byte[] data=boas.toByteArray();
            UploadTask uploadTask=storageReference.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    finish();
                    return;
                }
            });

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downlaoduri=taskSnapshot.getUploadSessionUri(); //getdownload uri is not present in this
                    Map newimage=new HashMap();
                    newimage.put("profileimageurl",downlaoduri.toString());
                    mcustomerdatabaseref.updateChildren(newimage);

                    finish();
                    return;

                }
            });
        }
        else {
            finish();
        }

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

                    if (map.get("profileimageurl")!=null)
                    {
                        profileimageurl=map.get("profileimageurl").toString();
                        Glide.with(getApplication()).load(profileimageurl).into(imageView);


                    }                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode== Activity.RESULT_OK)
        {
            final Uri imageuri=data.getData();
            resulturi=imageuri;
            imageView.setImageURI(resulturi);
        }
    }
}
