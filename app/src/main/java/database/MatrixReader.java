package database;

import android.provider.BaseColumns;

public final class MatrixReader {

    private MatrixReader() {}

    public static class MatrixEntry implements BaseColumns {
        public static final String TABLE_NAME = "t_matrix";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_VALUE = "value";
        public static final String COLUMN_LINES = "lines";
        public static final String COLUMN_COLUMNS = "columns";
    }
}
