/*
 * See COPYING for license information.
 */

package com.mosso.client.cloudfiles;

 import org.apache.log4j.Logger;

public class FilesContainer {
	private String name;
	private FilesClient client = null;
	private static Logger logger = Logger.getLogger(FilesContainer.class);


	/**
	 * @param name   The name of the container
	 * @param client A logged in client
	 */
	public FilesContainer(String name, FilesClient client) {
		this.name = name;
		this.client = client;
	}

	/**
	 * Get the name of the container
	 *
	 * @return The name of this container
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the name of the container
	 *
	 * @param name The new name
	 */
	public void setName(String name) {
		this.name = name;
	}


	/**
	 * Returns the instance of the client we're using
	 *
	 * @return The FilesClient
	 */
	public FilesClient getClient() {
		return this.client;
	}

}
