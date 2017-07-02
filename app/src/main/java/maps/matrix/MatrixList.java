package maps.matrix;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import database.Matrix;
import database.MatrixDatabase;
import gui.MatrixListAdapter;

public class MatrixList extends AppCompatActivity {
    private String passkey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matrix_list);

        passkey = getIntent().getStringExtra("passkey");

        MatrixDatabase mDbHelper = new MatrixDatabase(MatrixList.this);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        final ArrayList<Matrix> matrices = Matrix.getAll(db);

        TextView emptyMessage = (TextView) findViewById(R.id.no_matrix);//empty message
        ListView deviceList = (ListView) findViewById(R.id.matrixList);//list view


        if(matrices.size() == 0){
            emptyMessage.setVisibility(View.VISIBLE);
        }else{
            emptyMessage.setVisibility(View.GONE);
        }
        ListAdapter adapter = new MatrixListAdapter(MatrixList.this,matrices);
        deviceList.setAdapter(adapter);
        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //show single matrix details
                Intent intentMatrixDetails =  new Intent(MatrixList.this, MatrixActivity.class);
                intentMatrixDetails.putExtra("matrixId", matrices.get(position).id);
                intentMatrixDetails.putExtra("update", true);
                intentMatrixDetails.putExtra("passkey", passkey);
                startActivity(intentMatrixDetails);
            }
        });
        mDbHelper.close();
    }

    public void btnAddMatrix(View view) {
        Intent intentNewMatrix =  new Intent(MatrixList.this, UpdateMatrix.class);
        intentNewMatrix.putExtra("update", false);
        intentNewMatrix.putExtra("passkey", passkey);
        startActivity(intentNewMatrix);
    }
}
