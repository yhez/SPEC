package specular.systems;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
    public static final int EDIT = 1, SIMPLE = 2;
    private static MySimpleArrayAdapter adapter;
    private static List<Contact> list;
    int type;
    private Activity a;
    private ArrayList<bmp> fullList = new ArrayList<bmp>();
    private ArrayList<bmp> lstBmp = new ArrayList<bmp>();

    public MySimpleArrayAdapter(Activity a, int type) {
        super(a, R.layout.list_row, list);
        list = ContactsDataSource.fullList;
        this.a = a;
        this.type = type;
        adapter = this;
        if (ContactsDataSource.fullList == null) {
            ContactsDataSource.contactsDataSource = new ContactsDataSource(a);
            ContactsDataSource.fullList = ContactsDataSource.contactsDataSource.getAllContacts();
        }
        for (Contact c : list)
            fullList.add(new bmp(c));
        lstBmp = fullList;
    }

    public static MySimpleArrayAdapter getAdapter() {
        return adapter;
    }

    public void updateCont(Activity aa, Contact c) {
        for (int a = 0; a < ContactsDataSource.fullList.size(); a++)
            if (ContactsDataSource.fullList.get(a).getId() == c.getId()) {
                ContactsDataSource.fullList.remove(a);
                fullList.remove(a);
                break;
            }
        ContactsDataSource.fullList.add(c);
        fullList.add(new bmp(c));
        refreshList(aa);
    }

    public void removeCont(Activity a, int index) {
        ContactsDataSource.fullList.remove(index);
        fullList.remove(index);
        refreshList(a);
    }

    public void addCont(Activity a, Contact c) {
        ContactsDataSource.fullList.add(c);
        fullList.add(new bmp(c));
        refreshList(a);
    }

    private void refreshList(Activity a) {
        list = ContactsDataSource.fullList;
        Collections.sort(list, new Comparator<Contact>() {
            @Override
            public int compare(Contact contact, Contact contact2) {
                return (contact.getContactName().toLowerCase() + contact.getEmail().toLowerCase())
                        .compareTo((contact2.getContactName().toLowerCase() + contact2.getEmail().toLowerCase()));
            }
        });
        lstBmp=fullList;
        Collections.sort(lstBmp, new Comparator<bmp>() {
            @Override
            public int compare(bmp contact, bmp contact2) {
                return contact.nameEmail.compareTo(contact2.nameEmail);
            }
        });
        if (FragmentManagement.currentLayout == R.layout.encrypt) {
            View v = a.findViewById(ContactsGroup.CONTACTS).findViewById(R.id.list);
            if (v != null)
                if (ContactsDataSource.fullList.size() > 0) {
                    v.setVisibility(View.VISIBLE);
                    a.findViewById(ContactsGroup.CONTACTS).findViewById(R.id.no_contacts).setVisibility(View.GONE);
                } else {
                    v.setVisibility(View.GONE);
                    a.findViewById(ContactsGroup.CONTACTS).findViewById(R.id.no_contacts).setVisibility(View.VISIBLE);
                }
            adapter.notifyDataSetChanged();
        }
    }

    public void showOriginal() {
        if (list != ContactsDataSource.fullList) {
            list = ContactsDataSource.fullList;
        }
        if(fullList!=lstBmp){
            lstBmp=fullList;
        }
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

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null ||
                !((TextView) convertView.findViewById(R.id.id_contact)).getText().equals(getItem(position).getId() + "")) {
            if (convertView == null) {
                LayoutInflater inflater = a.getLayoutInflater();
                if (type == EDIT)
                    convertView = inflater.inflate(R.layout.list_row, parent, false);
                else
                    convertView = inflater.inflate(R.layout.simple_list_row, parent, false);
            }
            final Contact c = list.get(position);
            TextView name = (TextView) convertView.findViewById(R.id.first_line);
            TextView email = (TextView) convertView.findViewById(R.id.sec_line);
            final ImageView imageView = (ImageView) convertView.findViewById(R.id.icon);
            TextView id = (TextView) convertView.findViewById(R.id.id_contact);
            id.setText("" + c.getId());
            if (type == EDIT) {
                convertView.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Main.exit = false;
                        Fragment fragment = new FragmentManagement();
                        Bundle args = new Bundle();
                        FragmentManagement.currentLayout = edit_contact;
                        args.putInt("index", position);
                        fragment.setArguments(args);
                        FragmentManager fragmentManager = Main.main.getSupportFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.content_frame, fragment).commit();
                    }
                });
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Main.main.contactChosen(true, c.getId());
                    }
                });
            }
            imageView.setImageBitmap(lstBmp.get(position).bitmap);
            /*
        new Thread(new Runnable() {
            public void run() {
                final Bitmap bitmap = c.getPhoto();
                imageView.postDelayed(new Runnable() {
                    public void run() {
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageBitmap(bitmap);
                        imageView.animate().setDuration(500).alpha(1f).start();
                    }
                }, 250);
            }
        }).start();*/
            email.setText(c.getEmail());
            email.setTypeface(FilesManagement.getOs(a));
            name.setText(c.getContactName());
            name.setTypeface(FilesManagement.getOs(a));
        }
        return convertView;
    }

    //todo should be ok now, this method can probably deleted
    public void updateViewAfterFilter(Activity a) {
        this.a = a;
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
                lstBmp = new ArrayList<bmp>();
                constraint = constraint.toString().toLowerCase();
                for (int i = 0; i < ContactsDataSource.fullList.size(); i++) {
                    String dataNames = ContactsDataSource.fullList.get(i).getEmail() + ContactsDataSource.fullList.get(i).getContactName();
                    if (dataNames.toLowerCase().contains(constraint.toString())) {
                        FilteredArrayNames.add(ContactsDataSource.fullList.get(i));
                        lstBmp.add(fullList.get(i));
                    }
                }
                results.values = FilteredArrayNames;
                return results;
            }
        };
    }

    private void updateContactList() {
        if (isEmpty()) {
            a.findViewById(ContactsGroup.CONTACTS).findViewById(R.id.list).setVisibility(View.GONE);
            ((TextView) a.findViewById(ContactsGroup.CONTACTS).findViewById(R.id.no_contacts)).setText(R.string.no_result_filter);
            a.findViewById(ContactsGroup.CONTACTS).findViewById(R.id.no_contacts).setVisibility(View.VISIBLE);
        } else if(a.findViewById(R.id.contact_id_to_send)==null
                ||((TextView)a.findViewById(R.id.contact_id_to_send)).getText().length()==0){
            a.findViewById(ContactsGroup.CONTACTS).findViewById(R.id.no_contacts).setVisibility(View.GONE);
            a.findViewById(ContactsGroup.CONTACTS).findViewById(R.id.list).setVisibility(View.VISIBLE);
        }
    }

    private class bmp {
        String nameEmail;
        Bitmap bitmap;

        public bmp(Contact c) {
            bitmap = c.getPhoto();
            nameEmail = c.getContactName().toLowerCase() + c.getEmail().toLowerCase();
        }
    }
}