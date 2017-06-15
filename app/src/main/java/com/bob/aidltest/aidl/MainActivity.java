package com.bob.aidltest.aidl;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int MESSAGE_NEW_BOOK_ARRIVED=1;
    private static final int MESSAGE_QUERY_BOOKLIST=2;

    private IBookManager mRemoteBookManager;

    private IOnNewBookArrivedListener[] mOnNewBookArrivedListener=new IOnNewBookArrivedListener[10];

    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_NEW_BOOK_ARRIVED:
                    Log.i(TAG, "new book has arrived:" + msg.obj);
                    break;
                case MESSAGE_QUERY_BOOKLIST:
                    Toast.makeText(MainActivity.this, msg.obj.toString(),Toast.LENGTH_SHORT).show();
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private void addListener() {
        for(int i=0 ;i<mOnNewBookArrivedListener.length ;i++){
            mOnNewBookArrivedListener[i]=new IOnNewBookArrivedListener.Stub() {
                @Override
                public void onNewBookArrived(Book book) throws RemoteException {
                    mHandler.obtainMessage(MESSAGE_NEW_BOOK_ARRIVED,book).sendToTarget();
                }
            };
        }
    }

    private IBinder.DeathRecipient mDeathRecipient=new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.i(TAG, "binder died,thread name:" + Thread.currentThread().getName());
            if (mDeathRecipient == null) {
                return;
            }
            mRemoteBookManager.asBinder().unlinkToDeath(mDeathRecipient, 0);
            mRemoteBookManager = null;
        }
    };

    private ServiceConnection mConnection=new ServiceConnection() {
        //onServiceConnected与onServiceDisconnected都是在主线程中的，所以如果里面如果涉及到服务端的耗时操作那么需要在子线程中进行
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            final IBookManager bookManager =IBookManager.Stub.asInterface(service);
            mRemoteBookManager = bookManager;
            try{
                mRemoteBookManager.asBinder().linkToDeath(mDeathRecipient,0);
                Book newbook = new Book(3, "机器学习");
                bookManager.addBook(newbook);
                Log.i(TAG, "after add book:" + bookManager.getBookList().toString());
                addListener();
                for(int i=0 ;i<mOnNewBookArrivedListener.length  ;i++){
                    bookManager.registerListener(mOnNewBookArrivedListener[i]);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mRemoteBookManager = null;
            Log.i(TAG, "onServiceDisconnected.threadname:" + Thread.currentThread().getName());

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = new Intent(this, BookManagerService.class);
        bindService(intent, mConnection, BIND_AUTO_CREATE);

    }

    public void get_book_list(View view) {
        Toast.makeText(this, "正在获取书的列表", Toast.LENGTH_SHORT).show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mRemoteBookManager != null) {
                    try{
                        final List<Book> books = mRemoteBookManager.getBookList();
                        Log.i(TAG, "query book list,list type:" + books.getClass().getCanonicalName());
                        Log.i(TAG, "query book from server:" + books.toString());
                        mHandler.obtainMessage(MESSAGE_QUERY_BOOKLIST, books).sendToTarget();

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();


    }

    @Override
    protected void onDestroy() {
        if (mRemoteBookManager != null && mRemoteBookManager.asBinder().isBinderAlive()) {
            try{
                Log.i(TAG, "client unregisters listener:" + mOnNewBookArrivedListener);
                for(int i=0 ;i<mOnNewBookArrivedListener.length  ;i++){
                    mRemoteBookManager.unregisterListener(mOnNewBookArrivedListener[i]);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        unbindService(mConnection);
        super.onDestroy();
    }
}
