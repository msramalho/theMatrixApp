package maps.matrix;

import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MatrixActivity extends AppCompatActivity {

    private Spinner sp11, sp12, sp13;
    private Spinner sp21, sp22, sp23;
    private Spinner sp31, sp32, sp33;
    private TextView tv1, tv2, tv3;

    private String[][] matrix = {
            {"000","000","000","000","000","000","000","000"},
            {"000","000","000","000","000","000","000","000"},
            {"000","000","000","000","000","000","000","000"},
            {"000","000","000","000","000","000","000","000"},
            {"000","000","000","000","000","000","000","000"},
            {"000","000","000","000","000","000","000","000"},
            {"000","000","000","000","000","000","000","000"},
            {"000","000","000","000","000","000","000","000"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matrix);

        //initialize the spinners
        sp11 = (Spinner) findViewById(R.id.cell11);
        sp12 = (Spinner) findViewById(R.id.cell12);
        sp13 = (Spinner) findViewById(R.id.cell13);
        sp21 = (Spinner) findViewById(R.id.cell21);
        sp22 = (Spinner) findViewById(R.id.cell22);
        sp23 = (Spinner) findViewById(R.id.cell23);
        sp31 = (Spinner) findViewById(R.id.cell31);
        sp32 = (Spinner) findViewById(R.id.cell32);
        sp33 = (Spinner) findViewById(R.id.cell33);

        //initialize the textViews
        tv1 = (TextView) findViewById(R.id.output1);
        tv2 = (TextView) findViewById(R.id.output2);
        tv3 = (TextView) findViewById(R.id.output3);

        //adapter for the letters
        ArrayAdapter<CharSequence> letterAdapter = ArrayAdapter.createFromResource(this, R.array.lettersArray, android.R.layout.simple_spinner_item);
        letterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp11.setAdapter(letterAdapter);
        sp21.setAdapter(letterAdapter);
        sp31.setAdapter(letterAdapter);

        //adapter for the numbers 1 to 9
        ArrayAdapter<CharSequence> numberAdapter = ArrayAdapter.createFromResource(this, R.array.numbersArray, android.R.layout.simple_spinner_item);
        numberAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp12.setAdapter(numberAdapter);
        sp22.setAdapter(numberAdapter);
        sp32.setAdapter(numberAdapter);

        //adapter for the indexes
        ArrayAdapter<CharSequence> indexesAdapter = ArrayAdapter.createFromResource(this, R.array.indexArray, android.R.layout.simple_spinner_item);
        indexesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp13.setAdapter(indexesAdapter);
        sp23.setAdapter(indexesAdapter);
        sp33.setAdapter(indexesAdapter);
        resetValues();
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }

    public void displayMatrix(View view) {
        displayCoordinate(sp11,sp12,sp13,tv1);
        displayCoordinate(sp21,sp22,sp23,tv2);
        displayCoordinate(sp31,sp32,sp33,tv3);
    }

    private void displayCoordinate(Spinner s1, Spinner s2, Spinner s3, TextView t){
        int letterPos = getIndexFromLetter(s1.getSelectedItem().toString());
        int numberPos = Integer.parseInt(s2.getSelectedItem().toString()) - 1;
        int indexPos = Integer.parseInt(s3.getSelectedItem().toString()) - 1;
        t.setText(String.valueOf(matrix[letterPos][numberPos].charAt(indexPos)));
    }

    private int getIndexFromLetter(String letter){
        char c = letter.charAt(0);
        return ((int)c) - 65;
    }

    public void clearData(View view) {
        resetValues();
    }

    private void resetValues(){
        //set the default values
        sp11.setSelection(0);
        sp12.setSelection(0);
        sp13.setSelection(0);
        sp21.setSelection(0);
        sp22.setSelection(0);
        sp23.setSelection(0);
        sp31.setSelection(0);
        sp32.setSelection(0);
        sp33.setSelection(0);
        //text views
        tv1.setText("");
        tv2.setText("");
        tv3.setText("");
    }
}
