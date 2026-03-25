package com.bk.callblocker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NumberAdapter extends RecyclerView.Adapter<NumberAdapter.ViewHolder> {

    interface OnDeleteListener {
        void onDelete(String number);
    }

    private List<String> numbers;
    private final OnDeleteListener listener;

    public NumberAdapter(List<String> numbers, OnDeleteListener listener) {
        this.numbers = new ArrayList<>(numbers);
        this.listener = listener;
    }

    public void updateList(List<String> newNumbers) {
        this.numbers = new ArrayList<>(newNumbers);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_number, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String number = numbers.get(position);
        holder.numberText.setText(number);
        holder.deleteButton.setOnClickListener(v -> listener.onDelete(number));
    }

    @Override
    public int getItemCount() {
        return numbers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView numberText;
        ImageButton deleteButton;

        ViewHolder(View v) {
            super(v);
            numberText = v.findViewById(R.id.numberText);
            deleteButton = v.findViewById(R.id.deleteButton);
        }
    }
}
