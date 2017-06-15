package com.bob.aidltest.aidl;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by bob on 17-6-13.
 */

public class BookManagerService extends Service {

    private static final String TAG = "BookManagerService";
    private AtomicBoolean mIsServiceDestroyed = new AtomicBoolean(false);

    private CopyOnWriteArrayList<Book> mbookList = new CopyOnWriteArrayList<Book>();
    //    private CopyOnWriteArrayList<IOnNewBookArrivedListener> mListenerList = new CopyOnWriteArrayList<IOnNewBookArrivedListener>();

    //由于跨进程之间传递的不是同一个对象，所以使用CopyOnWriteArrayList会导致取消注册时候出错
    //因此采用RemoteCallbackList可以讲Binder与listener进行绑定存储在map中，这样就不会出错
    private RemoteCallbackList<IOnNewBookArrivedListener> mlistenerList = new RemoteCallbackList<>();



    private Binder mBinder=new IBookManager.Stub(){

        @Override
        public List<Book> getBookList() throws RemoteException {
            SystemClock.sleep(5000);
            return mbookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            mbookList.add(book);
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener) throws RemoteException {
//            if (!mListenerList.contains(listener)) {
//                mListenerList.add(listener);
//            } else {
//                Log.i(TAG, "already exists");
//            }
//            Log.i(TAG, "register listener size:" + mListenerList.size());
            mlistenerList.register(listener);
            final int N = mlistenerList.beginBroadcast();
            mlistenerList.finishBroadcast();
            Log.i(TAG, "register listener size:" + N);
        }

        @Override
        public void unregisterListener(IOnNewBookArrivedListener listener) throws RemoteException {
//            if (mListenerList.contains(listener)) {
//                mListenerList.remove(listener);
//                Log.i(TAG, "unregister listener successfully");
//            } else {
//                Log.i(TAG, "not found,can not unregiter");
//            }
            boolean isSuccess=mlistenerList.unregister(listener);
            if (isSuccess) {
                Log.i(TAG, "unregister listener successfully");
            } else {
                Log.i(TAG, "not found,can not unregiter");
            }

            final int N = mlistenerList.beginBroadcast();
            mlistenerList.finishBroadcast();
            Log.i(TAG, "unregister listener size" + N);
        }

        //第二种验证权限的方法
        //验证权限以及包名
        @Override
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            //验证权限
            int check = checkCallingOrSelfPermission("com.bob.aidltest.aidl.ACCESS_BOOK_SERVICE");
            Log.i(TAG, "onTransact is checking permission:" + check);
            if (check == PackageManager.PERMISSION_DENIED) {
                return false;
            }
            String packageName = null;
            String[] packages = getPackageManager().getPackagesForUid(getCallingUid());
            if (packages != null && packages.length > 0) {
                packageName = packages[0];
            }
            Log.i(TAG, "onTranscat:" + packageName);
            if (!packageName.startsWith("com.bob")) {
                return false;
            }
            return super.onTransact(code, data, reply, flags);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        mbookList.add(new Book(1, "Android"));
        mbookList.add(new Book(2, "IOS "));
        new Thread(new ServiceWorker()).start();
    }

    //第一种检查权限的方法
    //检查在清单文件中定义的权限是否授权
    @Override
    public IBinder onBind(Intent intent) {
//        int check = checkCallingOrSelfPermission("com.bob.aidltest.aidl.ACCESS_BOOK_SERVICE");
//        Log.i(TAG, "onbind is checking permission:" + check);
//        if (check == PackageManager.PERMISSION_DENIED) {
//            return null;
//        }
        return mBinder;
    }

    @Override
    public void onDestroy() {
        mIsServiceDestroyed.set(true);
        super.onDestroy();
    }

    /**
     * 新增图书并向每个客户端发送通知
     * @param book
     * @throws RemoteException
     */
    private void onNewBookArrived(Book book) throws RemoteException{
        synchronized (this){
            if (!mIsServiceDestroyed.get()) {
                mbookList.add(book);
                Log.i(TAG, "new book has arrived:" + book.toString());
//        Log.i(TAG, "the listeners should be notified is:" + mListenerList.size());
                final int N = mlistenerList.beginBroadcast();

                //开始一个一个通知
                for (int i = 0; i <N; i++) {
                    IOnNewBookArrivedListener listener = mlistenerList.getBroadcastItem(i);
                    if (listener != null) {
                        try{
                            listener.onNewBookArrived(book);
                            Log.i(TAG, "system is now notifying listener:" + listener);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                mlistenerList.finishBroadcast();
            }
        }
    }

    /**
     * 线程用来模拟每5s新生成一本新书
     */
    private class ServiceWorker implements Runnable{
        @Override
        public void run() {
            //如果service还没有被销毁就每隔5s向服务端新增一本书
            while (!mIsServiceDestroyed.get()) {
                try{
                    Thread.sleep(5000);
                }catch(Exception e){
                    e.printStackTrace();
                }
                int bookID = mbookList.size() + 1;
                Book newBook = new Book(bookID, "新书名newbookid#" + bookID);
                try{
                    onNewBookArrived(newBook);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}
