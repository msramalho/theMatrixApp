package database;

import android.provider.BaseColumns;

final class MatrixReader {

    private MatrixReader() {}

    static class MatrixEntry implements BaseColumns {
        static final String TABLE_NAME = "t_matrix";
        static final String COLUMN_NAME = "name";
        static final String COLUMN_VALUE = "value";
        static final String COLUMN_LINES = "lines";
        static final String COLUMN_COLUMNS = "columns";
    }
}
