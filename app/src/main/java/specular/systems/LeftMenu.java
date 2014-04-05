package specular.systems;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class LeftMenu extends ArrayAdapter<String> {
    private final Activity context;
    private final String[] s;
    private final int[] drb;

    public LeftMenu(Activity context, String[] s, int[] m) {
        super(context, R.layout.drawer_list_item, s);
        this.context = context;
        this.s = s;
        this.drb = m;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView!=null)
            return convertView;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.drawer_list_item, parent, false);
        ImageView iv = (ImageView)rowView.findViewById(R.id.icon_lst);
        TextView tv = (TextView)rowView.findViewById(R.id.text_lst);
        if(position<(s.length-1)){
            iv.setImageResource(drb[position]);
            tv.setText(s[position]);
        }else{
            rowView.setBackgroundColor(Color.WHITE);
            tv.setTextColor(Color.BLACK);
            if(CryptMethods.privateExist()){
                iv.setImageResource(R.drawable.green);
                tv.setText(R.string.private_exist);
            }else{
                if(CryptMethods.publicExist()){
                    iv.setImageResource(R.drawable.red);
                    tv.setText(R.string.no_private);
                }else{
                    rowView.setVisibility(View.GONE);
                }
            }
        }
        tv.setTypeface(FilesManagement.getOs(context));
        return rowView;
    }
}

