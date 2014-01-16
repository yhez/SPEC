package specular.systems;

import android.app.Activity;
import android.content.Intent;
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
import specular.systems.activities.StartScan;

import static specular.systems.R.layout.edit_contact;

public class GroupsAdapter extends ArrayAdapter<Group> implements Filterable {
    private static GroupsAdapter adapter;
    private static List<Group> list;
    int type;
    private Activity a;

    public GroupsAdapter(Activity a) {
        super(a, R.layout.list_row, list);
        this.a = a;
        adapter = this;
        if (GroupDataSource.fullListG == null) {
            GroupDataSource.groupDataSource = new GroupDataSource(a);
            GroupDataSource.fullListG = GroupDataSource.groupDataSource.getAllGroups();
        }
        list = GroupDataSource.fullListG;
    }

    public static GroupsAdapter getAdapter() {
        return adapter;
    }

    public static void updateCont(Activity aa, Group c) {
        for (int a = 0; a < GroupDataSource.fullListG.size(); a++)
            if (GroupDataSource.fullListG.get(a).getId() == c.getId()) {
                GroupDataSource.fullListG.remove(a);
                break;
            }
        GroupDataSource.fullListG.add(c);
        if (adapter != null)
            refreshList(aa);
    }

    public static void removeCont(Activity a, int index) {
        GroupDataSource.fullListG.remove(index);
        if (adapter != null)
            refreshList(a);
    }

    public static void addCont(Activity a, Group c) {
        GroupDataSource.fullListG.add(c);
        if (adapter != null)
            refreshList(a);
    }

    private static void refreshList(Activity a) {
        list = GroupDataSource.fullListG;
        Collections.sort(list, new Comparator<Group>() {
            @Override
            public int compare(Group contact, Group contact2) {
                return (contact.getGroupName().toLowerCase() + contact.getEmail().toLowerCase())
                        .compareTo((contact2.getGroupName().toLowerCase() + contact2.getEmail().toLowerCase()));
            }
        });
        if (FragmentManagement.currentLayout == R.layout.encrypt) {
            View v = a.findViewById(ContactsGroup.GROUPS).findViewById(R.id.list);
            if (v != null)
                if (GroupDataSource.fullListG.size() > 0) {
                    v.setVisibility(View.VISIBLE);
                    a.findViewById(ContactsGroup.GROUPS).findViewById(R.id.no_contacts).setVisibility(View.GONE);
                } else {
                    v.setVisibility(View.GONE);
                    a.findViewById(ContactsGroup.GROUPS).findViewById(R.id.no_contacts).setVisibility(View.VISIBLE);
                }
            if (adapter != null)
                adapter.notifyDataSetChanged();
        }
    }

    public static void showOriginal() {
        if (list != GroupDataSource.fullListG) {
            list = GroupDataSource.fullListG;
            if (adapter != null)
                adapter.notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Group getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = a.getLayoutInflater();
        View rowView;
        rowView = inflater.inflate(R.layout.list_row, parent, false);
        final Group c = list.get(position);
        TextView name = (TextView) rowView.findViewById(R.id.first_line);
        View bt = rowView.findViewById(R.id.scan);
        bt.setVisibility(View.VISIBLE);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(a, StartScan.class);
                i.putExtra("type",StartScan.MESSAGE);
                i.putExtra("id",c.getId());
                a.startActivityForResult(i,Main.SCAN_FOR_GROUP);
            }
        });
        TextView email = (TextView) rowView.findViewById(R.id.sec_line);
        final ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView id = (TextView) rowView.findViewById(R.id.id_contact);
        id.setText("" + c.getId());
            rowView.findViewById(R.id.edit).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Main.exit = false;
                    Fragment fragment = new FragmentManagement();
                    Bundle args = new Bundle();
                    FragmentManagement.currentLayout = edit_contact;
                    args.putBoolean("groups",true);
                    args.putInt("index", position);
                    fragment.setArguments(args);
                    FragmentManager fragmentManager = Main.main.getSupportFragmentManager();
                    fragmentManager.beginTransaction()
                            .replace(R.id.content_frame, fragment).commit();
                }
            });
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Main.main.contactChosen(false,c.getId());
                }
            });
        new Thread(new Runnable() {
            public void run() {
                final Bitmap bitmap = c.getPhoto();
                imageView.postDelayed(new Runnable() {
                    public void run() {
                        imageView.setVisibility(View.VISIBLE);
                        imageView.setImageBitmap(bitmap);
                        imageView.animate().setDuration(500).alpha(1f).start();
                    }
                },100);
            }
        }).start();
        email.setText(c.getEmail());
        email.setTypeface(FilesManagement.getOs(a));
        name.setText(c.getGroupName());
        name.setTypeface(FilesManagement.getOs(a));
        return rowView;
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
                list = (List<Group>) results.values;
                updateContactList();
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                ArrayList<Group> FilteredArrayNames = new ArrayList<Group>();
                constraint = constraint.toString().toLowerCase();
                for (int i = 0; i < GroupDataSource.fullListG.size(); i++) {
                    String dataNames = GroupDataSource.fullListG.get(i).getEmail() + GroupDataSource.fullListG.get(i).getGroupName();
                    if (dataNames.toLowerCase().contains(constraint.toString())) {
                        FilteredArrayNames.add(GroupDataSource.fullListG.get(i));
                    }
                }
                results.values = FilteredArrayNames;
                return results;
            }
        };
    }

    private void updateContactList() {
        if (isEmpty()) {
            a.findViewById(ContactsGroup.GROUPS).findViewById(R.id.list).setVisibility(View.GONE);
            ((TextView) a.findViewById(ContactsGroup.GROUPS).findViewById(R.id.no_contacts)).setText(R.string.no_result_filter);
            a.findViewById(ContactsGroup.GROUPS).findViewById(R.id.no_contacts).setVisibility(View.VISIBLE);
        } else {
            a.findViewById(ContactsGroup.GROUPS).findViewById(R.id.no_contacts).setVisibility(View.GONE);
            a.findViewById(ContactsGroup.GROUPS).findViewById(R.id.list).setVisibility(View.VISIBLE);
        }
    }
}
