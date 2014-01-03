package specular.systems;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
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

import specular.systems.activities.Main;

import static specular.systems.R.layout.edit_contact;

public class MySimpleArrayAdapter extends ArrayAdapter<Contact> implements Filterable {
    private Activity a;
    private static List<Contact> list;

    public MySimpleArrayAdapter(Activity a) {
        super(a, R.layout.list_row, list);
        list=StaticVariables.fullList;
        this.a = a;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Contact getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateCont(Activity aa, Contact c) {
        for (int a = 0; a < StaticVariables.fullList.size(); a++)
            if (StaticVariables.fullList.get(a).getId() == c.getId()) {
                StaticVariables.fullList.remove(a);
                break;
            }
        StaticVariables.fullList.add(c);
        refreshList(aa);
    }

    public void removeCont(Activity a, int index) {
        StaticVariables.fullList.remove(index);
        refreshList(a);
    }

    public void addCont(Activity a, Contact c) {
        StaticVariables.fullList.add(c);
        refreshList(a);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) a
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_row, parent, false);
        final Contact c = list.get(position);
        TextView name = (TextView) rowView.findViewById(R.id.first_line);
        TextView email = (TextView) rowView.findViewById(R.id.sec_line);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView id = (TextView) rowView.findViewById(R.id.id_contact);
        id.setText("" + c.getId());
        rowView.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Main.exit=false;
                Fragment fragment = new FragmentManagement();
                Bundle args = new Bundle();
                StaticVariables.currentLayout = edit_contact;
                args.putInt("index", position);
                fragment.setArguments(args);
                FragmentManager fragmentManager = StaticVariables.main.getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragment).commit();
            }
        });
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StaticVariables.fragmentManagement.contactChosen(c.getId());
            }
        });
        imageView.setImageBitmap(c.getPhoto());
        email.setText(c.getEmail());
        email.setTypeface(FilesManagement.getOs(a));
        name.setText(c.getContactName());
        name.setTypeface(FilesManagement.getOs(a));
        return rowView;
    }
    public void updateViewAfterFilter(Activity a){
        this.a=a;
    }
    @Override
    public Filter getFilter() {
        return new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                list = (List<Contact>) results.values;
                updateContactList();
                notifyDataSetChanged();
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
    }

    private void refreshList(Activity a) {
        list = StaticVariables.fullList;
        Collections.sort(list, new Comparator<Contact>() {
            @Override
            public int compare(Contact contact, Contact contact2) {
                return (contact.getContactName().toLowerCase()+contact.getEmail().toLowerCase())
                        .compareTo((contact2.getContactName().toLowerCase()+contact2.getEmail().toLowerCase()));
            }
        });
        if(StaticVariables.currentLayout==R.layout.encrypt){
        View v = a.findViewById(R.id.list);
        if (v != null)
            if (StaticVariables.fullList.size() > 0) {
                v.setVisibility(View.VISIBLE);
                a.findViewById(R.id.no_contacts).setVisibility(View.GONE);
            } else {
                v.setVisibility(View.GONE);
                a.findViewById(R.id.no_contacts).setVisibility(View.VISIBLE);
            }
            notifyDataSetChanged();
        }
    }
    public void showOriginal(){
        if(list!=StaticVariables.fullList){
            list = StaticVariables.fullList;
            notifyDataSetChanged();
        }
    }
    private void updateContactList() {
        if (isEmpty()) {
            a.findViewById(R.id.list).setVisibility(View.GONE);
            ((TextView) a.findViewById(R.id.no_contacts)).setText(R.string.no_result_filter);
            a.findViewById(R.id.no_contacts).setVisibility(View.VISIBLE);
        } else {
            a.findViewById(R.id.no_contacts).setVisibility(View.GONE);
            a.findViewById(R.id.list).setVisibility(View.VISIBLE);
        }
    }
}