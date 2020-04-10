package com.ookiisoftware.album.auxiliar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.ookiisoftware.album.R;
import com.ookiisoftware.album.activity.PreviewPagerActivity.PreviewAdapter;
import com.ookiisoftware.album.adapter.AlbumAdapter;
import com.ookiisoftware.album.async.InBackground;
import com.ookiisoftware.album.modelo.Item;

import java.io.File;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class Function {

    //region Variáveis

//    private static final String TAG = "Function";
    public static ArrayList<HashMap<String, String>> imageList = null;

    //endregion

    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static String converToTime(String timestamp) {
        long datetime = Long.parseLong(timestamp);
        Date date = new Date(datetime);
        DateFormat formatter = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
        return formatter.format(date);
    }

    public static String converToMb (double bytes){
        int KILOBYTE = 1000;//1  Kilobyte
        double MEGABYTE = 1000000;//1 Megabyte
        NumberFormat nf = NumberFormat.getInstance();

        if(bytes < MEGABYTE) {
            nf.setMaximumFractionDigits(0);
            return nf.format((bytes / KILOBYTE)) + " Kb";//Kilobyte
        } else {
            nf.setMaximumFractionDigits(2);
            return nf.format((bytes / MEGABYTE)) + " Mb";//Megabyte
        }
    }

    private static void ordenarLista(ArrayList<Integer> dados, int inicio, int fim){
        int i, j, pivo, aux;
        i = inicio;
        j = fim-1;
        pivo = dados.get((inicio + fim) / 2);
        while(i <= j) {
            while(dados.get(i) < pivo && i < fim)
                i++;
            while(dados.get(j) > pivo && j > inicio)
                j--;
            if(i <= j) {
                aux = dados.get(i);
                dados.set(i, dados.get(j));//[i] = dados[j];
                dados.set(j, aux);//[j] = aux;
                i++;
                j--;
            }
        }
        if(j > inicio)
            ordenarLista(dados, inicio, j+1);
        if(i < fim)
            ordenarLista(dados, i, fim);
    }

    public static void DeletarDados(Activity activity, ArrayList<HashMap<String, String>> imageList, AlbumAdapter albumAdapter, PreviewAdapter previewAdapter){
        String titulo;
        String mensagem = "";
        if(imageList.size() == 1)
            titulo = activity.getResources().getString(R.string.excluir_item);
        else {
            titulo = activity.getResources().getString(R.string.excluir_itens);
            mensagem = imageList.size() + activity.getResources().getString(R.string.itens_selecionados);
        }
        popupDelete(activity, imageList, titulo, mensagem, albumAdapter, previewAdapter);
    }

    public static void CompartilharDados(Context context, ArrayList<HashMap<String, String>> files){
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("image/jpeg");

        ArrayList<Uri> uri = new ArrayList<>();
        for (HashMap<String, String> item : files)
            uri.add(Uri.parse(item.get(Item.KEY_PATH)));

        intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(intent, context.getResources().getString(R.string.menu_compartilhar)));

    }

    @SuppressLint("InflateParams")
    private static void popupDelete(final Activity activity, final ArrayList<HashMap<String, String>> imageList, String _titulo,
                                    String _mensagem, final AlbumAdapter albumAdapter, final PreviewAdapter previewAdapter) {
        final Dialog dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout;

        assert inflater != null;
        try {
            layout = inflater.inflate(R.layout.popup_delete, null);//(ViewGroup) layout.findViewById(R.id.popup_card)
        } catch (Exception ex) {
            layout = inflater.inflate(R.layout.popup_delete_alternative, null);
        }

        TextView titulo = layout.findViewById(R.id.titulo);
        titulo.setText(_titulo);

        final TextView nome_foto = layout.findViewById(R.id.nome);
        final Button btn_sim = layout.findViewById(R.id.btn_sim);
        final Button btn_nao = layout.findViewById(R.id.btn_nao);
        btn_sim.setOnClickListener(view -> {
            if(albumAdapter == null) {
                InBackground inBackground = new InBackground(activity, imageList, previewAdapter, dialog);
                inBackground.execute();
            } else {
                InBackground inBackground = new InBackground(activity, imageList, albumAdapter, dialog);
                inBackground.execute();
            }

            nome_foto.setText(activity.getResources().getString(R.string.aguarde));
            btn_nao.setActivated(false);
            btn_sim.setActivated(false);
            //dialog.dismiss();
        });
        btn_nao.setOnClickListener(view -> dialog.dismiss());

        if (imageList.size() == 1) {
            TextView tamanho_foto = layout.findViewById(R.id.tamanho);
            TextView dimenssao_foto = layout.findViewById(R.id.dimenssao);
            ImageView image = layout.findViewById(R.id.image);

            //HashMap<String, String> song = imageList.get(item_id);
            String path = imageList.get(0).get(Item.KEY_PATH);
            if (path != null) {
                Glide.with(activity).load(new File(path)).into(image);
            }

            nome_foto.setText(imageList.get(0).get(Item.KEY_ITEM_NAME));
            tamanho_foto.setText(imageList.get(0).get(Item.KEY_ITEM_SIZE));

            String dimensoes = imageList.get(0).get(Item.KEY_ITEM_WIDTH) + " x " + imageList.get(0).get(Item.KEY_ITEM_HEIGHT);
            dimenssao_foto.setText(dimensoes);

            dimenssao_foto.setVisibility(View.VISIBLE);
            tamanho_foto.setVisibility(View.VISIBLE);
            image.requestLayout();
        } else {
            nome_foto.setText(_mensagem);
        }

        dialog.setContentView(layout);
        dialog.show();
    }



    //================================================== SET WALLPAPER =============================================================
/*

    static Bitmap bitmap1, bitmap2;
    static int width, height;

    public static void SetWallpaper (Context context, String path, WindowManager windowManager, LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.wallpaper_layout, null);

        ImageView imageView = view.findViewById(R.id.image_wallpaper);
        Glide.with(context).load(path).into(imageView);

        WallpaperManager wallpaperManager  = WallpaperManager.getInstance(context);

        BitmapDrawable bitmapDrawable = (BitmapDrawable) imageView.getDrawable();

        bitmap1 = bitmapDrawable.getBitmap();

        //GetScreenWidthHeight(windowManager);

        //SetBitmapSize();

        try {
            wallpaperManager.setBitmap(bitmap1);
            Toast.makeText(context, "Papel de parede aplicado", Toast.LENGTH_SHORT).show();
            //wallpaperManager.suggestDesiredDimensions(width, height);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Erro", Toast.LENGTH_SHORT).show();
        }
    }

    private static void GetScreenWidthHeight(WindowManager windowManager){

        DisplayMetrics displayMetrics = new DisplayMetrics();

        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        width = displayMetrics.widthPixels;

        height = displayMetrics.heightPixels;

    }

    private static void SetBitmapSize(){

        bitmap2 = Bitmap.createScaledBitmap(bitmap1, width, height, false);

    }
*/
    //==========================================================================================================
}
