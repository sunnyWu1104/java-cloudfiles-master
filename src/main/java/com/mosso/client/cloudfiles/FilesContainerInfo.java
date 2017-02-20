/*
 * See COPYING for license information.
 */ 

package com.mosso.client.cloudfiles;

/**
 * Contains basic information about the container
 * 
 * @author lvaughn
 *
 */
public class FilesContainerInfo
{
    private long bytes;
    private long count;
    private String name;

	@Override
	public String toString() {
		return "FilesContainerInfo{" +
				"bytes=" + bytes +
				", count=" + count +
				", name='" + name + '\'' +
				'}';
	}

	public long getBytes() {
		return bytes;
	}

	public void setBytes(long bytes) {
		this.bytes = bytes;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
