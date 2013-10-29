package specular.systems;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

/**
 * Created by yehezkelk on 10/29/13.
 */
class TurnNFCOn extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("turn NFC on? if not keys will be saved on your device")
                // Set the action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                        startActivityForResult(intent, 71);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //...
                        dialog.cancel();
                        while (Main.createKeys.isAlive())
                            synchronized (this) {
                                try {
                                    wait(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        Main.saveKeys.start(getActivity());
                        while (Main.saveKeys.isAlive())
                            synchronized (this) {
                                try {
                                    wait(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        Intent intent = new Intent(getActivity(),Splash.class);
                        startActivity(intent);
                    }
                });

        return builder.create();
    }

}