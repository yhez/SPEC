package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import specular.systems.R;
import specular.systems.Visual;

public class NoteEdit extends DialogFragment {
    String text;
    public NoteEdit(FragmentManager fm,String text){
        this.text = text;
        show(fm,"");
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTransparent);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View v = inflater.inflate(R.layout.note_edit, null);
        ((TextView)v.findViewById(R.id.text_view)).setText(text);
        builder.setView(v);
        Visual.setAllFonts(getActivity(), (ViewGroup) v);
        return builder.create();
    }
}
