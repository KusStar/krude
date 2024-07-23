package com.kuss.krude.shizuku.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.File;

public class BeanFile implements Parcelable {
    public BeanFile(File file) {
        this.name = file.getName();
        this.path = file.getPath();
        this.absolutePath = file.getAbsolutePath();
        this.parent = file.getParent();
        this.isFile = file.isFile();
        this.isHidden = file.isHidden();
        this.length = file.length();
        this.lastModified = file.lastModified();
    }

    public BeanFile(String path) {
        File file = new File(path);
        this.name = file.getName();
        this.path = file.getPath();
        this.absolutePath = file.getAbsolutePath();
        this.parent = file.getParent();
        this.isFile = file.isFile();
        this.isHidden = file.isHidden();
        this.length = file.length();
        this.lastModified = file.lastModified();
    }

    public String name;
    public String path;
    public String absolutePath;
    public String parent;
    public boolean isFile;
    public boolean isHidden;
    public long length;
    public long lastModified;

    protected BeanFile(Parcel in) {
        name = in.readString();
        path = in.readString();
        absolutePath = in.readString();
        parent = in.readString();
        isFile = in.readByte() != 0;
        isHidden = in.readByte() != 0;
        length = in.readLong();
        lastModified = in.readLong();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(path);
        dest.writeString(absolutePath);
        dest.writeString(parent);
        dest.writeByte((byte) (isFile ? 1 : 0));
        dest.writeByte((byte) (isHidden ? 1 : 0));
        dest.writeLong(length);
        dest.writeLong(lastModified);
    }

    public static final Creator<BeanFile> CREATOR = new Creator<>() {
        @Override
        public BeanFile createFromParcel(Parcel in) {
            return new BeanFile(in);
        }

        @Override
        public BeanFile[] newArray(int size) {
            return new BeanFile[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
