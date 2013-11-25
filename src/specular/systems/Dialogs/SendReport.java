package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;

import java.util.List;

import specular.systems.R;


public class SendReport extends DialogFragment {
    String trace;

    public SendReport(String trace) {
        this.trace = trace;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.putExtra(Intent.EXTRA_TEXT, trace);
                i.putExtra(Intent.EXTRA_SUBJECT, "bug_report");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"yhezkel88@gmail.com"});
                List<ResolveInfo> rs = (List<ResolveInfo>) getActivity().getPackageManager().resolveActivity(i, 0);
                for (ResolveInfo ri : rs) {
                    if (ri.activityInfo.packageName.equals("com.google.android.gm")) {
                        i.setComponent(new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name));
                        break;
                    }
                }
                startActivity(i);
                getActivity().finish();
            }
        })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SendReport.this.getDialog().cancel();
                    }
                }).setMessage("please send report to hez").setTitle("there was an unexpected error");
        return builder.create();
    }
}
