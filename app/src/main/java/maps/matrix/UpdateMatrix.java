package maps.matrix;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import database.Matrix;
import database.MatrixDatabase;

public class UpdateMatrix extends AppCompatActivity {
    private LinearLayout matrix;

    private ArrayList<EditText> ets = new ArrayList<>();

    private String passkey;
    private boolean update;
    private Matrix m;
    private MatrixDatabase mDbHelper;
    private SQLiteDatabase db;

    private EditText matrixName, lines, columns;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_update_matrix);

        //get views
        matrixName = (EditText) findViewById(R.id.matrixName);
        lines = (EditText) findViewById(R.id.lines);
        columns = (EditText) findViewById(R.id.columns);

        lines.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().matches("^-?\\d+$")){
                    int temp = Integer.valueOf(s.toString());
                    if(temp > 0 && temp <= 20){
                        m.lines = temp;
                        drawMatrix(false);
                    }else{
                        Toast.makeText(UpdateMatrix.this, "Invalid - Use between 0 and 20 lines", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        columns.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().matches("^-?\\d+$")){
                    int temp = Integer.valueOf(s.toString());
                    if(temp > 0 && temp <= 20){
                        m.columns = temp;
                        drawMatrix(false);
                    }else{
                        Toast.makeText(UpdateMatrix.this, "Invalid - Use between 0 and 20 columns", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        //get values from intent: id, action update, passkey
        Intent i = getIntent();
        long matrixId = i.getLongExtra("matrixId", 0);
        update = i.getBooleanExtra("update", false);
        passkey = i.getStringExtra("passkey");

        mDbHelper = new MatrixDatabase(UpdateMatrix.this);
        db = mDbHelper.getReadableDatabase();
        if(update){//read matrix from db
            m = Matrix.getMatrixById(db, matrixId);
            matrixName.setText(m.name);
            lines.setText(m.lines);
            columns.setText(m.columns);
        }else{
            m = new Matrix();
        }

        matrix = (LinearLayout) findViewById(R.id.matrix);

        drawMatrix();
    }

    private void drawMatrix() {drawMatrix(true);}
    private void drawMatrix(boolean focus) {
        matrix.removeAllViews();
        ets.clear();

        //add top row with numbers
        LinearLayout numbersLayout = new LinearLayout(this);
        numbersLayout.setOrientation(LinearLayout.HORIZONTAL);
        numbersLayout.setGravity(Gravity.CENTER);

        //textview to simulate the space occupied by the first collumn->letters
        TextView tvEmpty = new TextView(this);
        tvEmpty.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
        tvEmpty.setText("H");
        tvEmpty.setTextColor(Color.WHITE);
        tvEmpty.setPadding(5,0,5,0);
        numbersLayout.addView(tvEmpty);

        //add the numbers in the first row
        for (int j = 0; j < m.columns; j++) {
            TextView tvNumber = new TextView(this);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            llp.weight = 1;
            tvNumber.setLayoutParams(llp);
            tvNumber.setText(String.valueOf( j + 1));
            tvNumber.setGravity(Gravity.CENTER);
            numbersLayout.addView(tvNumber);
        }
        matrix.addView(numbersLayout);

        //compute all of the edit texts in the matrix
        for (int i = 0; i < m.lines; i++) {
            LinearLayout layout = new LinearLayout(this);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setGravity(Gravity.CENTER);

            //add letter TextView to start of each line, A to H, for 1 to 8
            TextView tvLetter = new TextView(this);
            tvLetter.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
            tvLetter.setText(String.valueOf((char) (65 + i)));
            tvLetter.setPadding(5,0,5,0);
            layout.addView(tvLetter);

            for (int j = 0; j < m.columns; j++) {
                EditText text = new EditText(this);
                text.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
                text.setHint("000");
                text.setText("666");
                text.setInputType(InputType.TYPE_CLASS_NUMBER);
                text.setFilters(new InputFilter[] {new InputFilter.LengthFilter(3)});//max size
                layout.addView(text);
                ets.add(text);
            }
            matrix.addView(layout);
        }
        generateListeners();
        if (focus)
            ets.get(0).requestFocus();
    }

    private void generateListeners() {//associate the text watcher to make the cursor jump to the next
        for(int i = 0; i < ets.size()-1; i++){
            ets.get(i).addTextChangedListener(new CustomTextWatcher(i > 0?ets.get(i-1):null, ets.get(i), ets.get(i+1)));
        }
    }

    public void updateMatrix(View view) {
        String matrixString = getStringFromMatrix();
        m.value = Matrix.encrypt(passkey, matrixString);


        if(matrixString.length() != m.getTotalChars()){//8*8*3
            Toast.makeText(this, "Incomplete matrix (" + matrixString.length() + "/"+ m.getTotalChars()+")", Toast.LENGTH_SHORT).show();
        }else{//save the matrix, valid data
            if(update){//prompt the user
                new AlertDialog.Builder(this)
                    .setTitle("Confirmation")
                    .setMessage("Do you really want to replace the matrix?")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            m.updateMatrix(db);
                            finish();
                        }})
                    .setNegativeButton(android.R.string.no, null).show();
            }else{//insert new matrix
                m.insertMatrix(db);
                finish();
            }
        }
    }

    public void abortUpdate(View view) {
        finish();
    }

    private String getStringFromMatrix(){
        String result = "";
        for(EditText e: ets){
            result+=e.getText().toString();
        }
        return result;
    }

    @Override
    protected void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }
}
