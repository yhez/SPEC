package specular.systems.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import specular.systems.R;
import specular.systems.Visual;


public class ExplainDialog extends DialogFragment {
    String title, title2, content;
    final String details;
    public final static int HASH = 0, SESSION = 1, REPLAY = 2,HASH_QR=3,REPLAY_QR=4;

    public ExplainDialog(Activity aaa,int type, String details) {
        switch (type) {
            case HASH:
                content =  aaa.getString(R.string.hash_explain);
                title =  aaa.getString(R.string.what_is_hash)+ aaa.getString(R.string.hash_what_we_did);
                title2 = aaa.getString(R.string.hash_title);
                break;
            case SESSION:
                content =  aaa.getString(R.string.session_explain);
                title =  aaa.getString(R.string.what_is_session)+ aaa.getString(R.string.session_what_we_did);
                title2 =  aaa.getString(R.string.session_title);
                break;
            case REPLAY:
                content =  aaa.getString(R.string.replay_explain);
                title =  aaa.getString(R.string.what_is_replay)+ aaa.getString(R.string.replay_what_we_did);
                title2 =  aaa.getString(R.string.replay_title);
                break;
            case HASH_QR:
                content =  aaa.getString(R.string.hash_explain);
                title =  aaa.getString(R.string.what_is_hash)+ aaa.getString(R.string.hash_qr_what_we_did);
                title2 =  aaa.getString(R.string.hash_title);
                break;
            case REPLAY_QR:
                content =  aaa.getString(R.string.replay_explain);
                title =  aaa.getString(R.string.what_is_replay)+ aaa.getString(R.string.replay_qr_what_we_did);
                title2 =  aaa.getString(R.string.replay_title);
                break;
        }
        this.details = details;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.tech_details, null);
        builder.setView(v);
        final TextView tvTitle = (TextView) v.findViewById(R.id.title);
        tvTitle.setText(title2);
        final TextView tvContent = (TextView) v.findViewById(R.id.text_content);
        tvContent.setText(details);
        final Button btDetails = (Button) v.findViewById(R.id.my_details);
        final Button btExplain = (Button) v.findViewById(R.id.general_explain);
        final View[] vv = new View[2];
        vv[0] = ((ViewGroup) v.findViewById(R.id.ll_titles)).getChildAt(0);
        vv[1] = ((ViewGroup) v.findViewById(R.id.ll_titles)).getChildAt(1);
        btExplain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvTitle.setText(title);
                tvContent.setText(content);
                vv[1].setVisibility(View.VISIBLE);
                vv[0].setVisibility(View.INVISIBLE);
            }
        });
        btDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvTitle.setText(title2);
                tvContent.setText(details);
                vv[0].setVisibility(View.VISIBLE);
                vv[1].setVisibility(View.INVISIBLE);
            }
        });
        Visual.setAllFonts(getActivity(), (ViewGroup) v);
        return builder.create();
    }
}
