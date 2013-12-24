package specular.systems.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import specular.systems.activitys.Main;
import specular.systems.R;
import specular.systems.activitys.Splash;

public class TurnNFCOn extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false)
                .setTitle(getActivity().getString(R.string.title_turn_nfc_on))
                        // Set the action buttons
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //...
                        dialog.cancel();
                        Main.saveKeys.start(getActivity());
                        while (Main.saveKeys.isAlive())
                            synchronized (this) {
                                try {
                                    wait(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        Intent intent = new Intent(getActivity(), Splash.class);
                        startActivity(intent);
                    }
                });

        return builder.create();
    }

}