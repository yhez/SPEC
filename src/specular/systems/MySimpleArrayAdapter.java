package specular.systems;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MySimpleArrayAdapter extends ArrayAdapter<String> implements Filterable {
    private final Activity a;

    public MySimpleArrayAdapter(Activity a, List<?> lst) {
        super(a, R.layout.list_row, (List<String>) lst);
        this.a = a;
    }

    public void updateCont(Activity aa,Contact c, int index,boolean needRefresh) {
        if (!(index < 0)) {
            StaticVariables.fullList.remove(
                    StaticVariables.currentList.get(index));
            StaticVariables.currentList.remove(index);
        } else
            for (int a = 0; a < StaticVariables.fullList.size(); a++)
                if (StaticVariables.fullList.get(a).getId() == c.getId()) {
                    StaticVariables.currentList.remove(StaticVariables.fullList.get(a));
                    StaticVariables.fullList.remove(a);
                    break;
                }
        StaticVariables.currentList.add(c);
        StaticVariables.fullList.add(c);
        if(needRefresh)
            refreshList(aa);
    }

    public void removeCont(Activity a,int index) {
        StaticVariables.fullList.remove(index);
        refreshList(a);
    }

    public void addCont(Activity a,Contact c) {
        StaticVariables.fullList.add(c);
        refreshList(a);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) a
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_row, parent, false);
        Contact c = StaticVariables.currentList.get(position);
        TextView name = (TextView) rowView.findViewById(R.id.first_line);
        TextView email = (TextView) rowView.findViewById(R.id.sec_line);
        // TextView session = (TextView) rowView.findViewById(R.id.thirdLine);
        // TextView status = (TextView) rowView.findViewById(R.id.forthLine);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView id = (TextView) rowView.findViewById(R.id.id_contact);
        id.setText("" + c.getId());
        imageView.setImageBitmap(c.getPhoto());
        email.setText(c.getEmail());
        email.setTypeface(FilesManagement.getOs(a));
        // session.setText(c.getSession());
        name.setText(c.getContactName());
        name.setTypeface(FilesManagement.getOs(a));
        //pbk.setText(c.getPublicKey());
        // status.setText("" + c.getConversationStatus());
        return rowView;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                List<Contact> lc = (List<Contact>) results.values;
                //todo
                //todo
                //todo i dont need to copy it to another array just work with it until you done and then go to original list
                if (lc.size() > 0) {
                    for (Contact aLc : lc)
                        if (!StaticVariables.currentList.contains(aLc))
                            StaticVariables.currentList.add(aLc);
                    for (int a = 0; a < StaticVariables.currentList.size(); a++)
                        if (!lc.contains(StaticVariables.currentList.get(a)))
                            StaticVariables.currentList.remove(StaticVariables.currentList.get(a));
                    Collections.sort(StaticVariables.currentList, new Comparator<Contact>() {
                        @Override
                        public int compare(Contact contact, Contact contact2) {
                            return contact.getEmail().compareTo(contact2.getEmail());
                        }
                    });
                    notifyDataSetChanged();
                }
                else{
                    StaticVariables.currentList.removeAll(StaticVariables.currentList);
                    notifyDataSetChanged();
                }
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                ArrayList<Contact> FilteredArrayNames = new ArrayList<Contact>();
                constraint = constraint.toString().toLowerCase();
                for (int i = 0; i < StaticVariables.fullList.size(); i++) {
                    String dataNames = StaticVariables.fullList.get(i).getEmail() + StaticVariables.fullList.get(i).getContactName();
                    if (dataNames.toLowerCase().contains(constraint.toString())) {
                        FilteredArrayNames.add(StaticVariables.fullList.get(i));
                    }
                }
                results.values = FilteredArrayNames;
                return results;
            }
        };
        return filter;
    }

    public void refreshList(Activity a) {
        ArrayList<Contact> tmp = new ArrayList<Contact>();
        boolean changed=false;
        for (Contact c : StaticVariables.currentList)
            if (!StaticVariables.fullList.contains(c))
                tmp.add(c);
        if(tmp.size()>0){
            StaticVariables.currentList.removeAll(tmp);
            changed=true;
        }
        for (Contact c : StaticVariables.fullList)
            if (!StaticVariables.currentList.contains(c)){
                StaticVariables.currentList.add(c);
                changed=true;
            }

        if(changed){
            Collections.sort(StaticVariables.currentList, new Comparator<Contact>() {
                @Override
                public int compare(Contact contact, Contact contact2) {
                    return contact.getEmail().compareTo(contact2.getEmail());
                }
            });
            StaticVariables.adapter.notifyDataSetChanged();
        }
        View v = a.findViewById(R.id.list);
        if (v != null)
            if (v.getVisibility() == View.GONE) {
                v.setVisibility(View.VISIBLE);
                a.findViewById(R.id.no_contacts).setVisibility(View.GONE);
            } else {
                if (StaticVariables.fullList.size() == 0) {
                    v.setVisibility(View.GONE);
                    a.findViewById(R.id.no_contacts).setVisibility(View.VISIBLE);
                }
            }
    }
}