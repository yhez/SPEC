package specular.systems;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Map;

import specular.systems.Dialogs.NotImplemented;


public class Safe extends FragmentStatePagerAdapter {
    public static int currentPage = 0;

    public Safe(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return i==0?new SectionSafe():new SectionNotes();
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
            View rootView = inflater.inflate(R.layout.list_files,container,false);
            ListView lv = (ListView)rootView.findViewById(R.id.list);
            final ArrayList<String> files = new ArrayList<String>();
            SharedPreferences sp = getActivity().getSharedPreferences("saved_files", Context.MODE_PRIVATE);
            Map m = sp.getAll();
            Object[] o = m.values().toArray();
            for (int a = 0; a < o.length; a++)
                files.add((String) o[a]);
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, files);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    NotImplemented ni = new NotImplemented();
                    ni.show(getActivity().getFragmentManager(), "hfhf");
                }
            });
            return rootView;
        }
    }

    public static class SectionNotes extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
            return inflater.inflate(R.layout.text_view,container,false);
        }
    }
}
