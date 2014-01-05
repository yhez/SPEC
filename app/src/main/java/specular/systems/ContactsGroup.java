package specular.systems;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import specular.systems.widget.SimpleList;


public class ContactsGroup extends FragmentStatePagerAdapter {

    public ContactsGroup(FragmentManager fm){
        super(fm);
    }
    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new PageList();
        return fragment;
    }

    @Override
    public int getCount() {
        return 2;
    }
    @Override
    public CharSequence getPageTitle(int p){
        return (p==0?"contacts":"groups");
    }
    public static class PageList extends Fragment{
        @Override
        public View onCreateView(LayoutInflater layoutInflater,ViewGroup container,Bundle b){
            int i = b.getInt("i");
            ListView lv = new ListView(getActivity());
            SimpleList sl = new SimpleList(getActivity());
            lv.setAdapter(sl);
            return lv;
        }
    }
}
