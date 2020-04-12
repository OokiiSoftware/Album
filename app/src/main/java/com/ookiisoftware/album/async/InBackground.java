package com.ookiisoftware.album.async;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.ookiisoftware.album.R;
import com.ookiisoftware.album.activity.PreviewPagerActivity.PreviewAdapter;
import com.ookiisoftware.album.adapter.AlbumAdapter;
import com.ookiisoftware.album.auxiliar.Constantes;
import com.ookiisoftware.album.auxiliar.Import;
import com.ookiisoftware.album.modelo.Item;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class InBackground extends AsyncTask<Void, Void, Boolean> {

    //region Variáveis
    private static final String TAG = "InBackground";
    private int erros = 0;
    @SuppressLint("StaticFieldLeak")
    private Activity activity;
    private ArrayList<HashMap<String, String>> itens;
    private PreviewAdapter previewAdapter;
    private AlbumAdapter albumAdapter;
    private String destino;
    private int itemsCount;
    private int acao;

    private Dialog dialog;
    //endregion

    public InBackground(Activity activity, ArrayList<HashMap<String, String>> itens, AlbumAdapter albumAdapter, Dialog dialog, int acao) {
//        this.context = activity.getApplicationContext();
        this.albumAdapter = albumAdapter;
        this.activity = activity;
        this.dialog = dialog;
        this.itens = itens;
        this.acao = acao;
    }
    public InBackground(Activity activity, ArrayList<HashMap<String, String>> itens, AlbumAdapter albumAdapter, Dialog dialog, int acao, String destino) {
//        this.context = activity.getApplicationContext();
        this.albumAdapter = albumAdapter;
        this.activity = activity;
        this.dialog = dialog;
        this.destino = destino;
        this.itens = itens;
        this.acao = acao;
    }
    public InBackground(Activity activity, ArrayList<HashMap<String, String>> itens, PreviewAdapter previewAdapter, Dialog dialog, int acao) {
//        this.context = activity.getApplicationContext();
        this.previewAdapter = previewAdapter;
        this.activity = activity;
        this.dialog = dialog;
        this.itens = itens;
        this.acao = acao;
    }

    @Override
    protected void onPreExecute(){
        itemsCount = itens.size();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        for (HashMap<String, String> item : itens)
            switch (acao) {
                case Constantes.FILE_MOVE: {
                    if (mover(item.get(Item.KEY_PATH), destino)) {
                        if (albumAdapter != null)
                            albumAdapter.getItems().remove(item);
                    } else
                        erros++;
                    break;
                }
                case Constantes.FILE_COPY: {
                    if (!copyFile(item.get(Item.KEY_PATH), item.get(Item.KEY_ITEM_NAME), destino)) {
                        erros++;
                    }
                    break;
                }
                case Constantes.FILE_DELETE: {
                    if (DeleteImage(item.get(Item.KEY_PATH), Objects.equals(item.get(Item.KEY_ITEM_TYPE), Constantes.ITEM_TYPE_IMAGE))) {
                        if (previewAdapter != null) {
                            previewAdapter.getItems().remove(item);
                            Import.itemsDeletados.add(item);
                        }
                        if (albumAdapter != null)
                            albumAdapter.getItems().remove(item);
                    } else
                        erros++;
                }
            }
        itens.clear();
        return erros == 0;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            switch (acao) {
                case Constantes.FILE_DELETE: {
                    if (itemsCount == 1)
                        Import.Alert.toast(activity, activity.getResources().getString(R.string.item_excluido));
                    else
                        Import.Alert.toast(activity, itemsCount + activity.getResources().getString(R.string.itens_excluidos));
                    break;
                }
                case Constantes.FILE_COPY: {
                    if (itemsCount == 1)
                        Import.Alert.toast(activity, activity.getResources().getString(R.string.item_copiado));
                    else
                        Import.Alert.toast(activity, itemsCount + activity.getResources().getString(R.string.itens_copiados));
                    break;
                }
                case Constantes.FILE_MOVE: {
                    if (itemsCount == 1)
                        Import.Alert.toast(activity, activity.getResources().getString(R.string.item_movido));
                    else
                        Import.Alert.toast(activity, itemsCount + activity.getResources().getString(R.string.itens_movidos));
                    break;
                }
            }
        } else {
            switch (acao) {
                case Constantes.FILE_DELETE: {
                    if (itemsCount == 1)
                        Import.Alert.toast(activity, activity.getResources().getString(R.string.msg_erro_delete));
                    else
                        Import.Alert.toast(activity, activity.getResources().getString(R.string.msg_erro_deletes) + " " + erros
                                + " " + activity.getResources().getString(R.string.itens));
                    break;
                }
                case Constantes.FILE_COPY: {
                    if (itemsCount == 1)
                        Import.Alert.toast(activity, activity.getResources().getString(R.string.msg_erro_copia));
                    else
                        Import.Alert.toast(activity, activity.getResources().getString(R.string.msg_erro_copias) + " " + erros
                                + " " + activity.getResources().getString(R.string.itens));
                    break;
                }
                case Constantes.FILE_MOVE: {
                    if (itemsCount == 1)
                        Import.Alert.toast(activity, activity.getResources().getString(R.string.msg_erro_move));
                    else
                        Import.Alert.toast(activity, activity.getResources().getString(R.string.msg_erro_moves) + " " + erros
                                + " " + activity.getResources().getString(R.string.itens));
                    break;
                }
            }
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
        ContentResolver contentResolver = activity.getContentResolver();

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

    private boolean moveFile(String inputPath, String inputFile, String outputPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
            new File(inputPath + inputFile).delete();
            return true;
        } catch (Exception ex) {
            Import.Alert.erro(TAG, ex);
            return false;
        }
    }

    private boolean copyFile(String inputPath, String inputFile, String outputPath) {
        inputPath = inputPath.replace(inputFile, "");
        try {
            //create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            InputStream in = new FileInputStream(inputPath + inputFile);
            OutputStream out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
//            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
//            out = null;
            return true;
        }
        catch (Exception ex) {
            Import.Alert.erro(TAG, ex);
            return false;
        }
    }

    private boolean mover(String de, String para) {
        File from = new File(de);//Environment.getExternalStorageDirectory().getAbsolutePath()+"/kaic1/imagem.jpg");
        File to = new File(para);//Environment.getExternalStorageDirectory().getAbsolutePath()+"/kaic2/imagem.jpg");
        return from.renameTo(to);
    }
}
