package temperatureconverter.android.vogella.com.olauber;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button mDriver,mcustomer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDriver=findViewById(R.id.main_driver);
        mcustomer=findViewById(R.id.main_customer);

        mDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent DriverIntent=new Intent(MainActivity.this,DriverLogin.class);
                startActivity(DriverIntent);
                finish();
                return;
            }
        });

        mcustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent CustomerIntent=new Intent(MainActivity.this,CustomerLogin.class);
                startActivity(CustomerIntent);
                finish();
                return;
            }
        });
    }

}
