package specular.systems;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;


public class ContactsGroup extends FragmentStatePagerAdapter {
    public static final int CONTACTS = 97978, GROUPS = 97979;

    public ContactsGroup(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new PageList(i==0?CONTACTS:GROUPS);
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int p) {
        return (p == 0 ? "Contacts" : "Groups");
    }

    public static class PageList extends Fragment {
        private int currentLayout;

        public PageList(int currentLayout) {
            super();
            this.currentLayout = currentLayout;
        }

        @Override
        public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle b) {
            View rootView = layoutInflater.inflate(R.layout.list, container, false);
            StaticVariables.luc.showIfNeeded(getActivity(), rootView);
            TextView tv = (TextView) rootView.findViewById(R.id.no_contacts);
            ListView lv = (ListView) rootView.findViewById(R.id.list);
            if (currentLayout == CONTACTS) {
                rootView.setId(CONTACTS);
                if (StaticVariables.fullList.size() == 0) {
                    tv.setVisibility(View.VISIBLE);
                    lv.setVisibility(View.GONE);
                } else {
                    lv.setAdapter(new MySimpleArrayAdapter(getActivity(),MySimpleArrayAdapter.EDIT));
                    MySimpleArrayAdapter.showOriginal();
                    lv.setVisibility(View.VISIBLE);
                    tv.setVisibility(View.GONE);
                }
            } else if (currentLayout == GROUPS) {
                rootView.setId(GROUPS);
                if(StaticVariables.fullListG.size()==0){
                    tv.setText(R.string.there_is_no_groups);
                    tv.setVisibility(View.VISIBLE);
                    lv.setVisibility(View.GONE);
                }else{
                    lv.setAdapter(new GroupsAdapter(getActivity(),GroupsAdapter.EDIT));
                    GroupsAdapter.showOriginal();
                    lv.setVisibility(View.VISIBLE);
                    tv.setVisibility(View.GONE);
                }
            }
            Visual.setAllFonts(getActivity(), (ViewGroup) rootView);
            return rootView;
        }
    }
}
