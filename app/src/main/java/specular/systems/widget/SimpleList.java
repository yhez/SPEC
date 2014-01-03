package specular.systems.widget;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import specular.systems.Contact;
import specular.systems.ContactsDataSource;
import specular.systems.FilesManagement;
import specular.systems.R;
import specular.systems.StaticVariables;

public class SimpleList extends ArrayAdapter<Contact>  {
    private static List<Contact> list;
    private Activity a;

    public SimpleList(Activity a) {
        super(a, R.layout.simple_list_row, list);
        if(StaticVariables.fullList==null){
            ContactsDataSource cdc = new ContactsDataSource(a);
            StaticVariables.fullList=cdc.getAllContacts();
        }
        list=StaticVariables.fullList;
        this.a=a;
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
        LayoutInflater inflater = (LayoutInflater) a
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.simple_list_row, parent, false);
        final Contact c = list.get(position);
        TextView name = (TextView) rowView.findViewById(R.id.first_line);
        TextView email = (TextView) rowView.findViewById(R.id.sec_line);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView id = (TextView) rowView.findViewById(R.id.id_contact);
        id.setText("" + c.getId());
        imageView.setImageBitmap(c.getPhoto());
        email.setText(c.getEmail());
        email.setTypeface(FilesManagement.getOs(a));
        name.setText(c.getContactName());
        name.setTypeface(FilesManagement.getOs(a));
        return rowView;
    }
}
