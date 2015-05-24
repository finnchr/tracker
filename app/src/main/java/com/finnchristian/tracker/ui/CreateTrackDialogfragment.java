package com.finnchristian.tracker.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Spinner;

import com.finnchristian.tracker.R;
import com.finnchristian.tracker.data.ContentResolverHelper;
import com.google.common.base.Strings;

import java.util.Date;

public class CreateTrackDialogFragment extends DialogFragment {

    public static CreateTrackDialogFragment newInstance() {
        return new CreateTrackDialogFragment();
    }


    public CreateTrackDialogFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_edit_track, null);

        final Date now = new Date();
        final String defaultTrackName = getActivity().getResources().getString(R.string.default_track_name, now);
        //final EditText input = new EditText(getActivity());
        final EditText input = (EditText) view.findViewById(R.id.track_name_edittext);
        input.setText(defaultTrackName);
        input.selectAll();

        final Spinner spinner = (Spinner) view.findViewById(R.id.track_type_spinner);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.create_track_dialog_title)
                //.setMessage(R.string.create_track_dialog_message)
                .setView(view)
                .setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String[] values = getResources().getStringArray(R.array.track_type_values);
                        final int selectedPos = spinner.getSelectedItemPosition();
                        final String type = (values.length > selectedPos) ? values[selectedPos] : spinner.getSelectedItem().toString();
                        final String name = input.getText().toString();

                        if (!Strings.isNullOrEmpty(name)) {
                            // TODO Add dropdown allowing the user to select type (Hiking, Walking, Running etc.)
                            new ContentResolverHelper(getActivity().getContentResolver()).createTrack(name, now.getTime(), type);
                        }
                    }
                })
                .create();

        showKeyboardOnShow(dialog, input);

        return dialog;
    }

    private void showKeyboardOnShow(final AlertDialog dialog, final EditText input) {
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                input.requestFocus();
                // Show soft keyboard for the user to enter the value.
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }
}
