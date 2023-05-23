package ru.mirea.rebrov.securesharedpreferences;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.security.keystore.KeyGenParameterSpec;
import android.util.Base64;
import android.view.View;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import ru.mirea.rebrov.securesharedpreferences.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity
{
    private ActivityMainBinding binding;
    private String cypherPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        KeyGenParameterSpec keyGenParameterSpec = MasterKeys.AES256_GCM_SPEC;
        String mainKeyAlias;
        try
        {
            mainKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec);
        }
        catch (GeneralSecurityException | IOException e)
        {
            throw new RuntimeException(e);
        }
        SharedPreferences secureSharedPreferences = null;
        try
        {
            secureSharedPreferences = EncryptedSharedPreferences.create(
                    "secret_shared_prefs",
                    mainKeyAlias,
                    getBaseContext(),
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        }
        catch (GeneralSecurityException | IOException e)
        {
            throw new RuntimeException(e);
        }
        if(!secureSharedPreferences.getString("poet", "null").equals("null") && !secureSharedPreferences.getString("photo", "null").equals("null"))
        {
            binding.editText.setText(secureSharedPreferences.getString("poet", "null"));
            byte[] photos = Base64.decode(secureSharedPreferences.getString("photo", "null"), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(photos, 0, photos.length);
            binding.imageView.setImageBitmap(bitmap);
        }
        SharedPreferences finalSecureSharedPreferences = secureSharedPreferences;

        binding.button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finalSecureSharedPreferences.edit().putString("poet", binding.editText.getText().toString()).apply();
                finalSecureSharedPreferences.edit().putString("photo", cypherPhoto).apply();
            }
        });

        binding.imageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, 101);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && null != data)
        {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            binding.imageView.setImageBitmap(bitmap);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] photos = baos.toByteArray();
            cypherPhoto = Base64.encodeToString(photos, Base64.DEFAULT);
        }
    }

}