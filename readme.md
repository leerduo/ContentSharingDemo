#发送简单的数据到其他的程序
使用Intent的`Intent.ACTION_SEND`触发Intent的动作，同时需要指定数据和数据的类型。从api14之后，可以在ActionBar上
添加一个分享的Item,并使用`ShareActionProvider`进行内容分享。
##发送简单的文本
```java
 public void sendTextContent(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT,"This is my extra text");
        //HTTP.PLAIN_TEXT_TYPE就是text/plain
        intent.setType(HTTP.PLAIN_TEXT_TYPE);
        startActivity(intent);
    }
```
可以使用`Intent.createChooser()`创建应用选择器，这样做的好处：
* 即使用户之前选择了默认的程序去处理这个Intent的Action，但是应用选择器还是会展示出来(Even if the user has previously selected a default action for this intent, the chooser will still be displayed.)
* 如果系统没有应用匹配当前的Action，那么Android系统会给出提示的信息
* 可以为应用选择器对话框指定一个标题
```java
  public void sendTextContent(View view){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT,"This is my extra text");
        //HTTP.PLAIN_TEXT_TYPE就是text/plain
        intent.setType(HTTP.PLAIN_TEXT_TYPE);
        startActivity(Intent.createChooser(intent,"选择分享的程序"));
    }
```
再比如分享昨天的那个Intent的Map相关的Action，如下：
```java
 public void sendTextContent(View view){
      /*  Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT,"This is my extra text");
        //HTTP.PLAIN_TEXT_TYPE就是text/plain
        intent.setType(HTTP.PLAIN_TEXT_TYPE);*/

        // Map point based on address
        Uri location = Uri.parse("geo:0,0?q=1600+Amphitheatre+Parkway,+Mountain+View,+California");
        // Or map point based on latitude/longitude
        // Uri location = Uri.parse("geo:37.422219,-122.08364?z=14"); // z param is zoom level
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);


        startActivity(Intent.createChooser(mapIntent,"选择分享的程序"));
    }
```
那么程序不会崩溃，会弹出一个dialog,如下：
![1](http://1.infotravel.sinaapp.com/pic/30.png)
##分享二进制数据
分享二进制数据需要的Action为`Intent.ACTION_SEND`并且extra为`Intent.EXTRA_STREAM`，这里分享多个图片，用的Action为：`Intent.ACTION_SEND_MULTIPLE`.
```java
 public void sendBinaryContent(View view){
        //"android.intent.action.SEND_MULTIPLE"
        Intent intent=new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("image/*");
        //"android.intent.extra.STREAM"
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, getUriListForImages());
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
        intent.putExtra(Intent.EXTRA_TEXT, "你好 ");
        intent.putExtra(Intent.EXTRA_TITLE, "我是标题");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, "请选择"));
    }


    private ArrayList<Uri> getUriListForImages(){
        ArrayList<Uri> myList = new ArrayList<>();
        String imageDirectoryPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/";
        File imageDirectory = new File(imageDirectoryPath);
        String[] fileList = imageDirectory.list();
        if (fileList.length != 0){
            for (int i = 0; i < 2; i++) {
                ContentValues values = new ContentValues(7);
                values.put(MediaStore.Images.Media.TITLE,fileList[i]);
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileList[i]);
                values.put(MediaStore.Images.Media.DATE_TAKEN, new Date().getTime());
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.ImageColumns.BUCKET_ID, imageDirectoryPath.hashCode());
                values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, fileList[i]);
                values.put("_data", imageDirectoryPath + fileList[i]);
                ContentResolver contentResolver = getApplicationContext().getContentResolver();
                Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                myList.add(uri);
            }
        }
        return myList;
    }
```
#从其他的程序接受简单的数据
上面的程序通过其他的程序分享想分享的内容，这里将自己程序处理为也可以接受别人分享过来的内容。
##更新清单文件
在清单文件的Activity属性下，添加intent-filter属性
```xml
 <intent-filter>
                <action android:name="android.intent.action.SEND" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="image/*" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.SEND" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="text/plain" />
            </intent-filter>
            <intent-filter>
                <action android:name="android.intent.action.SEND_MULTIPLE" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="image/*" />
            </intent-filter>
```
##处理接收到的内容
使用`getIntent()`得到Intent对象，通过Intent对象去做处理。
```java
 @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        // Get intent, action and MIME type
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent); // Handle multiple images being sent
            }
        } else {
            // Handle other intents, such as being started from the home screen
            Log.e("Test", "做其他的Action处理");
        }
    }
```
上面的两段代码在[MyBasicDemo中](https://github.com/leerduo/MyBasicDemo)
这时候，再去分享我们当前程序的多个图片，我们看到打印出来的uri为：
```xml
06-17 10:06:06.736  20299-20299/me.chenfuduo.mybasicdemo E/Test﹕ content://media/external/images/media/34
06-17 10:06:06.736  20299-20299/me.chenfuduo.mybasicdemo E/Test﹕ content://media/external/images/media/35
```
> 需要注意的是，假如其他的应用分享过来的图片非常大，我们需要特殊处理，不应该直接放到UI线程中去处理。

#添加分享
在ActionBar中添加分享的条目使用`ShareActionProvider`做出分享。
##更新Menu菜单
要使用`ShareActionProvider`，必须在item中指定`android:actionProviderClass`属性，如下：
```xml
<menu xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    tools:context=".MainActivity">
    <item
        android:id="@+id/action_settings"
        android:orderInCategory="100"
        android:title="@string/action_settings"
        app:showAsAction="never" />

    <item
        android:id="@+id/action_share"
        app:actionProviderClass="android.support.v7.widget.ShareActionProvider"
        android:icon="@android:drawable/ic_menu_share"
        android:title="分享"
        app:showAsAction="always" />
</menu>
```
为了使用`ShareActionProvider`,需要提供一个Action为`ACTION_SEND`的Intent，并且通过extras(如`EXTRA_TEXT`和`EXTRA_STREAM`)添加附加的数据。
首先得到需要分享动作的MenuItem，再去调用`Menu.getActionProvider()`得到`ShareActionProvider`的实例。最后使用`setShareIntent()`来更新item的intent。
按照文档上面的代码去写，最后程序挂了，报了这样一个错误：
```xml
java.lang.UnsupportedOperationException: This is not supported, use MenuItemCompat.getActionProvider()
```
`ShareActionProvider`需要在api14中才可以使用，但是一些兼容包提供了可以在低版本的设备中也可以使用。按照这个，我打开了官方的sample,
果然，和文档的不一样。我也是醉了。
demo中演示了分享文字图片等等内容。具体参见代码。
![2](http://1.infotravel.sinaapp.com/pic/31.png)
#创建文件分享
文件分享通常使用的API是`FileProvider`,通过`getUriForFile()`方法产生文件内容的uri。
##指定FileProvider
在清单文件中创建入口：
```xml
 <provider
            android:name="android.support.v4.content.FileProvider"
            android:authorities="me.chenfuduo.contentsharingdemo.fileprovider"
            android:grantUriPermissions="true"
            android:exported="false">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/filepaths" />
        </provider>
```
其中android:authorities属性指定了URI的权限，在元数据中，android:resource指定了分享的目录。
##指定分享的目录
在res文件夹下创建xml文件夹，在xml文件夹下创建filepaths.xml文件，指定内容为：
```xml
<?xml version="1.0" encoding="utf-8"?>
<paths>
    <files-path path="images/" name="myimages" />
</paths>
```
在上面的代码片段中`<files-path>`标签指定了分享内部存储的`files/`目录，`path`属性指定了分享`files/`
目录下的`images`子目录。`name`属性指定FileProvider将`myimages`文件夹添加到`files/images/`的尾部，
成为`files/images/myimages`
`<paths>`元素可以有多个孩子结点，每个孩子结点都指定了不同的分享目录。
在`<files-path>`标签下，可以使用`<external-path>`标签指定分享外部存储的目录，使用`<cache-path>`指定分享内部的
缓存目录。
> 需要注意的是，xml是指定要分享目录的唯一的方式，代码中不可以。

通过上面，我们可以得到一个图片的URI，如下：
```xml
content://me.chenfuduo.contentsharingdemo/myimages/default_image.jpg
```
#文件分享
上面的部分是建立文件分享的第一步，创建文件的URI，现在我们的程序需要提供一个选择文件的功能。
##接收文件请求
现在我们的程序称为server，要分享的程序称为client，client通过`startActivityForResult()`方法，并携带Action为
`Intent.ACTION_PICK`的Intent启动了server，完后server将URI返回给client。client再根据URI做出处理。
##发送文件请求
按照上面的分析，可以写出下面的代码：
```java
  private Intent mRequestFileIntent;
    //The FileDescriptor returned by Parcel.readFileDescriptor(), allowing you to close it when done with it.
    private ParcelFileDescriptor mInputPFD;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRequestFileIntent = new Intent(Intent.ACTION_PICK);
        mRequestFileIntent.setType("image/jpg");
    }

    public void fileSharing(View view){
        startActivityForResult(mRequestFileIntent, 0);
    }
```
##接收请求到的文件
server端通过Intent将URI传给client端，通过`onActivityResult()`得到URI，再通过`FileDescriptor`便可以得到文件了。
在整个过程中，文件的安全是可以得到保证的，因为client只能接收到URI，并且URI不包括目录的路径，client端不能发现和打开server端的
任意的文件。整个权限是短暂的，一旦client的任务栈结束，server端的file就不可接入了。
```java
 @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If the selection didn't work
        if (resultCode != RESULT_OK) {
            // Exit without doing anything else
            return;
        } else {
            // Get the file's content URI from the incoming Intent
            Uri returnUri = data.getData();
            /*
             * Try to open the file for "read" access using the
             * returned URI. If the file isn't found, write to the
             * error log and return.
             */
            try {
                /*
                 * Get the content resolver instance for this context, and use it
                 * to get a ParcelFileDescriptor for the file.
                 */
                mInputPFD = getContentResolver().openFileDescriptor(returnUri, "r");
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("MainActivity", "File not found.");
                return;
            }
            // Get a regular file descriptor for the file
            FileDescriptor fd = mInputPFD.getFileDescriptor();
        }

    }
```
得到`FileDescriptor`后，便可以处理File了。
上面的两步另外的一个工程中完成。
##创建文件选择器
在Activity下，指定intent-filter属性：
```xml
  <activity
            android:name=".fileprovider.FileSelectActivity"
            android:label="@string/title_activity_file_select" >

            <intent-filter>
                <action
                    android:name="android.intent.action.PICK"/>
                <category
                    android:name="android.intent.category.DEFAULT"/>
                <category
                    android:name="android.intent.category.OPENABLE"/>
                <data android:mimeType="text/plain"/>
                <data android:mimeType="image/*"/>
            </intent-filter>


        </activity>
```
需要指定action和相应的category以及data。
接下来在Activity中展示应用的`files/images/`目录下用户可以选择的文件。
```java
 @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_select);
        mResultIntent = new Intent("me.chenfuduo.contentsharingdemo.ACTION_RETURN_FILE");
        mPrivateRootDir = getFilesDir();
        mImagesDir = new File(mPrivateRootDir,"images");
        mImageFiles = mImagesDir.listFiles();
        setResult(Activity.RESULT_CANCELED,null);
        list = (ListView) findViewById(R.id.list);
        mImageFileNames = new String[mImageFiles.length];
        for (int i = 0; i < mImageFiles.length; i++) {
            mImageFileNames[i] = mImageFiles[i].getName();
        }
        list.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,android.R.id.text1,mImageFileNames));
    }
```
##响应文件选择
在ListView的onItemClick事件中，通过选择的文件名得到File对象，再将File对象传入到FileProvider的`getUriForFile()`方法中，
这样我们就可以得到一个对象了，在`getUriForFile()`方法中，同时需要传入authority，就是之前在清单文件中定义的。
> 需要注意的是，我们之前在清单文件中的元数据中，指定了`<paths>`元素，我们只能为该path下的目录的文件产生uri，其他的不行，
会报`IllegalArgumentException `的异常。

##为文件授予权限
```java
list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*
                 * Get a File for the selected file name.
                 * Assume that the file names are in the
                 * mImageFilename array.
                 */
                File newRequestFile = new File(mImageFileNames[position]);
                try {
                    Uri uri = FileProvider.getUriForFile(FileSelectActivity.this,
                            "me.chenfuduo.contentsharingdemo.fileprovider", newRequestFile);
                    if (uri != null){
                        mResultIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                    }
                } catch (IllegalArgumentException  e) {
                    e.printStackTrace();
                    Log.e("Test","当前选择的文件不能被分享:" + mImageFileNames[position]);
                }

            }
        });
```
##将文件分享给请求应用
```java
 list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /*
                 * Get a File for the selected file name.
                 * Assume that the file names are in the
                 * mImageFilename array.
                 */
                File newRequestFile = new File(mImageFileNames[position]);
                try {
                    Uri uri = FileProvider.getUriForFile(FileSelectActivity.this,
                            "me.chenfuduo.contentsharingdemo.fileprovider", newRequestFile);
                    if (uri != null) {
                        mResultIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                        mResultIntent.setDataAndType(uri, getContentResolver().getType(uri));
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
```
#得到文件信息
> 注意：上面的代码在测试的过程中，出现了很多问题，现在都修正了，具体的参见demo.

主要修正的地方在这里：
```java
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
```
在client端，可以得到文件的信息了。
以分享图片为例，将从server得到的图片设置下：
```java
 Uri returnUri = data.getData();
            try {
                InputStream stream = getContentResolver().openInputStream(returnUri);
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                iv.setImageBitmap(bitmap);
```
(具体参见完整代码)
获取图片的信息：
```java
@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If the selection didn't work
        if (resultCode != RESULT_OK) {
            // Exit without doing anything else
            return;
        } else {
            // Get the file's content URI from the incoming Intent
            Uri returnUri = data.getData();
            try {
                InputStream stream = getContentResolver().openInputStream(returnUri);
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                iv.setImageBitmap(bitmap);
                //========================================================================


                ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(returnUri,"r");

                FileDescriptor fd = pfd.getFileDescriptor();

                String mimeType = getContentResolver().getType(returnUri);

                Cursor returnCursor = getContentResolver().query(returnUri,null,null,null,null);

                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);

                returnCursor.moveToFirst();



                Log.e("Test","MIMEType: " + mimeType + " nameIndex: " + nameIndex + " sizeIndex: " + sizeIndex);



            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.e("MainActivity","出错了:" + e.getMessage());
            }
        }

    }
```
ok,到这里我们的这部分就完工了。
server端的[源码](https://github.com/leerduo/ContentSharingDemo)
client端的[源码](https://github.com/leerduo/RequestFileSharing_ContentSharingDemo)











































