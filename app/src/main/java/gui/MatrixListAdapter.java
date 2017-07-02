package gui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import database.Matrix;
import maps.matrix.R;

public class MatrixListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Matrix> data;
    private LayoutInflater inflater = null;

    public MatrixListAdapter(Context context, ArrayList<Matrix> data) {
        this.context = context;
        this.data = data;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return data.get(position).id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.row_layout_matrix, null);

        ((TextView) vi.findViewById(R.id.matrixName)).setText(data.get(position).name);
        ((TextView) vi.findViewById(R.id.dimensions)).setText(data.get(position).getDimensionString());

        return vi;
    }
}
