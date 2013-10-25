package specular.systems;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class MySimpleArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<Contact> lst;

    public MySimpleArrayAdapter(Context context, String[] s, List<Contact> lst) {
        super(context, R.layout.list_row, s);
        this.context = context;
        this.lst = lst;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_row, parent, false);
        Contact c = lst.get(position);
        TextView name = (TextView) rowView.findViewById(R.id.first_line);
       // TextView email = (TextView) rowView.findViewById(R.id.secondLine);
       // TextView session = (TextView) rowView.findViewById(R.id.thirdLine);
       // TextView status = (TextView) rowView.findViewById(R.id.forthLine);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        TextView id = (TextView) rowView.findViewById(R.id.id_contact);
        id.setText("" + c.getId());
        imageView.setImageBitmap(c.getPhoto());
        // email.setText(c.getEmail());
        // session.setText(c.getSession());
        name.setText(c.getContactName());
        //pbk.setText(c.getPublicKey());
        // status.setText("" + c.getConversationStatus());
        return rowView;
    }
}
