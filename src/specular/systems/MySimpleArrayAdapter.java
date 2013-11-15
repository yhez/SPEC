package specular.systems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

class MySimpleArrayAdapter extends ArrayAdapter<String> implements Filterable {
    private final Context context;

    public MySimpleArrayAdapter(Context context, List<?> lst) {
        super(context, R.layout.list_row, (List<String>) lst);
        this.context = context;
    }

    public boolean isNotEmpty() {
        return Main.fullList.size() != 0;
    }

    public void removeCont(int index) {
        Main.currentList.remove(index);
        Main.fullList.remove(index);
        notifyDataSetChanged();
    }

    public void addCont(Contact c) {
        Main.currentList.add(c);
        Main.fullList.add(c);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_row, parent, false);
            Contact c = Main.currentList.get(position);
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
                if (lc.size() == 0)
                    Toast.makeText(getContext(), R.string.no_result, Toast.LENGTH_LONG).show();
                for (int a = 0; a < lc.size(); a++)
                    if (!Main.currentList.contains(lc.get(a)))
                        Main.currentList.add(lc.get(a));
                for (int a = 0; a < Main.currentList.size(); a++)
                    if (!lc.contains(Main.currentList.get(a)))
                        Main.currentList.remove(Main.currentList.get(a));
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                ArrayList<Contact> FilteredArrayNames = new ArrayList<Contact>();
                constraint = constraint.toString().toLowerCase();
                for (int i = 0; i < Main.fullList.size(); i++) {
                    String dataNames = Main.fullList.get(i).getEmail() + Main.fullList.get(i).getContactName();
                    if (dataNames.toLowerCase().contains(constraint.toString())) {
                        FilteredArrayNames.add(Main.fullList.get(i));
                    }
                }
                results.values = FilteredArrayNames;
                return results;
            }
        };
        return filter;
    }
}