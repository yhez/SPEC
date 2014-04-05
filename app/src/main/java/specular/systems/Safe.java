package specular.systems;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import specular.systems.Dialogs.FileDlg;
import specular.systems.Dialogs.NoteEdit;
import specular.systems.activities.FilesOpener;
import specular.systems.activities.Main;


public class Safe extends FragmentStatePagerAdapter {
    public static int currentPage = 0;
    Activity a;
    public Safe(Main fm) {
        super(fm.getSupportFragmentManager());
        this.a=fm;
    }

    @Override
    public Fragment getItem(int i) {
        return i == 0 ? new SectionSafe() : new SectionNotes();
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return position == 0 ? a.getString(R.string.files_title_safe) : a.getString(R.string.safe_title_notes);
    }

    public static class SectionSafe extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
            if(!CryptMethods.privateExist()){
                return privateNotExist(getActivity(),R.string.nfc_for_files);
            }
            ListView lv = new ListView(getActivity());
            final ArrayList<ListFiles.FilesRows> files = new ArrayList<ListFiles.FilesRows>();
            SharedPreferences sp = getActivity().getSharedPreferences("saved_files", Context.MODE_PRIVATE);
            Map m = sp.getAll();
            if(m==null||m.size()==0){
                TextView tv = new TextView(getActivity());
                tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                tv.setText(R.string.no_file_safe);
                tv.setGravity(Gravity.CENTER);
                tv.setTextColor(getResources().getColor(R.color.spec_black));
                tv.setTypeface(FilesManagement.getOs(getActivity()));
                tv.setTextSize(25);
                tv.setPadding(25,25,25,25);
                return tv;
            }
            Object[] o = m.values().toArray();
            for (Object anO : o) files.add(new ListFiles.FilesRows((String) anO));
            final ListFiles adapter = new ListFiles(getActivity(),files);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (CryptMethods.privateExist()) {
                        String name = ((TextView) view.findViewById(R.id.text1)).getText().toString();
                        try {
                            FilesManagement.getFromSafe(getActivity(), name);
                            Intent in = new Intent(getActivity(), FilesOpener.class);
                            in.putExtra("file_name", name);
                            startActivity(in);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }else{
                        Visual.toast(getActivity(), R.string.keys_loaded);
                    }
                }
            });
            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    if(CryptMethods.privateExist())
                        new FileDlg(getActivity().getFragmentManager(),((TextView) view.findViewById(R.id.text1)).getText().toString());
                    else{
                        Visual.toast(getActivity(), R.string.open_file_no_private);
                    }
                    return true;
                }
            });
            return lv;
        }
    }

    public static class SectionNotes extends Fragment {
        ArrayList<TextView> textViews;
        LinearLayout main;
        ArrayList<String> texts;
        Object[] o;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
            if(!CryptMethods.privateExist()){
                return privateNotExist(getActivity(),R.string.nfc_to_decrypt_notes);
            }
            View v = inflater.inflate(R.layout.notes, container, false);
            texts = new ArrayList<String>();
            main = (LinearLayout)v.findViewById(R.id.main);
            final EditText et = (EditText)v.findViewById(R.id.note);
            et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE && et.length() > 0) {
                        final String text = et.getText().toString();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                FilesManagement.saveNoteToSafe(getActivity(), text);
                            }
                        }).start();
                        et.getText().clear();
                        addNote(text);
                    }
                    return false;
                }
            });
            Map m = getActivity().getSharedPreferences("notes", Context.MODE_PRIVATE).getAll();
            if(m==null||m.values().size()==0){
                TextView tv = new TextView(getActivity());
                tv.setId(8777);
                tv.setPadding(25, 25, 25, 25);
                tv.setText(R.string.no_notes_yet);
                tv.setTextColor(getResources().getColor(R.color.spec_black));
                tv.setTypeface(FilesManagement.getOs(getActivity()));
                tv.setTextSize(25);
                tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                tv.setGravity(Gravity.CENTER);
                main.addView(tv);
            }else {
                o = m.values().toArray();
                textViews = new ArrayList<TextView>();
                for (Object ignored : o) {
                    try {
                        TextView tv = new TextView(getActivity());
                        tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                        tv.setTextColor(getResources().getColor(R.color.spec_gray));
                        tv.setPadding(20, 20, 20, 20);
                        tv.setText(R.string.loading);
                        tv.setBackgroundResource(R.drawable.border_botom);
                        textViews.add(tv);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                LinearLayout left = (LinearLayout) v.findViewById(R.id.left);
                LinearLayout right = (LinearLayout) v.findViewById(R.id.right);
                for (int a = 0; a < textViews.size(); a++) {
                    TextView tv = textViews.get(a);
                    if (a % 2 == 0)
                        left.addView(tv);
                    else
                        right.addView(tv);
                }
                at.execute();
            }
            return v;
        }
        AsyncTask<Void,Integer,Long> at = new AsyncTask<Void, Integer, Long>() {
            @Override
            protected Long doInBackground(Void... params) {
                for(int a=0;a<textViews.size();a++){
                    try {
                        texts.add(FilesManagement.getNoteFromSafe(getActivity(),(String)o[a]));
                        publishProgress(a);
                    } catch (Exception ignore) {}
                }
                return null;
            }
            @Override
            protected void onProgressUpdate(final Integer... progress) {
                TextView tv = textViews.get(progress[0]);
                final String s = texts.get(progress[0]);
                tv.setText(s);
                tv.setTextColor(getResources().getColor(R.color.spec_black));
                tv.setTypeface(FilesManagement.getOs(getActivity()));
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new NoteEdit(getActivity().getFragmentManager(),s);
                    }
                });
            }
        };
        private void addNote(String note){
            TextView tv = new TextView(getActivity());
            tv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            tv.setTextColor(Color.BLACK);
            tv.setBackgroundResource(R.drawable.border_botom);
            tv.setText(note);
            tv.setPadding(20,20,20,20);
            View v = main.findViewById(8777);
            if(v!=null) {
                main.removeView(v);
            }
            LinearLayout left = (LinearLayout) main.findViewById(R.id.left);
            LinearLayout right = (LinearLayout) main.findViewById(R.id.right);
            if(left.getChildCount()>right.getChildCount()){
                right.addView(tv);
            }else{
                left.addView(tv);
            }
        }
    }
    public static View privateNotExist(Activity a,int s){
        FrameLayout fl = new FrameLayout(a);
        fl.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        fl.setBackgroundColor(Color.TRANSPARENT);
        TextView tv = new TextView(a);
        tv.setText(s);
        tv.setTextSize(25);
        tv.setPadding(20, 20, 20, 20);
        tv.setBackgroundColor(Color.WHITE);
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        tv.setLayoutParams(p);
        fl.addView(tv);
        return fl;
    }
}
