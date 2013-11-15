package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;


public class ExplainDialog extends DialogFragment{
    int title,content;
    public ExplainDialog(int title,int content){
        this.title=title;
        this.content=content;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton("cool", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                ExplainDialog.this.getDialog().cancel();
            }
        }).setMessage(content).setTitle(title);
        return builder.create();
    }
}
