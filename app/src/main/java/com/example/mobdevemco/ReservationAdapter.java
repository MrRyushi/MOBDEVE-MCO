package com.example.mobdevemco;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReservationAdapter extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {
    private List<ReservationData> reservationDataList;
    private Context context;

    public ReservationAdapter(List<ReservationData> reservationDataList, ReservationsHistory mainActivity) {
        this.reservationDataList = reservationDataList;
        this.context = mainActivity;
    }

    @NonNull
    @Override
    public ReservationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.reservation_item_list,parent,false);
        ReservationAdapter.ViewHolder viewHolder = new ReservationAdapter.ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ReservationAdapter.ViewHolder holder, int position) {
        final ReservationData reservationData = reservationDataList.get(position);
        holder.courtName.setText(reservationData.getCourtName());
        holder.reservationDate.setText("Date: " + reservationData.getReservationDate());
        String timeSlot = TextUtils.join(", ", reservationData.getReservationTimeSlot());
        holder.reservationTime.setText("Time: " + timeSlot);
    }

    @Override
    public int getItemCount() {
        return reservationDataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView courtName;
        TextView reservationDate;
        TextView reservationTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            courtName = itemView.findViewById(R.id.courtName);
            reservationDate = itemView.findViewById(R.id.reservationDate);
            reservationTime = itemView.findViewById(R.id.reservationTime);
        }
    }
}
