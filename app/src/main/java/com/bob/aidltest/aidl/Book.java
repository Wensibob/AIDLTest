package com.bob.aidltest.aidl;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by bob on 17-6-13.
 */

public class Book implements Parcelable{

    public int bookID;
    public String bookName;

    public Book() {
    }

    public Book(int bookID, String bookName) {
        this.bookID = bookID;
        this.bookName = bookName;
    }

    protected Book(Parcel in) {
        bookID = in.readInt();
        bookName = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(bookID);
        dest.writeString(bookName);
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    @Override
    public String toString() {
        return String.format("[BookID: %s , BookName: %s]", bookID, bookName);
    }
}
