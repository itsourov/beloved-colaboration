package net.sourov.fdmanagerfinal;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

public class Sourov {
    private Context context;
    AlertDialog progressDialog;


    public Sourov(Context context) {
        this.context = context;

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        final View progressView = layoutInflater.inflate(R.layout.progress_bar, null);
        progressDialog = new AlertDialog.Builder(context).create();
        progressDialog.setView(progressView);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

    }

    public AlertDialog spinner() {
        return progressDialog;
    }

}
