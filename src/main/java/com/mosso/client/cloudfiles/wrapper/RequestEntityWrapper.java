/**
 *
 */
package com.mosso.client.cloudfiles.wrapper;

import com.mosso.client.cloudfiles.IFilesTransferCallback;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.BasicHttpEntity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author lvaughn
 */
public class RequestEntityWrapper implements HttpEntity {
	private BasicHttpEntity entity;
	private IFilesTransferCallback callback = null;

	public RequestEntityWrapper(BasicHttpEntity entity, IFilesTransferCallback callback) {
		this.entity = entity;
		this.callback = callback;
	}

	@Override
	public Header getContentEncoding() {
		return null;
	}

	@Override
	public InputStream getContent() throws IOException, UnsupportedOperationException {
		return null;
	}

	@Override
	public void writeTo(OutputStream outputStream) throws IOException {
		entity.writeTo(new OutputStreamWrapper(outputStream, callback));
	}

	@Override
	public boolean isStreaming() {
		return false;
	}

	@Override
	public void consumeContent() throws IOException {

	}

	@Override
	public boolean isRepeatable() {
		return entity.isRepeatable();
	}

	@Override
	public boolean isChunked() {
		return false;
	}

	@Override
	public long getContentLength() {
		return 0;
	}

	@Override
	public Header getContentType() {
		return null;
	}
}
