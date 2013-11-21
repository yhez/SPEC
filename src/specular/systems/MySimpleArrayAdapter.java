package specular.systems;

import android.content.Context;
import android.view.Gravity;
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
        return PublicStaticVariables.fullList.size() != 0;
    }

    public void removeCont(int index) {
        PublicStaticVariables.currentList.remove(index);
        PublicStaticVariables.fullList.remove(index);
        notifyDataSetChanged();
    }

    public void addCont(Contact c) {
        PublicStaticVariables.currentList.add(c);
        PublicStaticVariables.fullList.add(c);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
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
    Toast t =Toast.makeText(getContext(), R.string.no_result, Toast.LENGTH_LONG);
    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                List<Contact> lc = (List<Contact>) results.values;
                if (lc.size() == 0){
                    t.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    t.show();
                }
                else
                    t.cancel();
                for (int a = 0; a < lc.size(); a++)
                    if (!PublicStaticVariables.currentList.contains(lc.get(a)))
                        PublicStaticVariables.currentList.add(lc.get(a));
                for (int a = 0; a < PublicStaticVariables.currentList.size(); a++)
                    if (!lc.contains(PublicStaticVariables.currentList.get(a)))
                        PublicStaticVariables.currentList.remove(PublicStaticVariables.currentList.get(a));
                notifyDataSetChanged();
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
        for (int a = 0; a < PublicStaticVariables.fullList.size(); a++)
            if (!PublicStaticVariables.currentList.contains(PublicStaticVariables.fullList.get(a)))
                PublicStaticVariables.currentList.add(PublicStaticVariables.fullList.get(a));
        if (PublicStaticVariables.currentLayout == R.layout.encrypt)
            PublicStaticVariables.luc.show();
        PublicStaticVariables.adapter.notifyDataSetChanged();
    }
}