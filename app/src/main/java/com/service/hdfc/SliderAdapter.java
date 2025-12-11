    package com.service.hdfc;

    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.ImageView;
    import android.widget.TextView;

    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.RecyclerView;

    import java.util.List;

    public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder> {
        private final List<SliderItem> sliderItems;

        public SliderAdapter(List<SliderItem> sliderItems) {
            this.sliderItems = sliderItems;
        }

        @NonNull
        @Override
        public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_slider, parent, false);
            return new SliderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
            SliderItem item = sliderItems.get(position);
            holder.title.setText(item.getTitle());
            holder.desc.setText(item.getDescription());
            holder.image.setImageResource(item.getImageRes());
        }

        @Override
        public int getItemCount() {
            return sliderItems.size();
        }

        static class SliderViewHolder extends RecyclerView.ViewHolder {
            TextView title, desc;
            ImageView image;

            public SliderViewHolder(@NonNull View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.slideTitle);
                desc = itemView.findViewById(R.id.slideDesc);
                image = itemView.findViewById(R.id.slideImage);
            }
        }
    }
