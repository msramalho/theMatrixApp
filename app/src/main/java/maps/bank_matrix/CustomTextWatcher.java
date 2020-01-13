package maps.bank_matrix;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class CustomTextWatcher implements TextWatcher {

    private EditText previous, current, next;
    private int previousSize = 0;
    CustomTextWatcher(EditText previous, EditText current, EditText next) {
        this.previous = previous;
        this.current = current;
        this.next = next;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if(current.getSelectionStart() == 3)//move to the next
            next.requestFocus();
        else if (previousSize > current.getText().length() && current.getText().length() == 0 && previous!=null)
            previous.requestFocus();
        previousSize = current.getText().length();
    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }
    @Override
    public void afterTextChanged(Editable s) {

    }


}
