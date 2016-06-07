package draw.heshi.com.drawpane;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import draw.heshi.com.drawpane.views.BrushPicker;
import draw.heshi.com.drawpane.views.canvas.CanvasView;
import draw.heshi.com.drawpane.views.event.OnBrushPickerSelectedListener;

public class MainActivity extends AppCompatActivity{

    private Toolbar mToolbar_top;
    private Toolbar mToolbar_bottom;
    private static final String LOG_CAT = MainActivity.class.getSimpleName();
    private CanvasView mCustomView;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            verifyStoragePermissions(this);
            
            mCustomView = (CanvasView)findViewById(R.id.canvasView);

            mToolbar_top = (Toolbar) findViewById(R.id.toolbar_top);
            setSupportActionBar(mToolbar_top);

            mToolbar_bottom = (Toolbar)findViewById(R.id.toolbar_bottom);
            mToolbar_bottom.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_dots_vertical_white_24dp));



            mToolbar_bottom.inflateMenu(R.menu.draw_menu);
            mToolbar_bottom.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    handleDrawingIconTouched(item.getItemId());
                    return false;
                }
            });


        }

        private void handleDrawingIconTouched(int itemId) {
            switch (itemId){
                case R.id.action_delete:
                    deleteDialog();
                    break;
                case R.id.action_undo:
                    mCustomView.onClickUndo();
                    break;
                case R.id.action_redo:
                    mCustomView.onClickRedo();
                    break;
                case R.id.action_save:
                    saveDrawingDialog();
                    break;
                case R.id.action_share:
                    shareDrawing();
                    break;
                case R.id.action_brush:
                    brushSizePicker();
                    break;
            }
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
//            getMenuInflater().inflate(R.menu.draw_menu, menu);

            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
//            handleDrawingIconTouched(item.getItemId());
            return super.onOptionsItemSelected(item);
        }

        private void deleteDialog(){
            AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
            deleteDialog.setTitle(getString(R.string.delete_drawing));
            deleteDialog.setMessage(getString(R.string.new_drawing_warning));
            deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    mCustomView.eraseAll();
                    dialog.dismiss();
                }
            });
            deleteDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            deleteDialog.show();
        }

        public void saveDrawingDialog(){
            //save drawing attach to Notification Bar and let User Open Image to share.
            AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
            saveDialog.setTitle("drawing title");
            saveDialog.setMessage("Save drawing to device Gallery?");
            saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    saveThisDrawing();
                }
            });
            saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            saveDialog.show();
        }

        public void saveThisDrawing()
        {
            String path = Environment.getExternalStorageDirectory().toString();
            path = path  +"/"+ getString(R.string.app_name);
            File dir = new File(path);
            //save drawing
            mCustomView.setDrawingCacheEnabled(true);

            //attempt to save
            String imTitle = "Draw" + "_" + System.currentTimeMillis()+".png";
            String imgSaved = MediaStore.Images.Media.insertImage(
                    getContentResolver(), mCustomView.getDrawingCache(),
                    imTitle, "a drawing");

            try {
                if (!dir.isDirectory()|| !dir.exists()) {
                    dir.mkdirs();
                }
                mCustomView.setDrawingCacheEnabled(true);
                File file = new File(dir, imTitle);
                FileOutputStream fOut = new FileOutputStream(file);
                Bitmap bm =  mCustomView.getDrawingCache();
                bm.compress(Bitmap.CompressFormat.PNG, 100, fOut);


            } catch (FileNotFoundException e) {
                e.printStackTrace();
                AlertDialog.Builder alert = new AlertDialog.Builder(this);
                alert.setTitle(R.string.file_not_found_exception_Title);
                alert.setMessage(R.string.file_not_found_exception_Messaage);
                alert.setPositiveButton(R.string.OK, null);
                alert.show();

            } catch (IOException e) {
                Toast unsavedToast = Toast.makeText(getApplicationContext(),
                        R.string.file_not_found_exception_Messaage, Toast.LENGTH_SHORT);
                unsavedToast.show();
                e.printStackTrace();
            }

            if(imgSaved!=null){
                Snackbar.make(mToolbar_bottom, R.string.saved_tost_messages, Snackbar.LENGTH_LONG).show();

//                snackbar.show();
//                Toast savedToast = Toast.makeText(getApplicationContext(),
//                        "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
//                savedToast.show();
            }

            mCustomView.destroyDrawingCache();
        }

        private void shareDrawing() {
            mCustomView.setDrawingCacheEnabled(true);
            mCustomView.invalidate();
            File path1 = Environment.getDataDirectory();
            String path = Environment.getExternalStorageDirectory().toString();
            OutputStream fOut = null;
            File file = new File(path,"Draw_"+System.currentTimeMillis()+".jpg");
           // boolean test = file.getParentFile().mkdirs();

            try {
                file.createNewFile();
            } catch (Exception e) {
                Log.e(LOG_CAT, e.getCause() + e.getMessage());
            }

            try {
                fOut = new FileOutputStream(file);
            } catch (Exception e) {
                Log.e(LOG_CAT, e.getCause() + e.getMessage());
            }

            if (mCustomView.getDrawingCache() == null) {
                Log.e(LOG_CAT,"Unable to get drawing cache ");
            }

            mCustomView.getDrawingCache()
                    .compress(Bitmap.CompressFormat.JPEG, 85, fOut);

            try {
                fOut.flush();
                fOut.close();
            } catch (IOException e) {
                Log.e(LOG_CAT, e.getCause() + e.getMessage());
            }

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            shareIntent.setType("image/jpg");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share image"));


        }

        private void brushSizePicker(){
            //Implement get/set brush size
            BrushPicker brushDialog = BrushPicker.NewInstance((int) mCustomView.getLastBrushSize());
            brushDialog.setOnBrushPickerSelectedListener(new OnBrushPickerSelectedListener() {
                @Override
                public void OnNewBrushSizeSelected(float newBrushSize) {
                    mCustomView.setBrushSize(newBrushSize);
                    mCustomView.setLastBrushSize(newBrushSize);
                }
            });
            brushDialog.show(getSupportFragmentManager(), "Dialog");
        }


}
