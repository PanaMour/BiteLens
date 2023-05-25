package com.example.bitelens;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        holder.mealCalories.setText("Calories: " + String.valueOf(meal.getCalories()));

        String timestampString = meal.getDate();  // "Timestamp(seconds=1684235164, nanoseconds=17000000)"

        Pattern pattern = Pattern.compile("Timestamp\\(seconds=(\\d+), nanoseconds=(\\d+)\\)");
        Matcher matcher = pattern.matcher(timestampString);
        if (matcher.find()) {
            long seconds = Long.parseLong(matcher.group(1));
            Date date = new Date(seconds * 1000); // Convert seconds to milliseconds

            // Format the Date
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy h:mm a", Locale.getDefault());
            String formattedDate = dateFormat.format(date);

            holder.mealDate.setText(formattedDate);
        }
        // Get a reference to the image in Firebase Storage
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference imageRef = storageRef.child(meal.getImageurl());

        // Download the image data
        imageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Convert the bytes to a Bitmap
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                // Set the Bitmap to the ImageView
                holder.mealPhoto.setImageBitmap(bitmap);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle any errors
            }
        });
        holder.mealLocation.setText("Location: "+meal.getLocation() + " (" + meal.getPlace() +")");
    }

    @Override
    public int getItemCount() {
        return mealList.size();
    }

    public static class MealViewHolder extends RecyclerView.ViewHolder {
        public TextView mealName;
        public TextView mealCalories;
        public TextView mealDate;
        public ImageView mealPhoto;
        public TextView mealLocation;

        public MealViewHolder(View view) {
            super(view);
            mealName = view.findViewById(R.id.meal_name);
            mealCalories = view.findViewById(R.id.meal_calories);
            mealDate = view.findViewById(R.id.meal_date);
            mealPhoto = view.findViewById(R.id.meal_photo);
            mealLocation = view.findViewById(R.id.meal_location);
        }
    }
}
