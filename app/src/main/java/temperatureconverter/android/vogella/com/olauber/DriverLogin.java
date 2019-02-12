package temperatureconverter.android.vogella.com.olauber;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DriverLogin extends AppCompatActivity {
    private static final String TAG = "DriverLogin";

    private Button mlogin,mregister;
    private FirebaseAuth mAuth;
    private EditText memail,mpassword;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        mAuth=FirebaseAuth.getInstance();

        memail=findViewById(R.id.email_driver);
        mpassword=findViewById(R.id.password_driver);

        mlogin=findViewById(R.id.login_driver);
        mregister=findViewById(R.id.register_driver);

        mregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email=memail.getText().toString();
                final String password=mpassword.getText().toString();

                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(DriverLogin.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful())
                        {
                            String user_id=mAuth.getCurrentUser().getUid();
                            DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference().child("users").child("Drivers").child(user_id);
                            databaseReference.setValue(true);
                        }

                        else {
                            Toast.makeText(DriverLogin.this,"hey for you",Toast.LENGTH_LONG).show();
                    }
                    }
                });
            }
        });

        mlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email=memail.getText().toString();
                final String password=mpassword.getText().toString();

                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(DriverLogin.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful())
                        {
                            Toast.makeText(DriverLogin.this,"Successss",Toast.LENGTH_LONG).show();
                            android.content.Intent mainIntent=new android.content.Intent(DriverLogin.this,DriverMapActivity.class);
                            startActivity(mainIntent);
                            finish();
                        }

                        else {
                            Toast.makeText(DriverLogin.this,"hey for you",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

    }


}
