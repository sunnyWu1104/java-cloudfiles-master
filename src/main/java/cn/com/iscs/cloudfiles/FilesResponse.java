/*
 * See COPYING for license information.
 */

package cn.com.iscs.cloudfiles;

import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import java.io.IOException;


public class FilesResponse {
	private Request method = null;
	private Response response = null;

	private static Logger logger = Logger.getLogger(com.mosso.client.cloudfiles.FilesResponse.class);

	/**
	 * @param response The HttpMethod that generated this response
	 */
	public FilesResponse(Request method, Response response) {
		this.method = method;
		this.response = response;

		if (logger.isDebugEnabled()) {
			logger.debug("Request Method: " + method.method());
			logger.debug("Request Path: " + method.url());
			logger.debug("response : " + response.message());
		}
	}

	/**
	 * Checks to see if the user managed to login with their credentials.
	 *
	 * @return true is login succeeded false otherwise
	 */
	public boolean loginSuccess() {
		if (getStatusCode() == HttpStatus.SC_UNAUTHORIZED)
			return false;

		if (getStatusCode() == HttpStatus.SC_NO_CONTENT)
			return true;

		return false;
	}



	/**
	 * This method makes no assumptions about the user having been logged in.  It simply looks for the Storage Token header
	 * as defined by FilesConstants.X_STORAGE_TOKEN and if this exists it returns its value otherwise the value returned will be null.
	 *
	 * @return null if the user is not logged into Cloud FS or the Storage token
	 */
	public String getAuthToken() {
		return response.header(FilesConstants.X_AUTH_TOKEN);
	}

	/**
	 * This method makes no assumptions about the user having been logged in.  It simply looks for the Storage URL header
	 * as defined by FilesConstants.X_STORAGE_URL and if this exists it returns its value otherwise the value returned will be null.
	 *
	 * @return null if the user is not logged into Cloud FS or the Storage URL
	 */
	public String getStorageURL() {
		return response.header(FilesConstants.X_STORAGE_URL);
	}


	/**
	 * Get the content type
	 *
	 * @return The content type (e.g., MIME type) of the response
	 */
	public String getContentType() {
		return response.header("Content-Type");
	}

	/**
	 * Get the content length of the response (as reported in the header)
	 *
	 * @return the length of the content
	 */
	public String getContentLength() {
		return response.header("Content-Length");
	}

	/**
	 * The Etag is the same as the objects MD5SUM
	 *
	 * @return The ETAG
	 */
	public String getETag() {
		String hdr = response.header(FilesConstants.E_TAG);
		if (hdr == null) return null;
		return hdr;
	}

	/**
	 * The last modified header
	 *
	 * @return The last modified header
	 */
	public String getLastModified() {
		return response.header("Last-Modified");
	}

	/**
	 * The HTTP Status line (both the status code and the status message).
	 *
	 * @return The status line
	 */

	/**
	 * Get the HTTP status code
	 *
	 * @return The status code
	 */
	public int getStatusCode() {
		return response.code();
	}

	/**
	 * Get the HTTP status message
	 *
	 * @return The message portion of the status line
	 */
	public String getStatusMessage() {
		return response.message();
	}

	/**
	 * The HTTP Method (put, get, etc) of the request that generated this response
	 *
	 * @return The method name
	 */
	public String getMethodName() {
		return method.method();
	}

	/**
	 * Returns the response body as text
	 *
	 * @return The response body
	 * @throws IOException
	 */
	public String getResponseBodyAsString() throws IOException {
		return response.body().string();
	}

	/**
	 * Get the number of objects in the header
	 *
	 * @return -1 if the header is not present or the correct value as defined by the header
	 */
	public int getContainerObjectCount() {
		String contCountHeader = response.header(FilesConstants.X_CONTAINER_OBJECT_COUNT);
		if (contCountHeader != null)
			return Integer.parseInt(contCountHeader);
		return -1;
	}

	/**
	 * Get the number of bytes used by the container
	 *
	 * @return -1 if the header is not present or the correct value as defined by the header
	 */
	public long getContainerBytesUsed() {
		String contBytesUsedHeader = response.header(FilesConstants.X_CONTAINER_BYTES_USED);
		if (contBytesUsedHeader != null)
			return Long.parseLong(contBytesUsedHeader);
		return -1;
	}

	/**
	 * Get the number of objects in the header
	 *
	 * @return -1 if the header is not present or the correct value as defined by the header
	 */
	public int getAccountContainerCount() {
		String contCountHeader = response.header(FilesConstants.X_ACCOUNT_CONTAINER_COUNT);
		if (contCountHeader != null)
			return Integer.parseInt(contCountHeader);
		return -1;
	}

	/**
	 * Get the number of bytes used by the container
	 *
	 * @return -1 if the header is not present or the correct value as defined by the header
	 */
	public long getAccountBytesUsed() {
		String accountBytesUsedHeader = response.header(FilesConstants.X_ACCOUNT_BYTES_USED);
		if (accountBytesUsedHeader != null)
			return Long.parseLong(accountBytesUsedHeader);
		return -1;
	}

	/**
	 * Get the URL For a shared container
	 *
	 * @return null if the header is not present or the correct value as defined by the header
	 */
	public String getCdnUrl() {
		String cdnHeader = response.header(FilesConstants.X_CDN_URI);
		if (cdnHeader != null)
			return cdnHeader;
		return null;
	}
}
