package com.ookiisoftware.album.async;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.Toast;

import com.ookiisoftware.album.R;
import com.ookiisoftware.album.activity.PreviewPagerActivity.PreviewAdapter;
import com.ookiisoftware.album.adapter.AlbumAdapter;
import com.ookiisoftware.album.auxiliar.Constantes;
import com.ookiisoftware.album.auxiliar.Import;
import com.ookiisoftware.album.modelo.Item;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class InBackground extends AsyncTask<Void, Void, Boolean> {

    //region Variáveis
    private int erros = 0;
    @SuppressLint("StaticFieldLeak")
    private Activity activity;
    @SuppressLint("StaticFieldLeak")
    private Context context;
    private ArrayList<HashMap<String, String>> itens;
    private PreviewAdapter previewAdapter;
    private AlbumAdapter albumAdapter;
    private int itemsCount;

    private Dialog dialog;
    //endregion

    public InBackground(Activity activity, ArrayList<HashMap<String, String>> itens, AlbumAdapter albumAdapter, Dialog dialog) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.itens = itens;
        this.albumAdapter = albumAdapter;
        this.dialog = dialog;
    }
    public InBackground(Activity activity, ArrayList<HashMap<String, String>> itens, PreviewAdapter previewAdapter, Dialog dialog) {
        this.activity = activity;
        this.context = activity.getApplicationContext();
        this.itens = itens;
        this.previewAdapter = previewAdapter;
        this.dialog = dialog;
    }

    @Override
    protected void onPreExecute(){
        itemsCount = itens.size();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        for (HashMap<String, String> item : itens) {
            if (DeleteImage(item.get(Item.KEY_PATH), Objects.equals(item.get(Item.KEY_ITEM_TYPE), Constantes.ITEM_TYPE_IMAGE))) {
                if(previewAdapter != null) {
                    previewAdapter.getItems().remove(item);
                    Import.itemsDeletados.add(item);
                }
                if(albumAdapter != null)
                    albumAdapter.getItems().remove(item);
            } else
                erros++;
        }
        itens.clear();
        return erros == 0;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            if (itemsCount == 1)
                Toast.makeText(context, activity.getResources().getString(R.string.item_excluido), Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(context, itemsCount + activity.getResources().getString(R.string.itens_excluidos), Toast.LENGTH_SHORT).show();
        } else {
            // Arquivo não encontrado no banco de dados do armazenamento de mídia
            if (itemsCount == 1)
                Toast.makeText(context, activity.getResources().getString(R.string.msg_erro_delete), Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context, activity.getResources().getString(R.string.msg_erro_deletes) + erros + " Itens", Toast.LENGTH_LONG).show();
        }
        if(previewAdapter != null) {
            previewAdapter.notifyDataSetChanged();
            if (previewAdapter.getCount() == 0) {
                activity.onBackPressed();
            }//*/
        }
        if(albumAdapter != null) {
            albumAdapter.notifyDataSetChanged();
            if (albumAdapter.getItemCount() == 0) {
                activity.onBackPressed();
            }//*/
        }
        dialog.dismiss();
        erros = 0;
    }

    private boolean DeleteImage(String path, boolean isImage) {
        File file = new File(Objects.requireNonNull(path));
        String[] selectionArgs = new String[] { file.getAbsolutePath() };
        ContentResolver contentResolver = context.getContentResolver();

        if (isImage) {
            // Configure a projeção (precisamos apenas do ID)
            String[] projection = { MediaStore.Images.Media._ID };

            // Corresponder no caminho do arquivo
            String selection = MediaStore.Images.Media.DATA + " = ?";

            // Consulta para o ID da mídia que corresponde ao caminho do arquivo
            Uri queryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Cursor c = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
            if (c != null)
                if (c.moveToFirst()) {
                    // Encontramos o ID. A exclusão do item pelo provedor de conteúdo também removerá o arquivo
                    long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                    Uri deleteUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                    int teste = contentResolver.delete(deleteUri, null, null);

                    c.close();
                    return teste == 1;
                }
        } else {
            // Configure a projeção (precisamos apenas do ID)
            String[] projection = { MediaStore.Video.Media._ID };

            // Corresponder no caminho do arquivo
            String selection = MediaStore.Video.Media.DATA + " = ?";

            // Consulta para o ID da mídia que corresponde ao caminho do arquivo
            Uri queryUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            Cursor c = contentResolver.query(queryUri, projection, selection, selectionArgs, null);
            if (c != null)
                if (c.moveToFirst()) {
                    // Encontramos o ID. A exclusão do item pelo provedor de conteúdo também removerá o arquivo
                    long id = c.getLong(c.getColumnIndexOrThrow(MediaStore.Video.Media._ID));
                    Uri deleteUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                    int teste = contentResolver.delete(deleteUri, null, null);

                    c.close();
                    return teste == 1;
                }
        }
        return false;
    }
}
