package com.example.mobdevemco;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Objects;

public class CurrentReservationsAdapter extends RecyclerView.Adapter<CurrentReservationsAdapter.ViewHolder> {
    private List<ReservationData> reservationDataList;
    private Context context;

    public CurrentReservationsAdapter(List<ReservationData> reservationDataList, CurrentReservations mainActivity) {
        this.reservationDataList = reservationDataList;
        this.context = mainActivity;
    }

    @NonNull
    @Override
    public CurrentReservationsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.reservation_item_list_current, parent, false);
        return new CurrentReservationsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CurrentReservationsAdapter.ViewHolder holder, int position) {
        final ReservationData reservationData = reservationDataList.get(position);
        holder.courtName.setText(reservationData.getCourtName());
        holder.reservationDate.setText("Date: " + reservationData.getReservationDate());
        String timeSlot = TextUtils.join(", ", reservationData.getReservationTimeSlot());
        holder.reservationTime.setText("Time: " + timeSlot);

        holder.cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show confirmation dialog before removing the reservation
                new AlertDialog.Builder(context)
                        .setTitle("Cancel Reservation")
                        .setMessage("Are you sure you want to cancel the reservation for " + reservationData.getCourtName() + "?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // Call the remove method if user confirms
                                if (NetworkUtils.isInternetAvailable(context)) {
                                    removeItemById(reservationData.getId());
                                }
                                else {
                                    new AlertDialog.Builder(context)
                                            .setTitle("No Internet Connection")
                                            .setMessage("Please check your internet connection and try again.")
                                            .setPositiveButton("OK", null)
                                            .show();
                                }
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss(); // Close dialog without removing
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return reservationDataList.size();
    }

    public void removeItemById(String reservationId) {
        for (int i = 0; i < reservationDataList.size(); i++) {
            if (Objects.equals(reservationDataList.get(i).getId(), reservationId)) {
                // Remove from Firebase
                DatabaseReference reservationRef = FirebaseDatabase.getInstance().getReference("reservations").child(reservationId);
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(reservationDataList.get(i).getUserId());

                int finalI = i;
                reservationRef.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Fetch totalReservations, decrement by 1, and update
                        userRef.child("totalReservations").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Integer totalReservations = snapshot.getValue(Integer.class);
                                if (totalReservations != null && totalReservations > 0) {
                                    // Decrement totalReservations by 1
                                    userRef.child("totalReservations").setValue(totalReservations - 1)
                                            .addOnCompleteListener(updateTask -> {
                                                if (updateTask.isSuccessful()) {
                                                    // Remove from local list and notify adapter
                                                    reservationDataList.remove(finalI);
                                                    notifyItemRemoved(finalI);
                                                } else {
                                                    Toast.makeText(context, "Error updating total reservations.", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(context, "Error fetching total reservations.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(context, "Error removing reservation.", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            }
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView courtName;
        TextView reservationDate;
        TextView reservationTime;
        Button cancelBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            courtName = itemView.findViewById(R.id.courtName);
            reservationDate = itemView.findViewById(R.id.reservationDate);
            reservationTime = itemView.findViewById(R.id.reservationTime);
            cancelBtn = itemView.findViewById(R.id.cancelBtn);
        }
    }
}
