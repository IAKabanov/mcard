package ru.kai.mcard;

/**
 * Created by akabanov on 09.01.2016.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class VisitsSearchArrayAdapter extends ArrayAdapter<String>{
    private Context mContext;
    private LayoutInflater mInflater;
    private List<String> mSourseList = null;
    private ArrayList<String> mArraylist;

    VisitsSearchArrayAdapter(Context context, int layoutResurceID, List<String> sourseList){
        super(context, layoutResurceID,sourseList);
        this.mContext = context;
        this.mSourseList = sourseList;
        this.mInflater = LayoutInflater.from(mContext);
        this.mArraylist = new ArrayList<String>();
        this.mArraylist.addAll(sourseList);
    }

    public class ViewHolder {
        TextView name;
    }

    @Override
    public int getCount() {
        return mSourseList.size();
    }

    @Override
    public String getItem(int position) {
        return mSourseList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;
        if (convertView == null){
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.list_row_dropdown, null);
            holder.name = (TextView)convertView.findViewById(R.id.lrdd_adapter_name);
            convertView.setTag(holder);

        }else {
            holder = (ViewHolder)convertView.getTag();
        }
        holder.name.setText(mSourseList.get(position).toString());

        return convertView;
    }


    // Filter Class
    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        mSourseList.clear();
        if (charText.length() == 0) {
            mSourseList.addAll(mArraylist);
        } else {
            for (String wp : mArraylist) {
                if (wp.toString().toLowerCase(Locale.getDefault())
                        .contains(charText)) {
                    mSourseList.add(wp);
                }
            }
        }
        notifyDataSetChanged();
    }

}
