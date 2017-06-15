//package com.bob.aidltest;
//
//import android.content.ComponentName;
//import android.content.Intent;
//import android.content.ServiceConnection;
//import android.os.IBinder;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.util.Log;
//
//import com.bob.aidltest.aidl.Book;
//import com.bob.aidltest.aidl.BookManagerService;
//import com.bob.aidltest.aidl.IBookManager;
//
//import java.util.List;
//
//public class MainActivity extends AppCompatActivity {
//
//    private static final String TAG = "MainActivity";
//
//    private ServiceConnection mConnection=new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {
//            IBookManager bookManager =IBookManager.Stub.asInterface(service);
//            try{
//                List<Book> list = bookManager.getBookList();
//                Log.i(TAG, "query book list,list type:" + list.getClass().getCanonicalName());
//                Log.i(TAG, "query book from server:" + list.toString());
//            }catch(Exception e){
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {
//
//        }
//    };
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        Intent intent = new Intent(this, BookManagerService.class);
//        bindService(intent, mConnection, BIND_AUTO_CREATE);
//
//    }
//
//    @Override
//    protected void onDestroy() {
//        unbindService(mConnection);
//        super.onDestroy();
//    }
//}
