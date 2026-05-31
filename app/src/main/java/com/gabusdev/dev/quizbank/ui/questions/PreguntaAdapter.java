package com.gabusdev.dev.quizbank.ui.questions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.gabusdev.dev.quizbank.R;
import com.gabusdev.dev.quizbank.data.models.PreguntaEntity;
import com.gabusdev.dev.quizbank.databinding.ItemPreguntaBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PreguntaAdapter extends RecyclerView.Adapter<PreguntaAdapter.ViewHolder> {
    private List<PreguntaEntity> allQuestions = new ArrayList<>();
    private List<PreguntaEntity> filteredQuestions = new ArrayList<>();
    private final java.util.Set<Integer> selectedIds = new java.util.HashSet<>();
    private boolean selectionMode = false;
    
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final OnQuestionActionListener listener;

    public interface OnQuestionActionListener {
        void onEdit(PreguntaEntity pregunta);
        void onDelete(PreguntaEntity pregunta);
        void onSelectionChanged(int count);
    }

    public PreguntaAdapter(OnQuestionActionListener listener) {
        this.listener = listener;
    }

    public void setQuestions(List<PreguntaEntity> questions) {
        this.allQuestions = questions;
        updateList(new ArrayList<>(questions));
    }

    public void filter(String level) {
        List<PreguntaEntity> newList = new ArrayList<>();
        if (level == null || level.isEmpty() || level.equals("Todas")) {
            newList.addAll(allQuestions);
        } else {
            for (PreguntaEntity p : allQuestions) {
                if (p.nivel != null && p.nivel.equals(level)) {
                    newList.add(p);
                }
            }
        }
        updateList(newList);
    }

    private void updateList(List<PreguntaEntity> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallback(this.filteredQuestions, newList));
        this.filteredQuestions = newList;
        diffResult.dispatchUpdatesTo(this);
    }

    public void setSelectionMode(boolean enabled) {
        if (this.selectionMode == enabled) return;
        this.selectionMode = enabled;
        if (!enabled) selectedIds.clear();
        notifyItemRangeChanged(0, getItemCount());
    }

    public List<PreguntaEntity> getSelectedQuestions() {
        List<PreguntaEntity> selected = new ArrayList<>();
        for (PreguntaEntity p : allQuestions) {
            if (selectedIds.contains(p.id)) {
                selected.add(p);
            }
        }
        return selected;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPreguntaBinding binding = ItemPreguntaBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PreguntaEntity question = filteredQuestions.get(position);
        holder.binding.tvEnunciadoPreview.setText(question.enunciado);
        holder.binding.tvNivelTag.setText(question.nivel);
        holder.binding.tvFecha.setText(holder.itemView.getContext().getString(R.string.item_created_at, dateFormat.format(new Date(question.fechaCreacion))));

        holder.binding.btnEdit.setOnClickListener(v -> listener.onEdit(question));
        holder.binding.btnDelete.setOnClickListener(v -> listener.onDelete(question));
        
        // Manejo de selección
        if (selectionMode) {
            holder.binding.btnEdit.setVisibility(View.GONE);
            holder.binding.btnDelete.setVisibility(View.GONE);
            holder.itemView.setSelected(selectedIds.contains(question.id));
            holder.itemView.setAlpha(selectedIds.contains(question.id) ? 1.0f : 0.6f);
            
            holder.itemView.setOnClickListener(v -> {
                if (selectedIds.contains(question.id)) {
                    selectedIds.remove(question.id);
                } else {
                    selectedIds.add(question.id);
                }
                notifyItemChanged(position);
                listener.onSelectionChanged(selectedIds.size());
            });
        } else {
            holder.binding.btnEdit.setVisibility(View.VISIBLE);
            holder.binding.btnDelete.setVisibility(View.VISIBLE);
            holder.itemView.setAlpha(1.0f);
            holder.itemView.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return filteredQuestions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemPreguntaBinding binding;
        ViewHolder(ItemPreguntaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private static class DiffCallback extends DiffUtil.Callback {
        private final List<PreguntaEntity> oldList;
        private final List<PreguntaEntity> newList;

        DiffCallback(List<PreguntaEntity> oldList, List<PreguntaEntity> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldList.get(oldItemPosition).id == newList.get(newItemPosition).id;
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            PreguntaEntity oldItem = oldList.get(oldItemPosition);
            PreguntaEntity newItem = newList.get(newItemPosition);
            return Objects.equals(oldItem.enunciado, newItem.enunciado) &&
                    Objects.equals(oldItem.nivel, newItem.nivel) &&
                    oldItem.fechaCreacion == newItem.fechaCreacion;
        }
    }
}
