package com.gabusdev.dev.quizbank.ui.questions;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.gabusdev.dev.quizbank.data.models.PreguntaEntity;
import com.gabusdev.dev.quizbank.databinding.ItemPreguntaBinding;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PreguntaAdapter extends RecyclerView.Adapter<PreguntaAdapter.ViewHolder> {
    private List<PreguntaEntity> questions = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public void setQuestions(List<PreguntaEntity> questions) {
        this.questions = questions;
        notifyDataSetChanged();
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
        PreguntaEntity question = questions.get(position);
        holder.binding.tvEnunciadoPreview.setText(question.enunciado);
        holder.binding.tvNivelTag.setText(question.nivel);
        holder.binding.tvFecha.setText("Creado el " + dateFormat.format(new Date(question.fechaCreacion)));
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemPreguntaBinding binding;
        ViewHolder(ItemPreguntaBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
