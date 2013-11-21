package specular.systems;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class LeftMenu extends ArrayAdapter<String> {
    private final Context context;
    private final String[] s;
    private final int[] drb;

    public LeftMenu(Context context, String[] s, int[] m) {
        super(context, R.layout.drawer_list_item, s);
        this.context = context;
        this.s = s;
        this.drb = m;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.drawer_list_item, parent, false);
        ((ImageView) rowView.findViewById(R.id.icon_lst)).setImageResource(drb[position]);
        ((TextView) rowView.findViewById(R.id.text_lst)).setText(s[position]);
        return rowView;
    }
}

