package maps.matrix;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        password = (EditText) findViewById(R.id.password);

        password.setOnKeyListener(new View.OnKeyListener(){//code to make keyboard enter be the same as button click
            public boolean onKey(View v, int keyCode, KeyEvent event){
                if (event.getAction() == KeyEvent.ACTION_DOWN){
                    if(keyCode == KeyEvent.KEYCODE_ENTER){
                        advanceToMatrix((Button) findViewById(R.id.go_btn));
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void advanceToMatrix(View view) {
        String realPass = "";
        if(password.getText().toString().equals(realPass)){
            password.setText("");
            Intent intentShowMap =  new Intent(this, MatrixActivity.class);
            startActivity(intentShowMap);
        }else{
            Toast.makeText(MainActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
        }



    }
}
