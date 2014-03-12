package specular.systems;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;


public class ListFiles extends ArrayAdapter<ListFiles.FilesRows> {
    private final Activity context;
    private final ArrayList<FilesRows> s;

    public ListFiles(Activity context, ArrayList<FilesRows> s) {
        super(context, R.layout.row_files, s);
        this.context = context;
        this.s = s;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.row_files, parent, false);
        ((ImageView) rowView.findViewById(R.id.image)).setImageResource(s.get(position).drb);
        TextView tv = (TextView) rowView.findViewById(R.id.text1);
        tv.setText(s.get(position).name);
        TextView tv2 = (TextView) rowView.findViewById(R.id.text2);
        tv2.setText(Visual.getSize(new File(context.getFilesDir()+"/safe",s.get(position).name).length()));
        tv2.setTypeface(FilesManagement.getOs(context));
        return rowView;
    }
    public static class FilesRows{
        public String name;
        public int drb;
        public FilesRows(String name){
            this.name = name;
            Visual.types t = Visual.getType(name);
            switch (t){
                case AUDIO:
                    drb = R.drawable.music;
                    break;

                case IMAGE:
                    drb = R.drawable.image;
                    break;

                case VIDEO:
                    drb = R.drawable.movie;
                    break;

                case TEXT:
                    drb = R.drawable.text;
                    break;

                case PDF:
                    drb = R.drawable.pdf;
                    break;

                case ZIP:
                    drb = R.drawable.compressed;
                    break;

                case APK:
                    drb = R.drawable.apk;
                    break;

                case UNKNOWN:
                    drb = R.drawable.unknown2;
                    break;

                case DOC:
                    drb = R.drawable.word;
                    break;

            }
        }
    }
}
