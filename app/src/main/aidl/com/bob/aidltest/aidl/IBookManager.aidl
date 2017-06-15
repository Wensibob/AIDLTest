package com.bob.aidltest.aidl;

import com.bob.aidltest.aidl.Book;
import com.bob.aidltest.aidl.IOnNewBookArrivedListener;

interface IBookManager {
    List<Book> getBookList();
    void addBook(in Book book);
    void registerListener(IOnNewBookArrivedListener listener);
    void unregisterListener(IOnNewBookArrivedListener listener);
}
