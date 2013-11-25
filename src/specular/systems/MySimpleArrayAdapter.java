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

class MySimpleArrayAdapter extends ArrayAdapter<String> implements Filterable {
    private final Activity a;

    public MySimpleArrayAdapter(Activity a, List<?> lst) {
        super(a, R.layout.list_row, (List<String>) lst);
        this.a = a;
    }

    public void updateCont(Contact c){
        Contact contact = PublicStaticVariables.contactsDataSource.findContact(c.getId());
        PublicStaticVariables.currentList.add(contact);
        PublicStaticVariables.fullList.add(contact);
        refreshList();
    }

    public void removeCont(int index) {
        PublicStaticVariables.fullList.remove(index);
        refreshList();
    }

    public void addCont(Contact c) {
        PublicStaticVariables.fullList.add(c);
        refreshList();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) a
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_row, parent, false);
        Contact c = PublicStaticVariables.currentList.get(position);
        TextView name = (TextView) rowView.findViewById(R.id.first_line);
        TextView email = (TextView) rowView.findViewById(R.id.sec_line);
        // TextView session = (TextView) rowView.findViewById(R.id.thirdLine);
        // TextView status = (TextView) rowView.findViewById(R.id.forthLine);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView id = (TextView) rowView.findViewById(R.id.id_contact);
        id.setText("" + c.getId());
        imageView.setImageBitmap(Contact.getPhoto(c.getPublicKey()));
        email.setText(c.getEmail());
        // session.setText(c.getSession());
        name.setText(c.getContactName());
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
                if (lc.size() == 0) {
                    a.findViewById(R.id.list).setVisibility(View.GONE);
                    ((TextView) a.findViewById(R.id.no_contacts)).setText(R.string.no_result_filter);
                    a.findViewById(R.id.no_contacts).setVisibility(View.VISIBLE);
                } else {
                    a.findViewById(R.id.no_contacts).setVisibility(View.GONE);
                    a.findViewById(R.id.list).setVisibility(View.VISIBLE);
                    for (Contact aLc : lc)
                        if (!PublicStaticVariables.currentList.contains(aLc))
                            PublicStaticVariables.currentList.add(aLc);
                    for (int a = 0; a < PublicStaticVariables.currentList.size(); a++)
                        if (!lc.contains(PublicStaticVariables.currentList.get(a)))
                            PublicStaticVariables.currentList.remove(PublicStaticVariables.currentList.get(a));
                    Collections.sort(PublicStaticVariables.currentList, new Comparator<Contact>() {
                        @Override
                        public int compare(Contact contact, Contact contact2) {
                            return contact.getEmail().compareTo(contact2.getEmail());
                        }
                    });
                    notifyDataSetChanged();
                }
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                ArrayList<Contact> FilteredArrayNames = new ArrayList<Contact>();
                constraint = constraint.toString().toLowerCase();
                for (int i = 0; i < PublicStaticVariables.fullList.size(); i++) {
                    String dataNames = PublicStaticVariables.fullList.get(i).getEmail() + PublicStaticVariables.fullList.get(i).getContactName();
                    if (dataNames.toLowerCase().contains(constraint.toString())) {
                        FilteredArrayNames.add(PublicStaticVariables.fullList.get(i));
                    }
                }
                results.values = FilteredArrayNames;
                return results;
            }
        };
        return filter;
    }

    public void refreshList() {
        ArrayList<Contact> tmp = new ArrayList<Contact>();
        for(Contact c:PublicStaticVariables.currentList)
            if(!PublicStaticVariables.fullList.contains(c))
                tmp.add(c);
        PublicStaticVariables.currentList.removeAll(tmp);
        for (Contact c:PublicStaticVariables.fullList)
            if (!PublicStaticVariables.currentList.contains(c))
                PublicStaticVariables.currentList.add(c);
        if (PublicStaticVariables.currentLayout == R.layout.encrypt &&
                PublicStaticVariables.fullList.size() > PublicStaticVariables.minContactSize)
            PublicStaticVariables.luc.show();
        Collections.sort(PublicStaticVariables.currentList, new Comparator<Contact>() {
            @Override
            public int compare(Contact contact, Contact contact2) {
                return contact.getEmail().compareTo(contact2.getEmail());
            }
        });
        PublicStaticVariables.adapter.notifyDataSetChanged();
        View v = a.findViewById(R.id.list);
        if (v != null)
            if (v.getVisibility() == View.GONE) {
                v.setVisibility(View.VISIBLE);
                a.findViewById(R.id.no_contacts).setVisibility(View.GONE);
            } else {
                if (PublicStaticVariables.fullList.size() == 0) {
                    v.setVisibility(View.GONE);
                    a.findViewById(R.id.no_contacts).setVisibility(View.VISIBLE);
                }
            }
    }
}