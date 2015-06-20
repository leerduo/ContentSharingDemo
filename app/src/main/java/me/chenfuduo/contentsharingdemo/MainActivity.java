package me.chenfuduo.contentsharingdemo;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ShareActionProvider mShareActionProvider;

    // The items to be displayed in the ViewPager
    private final ArrayList<ContentItem> mItems = getSampleContent();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve the ViewPager from the content view
        ViewPager vp = (ViewPager) findViewById(R.id.viewpager);

        // Set an OnPageChangeListener so we are notified when a new item is selected
        vp.setOnPageChangeListener(mOnPageChangeListener);

        // Finally set the adapter so the ViewPager can display items
        vp.setAdapter(mPagerAdapter);

    }

    /**
     * @return An ArrayList of ContentItem's to be displayed in this sample
     */
    static ArrayList<ContentItem> getSampleContent() {
        ArrayList<ContentItem> items = new ArrayList<ContentItem>();

        items.add(new ContentItem(ContentItem.CONTENT_TYPE_IMAGE, "photo_1.jpg"));
        items.add(new ContentItem(ContentItem.CONTENT_TYPE_TEXT, R.string.quote_1));
        items.add(new ContentItem(ContentItem.CONTENT_TYPE_TEXT, R.string.quote_2));
        items.add(new ContentItem(ContentItem.CONTENT_TYPE_IMAGE, "photo_2.jpg"));
        items.add(new ContentItem(ContentItem.CONTENT_TYPE_TEXT, R.string.quote_3));
        items.add(new ContentItem(ContentItem.CONTENT_TYPE_IMAGE, "photo_3.jpg"));

        return items;
    }


    public void sendTextContent(View view) {
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


        startActivity(Intent.createChooser(mapIntent, "选择分享的程序"));
    }


    public void sendBinaryContent(View view) {
        //"android.intent.action.SEND_MULTIPLE"
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("image/*");
        //"android.intent.extra.STREAM"
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, getUriListForImages());
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享");
        intent.putExtra(Intent.EXTRA_TEXT, "你好 ");
        intent.putExtra(Intent.EXTRA_TITLE, "我是标题");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(Intent.createChooser(intent, "请选择"));
    }


    private ArrayList<Uri> getUriListForImages() {
        ArrayList<Uri> myList = new ArrayList<>();
        String imageDirectoryPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download/";
        File imageDirectory = new File(imageDirectoryPath);
        String[] fileList = imageDirectory.list();
        if (fileList.length != 0) {
            for (int i = 0; i < 2; i++) {
                ContentValues values = new ContentValues(7);
                values.put(MediaStore.Images.Media.TITLE, fileList[i]);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu resource
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Retrieve the share menu item
        MenuItem shareItem = menu.findItem(R.id.action_share);

        // Now get the ShareActionProvider from the item
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);

        // Get the ViewPager's current item position and set its ShareIntent.
        int currentViewPagerItem = ((ViewPager) findViewById(R.id.viewpager)).getCurrentItem();
        setShareIntent(currentViewPagerItem);

        return super.onCreateOptionsMenu(menu);

    }

    /**
     * A PagerAdapter which instantiates views based on the ContentItem's content type.
     */
    private final PagerAdapter mPagerAdapter = new PagerAdapter() {
        LayoutInflater mInflater;

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // Just remove the view from the ViewPager
            container.removeView((View) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            // Ensure that the LayoutInflater is instantiated
            if (mInflater == null) {
                mInflater = LayoutInflater.from(MainActivity.this);
            }

            // Get the item for the requested position
            final ContentItem item = mItems.get(position);

            // The view we need to inflate changes based on the type of content
            switch (item.contentType) {
                case ContentItem.CONTENT_TYPE_TEXT: {
                    // Inflate item layout for text
                    TextView tv = (TextView) mInflater
                            .inflate(R.layout.item_text, container, false);

                    // Set text content using it's resource id
                    tv.setText(item.contentResourceId);

                    // Add the view to the ViewPager
                    container.addView(tv);
                    return tv;
                }
                case ContentItem.CONTENT_TYPE_IMAGE: {
                    // Inflate item layout for images
                    ImageView iv = (ImageView) mInflater
                            .inflate(R.layout.item_image, container, false);

                    // Load the image from it's content URI
                    iv.setImageURI(item.getContentUri());

                    // Add the view to the ViewPager
                    container.addView(iv);
                    return iv;
                }
            }

            return null;
        }
    };

    private void setShareIntent(int position) {

        if (mShareActionProvider != null) {
            // Get the currently selected item, and retrieve it's share intent
            ContentItem item = mItems.get(position);
            Intent shareIntent = item.getShareIntent(MainActivity.this);

            // Now update the ShareActionProvider with the new share intent
            mShareActionProvider.setShareIntent(shareIntent);
        }

    }

    /**
     * A OnPageChangeListener used to update the ShareActionProvider's share intent when a new item
     * is selected in the ViewPager.
     */
    private final ViewPager.OnPageChangeListener mOnPageChangeListener
            = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            // NO-OP
        }

        @Override
        public void onPageSelected(int position) {
            setShareIntent(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            // NO-OP
        }
    };




}
