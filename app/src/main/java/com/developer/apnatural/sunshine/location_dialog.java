package com.developer.apnatural.sunshine;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

/**
 * Created by APnaturals on 5/9/2016.
 */
public class location_dialog extends DialogFragment {

public String Location;

    public interface locationDialogListener{
         public String getLocation();
    }

    locationDialogListener listen;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try{
            listen=(locationDialogListener) activity;
        }
        catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());


        LayoutInflater inflater=getActivity().getLayoutInflater();
        View view=inflater.inflate(R.layout.dialog_location, null);
        builder.setTitle("Location").setView(view).setPositiveButton("ok",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Dialog foo=(Dialog) dialog;
                EditText edt=(EditText) foo.findViewById(R.id.location);
                 Location=edt.getText().toString();

                location_dialog.this.getDialog().dismiss();
            }
        }).setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                location_dialog.this.getDialog().cancel();
            }
        });
                return builder.create();
    }
}
