package com.example.mynotepad;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.mynotepad.feature.CallbackBundle;
import com.example.mynotepad.feature.OpenFileDialog;
import com.example.mynotepad.feature.SaveFileDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class NotepadActivity extends Activity {
    private final static String TAG = "NotepadActivity";
    private final static int OPEN_FILE_DIALOG_ID = 101;
    private final static int SAVE_FILE_DIALOG_ID = 102;

    private String currentPath = "/sdcard/";

    //view components
    private Button mBtnFile,mBtnEdit;
    private EditText mEtContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notepad);
        setupViewComponents();
    }

    private void setupViewComponents(){
        mBtnFile = (Button) findViewById(R.id.buttonFile);
        mBtnEdit = (Button) findViewById(R.id.buttonEdit);
        mEtContent = (EditText) findViewById(R.id.EditTextContent);
        mBtnFile.setOnClickListener(mBtnOCL);
    }

    private View.OnClickListener mBtnOCL = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(v.getId()==mBtnFile.getId()){
                //showDialog(OPEN_FILE_DIALOG_ID);
                createFilePopupWindow();
            }
            if( !(v instanceof EditText)){
                InputMethodManager manager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
            }
        }
    };

    private void createFilePopupWindow(){
        PopupMenu popupMenu = new PopupMenu(getApplicationContext(),mBtnFile);
        popupMenu.inflate(R.menu.file_popupmenu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.file_menu_open:
                        showDialog(OPEN_FILE_DIALOG_ID);
                        break;
                    case R.id.file_menu_save:
                        String str = mEtContent.getText().toString();
                        if(mEtContent.getText().toString() != null || mEtContent.getText().toString().length() > 0){
                            showDialog(SAVE_FILE_DIALOG_ID);
                        }else{
                            Toast.makeText(getApplicationContext(),"Content is empty!",Toast.LENGTH_LONG).show();
                        }
                        break;
                    case R.id.file_menu_exit:
                        finish();
                        break;
                }
                return true;
            }
        });
        popupMenu.show();

    }

    private String readTxtFile(String path){
        File file = new File(path);
        String  str = null;
        try {
            FileInputStream inputStream = new FileInputStream(file);
            int length = inputStream.available();
            byte[] buffer = new byte[length];
            while(inputStream.read(buffer) !=-1);
            inputStream.close();
            str = new String(buffer,"UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return str;
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        if(id == OPEN_FILE_DIALOG_ID){
            Map<String,Integer> images = new HashMap<String, Integer>();
            images.put(OpenFileDialog.sParent,R.drawable.filedialog_folder_up);
            images.put(OpenFileDialog.sRoot,R.drawable.filedialog_root);
            images.put(OpenFileDialog.sFolder,R.drawable.filedialog_folder);
            images.put("wav",R.drawable.filedialog_wavfile);
            images.put(OpenFileDialog.sEmpty,R.drawable.filedialog_file);
            //images.put("bin",R.drawable.filedialog_file);
            Dialog dialog = OpenFileDialog.createDialog(id,this,"Open File",new CallbackBundle(){

                        @Override
                        public void callback(Bundle bundle) {
                            String filepath = bundle.getString(OpenFileDialog.EXTRA_STRING_PATH);
                            Log.v(TAG,"path= "+ filepath);
                            currentPath = filepath.substring(0,filepath.lastIndexOf("/")+1);
                            String txt = readTxtFile(filepath);
                            if(txt != null){
                                mEtContent.setText(txt);
                            }
                        }
                    },"",images,currentPath
            );
            return dialog;
        }else if(id == SAVE_FILE_DIALOG_ID){
            Map<String,Integer> images = new HashMap<String, Integer>();
            images.put(SaveFileDialog.sParent,R.drawable.filedialog_folder_up);
            images.put(SaveFileDialog.sRoot,R.drawable.filedialog_root);
            images.put(SaveFileDialog.sFolder,R.drawable.filedialog_folder);
            images.put("wav",R.drawable.filedialog_wavfile);
            images.put(SaveFileDialog.sEmpty,R.drawable.filedialog_file);
            //images.put("bin",R.drawable.filedialog_file);
            Dialog dialog = SaveFileDialog.createDialog(id,this,"Save File",new CallbackBundle(){

                        @Override
                        public void callback(Bundle bundle) {
                            String path = bundle.getString(SaveFileDialog.EXTRA_STRING_PATH);
                            try {
                                FileOutputStream out = new FileOutputStream(new File(path));
                                byte[] data = mEtContent.getText().toString().getBytes("UTF-8");
                                if(data.length>0){
                                    Toast.makeText(getApplicationContext(),"Saved",Toast.LENGTH_LONG).show();
                                    out.write(data);
                                }else{
                                    Toast.makeText(getApplicationContext(),"Save failed",Toast.LENGTH_LONG).show();
                                }
                                out.close();
                            } catch (IOException e) {
                                Toast.makeText(getApplicationContext(),"Save failed",Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        }
                    },"",images,currentPath
            );
            return dialog;

        }
        return null;
    }
}
