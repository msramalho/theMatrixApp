package maps.bank_matrix;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
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
                        advanceToMatrix(findViewById(R.id.go_btn));
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void advanceToMatrix(View view) {
        validatePassword(password.getText().toString());
    }

    public void resetPassword(View view) {
        promptNewPassword();
    }

    private void validatePassword(String attempt){
        Preferences p = new Preferences(this);

        if (!p.isPasswordDefined()){
            Toast.makeText(MainActivity.this, "No password defined...", Toast.LENGTH_SHORT).show();
            promptNewPassword();
            return;
        }

        if(p.validatePassword(attempt)){//correct password
            password.setText("");
            Intent intentMatrix =  new Intent(this, MatrixList.class);
            intentMatrix.putExtra("passkey", attempt);
            startActivity(intentMatrix);
        }else{
            Toast.makeText(MainActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
        }
    }

    private void changePassword(String oldP, String newP){
        Preferences p = new Preferences(this);
        if(p.changePassword(oldP, newP)){
            Toast.makeText(MainActivity.this, "Password set", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(MainActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
        }
    }

    private void promptNewPassword(){
        LayoutInflater inflater = this.getLayoutInflater();

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
            .setMessage("Enter the old and the new password")
            .setTitle("Change Password")
            .setNegativeButton("Abort", null)
            .setPositiveButton("Go", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Dialog dialogObj =Dialog.class.cast(dialog);
                    EditText oldEt = (EditText) dialogObj.findViewById(R.id.oldP);
                    EditText newEt = (EditText) dialogObj.findViewById(R.id.newP);

                    changePassword(oldEt.getText().toString(), newEt.getText().toString());
                }
            })
            .setView(inflater.inflate(R.layout.change_password, null));
        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

}
