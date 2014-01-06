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

    public ContactsGroup(FragmentManager fm){
        super(fm);
    }
    @Override
    public Fragment getItem(int i) {
        Fragment fragment = new PageList(i);
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
        int index;
        public PageList(int i){
            super();
            index=i;
        }
        @Override
        public View onCreateView(LayoutInflater layoutInflater,ViewGroup container,Bundle b){
            View rootView = layoutInflater.inflate(R.layout.list,container,false);
            StaticVariables.luc.showIfNeeded(getActivity(), rootView);
            TextView tv = (TextView)rootView.findViewById(R.id.no_contacts);
            ListView lv = (ListView)rootView.findViewById(R.id.list);
            if(index==0){
                if (StaticVariables.fullList.size() == 0) {
                    tv.setVisibility(View.VISIBLE);
                    lv.setVisibility(View.GONE);
                }else{
                    lv.setAdapter(MySimpleArrayAdapter.adapter);
                    MySimpleArrayAdapter.adapter.showOriginal();
                    lv.setVisibility(View.VISIBLE);
                    tv.setVisibility(View.GONE);
                }

            }else{
                tv.setText("no groups yet");
                tv.setVisibility(View.VISIBLE);
                lv.setVisibility(View.GONE);
            }
            return rootView;
        }
    }
}
