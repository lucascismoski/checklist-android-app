package com.example.checklistapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CheckListAdapter extends RecyclerView.Adapter<CheckListAdapter.ChecklistViewHolder> {

    private List<CheckList> checklistList;
    private CheckListListener checklistListener;

    public CheckListAdapter(List<CheckList> checklistList, CheckListListener checklistListener) {
        this.checklistList = checklistList;
        this.checklistListener = checklistListener;
    }

    @NonNull
    @Override
    public ChecklistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checklist, parent, false);
        return new ChecklistViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ChecklistViewHolder holder, int position) {
        CheckList checklist = checklistList.get(position);
        holder.bind(checklist);
    }

    @Override
    public int getItemCount() {
        return checklistList.size();
    }

    public class ChecklistViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewDescription;
        private CheckBox checkBoxChecked;

        public ChecklistViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            checkBoxChecked = itemView.findViewById(R.id.checkBoxChecked);

            checkBoxChecked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        CheckList checklist = checklistList.get(position);
                        checklist.setChecked(isChecked);
                        checklistListener.onCheckListUpdated(checklist);
                    }
                }
            });
        }

        public void bind(CheckList checklist) {
            textViewDescription.setText(checklist.getDescription());
            checkBoxChecked.setChecked(checklist.isChecked());
        }
    }

    public interface CheckListListener {
        void onCheckListUpdated(CheckList checklist);
    }
}