package com.secure.taxapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Adaptateur pour la liste des numeros
 */
public class NumberAdapter extends RecyclerView.Adapter<NumberAdapter.ViewHolder> {
    
    private List<String> numbers;
    private boolean masked;
    
    public NumberAdapter(Set<String> numbers, boolean masked) {
        this.numbers = new ArrayList<>(numbers);
        this.masked = masked;
    }
    
    public void setMasked(boolean masked) {
        this.masked = masked;
        notifyDataSetChanged();
    }
    
    public void updateData(Set<String> newNumbers) {
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
        
        if (masked) {
            holder.numberText.setText("••••••••••••");
        } else {
            holder.numberText.setText(number);
        }
        
        holder.deleteButton.setOnClickListener(v -> {
            // FIX: Utiliser getAdapterPosition() au lieu de la variable position capturee
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION && currentPos < numbers.size()) {
                String numberToRemove = numbers.get(currentPos);
                ConfigManager configManager = new ConfigManager(v.getContext());
                configManager.removeNumber(numberToRemove);
                numbers.remove(currentPos);
                notifyItemRemoved(currentPos);
                // FIX: utiliser notifyItemRemoved + notifyItemRangeChanged
                // au lieu de notifyDataSetChanged (evite les problemes d'index)
                notifyItemRangeChanged(currentPos, numbers.size() - currentPos);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return numbers.size();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView numberText;
        ImageButton deleteButton;
        
        ViewHolder(View itemView) {
            super(itemView);
            numberText = itemView.findViewById(R.id.numberText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
