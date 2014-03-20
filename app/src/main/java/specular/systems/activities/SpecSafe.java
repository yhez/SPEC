package specular.systems.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import specular.systems.CryptMethods;
import specular.systems.FilesManagement;
import specular.systems.KeysDeleter;
import specular.systems.NfcStuff;
import specular.systems.R;


public class SpecSafe extends Activity {
    @Override
    public void onCreate(Bundle b){
        super.onCreate(b);
        int req = getIntent().getIntExtra("action",0);
        String name = getIntent().getStringExtra("name");
        ComponentName cn = getCallingActivity();
        if(cn==null||name==null||req==0) {
            finish();
            return;
        }
        File path = new File(getFilesDir()+"/SPEC_SAFE/"+cn.getPackageName());
        if(!path.exists())
            path.mkdirs();
        File file = new File(path,name);
        FilesManagement.getKeysFromSDCard(this);
        if(req==1){
            if(!CryptMethods.publicExist()) {
                setResult(RESULT_CANCELED);
                finish();
                return;
            }
            byte[] bytes = getIntent().getByteArrayExtra("bytes");
            byte[] encryptedData = CryptMethods.encrypt(bytes, CryptMethods.getPublic());
            if(encryptedData==null){
                setResult(RESULT_CANCELED);
                finish();
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(encryptedData);
                fos.close();
                setResult(RESULT_OK);
                finish();
            } catch (Exception e) {
                setResult(RESULT_CANCELED);
                finish();
            }
        }else if(req==2){
            try {
                FileInputStream fis = new FileInputStream(file);
                bytes = new byte[fis.available()];
                fis.read(bytes);
                if(!CryptMethods.privateExist()){
                    setContentView(R.layout.wait_nfc_decrypt);
                }else {
                    decrypt();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    byte[] bytes;
    private void decrypt(){
        byte[] decryptedBytes = CryptMethods.decrypt(bytes);
        if (decryptedBytes == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("bytes", decryptedBytes);
        setResult(RESULT_OK, intent);
        finish();
    }
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
    public void onNewIntent(Intent intent){
        if(CryptMethods.setPrivate(NfcStuff.getData(intent)))
            decrypt();
        else {
            Toast t = Toast.makeText(this, R.string.cant_find_private_key, Toast.LENGTH_SHORT);
            t.setGravity(Gravity.CENTER,0,0);
            t.show();
        }
    }
}
