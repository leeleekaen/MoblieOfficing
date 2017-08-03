package com.r2.scau.moblieofficing.activity;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.r2.scau.moblieofficing.R;
import com.r2.scau.moblieofficing.adapter.FMUploadSelectFileAdapter;
import com.r2.scau.moblieofficing.untils.Contacts;
import com.r2.scau.moblieofficing.untils.OkHttpClientManager;
import com.r2.scau.moblieofficing.untils.ToastUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.r2.scau.moblieofficing.untils.OkHttpClientManager.okHttpClient;

/**
 * Created by EdwinCheng on 2017/8/1.
 */

public class UploadSelectFileActivity extends BaseActivity{

    private static final String TAG = "UploadSelectFileActivity";
    private RecyclerView uploadRecycler;
    private FMUploadSelectFileAdapter uploadAdapter;
    private LinearLayoutManager linearLayoutManager;

    private List<File> uploadFilelist;
    private File[] files;
    //需要上传到的远程路径，
    private String remotePath;
    private String rootpath;
    private Stack<String> currentPath;

    private Toolbar mtoolbar;
    private TextView titltTV;

    private Bundle bundle;
    private Handler handler;
    private Message message;

    @Override
    protected void initView() {
        setContentView(R.layout.activity_upload_selectfile);
        mtoolbar = (Toolbar) findViewById(R.id.toolbar);
        titltTV = (TextView) findViewById(R.id.toolbar_title);
        uploadRecycler = (RecyclerView) findViewById(R.id.upload_selectefile_recycler);

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch(msg.what){
                    case Contacts.FILEMANAGER.FILEUPLOAD_SUCCESS:
                        setResult(RESULT_OK);
                        UploadSelectFileActivity.this.finish();

                        break;


                }
            }
        };
    }

    @Override
    protected void initData() {
        if (uploadFilelist == null){
            bundle = new Bundle();
            uploadFilelist = new ArrayList<>();
            currentPath = new Stack<>();
            linearLayoutManager = new LinearLayoutManager(UploadSelectFileActivity.this);
        }
        bundle = getIntent().getExtras();
        remotePath = bundle.getString("remotePath");
        if (remotePath.equals("")){
            remotePath = "/";
        }

        /**
         * Create by edwincheng in 2017/08/02
         * 设置 标题 以及左按钮
         */
        mtoolbar.setTitle("");
        UploadSelectFileActivity.this.setSupportActionBar(mtoolbar);
        UploadSelectFileActivity.this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        titltTV.setText(R.string.upload_selectfile);

        rootpath = Environment.getExternalStorageDirectory().toString();
        files =  Environment.getExternalStorageDirectory().listFiles();

        currentPath.push(rootpath);
        for(File f : files){
            uploadFilelist.add(f);
        }
        initRecycler();
    }

    private void initRecycler() {

        for (File f : uploadFilelist){
            Log.e(TAG, "initRecycler:文件？ 文件夹？ " + f.isFile()  + f.isDirectory());
        }
        uploadAdapter = new FMUploadSelectFileAdapter(UploadSelectFileActivity.this,uploadFilelist);
        uploadRecycler.setLayoutManager(linearLayoutManager);
        uploadRecycler.setAdapter(uploadAdapter);

        this.uploadAdapter.setOnItemClickLitener(new OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {
                //对文件的处理
                if (uploadFilelist.get(position).isDirectory()){
                    /**
                     * 如果是文件夹
                     * 清除列表数据
                     * 获得目录中的内容，计入列表中
                     * 适配器通知数据集改变
                     */
                    ToastUtils.show(UploadSelectFileActivity.this, "点击是文件夹路径:" + uploadFilelist.get(position).getAbsolutePath(), Toast.LENGTH_SHORT);
                    currentPath.push("/" + uploadFilelist.get(position).getName());
                    showChange(getPathString());
                }else if (uploadFilelist.get(position).isFile()){
                    ToastUtils.show(UploadSelectFileActivity.this, "点击的文件绝对路径：" + uploadFilelist.get(position).getAbsolutePath(), Toast.LENGTH_SHORT);

                    //网络post操作上传文件
                    doUpload(uploadFilelist.get(position));

                }else {
                    ToastUtils.show(UploadSelectFileActivity.this, "系统内部文件,访问拒绝" , Toast.LENGTH_SHORT);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                ToastUtils.show(UploadSelectFileActivity.this, "长点击position:" + position, Toast.LENGTH_SHORT);
            }
        });
    }

    //上传文件
    public void doUpload(File uploadFile){
        Log.e(TAG, "doUpload:  文件上传"  );

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("path",remotePath)
                .addFormDataPart("fileName",uploadFile.getName())
                .addFormDataPart("userPhone","123456789010")
                .addFormDataPart("file",uploadFile.getName(),RequestBody.create(null,uploadFile));

        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(Contacts.computer_ip + Contacts.file_Server + Contacts.fileUpload)
                .addHeader("cookie", OkHttpClientManager.loginSessionID)
                .post(requestBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // TODO: 10-0-1  请求失败
                Log.e(TAG,"getFileListFromServer  fail");
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // TODO: 10-0-1 请求成功
                message = handler.obtainMessage();
                if (response.code() == 200){
                    message.what = Contacts.FILEMANAGER.FILEUPLOAD_SUCCESS;
                    Log.e(TAG, "onResponse: " + response.body().string() );
                }else {
                    Log.e(TAG, "网络请求 错误  " + response.code() + "   " + response.message());
                    message.what = Contacts.FILEMANAGER.FILEUPLOAD_FAILURE;
                }
                handler.sendMessage(message);
            }
        });
    }

    private void showChange(String path){
        files = new File(path).listFiles();
        uploadFilelist.clear();
        for (File f : files){
            uploadFilelist.add(f);
        }

        for (File f : uploadFilelist){
            Log.e(TAG, "initRecycler:文件？ 文件夹？ " + f.isFile()  + f.isDirectory());
        }
        uploadAdapter.setFileList(uploadFilelist);
    }

    private String getPathString(){
        Stack<String> temp = new Stack<>();
        temp.addAll(currentPath);
        String result = "";
        while (temp.size() != 0) {
            result = temp.pop() + result;
        }
        return result;
    }

    @Override
    protected void initListener() {
    }

    @Override
    public void onClick(View v) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_CANCELED);
                UploadSelectFileActivity.this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (currentPath.peek() == rootpath) {
            setResult(RESULT_CANCELED);
            UploadSelectFileActivity.this.finish();
        } else {
            currentPath.pop();
            showChange(getPathString());
        }
    }
}
