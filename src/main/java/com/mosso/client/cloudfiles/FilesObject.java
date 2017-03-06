/*
 * See COPYING for license information.
 */ 

package com.mosso.client.cloudfiles;

public class FilesObject
{
    private String container;
    private String name;
    private String md5sum = null;
    private long size = -1;
    private String mimeType = null;
    private String contentType = null;
    private String lastModified = null;
    private String hash;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getContainer() {
        return container;
    }

    public void setContainer(String container) {
        this.container = container;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMd5sum() {
        return md5sum;
    }

    public void setMd5sum(String md5sum) {
        this.md5sum = md5sum;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public String toString() {
        return "FilesObject{" +
                "container='" + container + '\'' +
                ", name='" + name + '\'' +
                ", md5sum='" + md5sum + '\'' +
                ", size=" + size +
                ", mimeType='" + mimeType + '\'' +
                ", contentType='" + contentType + '\'' +
                ", lastModified='" + lastModified + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
