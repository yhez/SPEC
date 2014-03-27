package specular.systems.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import specular.systems.CryptMethods;
import specular.systems.CustomExceptionHandler;
import specular.systems.Dialogs.TurnNFCOn;
import specular.systems.FilesManagement;
import specular.systems.KeysDeleter;
import specular.systems.NfcStuff;
import specular.systems.R;
import specular.systems.Visual;


public class SpecSafe extends Activity {
    File file;
    byte[] bytes;
    int req;
    View content;

    @Override
    public void onCreate(Bundle b){
        super.onCreate(b);
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(Visual.getNameReprt(), this));
        }
        req = getIntent().getIntExtra("action",0);
        if(req<1||req>3) {
            setResult(12);
            finish();
            return;
        }
        ComponentName cn = getCallingActivity();
        if(cn==null){
            Log.e("can't find calling activity", "You must call SPEC by using the method startActivityForResult!");
            finish();
            return;
        }
        File path = new File(getFilesDir()+"/SPEC_SAFE/"+cn.getPackageName());
        if(req==3){
            Intent i = new Intent();
            if(path.exists())
                i.putExtra("files",path.list());
            else
                i.putExtra("files",new String[0]);
            setResult(RESULT_OK,i);
            finish();
            return;
        }
        String name = getIntent().getStringExtra("name");
        if(name==null||name.length()==0){
            setResult(11);
            finish();
            return;
        }
        file = new File(path,name);
        if(!file.exists()&&req==2){
            setResult(13);
            finish();
            return;
        }
        FilesManagement.getKeysFromSDCard(this);
        if(req==1){
            if(!CryptMethods.publicExist()) {
                setResult(14);
                finish();
                return;
            }
            bytes = getIntent().getByteArrayExtra("bytes");
            if(bytes==null){
                Uri uri = getIntent().getParcelableExtra(Intent.EXTRA_STREAM);
                try {
                    FileInputStream fis = new FileInputStream(uri.getPath());
                    bytes = new byte[fis.available()];
                    fis.read(bytes);
                    fis.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(!path.exists())
                path.mkdirs();
            encryptAndSave.execute(bytes);
            showDialog();
        }else if(req==2){
                if(!CryptMethods.privateExist()){
                    setContentView(R.layout.wait_nfc_decrypt);
                    content = findViewById(R.id.wait_for_nfc);
                    content.setAlpha(0.9f);
                    if(FilesManagement.id_picture.pictureExist(this))
                        ((ImageView)content.findViewById(R.id.sec_pic)).setImageDrawable(FilesManagement.id_picture.getPicture(this));
                    if(NfcStuff.nfcIsOff(this)){
                        new TurnNFCOn(getFragmentManager());
                    }
                }else {
                    decryptAndReturn.execute();
                    showDialog();
                }
        }
    }
    AsyncTask<byte[],Void,Integer> encryptAndSave = new AsyncTask<byte[],Void,Integer>() {
        @Override
        protected Integer doInBackground(byte[]... params) {
            byte[] encryptedData = CryptMethods.encrypt(bytes, CryptMethods.getPublic());
            int result=15;
            if(encryptedData!=null){
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(encryptedData);
                    fos.close();
                    result = RESULT_OK;
                } catch (Exception ignore) {
                }
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer result){
            if(pd!=null&&pd.isShowing())
                pd.cancel();
            setResult(result);
            finish();
        }
    };
    AsyncTask<Void,Void,byte[]> decryptAndReturn = new AsyncTask<Void, Void, byte[]>() {

        @Override
        protected byte[] doInBackground(Void... params) {
            try {
                FileInputStream fis = new FileInputStream(file);
                bytes = new byte[fis.available()];
                fis.read(bytes);
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return CryptMethods.decrypt(bytes);
        }

        @Override
        protected void onPostExecute(byte[] result){
            if(pd!=null&&pd.isShowing())
                pd.cancel();
            if(result==null) {
                setResult(15);
                finish();
            }else{
                Intent intent = new Intent();
                intent.putExtra("bytes",result);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    };
    @Override
    public void onResume(){
        super.onResume();
        NfcStuff.listen(this,getClass());
    }
    @Override
    public void onPause(){
        new KeysDeleter(this);
        super.onPause();
    }
    @Override
    public void onNewIntent(Intent intent) {
        if (CryptMethods.setPrivate(NfcStuff.getData(intent))) {
            decryptAndReturn.execute();
            ((ViewGroup)content.getParent()).removeView(content);
            showDialog();
        }
        else {
            Visual.toast(this, R.string.cant_find_private_key);
        }
    }
    ProgressDialog pd;
    private void showDialog(){
        pd = new ProgressDialog(this, R.style.dialogTransparent);
        pd.setTitle("Working...");
        pd.setMessage(req == 1 ? "Encrypting...\nSaving..." : "Getting file...\nDecrypting...");
        pd.setCancelable(false);
        pd.show();
    }
    @Override
    public void finish(){
        CryptMethods.deleteKeys();
        super.finish();
    }
}
