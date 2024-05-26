package com.example.wittelibrary;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;
import android.app.AlertDialog;

public class DownloadedBookAdapter extends RecyclerView.Adapter<DownloadedBookAdapter.ViewHolder> {

    private List<File> downloadedBooks;
    private Context context;

    public DownloadedBookAdapter(Context context, List<File> downloadedBooks) {
        this.context = context;
        this.downloadedBooks = downloadedBooks;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_downloaded_book, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File bookFile = downloadedBooks.get(position);
        holder.bind(bookFile);
    }

    @Override
    public int getItemCount() {
        return downloadedBooks.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView txtBookName;
        private File bookFile;

        private ImageButton deleteButton;



        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtBookName = itemView.findViewById(R.id.bookNameTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog(getAdapterPosition()));
            itemView.setOnClickListener(this);
        }

        public void bind(File bookFile) {
            this.bookFile = bookFile;
            // Здесь можно настроить отображение информации о книге
            txtBookName.setText(bookFile.getName().replace(".pdf", ""));
        }

        @Override
        public void onClick(View v) {
            // Получаем путь к PDF-файлу из объекта книги
            String filePath = bookFile.getAbsolutePath();

            // Создаем намерение для открытия PDF-файла с помощью PDFViewerActivity
            Intent intent = new Intent(v.getContext(), PDFView_local.class);
            intent.putExtra("file_path", filePath);
            v.getContext().startActivity(intent);
        }

        public void deleteBook(int position) {
            // Удаляем сам файл PDF из системы
            if (bookFile.delete()) {
                // Удаляем запись о книге из RecyclerView
                downloadedBooks.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, downloadedBooks.size());
                Toast.makeText(context, "Книга удалена", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Не удалось удалить книгу", Toast.LENGTH_SHORT).show();
            }
        }

        public void showDeleteConfirmationDialog(int position) {
            new AlertDialog.Builder(context)
                    .setTitle("Удаление книги")
                    .setMessage("Вы действительно хотите удалить книгу?")
                    .setPositiveButton("Да", (dialog, which) -> deleteBook(position))
                    .setNegativeButton("Нет", null)
                    .show();
        }
    }
}
