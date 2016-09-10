package info.patsch.ebl.books.search;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import info.patsch.ebl.R;

public class SearchEntryDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {

    private View form = null;
    private EditText searchQueryEntry = null;
    private OnDialogClosedListener mListener = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        form = getActivity().getLayoutInflater().inflate(R.layout.dialog_search, null);

        searchQueryEntry = (EditText)form.findViewById(R.id.search_query);

        searchQueryEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    dismiss();
                    performSearch();
                    return true;
                }
                return false;
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        return builder.setTitle(R.string.search).setView(form)
                .setPositiveButton(R.string.search, this)
                .setNegativeButton(android.R.string.cancel, null).create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    public void setOnDialogClosedListener(OnDialogClosedListener listener) {
        this.mListener = listener;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        performSearch();
    }

    private void performSearch() {
        Editable searchQueryEntryText = searchQueryEntry.getText();
        String query = searchQueryEntryText != null ? searchQueryEntryText.toString() : null;
        if (query != null && mListener != null) {
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
