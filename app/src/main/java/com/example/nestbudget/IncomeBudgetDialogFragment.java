//package com.example.nestbudget;
//
//import android.app.Dialog;
//import android.content.Intent;
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.DialogFragment;
//
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//
//public class IncomeBudgetDialogFragment extends DialogFragment {
//
//    private String username;
//
//    public IncomeBudgetDialogFragment(String username) {
//        this.username = username;
//    }
//
//    @NonNull
//    @Override
//    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//
//        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_income_budget, null);
//
//        EditText etIncome = view.findViewById(R.id.etIncome);
//        EditText etBudget = view.findViewById(R.id.etBudget);
//        TextView tvAlert = view.findViewById(R.id.tvAlert);
//        Button btnSave = view.findViewById(R.id.btnSave);
//        Button btnCancel = view.findViewById(R.id.btnCancel);
//
//        Dialog dialog = new Dialog(getActivity());
//        dialog.setContentView(view);
//        dialog.setCancelable(false);
//
//        btnSave.setOnClickListener(v -> {
//            String income = etIncome.getText().toString().trim();
//            String budget = etBudget.getText().toString().trim();
//
//            if (TextUtils.isEmpty(income) || TextUtils.isEmpty(budget)) {
//                tvAlert.setText("Both fields are required");
//                tvAlert.setVisibility(View.VISIBLE);
//            } else {
//                DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
//                ref.child("Users").child(username).child("monthlyIncome").setValue(income);
//                ref.child("Users").child(username).child("monthlyBudget").setValue(budget);
//                dialog.dismiss();
//
//                startActivity(new Intent(getActivity(), MainActivity.class));
//                requireActivity().finish();
//            }
//        });
//
//        btnCancel.setOnClickListener(v -> {
//            tvAlert.setText("Both fields are required");
//            tvAlert.setVisibility(View.VISIBLE);
//        });
//
//        return dialog;
//    }
//}
