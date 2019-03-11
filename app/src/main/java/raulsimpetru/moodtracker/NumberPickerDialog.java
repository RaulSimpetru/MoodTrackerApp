package raulsimpetru.moodtracker;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.NumberPicker;

public class NumberPickerDialog extends DialogFragment {
    private NumberPicker.OnValueChangeListener valueChangeListener;
    private int value = 5;

    void setValue(int _value) {
        if (_value != -1) {
            value = _value;
        } else value = 5;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final NumberPicker numberPicker = new NumberPicker(getActivity());

        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(10);
        numberPicker.setDisplayedValues(new String[] {"Depressive", "Miserable", "Very Bad", "Bad",
                "Not that well", "Alright", "Okay", "Good", "Very Good", "Great", "Awesome"});
        numberPicker.setValue(value);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("How was your day?");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                valueChangeListener.onValueChange(numberPicker,
                        numberPicker.getValue(), numberPicker.getValue());
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                valueChangeListener.onValueChange(numberPicker,
                        numberPicker.getValue(), numberPicker.getValue());
            }
        }).setView(numberPicker);

        return builder.create();
    }

    void setValueChangeListener(NumberPicker.OnValueChangeListener valueChangeListener) {
        this.valueChangeListener = valueChangeListener;
    }
}