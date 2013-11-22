package specular.systems;

import android.app.Activity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;


public class LastUsedContacts {
    final int NUM_LASTS = 4;
    final Contact[] lasts;
    ViewLastContact[] vlc;
    final Activity a;
    final long[] ids;
    int[] num;

    public LastUsedContacts(Activity a) {
        this.a = a;
        lasts = new Contact[NUM_LASTS];
        ids = new long[NUM_LASTS];
        num = new int[NUM_LASTS];
        String m = FilesManagement.getlasts(a);
        if (m != null) {
            String[] t = m.split(",");
            num = new int[t.length];
            for (int c = 0; c < t.length; c++)
                num[c] = Integer.parseInt(t[c].split("-")[1]);
            for (int c = 0; c < t.length && c < NUM_LASTS; c++) {
                long lg = Long.parseLong(t[c].split("-")[0]);
                for (int b = 0; b < PublicStaticVariables.fullList.size(); b++) {
                    if (PublicStaticVariables.fullList.get(b).getId() == lg) {
                        lasts[c] = PublicStaticVariables.fullList.get(b);
                        ids[c] = lg;
                        break;
                    }
                }
            }
        }
    }

    private int smallest(int[] arr) {
        int small = 0;
        for (int a = 1; a < arr.length; a++)
            if (arr[a] < arr[small])
                small = a;
        return small;
    }

    public void hide() {
        View v =a.findViewById(R.id.grid_lasts);
                if(v!=null)v.setVisibility(View.GONE);
    }

    public boolean show() {
        if (lasts.length == 0 || lasts[0] == null || a.findViewById(R.id.grid_lasts) == null) {
            return false;
        }
        a.findViewById(R.id.grid_lasts).setVisibility(View.VISIBLE);
        vlc = new ViewLastContact[NUM_LASTS];
        for (int c = 0; c < NUM_LASTS; c++) {
            vlc[c] = new ViewLastContact((LinearLayout) ((GridLayout) a.findViewById(R.id.grid_lasts)).getChildAt(c));
        }
        for (int c = 0; c < NUM_LASTS; c++)
            if (lasts[c] != null)
                vlc[c].setContent(lasts[c]);
        return true;
    }

    public void change(Contact contact) {
        int index;
        boolean isNew = true;
        int i;
        for (i = 0; i < lasts.length; i++)
            if (contact.getId() == ids[i]) {
                isNew = false;
                break;
            }
        if (!isNew)
            index = i;
        else if (lasts[3] == null)
            if (lasts[2] == null)
                if (lasts[1] == null)
                    if (lasts[0] == null)
                        index = 0;
                    else index = 1;
                else index = 2;
            else index = 3;
        else
            index = smallest(num);
        lasts[index] = contact;
        ids[index] = contact.getId();
        num[index] = isNew ? 1 : num[index] + 1;
        String raw = "";
        for (int n = 0; n < NUM_LASTS; n++)
            raw += ids[n] + "-" + num[n] + ",";
        FilesManagement.updateLasts(a, raw);
    }

    class ViewLastContact {
        final ImageButton ib;
        final TextView tv;
        final TextView id;

        public ViewLastContact(LinearLayout fl) {
            tv = (TextView) fl.getChildAt(1);
            ib = (ImageButton) fl.getChildAt(0);
            id = (TextView) fl.getChildAt(2);
        }

        public ImageButton getImage() {
            return ib;
        }

        public TextView getText() {
            return tv;
        }

        public void setContent(Contact contact) {
            ib.setImageBitmap(Contact.getPhoto(contact.getPublicKey()));
            tv.setText(contact.getContactName());
            id.setText(contact.getId() + "");
        }
    }
}
