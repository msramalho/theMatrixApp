package maps.bank_matrix;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

import database.Matrix;
import database.MatrixDatabase;
import gui.MatrixListAdapter;

public class MatrixList extends AppCompatActivity {
    private MatrixDatabase mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matrix_list);


        mDbHelper = new MatrixDatabase(MatrixList.this);
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();

        final ArrayList<Matrix> matrices = Matrix.getAll(db);

        TextView emptyMessage = findViewById(R.id.no_matrix);//empty message
        ListView deviceList = findViewById(R.id.matrixList);//list view


        if(matrices.size() == 0){
            emptyMessage.setVisibility(View.VISIBLE);
        }else{
            emptyMessage.setVisibility(View.GONE);
        }
        ListAdapter adapter = new MatrixListAdapter(MatrixList.this,matrices);
        deviceList.setAdapter(adapter);
        deviceList.setOnItemClickListener((parent, view, position, id) -> {
            //show single matrix details
            Intent intentMatrixDetails =  new Intent(MatrixList.this, MatrixActivity.class);
            intentMatrixDetails.putExtra("matrixId", matrices.get(position).id);
            intentMatrixDetails.putExtra("update", true);
            startActivity(intentMatrixDetails);
        });
        //long click --> prompt delete
        deviceList.setOnItemLongClickListener((parent, view, position, id) -> {
            new AlertDialog.Builder(MatrixList.this)
                .setTitle("Confirmation")
                .setMessage("Do you want to delete this matrix ("+matrices.get(position).getName(MatrixList.this)+")?")
                .setIcon(android.R.drawable.ic_menu_delete)
                .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                    matrices.get(position).deleteMatrix(db);
                    restart();
                })
                .setNegativeButton(android.R.string.no, null).show();
            return true;
        });

    }

    private void restart(){
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

    public void btnAddMatrix(View view) {
        Intent intentNewMatrix =  new Intent(MatrixList.this, UpdateMatrix.class);
        intentNewMatrix.putExtra("update", false);
        startActivity(intentNewMatrix);
    }

    @Override
    protected void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }
}
