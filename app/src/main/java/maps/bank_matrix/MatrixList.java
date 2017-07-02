package maps.bank_matrix;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
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
    private MatrixDatabase mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matrix_list);

        passkey = getIntent().getStringExtra("passkey");

        mDbHelper = new MatrixDatabase(MatrixList.this);
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();

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
        //long click --> prompt delete
        deviceList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(MatrixList.this)
                    .setTitle("Confirmation")
                    .setMessage("Do you want to delete this matrix ("+matrices.get(position).getName(MatrixList.this)+")?")
                    .setIcon(android.R.drawable.ic_menu_delete)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            matrices.get(position).deleteMatrix(db);
                            restart();
                        }})
                    .setNegativeButton(android.R.string.no, null).show();
                return true;
            }
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
        intentNewMatrix.putExtra("passkey", passkey);
        startActivity(intentNewMatrix);
    }

    @Override
    protected void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }
}
