package com.example.checklistapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChecklists;
    private CheckListAdapter checklistAdapter;
    private List<CheckList> checklistList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Button buttonClear;
    private Button buttonLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonClear = findViewById(R.id.buttonClear);

        buttonLogout = findViewById(R.id.buttonLogout);

        // Inicializar o FirebaseFirestore
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Configurar RecyclerView
        recyclerViewChecklists = findViewById(R.id.recyclerViewChecklists);
        recyclerViewChecklists.setLayoutManager(new LinearLayoutManager(this));
        checklistList = new ArrayList<>();
        checklistAdapter = new CheckListAdapter(checklistList, new CheckListAdapter.CheckListListener() {
            @Override
            public void onCheckListUpdated(CheckList checklist) {
                updateChecklist(checklist);
            }
        });
        recyclerViewChecklists.setAdapter(checklistAdapter);

        Button buttonAddChecklist = findViewById(R.id.buttonAddChecklist);
        buttonAddChecklist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewChecklist();
            }
        });

        fetchChecklists();

        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAllChecklists();
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();

                Toast.makeText(MainActivity.this, "Usuário deslogado com sucesso!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void fetchChecklists() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            db.collection("users").document(userId).collection("checklists")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            if (error != null) {
                                Toast.makeText(MainActivity.this, "Erro ao recuperar os checklists", Toast.LENGTH_SHORT).show();
                                Log.e("MainActivity", "Erro ao recuperar os checklists", error);
                                return;
                            }

                            checklistList.clear();

                            for (QueryDocumentSnapshot documentSnapshot : value) {
                                String checklistId = documentSnapshot.getId();
                                String description = documentSnapshot.getString("description");
                                boolean checked = documentSnapshot.getBoolean("checked");

                                CheckList checklist = new CheckList(checklistId, description, checked);
                                checklistList.add(checklist);
                            }

                            checklistAdapter.notifyDataSetChanged();
                        }
                    });
        }
    }

    private void updateChecklist(CheckList checklist) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            String checklistId = checklist.getId();

            DocumentReference checklistRef = db.collection("users").document(userId).collection("checklists").document(checklistId);

            Map<String, Object> checklistData = new HashMap<>();
            checklistData.put("description", checklist.getDescription());
            checklistData.put("checked", checklist.isChecked());

            checklistRef.set(checklistData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(MainActivity.this, "Checklist atualizado com sucesso", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Erro ao atualizar o checklist", Toast.LENGTH_SHORT).show();
                            Log.e("MainActivity", "Erro ao atualizar o checklist", e);
                        }
                    });
        }
    }

    private void addNewChecklist() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Novo Checklist");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Salvar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String description = input.getText().toString().trim();
                if (!description.isEmpty()) {
                    saveChecklist(description);
                } else {
                    Toast.makeText(MainActivity.this, "A descrição do checklist não pode estar vazia", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void saveChecklist(String description) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            Map<String, Object> checklistData = new HashMap<>();
            checklistData.put("description", description);
            checklistData.put("checked", false);

            db.collection("users").document(userId).collection("checklists")
                    .add(checklistData)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(MainActivity.this, "Novo checklist salvo com sucesso", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Erro ao salvar novo checklist", Toast.LENGTH_SHORT).show();
                            Log.e("MainActivity", "Erro ao salvar novo checklist", e);
                        }
                    });
        }
    }

    private void createNewChecklist(String description) {
        Map<String, Object> checklistData = new HashMap<>();
        checklistData.put("description", description);
        checklistData.put("checked", false);

        db.collection("checklists")
                .add(checklistData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(MainActivity.this, "Novo checklist salvo com sucesso", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Erro ao salvar novo checklist", Toast.LENGTH_SHORT).show();
                        Log.e("MainActivity", "Erro ao salvar novo checklist", e);
                    }
                });
    }

    private void deleteAllChecklists() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();

            CollectionReference checklistsRef = db.collection("users").document(userId).collection("checklists");

            checklistsRef.get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            WriteBatch batch = db.batch();

                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                batch.delete(documentSnapshot.getReference());
                            }

                            batch.commit()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(MainActivity.this, "Todos os checklists foram apagados", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(MainActivity.this, "Erro ao apagar os checklists", Toast.LENGTH_SHORT).show();
                                            Log.e("MainActivity", "Erro ao apagar os checklists", e);
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Erro ao obter os checklists", Toast.LENGTH_SHORT).show();
                            Log.e("MainActivity", "Erro ao obter os checklists", e);
                        }
                    });
        }
    }
}