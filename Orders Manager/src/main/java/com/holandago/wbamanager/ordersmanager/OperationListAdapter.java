package com.holandago.wbamanager.ordersmanager;

import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.holandago.wbamanager.R;
import com.holandago.wbamanager.library.Utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by razu on 11.09.14.
 */
public class OperationListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener{
    private LayoutInflater inflater;
    private ArrayList<HashMap<String,String>> data;
    private OperationsList mActivity;

    public OperationListAdapter(OperationsList activity, ArrayList<HashMap<String, String>> data){
        this.inflater = LayoutInflater.from(activity);
        this.data = data;
        mActivity = activity;
    }

    public int getCount(){
        return data.size();
    }

    @Override
    public void notifyDataSetChanged(){
        super.notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public HashMap<String,String> getItem(int position){
        return data.get(+position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        OperationViewHolder holder;
        if(convertView == null){
            convertView = inflater.inflate(R.layout.operations_list_v,null);
            holder = new OperationViewHolder();
            holder.operation_name = (TextView)convertView.findViewById(R.id.operation_list_part);
            holder.operation_machine =
                    (TextView)convertView.findViewById(R.id.operation_list_machine);
            holder.operation_wbaNo = (TextView)convertView.findViewById(R.id.operation_list_wba_no);
            holder.operation_time = (TextView)convertView.findViewById(R.id.operation_list_time);
            holder.operation_project = (TextView)convertView.findViewById(R.id.operation_list_project);
            holder.background = (LinearLayout)convertView.findViewById(R.id.operation_list_background);
            convertView.setTag(holder);
        }else{
            holder = (OperationViewHolder)convertView.getTag();
        }

        //Setting up font
        //TODO: Find a smarter way to do this
        Typeface font = Typeface.createFromAsset(mActivity.getAssets(),"HelveticaNeue_Lt.ttf");
        holder.operation_name.setTypeface(font, Typeface.BOLD);
        holder.operation_machine.setTypeface(font);
        holder.operation_wbaNo.setTypeface(font);
        holder.operation_time.setTypeface(font);
        holder.operation_project.setTypeface(font);

        holder.operation_name.setText(data.get(+position).get(Utils.PART_TAG));
        holder.operation_machine.setText(data.get(+position).get(Utils.MACHINE_TAG));
        holder.operation_wbaNo.setText("WBA Nr.: "+data.get(+position).get(Utils.WBA_NUMBER_TAG));
        holder.operation_time.setText("Zeit: "+data.get(+position).get(Utils.TIME_TAG));
        holder.operation_project.setText(
                data.get(+position).get(Utils.PROJECT_NAME_TAG));

        if(getItem(position).get(Utils.STATUS_TAG).equals("1")){
            holder.background.setBackgroundColor(Color.parseColor(Utils.WBA_ORANGE_COLOR));
        }else
        if(getItem(position).get(Utils.STATUS_TAG).equals("2")){
            holder.background.setBackgroundColor(Color.parseColor(Utils.WBA_DARK_GREY_COLOR));
            holder.operation_name.setTextColor(Color.parseColor(Utils.WBA_LIGHT_GREY_COLOR));
            holder.operation_machine.setTextColor(Color.parseColor(Utils.WBA_LIGHT_GREY_COLOR));
            holder.operation_wbaNo.setTextColor(Color.parseColor(Utils.WBA_LIGHT_GREY_COLOR));
            holder.operation_time.setTextColor(Color.parseColor(Utils.WBA_LIGHT_GREY_COLOR));
            holder.operation_project.setTextColor(Color.parseColor(Utils.WBA_LIGHT_GREY_COLOR));
        }else if(getItem(position).get(Utils.STATUS_TAG).equals("3")){
            holder.background.setBackgroundColor(Color.parseColor(Utils.WBA_BLUE_COLOR));
        }
        else{
            holder.background.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
        }

        return convertView;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
        //Sends the operations part of the JSONObject to the next activity
        mActivity.sendOperationMessage(data.get(+position));
    }

    private static class OperationViewHolder {
        TextView operation_name;
        TextView operation_machine;
        TextView operation_wbaNo;
        TextView operation_time;
        TextView operation_project;
        LinearLayout background;
    }

}

