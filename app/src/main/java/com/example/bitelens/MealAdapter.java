package com.example.bitelens;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MealAdapter extends RecyclerView.Adapter<MealAdapter.MealViewHolder> {

    private List<Meal> mealList;

    public MealAdapter(List<Meal> mealList) {
        this.mealList = mealList;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal, parent, false);
        return new MealViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = mealList.get(position);
        holder.mealName.setText(meal.getName());
        holder.mealCalories.setText(String.valueOf(meal.getCalories()));
        holder.mealDate.setText(meal.getDate().toString());  // You may want to format this date
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    public static class MealViewHolder extends RecyclerView.ViewHolder {
        public TextView mealName;
        public TextView mealCalories;
        public TextView mealDate;

        public MealViewHolder(View view) {
            super(view);
            mealName = view.findViewById(R.id.meal_name);
            mealCalories = view.findViewById(R.id.meal_calories);
            mealDate = view.findViewById(R.id.meal_date);
        }
    }
}
