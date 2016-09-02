package info.patsch.ebl.books.search;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.EditText;

import info.patsch.ebl.R;

/**
 * Created by patsch on 02.09.16.
 */
public class SearchEntryDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private View form = null;
    private OnDialogClosedListener mListener = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        form = getActivity().getLayoutInflater().inflate(R.layout.dialog_search, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        return builder.setTitle(R.string.search).setView(form)
                .setPositiveButton(R.string.search, this)
                .setNegativeButton(android.R.string.cancel, null).create();
    }

    public void setOnDialogClosedListener(OnDialogClosedListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        // search

        String query = ((EditText) form.findViewById(R.id.search_query)).getText().toString();
        if (mListener != null) {
            mListener.onSearchClicked(query);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    public interface OnDialogClosedListener {

        void onSearchClicked(String query);
    }
}
