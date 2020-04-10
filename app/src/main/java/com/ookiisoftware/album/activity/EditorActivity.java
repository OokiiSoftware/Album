package com.ookiisoftware.album.activity;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.ookiisoftware.album.R;
import com.ookiisoftware.album.auxiliar.Constantes;
import com.ookiisoftware.album.auxiliar.Import;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class EditorActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "EditorActivity";
    //================================================ IDs de cada ação
    ArrayList<Integer> acao = new ArrayList<>();
    ArrayList<Bitmap> bitmaps_temp = new ArrayList<>();
    final int id_rotacionar_esquerda = 1;
    final int id_rotacionar_direita = 2;
    final int id_inverter_h = 3;
    final int id_inverter_v = 4;
    final int id_recortar = 5;
    final int id_ignore = 6;
    private final int reset_indice = -1;
    private int indice_acao = reset_indice;
    private int indice_bitmap = reset_indice;
    //==============================
    final int id_desfazer = 10;
    final int id_refazer = 11;
    final int id_resetar = 12;
    final int id_salvar = 13;
    //=================================================================
    private final int ROTACAO_ESQUERTA = -1, ROTACAO_DIREITA = 1;
    private final int INVERTER_VERTICAI = -1, INVERTER_HORIZONTAL = 1;
    //=================================================================
    private int[] rotacao = {0, 90, 180, 270};
    private int indice_rotacao = 0;
    private String image_path;
    long image_id;
    private Uri image_uri;
    private CropImageView cropImageView;
    private Bitmap bitmap_para_salvar;

    private boolean sairConfirmado;
    //================ Menus
    private BottomNavigationView nav_top, nav_bot_principal;
    //================================================================= IDs Itens do Nav Top
    final int ID_TOP_DESFAZER = 0, ID_TOP_REFAZER = 1, ID_TOP_RESETAR = 2, ID_TOP_SALVAR = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(Import.theme.get(this));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Init();

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            image_path = bundle.getString(Constantes.IMAGE_EDITOR_PATH);
            image_id = bundle.getLong(Constantes.IMAGE_EDITOR_ID);
            image_uri = Uri.parse("file://" + image_path);
        }
        cropImageView.setImageUriAsync(image_uri);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            //============================== Menu Top
            case R.id.editor_btn_desfazer: {
                item.setCheckable(false);
                AjustarPainelSuperior(id_desfazer);
                break;
            }
            case R.id.editor_btn_refazer: {
                item.setCheckable(false);
                AjustarPainelSuperior(id_refazer);
                break;
            }
            case R.id.editor_btn_reset: {
                item.setCheckable(false);
                AjustarPainelSuperior(id_resetar);
                break;
            }
            case R.id.editor_btn_salvar: {
                item.setCheckable(false);
                AjustarPainelSuperior(id_salvar);
                break;
            }
            //============================== Menu Bot
            case R.id.editor_sair: {
                item.setCheckable(false);
                AvisoAntesDeSair();
                break;
            }
            case R.id.editor_girar: {
                TrocarNavMenu(R.menu.nav_editor_bot_girar);
                break;
            }
            case R.id.editor_cortar: {
                cropImageView.setShowCropOverlay(true);
                TrocarNavMenu(R.menu.nav_editor_bot_recortar);
                break;
            }
            case R.id.editor_inverter: {
                TrocarNavMenu(R.menu.nav_editor_bot_inverter);
                break;
            }
            //=============================== Girar
            case R.id.editor_girar_esquesra: {
                item.setCheckable(false);
                Rotacionar(ROTACAO_ESQUERTA);
                addAcao(id_rotacionar_esquerda);
                break;
            }
            case R.id.editor_girar_direita: {
                item.setCheckable(false);
                Rotacionar(ROTACAO_DIREITA);
                addAcao(id_rotacionar_direita);
                break;
            }
            // ================================ Inverter
            case R.id.editor_inverter_horizontal: {
                item.setCheckable(false);
                Inverter(INVERTER_HORIZONTAL);
                addAcao(id_inverter_h);
                break;
            }
            case R.id.editor_inverter_vertical: {
                item.setCheckable(false);
                Inverter(INVERTER_VERTICAI);
                addAcao(id_inverter_v);
                break;
            }
            // ============================= Recortar
            case R.id.editor_recortar_cortar: {
                if(bitmap_para_salvar == null){
                    Rect rect = cropImageView.getCropRect();
                    bitmap_para_salvar = getBitmap(false);
                    cropImageView.setCropRect(rect);
                }
                bitmaps_temp.add(bitmap_para_salvar);

                bitmap_para_salvar = getBitmap(true);

                cropImageView.setImageBitmap(bitmap_para_salvar);
                indice_rotacao = 0;
                cropImageView.setRotation(rotacao[indice_rotacao]);
                cropImageView.setFlippedHorizontally(false);
                cropImageView.setFlippedVertically(false);

                cropImageView.setShowCropOverlay(false);
                indice_bitmap++;
                addAcao(id_recortar);
                TrocarNavMenu(R.menu.nav_editor_bot);
                break;
            }
            case R.id.editor_voltar:
            case R.id.editor_recortar_cancelar: {
                cropImageView.setShowCropOverlay(false);
                TrocarNavMenu(R.menu.nav_editor_bot);
                break;
            }
        }
        return true;
    }
    private void TrocarNavMenu(int i){
        nav_bot_principal.getMenu().clear();
        nav_bot_principal.inflateMenu(i);
    }

    @Override
    public void onBackPressed() {
        if(sairConfirmado)
            super.onBackPressed();
        else
            AvisoAntesDeSair();
    }
    private void AvisoAntesDeSair() {
        if(nav_top.getMenu().getItem(ID_TOP_SALVAR).isCheckable()){
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Deseja sair do editor?");
            dialog.setMessage("Você não salvou a ultima alteração. Sua edição será perdida");
            dialog.setPositiveButton("Sair", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    sairConfirmado = true;
                    onBackPressed();
                    overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                }
            });
            dialog.setNegativeButton("Salvar e sair", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    sairConfirmado = true;
                    SalvarImagem();
                    onBackPressed();
                    overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_up);
                }
            });
            dialog.setNeutralButton("Cancelar", null);
            dialog.show();
        } else {
            sairConfirmado = true;
            onBackPressed();
        }
    }

    private void Init() {
        {
            nav_bot_principal = findViewById(R.id.nav_bot_principal);
            nav_top = findViewById(R.id.nav_top);

            nav_bot_principal.setOnNavigationItemSelectedListener(this);
            nav_top.setOnNavigationItemSelectedListener(this);

        }// Painéis
        {
            nav_top.getMenu().getItem(ID_TOP_DESFAZER).setCheckable(false);
            nav_top.getMenu().getItem(ID_TOP_REFAZER).setCheckable(false);
            nav_top.getMenu().getItem(ID_TOP_RESETAR).setCheckable(false);
            nav_top.getMenu().getItem(ID_TOP_SALVAR).setCheckable(false);

            DesativarTudo();
        }// Botões
        cropImageView = findViewById(R.id.editor_ucrop);
        cropImageView.setGuidelines(CropImageView.Guidelines.OFF);
        cropImageView.setScaleType(CropImageView.ScaleType.FIT_CENTER);
        cropImageView.setShowCropOverlay(false);
    }

    private void Rotacionar(int direcao){
        if(direcao > 0) {//direita
            if(indice_rotacao < rotacao.length -1)
                indice_rotacao++;
            else
                indice_rotacao = 0;
        } else {// esquerda
            if (indice_rotacao > 0)
                indice_rotacao--;
            else
                indice_rotacao = rotacao.length - 1;
        }
        cropImageView.setRotation(rotacao[indice_rotacao]);
        bitmap_para_salvar = getBitmap(false);
    }
    private void Inverter(int direcao) {
        switch (direcao){
            case INVERTER_VERTICAI:
                if(indice_rotacao == 1 || indice_rotacao == 3)
                    cropImageView.flipImageHorizontally();
                else
                    cropImageView.flipImageVertically();
                break;
            case INVERTER_HORIZONTAL:
                if(indice_rotacao == 1 || indice_rotacao == 3)
                    cropImageView.flipImageVertically();
                else
                    cropImageView.flipImageHorizontally();
                break;
        }
        bitmap_para_salvar = getBitmap(false);
    }

    private Bitmap getBitmap(boolean recortar) {

        /*Log.e(TAG, "A - Rot: " + cropImageView.getRotation());
        Log.e(TAG, "A - Crop: " + cropImageView.getCroppedImage().getWidth() + " - " + cropImageView.getCroppedImage().getHeight());
        Log.e(TAG, "A - Orig: " + cropImageView.getWholeImageRect().width() + " - " + cropImageView.getWholeImageRect().height());
        Log.e(TAG, "A - Flip: " + cropImageView.isFlippedHorizontally() + " - " + cropImageView.isFlippedVertically());*/

        Matrix matrix = new Matrix();
        int width, height;
        if(recortar) {
            width = cropImageView.getCroppedImage().getWidth();
            height = cropImageView.getCroppedImage().getHeight();
        }  else {
            width = cropImageView.getWholeImageRect().width();
            height = cropImageView.getWholeImageRect().height();

            cropImageView.setCropRect(new Rect(0, 0, width, height));
        }
        matrix.setRotate(cropImageView.getRotation());

        Bitmap temp = Bitmap.createScaledBitmap (cropImageView.getCroppedImage(), width, height, true);
        return Bitmap.createBitmap(temp, 0, 0, temp.getWidth(), temp.getHeight(), matrix, true);
    }

    private void addAcao(int acao_id) {
        {
            /*if(!nav_top.getMenu().getItem(ID_TOP_DESFAZER).isEnabled()) {
                nav_top.getMenu().getItem(ID_TOP_DESFAZER).setEnabled(true);
            }
            if(nav_top.getMenu().getItem(ID_TOP_REFAZER).isEnabled()) {
                nav_top.getMenu().getItem(ID_TOP_REFAZER).setEnabled(false);
            }*/
            if(!nav_top.getMenu().getItem(ID_TOP_RESETAR).isEnabled()) {
                nav_top.getMenu().getItem(ID_TOP_RESETAR).setEnabled(true);
            }
            if(!nav_top.getMenu().getItem(ID_TOP_SALVAR).isEnabled()) {
                nav_top.getMenu().getItem(ID_TOP_SALVAR).setEnabled(true);
            }
        }// Trocar icones do menu superior

        if(acao_id != id_ignore) {
            indice_acao++;
            if (indice_acao < acao.size()) {
                acao.subList(indice_acao, acao.size()).clear();// remove todos os itens apartir do indice_acao

                if (indice_bitmap < bitmaps_temp.size())
                    bitmaps_temp.subList(indice_bitmap + 1, bitmaps_temp.size()).clear();
            }
        }
//        Log.e(TAG, "A: " + indice_bitmap + " : " + bitmaps_temp.size());

        acao.add(acao_id);
    }

    private void AjustarPainelSuperior(int id) {
        switch (id) {
            case id_desfazer: {
                if(indice_acao >= 0) {
//                    Log.e(TAG, "Acao: " + acao.get(indice_acao));
                    switch (acao.get(indice_acao)) {
                        case id_rotacionar_direita: {
                            Rotacionar(ROTACAO_ESQUERTA);
                            break;
                        }
                        case id_rotacionar_esquerda: {
                            Rotacionar(ROTACAO_DIREITA);
                            break;
                        }
                        case id_recortar: {
                            if(!bitmaps_temp.get(bitmaps_temp.size()-1).equals(bitmap_para_salvar)) {
                                bitmaps_temp.add(bitmap_para_salvar);
                            }
                            if(indice_bitmap == bitmaps_temp.size()-1) {
                                indice_bitmap--;
                            }
                            if(indice_bitmap >= 0) {
                                bitmap_para_salvar = bitmaps_temp.get(indice_bitmap);
                                cropImageView.setImageBitmap(bitmap_para_salvar);

                                indice_rotacao = 0;
                                cropImageView.setRotation(rotacao[indice_rotacao]);
                                cropImageView.setFlippedHorizontally(false);
                                cropImageView.setFlippedVertically(false);

                                indice_bitmap--;
//                                Log.e(TAG, "D2: " + indice_bitmap + " --");
                            }
//                            Log.e(TAG, "D: " + indice_bitmap + " : " + bitmaps_temp.size());
                            break;
                        }
                    }
                    AjustarPainelSuperiorAux();
                    indice_acao--;
                    if(indice_acao < 0){
                        /*if(nav_top.getMenu().getItem(ID_TOP_DESFAZER).isEnabled()) {
                            nav_top.getMenu().getItem(ID_TOP_DESFAZER).setEnabled(false);
                        }*/
                        /*if(nav_top.getMenu().getItem(ID_TOP_RESETAR).isEnabled()) {
                            nav_top.getMenu().getItem(ID_TOP_RESETAR).setEnabled(false);
                        }*/
                        if(nav_top.getMenu().getItem(ID_TOP_SALVAR).isEnabled()) {
                            nav_top.getMenu().getItem(ID_TOP_SALVAR).setEnabled(false);
                        }
                    }
                    /*if(!nav_top.getMenu().getItem(ID_TOP_REFAZER).isEnabled()) {
                        nav_top.getMenu().getItem(ID_TOP_REFAZER).setEnabled(true);
                    }*/
                }
                break;
            }
            case id_refazer: {
                if(indice_acao < acao.size()-1) {
                    indice_acao++;
                    switch (acao.get(indice_acao)) {
                        case id_rotacionar_direita: {
                            Rotacionar(ROTACAO_DIREITA);
                            break;
                        }
                        case id_rotacionar_esquerda: {
                            Rotacionar(ROTACAO_ESQUERTA);
                            break;
                        }
                        case id_recortar:
                        case id_ignore: {
                            if(indice_bitmap < 0)
                                indice_bitmap = 0;
                            if(indice_bitmap < bitmaps_temp.size()-1) {
                                indice_bitmap++;

                                bitmap_para_salvar = bitmaps_temp.get(indice_bitmap);
                                cropImageView.setImageBitmap(bitmap_para_salvar);

                                indice_rotacao = 0;
                                cropImageView.setRotation(rotacao[indice_rotacao]);
                                cropImageView.setFlippedHorizontally(false);
                                cropImageView.setFlippedVertically(false);
                            }
                            break;
                        }
                    }
                    AjustarPainelSuperiorAux();
                    {
                        /*if(indice_acao >= acao.size()-1)
                            if(nav_top.getMenu().getItem(ID_TOP_REFAZER).isEnabled())
                                nav_top.getMenu().getItem(ID_TOP_REFAZER).setEnabled(false);

                        if(!nav_top.getMenu().getItem(ID_TOP_DESFAZER).isEnabled())
                            nav_top.getMenu().getItem(ID_TOP_DESFAZER).setEnabled(true);
*/
                        if(!nav_top.getMenu().getItem(ID_TOP_RESETAR).isEnabled())
                            nav_top.getMenu().getItem(ID_TOP_RESETAR).setEnabled(true);

                        if(!nav_top.getMenu().getItem(ID_TOP_SALVAR).isEnabled())
                            nav_top.getMenu().getItem(ID_TOP_SALVAR).setEnabled(true);
                    }

                }
                break;
            }
            case id_resetar: {
                Restaurar();;
            }
            case id_salvar: {
                SalvarImagem();
            }
        }
        Log.e(TAG, "D - Rot: " + cropImageView.getRotation());
    }
    private void AjustarPainelSuperiorAux() {
        switch (acao.get(indice_acao)) {
            case id_inverter_h: {
                Inverter(INVERTER_HORIZONTAL);
                break;
            }
            case id_inverter_v: {
                Inverter(INVERTER_VERTICAI);
                break;
            }
        }
    }


    private void SalvarImagem() {
        if(indice_acao >= 0) {
            try {
                boolean imageSaved = false;
                if (!(bitmap_para_salvar == null || bitmap_para_salvar.isRecycled())) {
                    ContentResolver contentResolver = getContentResolver();
                    removeThumbnails(contentResolver, image_id);
                    File file = new File(image_path);
                    /*if (!file.exists()) {
                        file.mkdirs();
                    }*/
                    //File imageFile = new File(file, String.format("%s.png", new Object[]{imageName}));
                    try {
                        FileOutputStream out = new FileOutputStream(file);
                        imageSaved = bitmap_para_salvar.compress(Bitmap.CompressFormat.PNG, 100, out);
                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        Log.e(TAG, "Unable to write the image to gallery", e);
                    }
                    ContentValues values = new ContentValues(8);
                    values.put("mime_type", "image/png");
                    values.put("description", "");
                    long millis = System.currentTimeMillis();
                    values.put("date_added", Long.valueOf(millis / 1000));
                    values.put("datetaken", Long.valueOf(millis));
                    values.put("_data", file.getAbsolutePath());

                    /*contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    MediaScannerConnection.scanFile(getApplicationContext(), new String[]{file.getPath()}, new String[]{"image/*"}, new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });*/
                }
                if (imageSaved) {
//                    Function.atualizar_adapter = true;
                    Toast.makeText(this, "Imagem salva", Toast.LENGTH_SHORT).show();

                    nav_top.getMenu().getItem(ID_TOP_SALVAR).setEnabled(false);
                } else {
                    Toast.makeText(this, "Erro ao salvar imagem", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e2) {
                Log.e(TAG, "saveSnapshot", e2);
                Toast.makeText(this, "Erro ao salvar imagem", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void Restaurar() {
        cropImageView.setRotation(0f);
        cropImageView.setImageUriAsync(image_uri);
        cropImageView.setFlippedHorizontally(false);
        cropImageView.setFlippedVertically(false);
        indice_acao = reset_indice;
        indice_bitmap = reset_indice;
        indice_rotacao = 0;
        acao = new ArrayList<>();
        bitmaps_temp = new ArrayList<>();
        bitmap_para_salvar = null;

        DesativarTudo();
    }
    private void DesativarTudo(){
//        nav_top.getMenu().getItem(ID_TOP_DESFAZER).setEnabled(false);
//        nav_top.getMenu().getItem(ID_TOP_REFAZER).setEnabled(false);
        nav_top.getMenu().getItem(ID_TOP_RESETAR).setEnabled(false);
        nav_top.getMenu().getItem(ID_TOP_SALVAR).setEnabled(false);
    }


    private void removeThumbnails(ContentResolver contentResolver, long photoId) {

        Cursor thumbnails = contentResolver.query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Thumbnails.IMAGE_ID + "=?",
                new String[]{String.valueOf(photoId)}, null);

        for (thumbnails.moveToFirst(); !thumbnails.isAfterLast(); thumbnails.moveToNext()) {

            long thumbnailId = thumbnails.getLong(thumbnails.getColumnIndex(MediaStore.Images.Thumbnails._ID));
            String path = thumbnails.getString(thumbnails.getColumnIndex(MediaStore.Images.Thumbnails.DATA));
            File file = new File(path);
            if (file.delete()) {

                contentResolver.delete(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                        MediaStore.Images.Thumbnails._ID + "=?", new String[]{String.valueOf(thumbnailId)});

            }
        }

    }
}