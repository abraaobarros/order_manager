package com.holandago.wbamanager.ordersmanager;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.holandago.wbamanager.R;
import com.holandago.wbamanager.library.Utils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by razu on 10.09.14.
 */
public class OperationGridAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

        private LayoutInflater inflater;
        private ArrayList<HashMap<String,String>> data;
        private OperationsList mActivity;

        public OperationGridAdapter(OperationsList activity, ArrayList<HashMap<String, String>> data){
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
                convertView = inflater.inflate(R.layout.operation_item,null);
                holder = new OperationViewHolder();
                holder.operation_name = (TextView)convertView.findViewById(R.id.operation_list_part);
                holder.operation_wbaNo = (TextView)convertView.findViewById(R.id.operation_list_wba_no);
                holder.operation_project = (TextView)convertView.findViewById(R.id.operation_list_project);
                holder.background = (RelativeLayout)convertView.findViewById(R.id.operation_list_background);
                convertView.setTag(holder);
            }else{
                holder = (OperationViewHolder)convertView.getTag();
            }

            //Setting up font
            //TODO: Find a smarter way to do this
            Typeface font = Typeface.createFromAsset(mActivity.getAssets(),"HelveticaNeue_Lt.ttf");
            holder.operation_name.setTypeface(font);
            holder.operation_wbaNo.setTypeface(font);
            holder.operation_project.setTypeface(font);

            holder.operation_name.setText(data.get(+position).get(Utils.PART_TAG));
            String wbaNo = data.get(+position).get(Utils.WBA_NUMBER_TAG);

            Log.i("WBA NUMBER","Is this "+wbaNo);
            String[] splitWbaNo = wbaNo.split("\\.");
            try {
                if (splitWbaNo[2].equals("1")) {
                    wbaNo = splitWbaNo[0]+"."+splitWbaNo[1];
                }
            }catch (ArrayIndexOutOfBoundsException e){
                e.printStackTrace();
                Log.e("WBA NUMBER",""+splitWbaNo.length);
            }
            holder.operation_wbaNo.setText(wbaNo);
            holder.operation_project.setText(
                    data.get(+position).get(Utils.PROJECT_NAME_TAG));

            if(getItem(position).get(Utils.STATUS_TAG).equals("1")){
                holder.background.setBackgroundColor(Color.parseColor(Utils.WBA_ORANGE_COLOR));
            }else
            if(getItem(position).get(Utils.STATUS_TAG).equals("2")){
                holder.background.setBackgroundColor(Color.parseColor(Utils.WBA_DARK_GREY_COLOR));
                holder.operation_name.setTextColor(Color.parseColor(Utils.WBA_LIGHT_GREY_COLOR));
                holder.operation_wbaNo.setTextColor(Color.parseColor(Utils.WBA_LIGHT_GREY_COLOR));
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
        TextView operation_wbaNo;
        TextView operation_project;
        RelativeLayout background;
    }
}
