package com.example.meubrecho.ui.dashboard;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.meubrecho.AppDataBase;
import com.example.meubrecho.R;
import com.example.meubrecho.databinding.FragmentDashboardBinding;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class DashboardFragment extends Fragment {

    private static final int IMAGE_PICK_CODE = 1000; // Código para selecionar a imagem
    private FragmentDashboardBinding binding;
    private AppDataBase appDataBase;
    private ProdutoAdapter adapter;
    private ArrayList<Produto> produtosList;
    //imagem
    private byte[] imagemSelecionada;
    private ImageView imagemPreview;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        appDataBase = new AppDataBase(getContext());
        produtosList = new ArrayList<>();

        // Inicializa o RecyclerView
        RecyclerView recyclerView = binding.recyclerViewProdutos;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProdutoAdapter(produtosList);
        recyclerView.setAdapter(adapter);

        // Popula a lista com dados do banco de dados
        populateList();

        // Configura o botão para atualizar a lista
        binding.buttonInserirdados.setOnClickListener(v -> showAddProductDialog());

        // Configura o clique longo para edição
        adapter.setOnProdutoLongClickListener(produto -> showEditProductDialog(produto));

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void populateList() {
        produtosList.clear(); // Limpa a lista atual

        Cursor cursor = appDataBase.getProdutos(); // Obtém os dados do banco de dados
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Produto produto = new Produto(
                            cursor.getString(cursor.getColumnIndexOrThrow("nome")),
                            cursor.getFloat(cursor.getColumnIndexOrThrow("valor")),
                            cursor.getFloat(cursor.getColumnIndexOrThrow("preco")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("reservado")) == 1,
                            cursor.getString(cursor.getColumnIndexOrThrow("reservadopara")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("pago")) == 1,
                            cursor.getString(cursor.getColumnIndexOrThrow("dataretirada")),
                            cursor.getBlob(cursor.getColumnIndexOrThrow("imagem"))
                    );
                    produtosList.add(produto);
                } while (cursor.moveToNext());

                cursor.close();
            }
        }

        // Notifica o adapter que os dados mudaram
        adapter.notifyDataSetChanged();
    }

    private void showAddProductDialog() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_product, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        builder.setTitle("Adicionar Produto");

        // Obter referências dos elementos do layout
        EditText editTextNome = dialogView.findViewById(R.id.editTextNome);
        EditText editTextValor = dialogView.findViewById(R.id.editTextValor);
        EditText editTextPreco = dialogView.findViewById(R.id.editTextPreco);
        CheckBox checkBoxReservado = dialogView.findViewById(R.id.checkBoxReservado);
        EditText editTextReservadopara = dialogView.findViewById(R.id.editTextReservadopara);
        EditText editTextDataretirada = dialogView.findViewById(R.id.editTextDataretirada);
        CheckBox checkBoxPago = dialogView.findViewById(R.id.checkBoxPago);
        Button buttonSalvar = dialogView.findViewById(R.id.buttonSalvar);
        Button buttonExcluir = dialogView.findViewById(R.id.buttonExcluir);
        imagemPreview = dialogView.findViewById(R.id.imagem_preview);
        Button buttonSelecionarImagem = dialogView.findViewById(R.id.button_selecionar_imagem);

        buttonExcluir.setVisibility(View.GONE); // Oculta o botão excluir

        AlertDialog dialog = builder.create();

        checkBoxReservado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editTextReservadopara.setVisibility(View.VISIBLE);
                editTextDataretirada.setVisibility(View.VISIBLE);
                checkBoxPago.setVisibility(View.VISIBLE);
            } else {
                editTextReservadopara.setText("");
                editTextDataretirada.setText("");
                checkBoxPago.setChecked(false);
                editTextReservadopara.setVisibility(View.GONE);
                editTextDataretirada.setVisibility(View.GONE);
                checkBoxPago.setVisibility(View.GONE);
            }
        });

        editTextDataretirada.setOnClickListener(v -> showDatePicker(editTextDataretirada));

        buttonSelecionarImagem.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, IMAGE_PICK_CODE);
        });

        buttonSalvar.setOnClickListener(v -> {
            String nome = editTextNome.getText().toString().trim();
            String valorStr = editTextValor.getText().toString().trim();
            String precoStr = editTextPreco.getText().toString().trim();
            boolean reservado = checkBoxReservado.isChecked();
            String reservadoPara = editTextReservadopara.getText().toString().trim();
            String dataRetirada = editTextDataretirada.getText().toString().trim();
            boolean pago = checkBoxPago.isChecked();

            if (nome.isEmpty() || valorStr.isEmpty() || precoStr.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, preencha os campos obrigatórios.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (reservado && reservadoPara.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, informe quem reservou o produto.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (reservado && !isDateValid(dataRetirada)) {
                Toast.makeText(getContext(), "Por favor, informe a data de retirada.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (appDataBase.produtoExiste(nome)) {
                Toast.makeText(getContext(), "Já existe um produto com este nome.", Toast.LENGTH_SHORT).show();
                return;
            }

            float valor = Float.parseFloat(valorStr);
            float preco = Float.parseFloat(precoStr);

            // Salvar os dados no banco de dados
            appDataBase.addProduto(nome, valor, preco, reservado, reservadoPara, pago, dataRetirada, imagemSelecionada);

            populateList();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showEditProductDialog(Produto produto) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_add_product, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);
        builder.setTitle("Editar Produto");

        // Obter referências dos elementos do layout
        EditText editTextNome = dialogView.findViewById(R.id.editTextNome);
        EditText editTextValor = dialogView.findViewById(R.id.editTextValor);
        EditText editTextPreco = dialogView.findViewById(R.id.editTextPreco);
        CheckBox checkBoxReservado = dialogView.findViewById(R.id.checkBoxReservado);
        EditText editTextReservadopara = dialogView.findViewById(R.id.editTextReservadopara);
        EditText editTextDataretirada = dialogView.findViewById(R.id.editTextDataretirada);
        CheckBox checkBoxPago = dialogView.findViewById(R.id.checkBoxPago);
        Button buttonSalvar = dialogView.findViewById(R.id.buttonSalvar);
        Button buttonExcluir = dialogView.findViewById(R.id.buttonExcluir);
        imagemPreview = dialogView.findViewById(R.id.imagem_preview);
        Button buttonSelecionarImagem = dialogView.findViewById(R.id.button_selecionar_imagem);

        buttonExcluir.setVisibility(View.VISIBLE); // Torna o botão excluir visível

        // Preencher o formulário com os dados do produto
        editTextNome.setText(produto.getNome());
        editTextValor.setText(String.valueOf(produto.getValor()));
        editTextPreco.setText(String.valueOf(produto.getPreco()));
        checkBoxReservado.setChecked(produto.isReservado());
        editTextReservadopara.setText(produto.getReservadoPara());
        editTextDataretirada.setText(produto.getDataRetirada());
        checkBoxPago.setChecked(produto.isPago());

        // Exibir a imagem se houver
        if (produto.getImagem() != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(produto.getImagem(), 0, produto.getImagem().length);
            imagemPreview.setImageBitmap(bitmap);
            imagemPreview.setVisibility(View.VISIBLE);
        }

        AlertDialog dialog = builder.create();

        checkBoxReservado.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editTextReservadopara.setVisibility(View.VISIBLE);
                editTextDataretirada.setVisibility(View.VISIBLE);
                checkBoxPago.setVisibility(View.VISIBLE);
            } else {
                editTextReservadopara.setText("");
                editTextDataretirada.setText("");
                checkBoxPago.setChecked(false);
                editTextReservadopara.setVisibility(View.GONE);
                editTextDataretirada.setVisibility(View.GONE);
                checkBoxPago.setVisibility(View.GONE);
            }
        });

        editTextDataretirada.setOnClickListener(v -> showDatePicker(editTextDataretirada));

        buttonSelecionarImagem.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, IMAGE_PICK_CODE);
        });

        buttonSalvar.setOnClickListener(v -> {
            String nome = editTextNome.getText().toString().trim();
            String valorStr = editTextValor.getText().toString().trim();
            String precoStr = editTextPreco.getText().toString().trim();
            boolean reservado = checkBoxReservado.isChecked();
            String reservadoPara = editTextReservadopara.getText().toString().trim();
            String dataRetirada = editTextDataretirada.getText().toString().trim();
            boolean pago = checkBoxPago.isChecked();

            if (nome.isEmpty() || valorStr.isEmpty() || precoStr.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, preencha os campos obrigatórios.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (reservado && reservadoPara.isEmpty()) {
                Toast.makeText(getContext(), "Por favor, informe quem reservou o produto.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (reservado && !isDateValid(dataRetirada)) {
                Toast.makeText(getContext(), "Por favor, informe a data de retirada.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Atualiza os dados no banco de dados
            appDataBase.updateProduto(produto.getNome(), nome, Float.parseFloat(valorStr), Float.parseFloat(precoStr), reservado, reservadoPara, pago, dataRetirada, imagemSelecionada);
            populateList();
            dialog.dismiss();
        });

        buttonExcluir.setOnClickListener(v -> {
            showDeleteProductDialog(produto);
            populateList();
            dialog.dismiss();
        });

        dialog.show();
    }
    private void showDeleteProductDialog(Produto produto) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Por que deseja Excluir Produto?");

        // Infla o layout personalizado
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_delete_product, null);
        builder.setView(dialogView);

        // Referências aos botões
        Button buttonDesisti = dialogView.findViewById(R.id.button_desisti);
        Button buttonVendi = dialogView.findViewById(R.id.button_vendi);
        Button buttonCancelar = dialogView.findViewById(R.id.button_cancelar);

        // Cria o diálogo
        AlertDialog dialog = builder.create();

        // Configura o comportamento dos botões
        buttonDesisti.setOnClickListener(v -> {
            appDataBase.deleteProduto(produto.getNome());
            Toast.makeText(getContext(), "Produto excluído.", Toast.LENGTH_SHORT).show();
            populateList(); // Atualizar a lista
            dialog.dismiss(); // Fecha o diálogo após a ação
        });

        buttonVendi.setOnClickListener(v -> {
            float valor = produto.getValor();
            float preco = produto.getPreco();
            appDataBase.updateGanhos(valor, preco); // Atualizar os ganhos
            appDataBase.deleteProduto(produto.getNome());
            Toast.makeText(getContext(), "Produto vendido e ganhos atualizados.", Toast.LENGTH_SHORT).show();
            populateList(); // Atualizar a lista
            dialog.dismiss(); // Fecha o diálogo após a ação
        });

        buttonCancelar.setOnClickListener(v -> dialog.dismiss()); // Fecha o diálogo sem fazer nada

        dialog.show(); // Mostra o diálogo
    }

    private void showDatePicker(EditText editText) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                (view, year, month, dayOfMonth) -> {
                    String date = dayOfMonth + "/" + (month + 1) + "/" + year;
                    editText.setText(date);
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private boolean isDateValid(String date) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            format.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_PICK_CODE && resultCode == getActivity().RESULT_OK) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imagemSelecionada = bitmapToByteArray(bitmap); // Converter bitmap para byte array
                imagemPreview.setImageBitmap(bitmap); // Exibir a imagem selecionada
                imagemPreview.setVisibility(View.VISIBLE); // Ativar a visibilidade da imagem
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        return stream.toByteArray();
    }
}
