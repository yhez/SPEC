package specular.systems.Dialogs;

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
    int title,title2, content;
    final String details;
    public final static int HASH = 0, SESSION = 1, REPLAY = 2;

    public ExplainDialog(int type, String details) {
        switch (type) {
            case HASH:
                content = R.string.hash_explain;
                title = R.string.what_is_hash;
                title2 = R.string.hash_title;
                break;
            case SESSION:
                content = R.string.session_explain;
                title = R.string.what_is_session;
                title2 = R.string.session_title;
                break;
            case REPLAY:
                content = R.string.replay_explain;
                title = R.string.what_is_replay;
                title2 = R.string.replay_title;
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
        btExplain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvTitle.setText(title);
                tvContent.setText(content);
            }
        });
        btDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvTitle.setText(title2);
                tvContent.setText(details);
            }
        });
        Visual.setAllFonts(getActivity(), (ViewGroup) v);
        return builder.create();
    }
}
