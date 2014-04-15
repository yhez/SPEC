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
    public ContactsGroup(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        return new PageList();
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public CharSequence getPageTitle(int p) {
        return "Contacts";
    }

    public static class PageList extends Fragment {
        public PageList(){
            super();
        }

        @Override
        public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle b) {
            View rootView = layoutInflater.inflate(R.layout.list, container, false);
            StaticVariables.luc.showIfNeeded(getActivity(), rootView);
            TextView tv = (TextView) rootView.findViewById(R.id.no_contacts);
            final ListView lv = (ListView) rootView.findViewById(R.id.list);
                MySimpleArrayAdapter ms = MySimpleArrayAdapter.getAdapter();
                if (ms != null)
                    ms.showOriginal();
                else
                    ms = new MySimpleArrayAdapter(getActivity());
                ms.setFlag(MySimpleArrayAdapter.EDIT);
                lv.setAdapter(ms);
                if (ContactsDataSource.fullList.size() == 0) {
                    tv.setVisibility(View.VISIBLE);
                    lv.setVisibility(View.GONE);
                } else {
                    lv.setVisibility(View.VISIBLE);
                    tv.setVisibility(View.GONE);
                }
            Visual.setAllFonts(getActivity(), (ViewGroup) rootView);
            return rootView;
        }
    }
}
