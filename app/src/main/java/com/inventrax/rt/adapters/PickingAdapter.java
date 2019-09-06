package com.inventrax.rt.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.inventrax.rt.R;
import com.inventrax.rt.model.BatchPickDTO;

import java.util.List;

public class PickingAdapter extends  RecyclerView.Adapter{

    private List<BatchPickDTO> obdInfoList;
    Context context;
    private OnItemClickListener onItemClickListener;
    private int selectedItem = RecyclerView.NO_POSITION;

    public PickingAdapter(Context context, List<BatchPickDTO> list,OnItemClickListener onItemClickListener) {
        this.context = context;
        this.obdInfoList = list;
        this.onItemClickListener = onItemClickListener;
    }


    public interface OnItemClickListener {
        void onItemClicked(int position, Object object);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtQty,txtLocation,txtBatch,txtOEMpart;// init the item view's

        public MyViewHolder(View itemView) {

            super(itemView);
            // get the reference of item view's
            txtQty = (TextView) itemView.findViewById(R.id.txtQty);
            txtLocation = (TextView) itemView.findViewById(R.id.txtLocation);
            txtBatch = (TextView) itemView.findViewById(R.id.txtBatch);
            txtOEMpart = (TextView) itemView.findViewById(R.id.txtOEMpart);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_batch_picking, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {


        BatchPickDTO batchPickDTO = (BatchPickDTO) obdInfoList.get(position);

        holder.itemView.setSelected(selectedItem == position);

        // Here I am just highlighting the background
        holder.itemView.setBackgroundColor(selectedItem == position ? Color.GREEN : Color.TRANSPARENT);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                onItemClickListener.onItemClicked(position, obdInfoList.get(position));
                notifyItemChanged(selectedItem);
                selectedItem = holder.getLayoutPosition();
                notifyItemChanged(selectedItem);

            }
        });

        // set the data in items

        ((MyViewHolder) holder).txtQty.setText(String.valueOf(batchPickDTO.getAvailableQuantity()));
        ((MyViewHolder) holder).txtBatch.setText(String.valueOf(batchPickDTO.getBatchNo()));
        ((MyViewHolder) holder).txtLocation.setText(String.valueOf(batchPickDTO.getLocation()));
        ((MyViewHolder) holder).txtOEMpart.setText(String.valueOf(batchPickDTO.getOEMBatchNo()));



    }


    @Override
    public int getItemCount() {
        return obdInfoList.size();
    }

}