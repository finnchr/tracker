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
import com.finnchristian.tracker.model.Track;
import com.google.common.base.Strings;

import java.util.Arrays;
import java.util.Date;

public class EditTrackDialogFragment extends DialogFragment {
    private static final String ARG_TRACK_ID = "ARG_TRACK_ID";

    private int trackId = -1;
    private ContentResolverHelper contentResolverHelper = null;

    public static DialogFragment newInstance(final int trackId) {
        final Bundle args = new Bundle();
        args.putInt(ARG_TRACK_ID, trackId);

        final DialogFragment dialogFragment = new EditTrackDialogFragment();
        dialogFragment.setArguments(args);
        return dialogFragment;
    }


    public EditTrackDialogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        contentResolverHelper = new ContentResolverHelper(getActivity().getContentResolver());
        trackId = getArguments().getInt(ARG_TRACK_ID, -1);

        if(trackId == -1) {
            throw new IllegalArgumentException("Track id can't be -1");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_edit_track, null);
        final Track track = contentResolverHelper.getTrackById(trackId);

        final EditText input = (EditText) view.findViewById(R.id.track_name_edittext);
        input.setText(track.getName());
        input.selectAll();

        final String[] values = getResources().getStringArray(R.array.track_type_values);
        final int selectedTypePos = Arrays.asList(values).indexOf(track.getType());

        final Spinner spinner = (Spinner) view.findViewById(R.id.track_type_spinner);
        spinner.setSelection((selectedTypePos != -1) ? selectedTypePos : 0);

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.dialog_title_edit_track_title))
                //.setMessage(R.string.create_track_dialog_message)
                .setView(view)
                .setPositiveButton(R.string.dialog_button_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final String[] values = getResources().getStringArray(R.array.track_type_values);
                        final int selectedPos = spinner.getSelectedItemPosition();
                        final String type = (values.length > selectedPos) ? values[selectedPos] : spinner.getSelectedItem().toString();
                        final String name = input.getText().toString();

                        if (!Strings.isNullOrEmpty(name) && !Strings.isNullOrEmpty(type)) {
                            contentResolverHelper.updateTrack(trackId, name, type);
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
