package maps.bank_matrix;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import database.Matrix;
import database.MatrixDatabase;


public class MatrixActivity extends AppCompatActivity {

    private Spinner sp11, sp12, sp13;
    private Spinner sp21, sp22, sp23;
    private Spinner sp31, sp32, sp33;
    private TextView tv1, tv2, tv3;

    private String[][] matrix;
    private Matrix m;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matrix);

        MatrixDatabase mDbHelper = new MatrixDatabase(MatrixActivity.this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Intent i = getIntent();
        long matrixId = i.getLongExtra("matrixId", 0);

        m = Matrix.getMatrixById(db, matrixId);

        if(m == null || !m.validMatrix()){
            Toast.makeText(this, "Matrix not set", Toast.LENGTH_SHORT).show();
            finish();
        }

        try {
            matrix = m.getMatrix();
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException | NoSuchProviderException | InvalidAlgorithmParameterException | BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Failed to decrypt: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        TextView matrixName = findViewById(R.id.matrixName);
        matrixName.setText(m.name);

        //initialize the spinners
        sp11 = findViewById(R.id.cell11);
        sp12 = findViewById(R.id.cell12);
        sp13 = findViewById(R.id.cell13);
        sp21 = findViewById(R.id.cell21);
        sp22 = findViewById(R.id.cell22);
        sp23 = findViewById(R.id.cell23);
        sp31 = findViewById(R.id.cell31);
        sp32 = findViewById(R.id.cell32);
        sp33 = findViewById(R.id.cell33);

        //initialize the textViews
        tv1 = findViewById(R.id.output1);
        tv2 = findViewById(R.id.output2);
        tv3 = findViewById(R.id.output3);

        //adapter for the letters
        ArrayList<String> letters = new ArrayList<>();
        for(int j = 0; j< m.lines; j++){
            letters.add(String.valueOf((char)(65 + j)));
        }
        ArrayAdapter<String> letterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, letters);
        letterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp11.setAdapter(letterAdapter);
        sp21.setAdapter(letterAdapter);
        sp31.setAdapter(letterAdapter);

        //adapter for the numbers 1 to 9
        ArrayList<String> numbers = new ArrayList<>();
        for(int j = 0; j< m.columns; j++){
            numbers.add(String.valueOf(j+1));
        }
        ArrayAdapter<String> numberAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, numbers);
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

    public void updateMatrix(View view) {
        Intent intentMatrix =  new Intent(this, UpdateMatrix.class);
        intentMatrix.putExtra("matrixId", m.id);
        intentMatrix.putExtra("update", true);
        startActivity(intentMatrix);
    }
}
