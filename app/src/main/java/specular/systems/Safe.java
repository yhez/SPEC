package specular.systems;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import specular.systems.Dialogs.FileDlg;
import specular.systems.activities.FilesOpener;


public class Safe extends FragmentStatePagerAdapter {
    public static int currentPage = 0;

    public Safe(FragmentManager fm) {
        super(fm);
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
        return position == 0 ? "Files" : "Notes";
    }

    public static class SectionSafe extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
            //View rootView = inflater.inflate(R.layout.list_files, container, false);
            ListView lv = new ListView(getActivity());//(ListView) rootView.findViewById(R.id.list);
            final ArrayList<ListFiles.FilesRows> files = new ArrayList<ListFiles.FilesRows>();
            SharedPreferences sp = getActivity().getSharedPreferences("saved_files", Context.MODE_PRIVATE);
            Map m = sp.getAll();
            Object[] o = m.values().toArray();
            for (int a = 0; a < o.length; a++)
                files.add(new ListFiles.FilesRows((String) o[a]));
            final ListFiles adapter = new ListFiles(getActivity(),files);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String name = ((TextView)view.findViewById(R.id.text1)).getText().toString();
                    try {
                        FilesManagement.getFromSafe(getActivity(), name);
                        Intent in = new Intent(getActivity(), FilesOpener.class);
                        in.putExtra("file_name", name);
                        startActivity(in);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    final FileDlg fd = new FileDlg(((TextView)view.findViewById(R.id.text1)).getText().toString());
                    fd.show(getActivity().getFragmentManager(),"fd");
                    Handler h = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            fd.getDialog().getWindow().clearFlags(
                                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                            | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                        }
                    },700);
                    return true;
                }
            });
            return lv;
        }
    }

    public static class SectionNotes extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
            return inflater.inflate(R.layout.text_view, container, false);
        }
    }
}
