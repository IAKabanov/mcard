package ru.kai.mcard;

import android.content.Context;
import android.database.Cursor;
import android.provider.Browser;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import java.util.ArrayList;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class CheckBoxSimpleCursorAdapter extends SimpleCursorAdapter {

    private Cursor c;
    private Context context;
    private ArrayList<Integer> checkList = new ArrayList<Integer>();
    private int currentProfileID;
    private ViewHolder viewHolder;

    public CheckBoxSimpleCursorAdapter(Context context, int layout, Cursor c,
                                       String[] from, int[] to, int i, int currentProfileID) {
        super(context, layout, c, from, to, i);
        this.c = c;
        this.context = context;
        this.currentProfileID = currentProfileID;
    }

    public void setCurrentProfileID(int currentProfileID){
        this.currentProfileID = currentProfileID;
    }

    public View getView(int pos, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_row_profiles, null);
            viewHolder = new ViewHolder();
            viewHolder.checkBox = (CheckedTextView) convertView.findViewById(R.id.lrp_adapter_profiles_check);
            viewHolder.tvID = (TextView) convertView.findViewById(R.id.lrp_adapter_profiles_id);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.lrp_adapter_profiles_name);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        this.c.moveToPosition(pos);

        String stringID = this.c.getString(this.c.getColumnIndex(DBMethods.TableProfiles._ID));
        viewHolder.tvID.setText(stringID);
        if (Integer.parseInt(stringID) == this.currentProfileID) {
            viewHolder.checkBox.setChecked(true);
        }else{
            viewHolder.checkBox.setChecked(false);
        }
        viewHolder.tvName.setText(this.c.getString(this.c.getColumnIndex(DBMethods.TableProfiles.COLUMN_PROFILES_NAME)));


        return(convertView);
    }

    static class ViewHolder {
        public CheckedTextView checkBox;
        public TextView tvID;
        public TextView tvName;
    }


}