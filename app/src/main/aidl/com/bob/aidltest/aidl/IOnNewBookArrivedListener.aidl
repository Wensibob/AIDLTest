package com.bob.aidltest.aidl;

import com.bob.aidltest.aidl.Book;

interface IOnNewBookArrivedListener {
    void onNewBookArrived(in Book book);
}
