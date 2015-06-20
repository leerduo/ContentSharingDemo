package me.chenfuduo.contentsharingdemo.fileprovider;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;

import me.chenfuduo.contentsharingdemo.R;

public class FileSelectActivity extends AppCompatActivity {
    //应用内部存储空间的主目录的路径
    private File mPrivateRootDir;
    //"images"子目录下的路径
    private File mImagesDir;
    //"images"子目录下的文件
    File[] mImageFiles;
    //mImageFiles对应的文件名
    String[] mImageFileNames;

    private Intent mResultIntent;

    private ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_select);
        mPrivateRootDir = getFilesDir();
        //   /data/data/me.chenfuduo.contentsharingdemo/files
        String name = mPrivateRootDir.getAbsolutePath();

        Log.e("TEst","name:" + name);
        //为毛这里报了空指针
        mImagesDir = new File(mPrivateRootDir, "images");
        mImageFiles = mImagesDir.listFiles();

        list = (ListView) findViewById(R.id.list);
        mImageFileNames = new String[mImageFiles.length];
        for (int i = 0; i < mImageFiles.length; i++) {
            mImageFileNames[i] = mImageFiles[i].getName();
        }

        setResult(Activity.RESULT_CANCELED, null);
        list.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mImageFileNames));

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*
                 * Get a File for the selected file name.
                 * Assume that the file names are in the
                 * mImageFilename array.
                 */
                File newRequestFile = new File(mImagesDir,mImageFileNames[position]);
                if (newRequestFile == null){
                    Log.e("Test","newRequestFile is null");
                    return;
                }
                try {
                    Uri uri = FileProvider.getUriForFile(FileSelectActivity.this,
                            "me.chenfuduo.contentsharingdemo.fileprovider", newRequestFile);
                    if (uri != null) {
                        mResultIntent = new Intent();

                        mResultIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        mResultIntent.setDataAndType(uri,getContentResolver().getType(uri));
                        FileSelectActivity.this.grantUriPermission("me.chenfuduo.contentsharingdemo", uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        FileSelectActivity.this.setResult(Activity.RESULT_OK, mResultIntent);
                    } else {
                        mResultIntent.setDataAndType(null,"");
                        FileSelectActivity.this.setResult(Activity.RESULT_CANCELED, mResultIntent);
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    Log.e("Test", "当前选择的文件不能被分享:" + mImageFileNames[position]);
                }

            }
        });
    }

    public void Done(View view){
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_file_select, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
