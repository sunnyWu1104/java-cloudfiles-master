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
    private int objectCount;
    private long totalSize;
    private String name;

	public int getObjectCount() {
		return objectCount;
	}

	public void setObjectCount(int objectCount) {
		this.objectCount = objectCount;
	}

	public long getTotalSize() {
		return totalSize;
	}

	public void setTotalSize(long totalSize) {
		this.totalSize = totalSize;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "FilesContainerInfo{" +
				"objectCount=" + objectCount +
				", totalSize=" + totalSize +
				", name='" + name + '\'' +
				'}';
	}
}
