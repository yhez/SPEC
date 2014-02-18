package specular.systems.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import specular.systems.Backup;
import specular.systems.CryptMethods;
import specular.systems.FilesManagement;
import specular.systems.R;
import specular.systems.Visual;
import specular.systems.activities.SendMsg;


public class BackupDialog extends DialogFragment {
   private int message;
    private Activity a;

    public BackupDialog(int str) {
        message=str;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        a= getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(a, R.style.dialogTransparent);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        switch (message){
                            case R.string.backup_no_public:
                                getDialog().cancel();
                                break;
                            case R.string.backup_explain:
                                final ProgressDlg prgd = new ProgressDlg(a, R.string.backup_progress);
                                prgd.setCancelable(false);
                                prgd.show();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        byte[] data = CryptMethods.encrypt(Backup.backup(a), CryptMethods.getPublic());
                                        boolean success = FilesManagement.createBackupFileToSend(a, Visual.bin2hex(data).getBytes());
                                        if (success) {
                                            Intent intent = new Intent(a, SendMsg.class);
                                            intent.putExtra("type", SendMsg.BACKUP);
                                            a.startActivity(intent);
                                        }
                                        prgd.cancel();
                                    }
                                }).start();
                                break;
                            case R.string.prevent_backup_no_private:
                                getDialog().cancel();
                                break;
                        }
                    }
                }).setNegativeButton(R.string.no,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getDialog().cancel();
            }
        }).setTitle(R.string.backup_title).setMessage(message);
        return builder.create();
    }
}
