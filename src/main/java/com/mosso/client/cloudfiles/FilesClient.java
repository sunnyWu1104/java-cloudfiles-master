package com.mosso.client.cloudfiles;

import com.alibaba.fastjson.JSON;
import okhttp3.*;
import org.apache.commons.lang.text.StrTokenizer;
import org.apache.log4j.Logger;

import java.io.*;
import java.math.BigInteger;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FilesClient {
	public static final String VERSION = "v1";
	public static int connectionTimeOut = 15000;

	private String username = null;
	private String password = null;
	private String account = "";
	private String authenticationURL;
	private String storageURL = null;
	private String authToken = null;
	private boolean isLoggedin = false;

	OkHttpClient client = null;


	private static Logger logger = Logger.getLogger(FilesClient.class);

	/**
	 * @param username          The username to log in to
	 * @param password          The password
	 * @param account           The Cloud Files account to use
	 * @param connectionTimeOut The connection timeout, in ms.
	 */
	public FilesClient(String username, String password, String account, String url, int connectionTimeOut) {
		this.username = username;
		this.password = password;
		this.account = account;
		this.authenticationURL = url;
		this.connectionTimeOut = connectionTimeOut;

		client = new OkHttpClient();

		if (logger.isDebugEnabled()) {
			logger.debug("UserName: " + this.username);
			logger.debug("AuthenticationURL: " + this.authenticationURL);
			logger.debug("ConnectionTimeOut: " + this.connectionTimeOut);
		}
	}

	/**
	 * This method uses the default connection time out of CONNECTON_TIMEOUT.  If <code>account</code>
	 * is null, "Mosso Style" authentication is assumed, otherwise standard Cloud Files authentication is used.
	 *
	 * @param username
	 * @param password
	 * @param account
	 */
	public FilesClient(String username, String password, String account, String url) {
		this(username, password, account, url, connectionTimeOut);
	}



	/**
	 * This method uses the default connection time out of CONNECTON_TIMEOUT and username, password,
	 * and account from FilesUtil
	 */
	public FilesClient() {
	}

	/**
	 * Returns the Account associated with the URL
	 *
	 * @return The account name
	 */
	public String getAccount() {
		return account;
	}

	/**
	 * Set the Account value and reassemble the Authentication URL.
	 *
	 * @param account
	 */
	public void setAccount(String account) {
		this.account = account;
	}

	/**
	 * Log in to CloudFiles.  This method performs the authentication and sets up the client's internal state.
	 *
	 * @return true if the login was successful, false otherwise.
	 * @throws IOException There was an IO error doing network communication
	 */
	public boolean login() throws IOException {
		if (client == null) {
			client = new OkHttpClient();
		}
		Request request = new Request.Builder()
				.url(authenticationURL)
				.addHeader(FilesConstants.X_STORAGE_USER, username)
				.addHeader(FilesConstants.X_STORAGE_PASS, password)
				.build();

		logger.debug("Logging in user: " + username + " using URL: " + authenticationURL);

		Response response = client.newCall(request).execute();
		if (response.isSuccessful()) {
			isLoggedin = true;
			storageURL = response.header(FilesConstants.X_STORAGE_URL);
			authToken = response.header(FilesConstants.X_AUTH_TOKEN);
			logger.debug("storageURL: " + storageURL);
			logger.debug("authToken: " + authToken);
		}
		return this.isLoggedin;
	}

	public List<FilesContainer> listContainers() throws Exception {
		return listContainers(-1, null);
	}

	public List<FilesContainer> listContainers(int limit) throws Exception {
		return listContainers(limit, null);
	}

	private Response doHttp(Request request) throws Exception {
		if (!isLoggedin) {
			this.login();
		}
		request = request.newBuilder().addHeader(FilesConstants.X_AUTH_TOKEN, authToken).build();
		Response response = client.newCall(request).execute();
		if (response.isSuccessful()) {
			return response;
		} else if (response.code() == 401) {
			this.login();
			return client.newCall(request).execute();
		} else {
			throw new Exception(response.message());
		}
	}

	public ArrayList<FilesContainer> listContainers(int limit, String marker) throws Exception {
		try {
			HttpUrl.Builder urlBuilder = HttpUrl.parse(storageURL).newBuilder();
			if (limit > 0) {
				urlBuilder.addQueryParameter("limit", String.valueOf(limit));
			}
			if (marker != null) {
				urlBuilder.addQueryParameter("marker", marker);
			}
			Request request = new Request.Builder().get().url(urlBuilder.build()).build();
			Response response = this.doHttp(request);

			StrTokenizer tokenize = new StrTokenizer(response.body().string());
			tokenize.setDelimiterString("\n");
			String[] containers = tokenize.getTokenArray();
			ArrayList<FilesContainer> containerList = new ArrayList<FilesContainer>();
			for (String container : containers) {
				containerList.add(new FilesContainer(container, this));
			}
			return containerList;
		} catch (Exception e) {
			logger.error("Unexpected container-info tag:", e);
		}
		return null;
	}


	public List<FilesContainerInfo> listContainersInfo() throws Exception {
		return listContainersInfo(-1, null);
	}

	public List<FilesContainerInfo> listContainersInfo(int limit) throws Exception {
		return listContainersInfo(limit, null);
	}

	public List<FilesContainerInfo> listContainersInfo(int limit, String marker) throws Exception {
		HttpUrl.Builder urlBuilder = HttpUrl.parse(storageURL).newBuilder();
		if (limit > 0) {
			urlBuilder.addQueryParameter("limit", String.valueOf(limit));
		}
		if (marker != null) {
			urlBuilder.addQueryParameter("marker", marker);
		}
		urlBuilder.addQueryParameter("format", "json");
		Request request = new Request.Builder().get().url(urlBuilder.build()).build();
		Response response = this.doHttp(request);
		List<FilesContainerInfo> containerList = JSON.parseArray(response.body().string(), FilesContainerInfo.class);

		return containerList;
	}

	public List<FilesObject> listObjects(String container) throws Exception {
		return listObjectsStaringWith(container, null, null, -1, null);
	}

	private void validContianerName(String name) throws Exception {
		if (name == null) throw new Exception("name is null");
		int length = name.length();
		if (length == 0 || length > FilesConstants.CONTAINER_NAME_LENGTH)
			throw new Exception("name too big,name too short");
		if (name.indexOf('/') != -1)
			throw new Exception("name contains /");
	}

	private void validObjectName(String name) throws Exception {
		if (name == null) throw new Exception("name is null");
		int length = name.length();
		if (length == 0 || length > FilesConstants.OBJECT_NAME_LENGTH)
			throw new Exception("name too big,name too short");
		//if (name.indexOf('?') != -1) return false;
	}

	public List<FilesObject> listObjectsStaringWith(String container, String startsWith, String path, int limit, String marker) throws Exception {
		validContianerName(container);
		HttpUrl.Builder urlBuilder = HttpUrl.parse(storageURL).newBuilder().addPathSegment(container);
		urlBuilder.addQueryParameter("format", "json");
		if (startsWith != null) {
			urlBuilder.addQueryParameter(FilesConstants.LIST_CONTAINER_NAME_QUERY, startsWith);
		}
		if (path != null) {
			urlBuilder.addQueryParameter("path", path);
		}
		if (limit > 0) {
			urlBuilder.addQueryParameter("limit", String.valueOf(limit));
		}
		if (marker != null) {
			urlBuilder.addQueryParameter("marker", marker);
		}
		Request request = new Request.Builder().get().url(urlBuilder.build()).build();
		Response response = this.doHttp(request);
		List<FilesObject> objectList = JSON.parseArray(response.body().string(), FilesObject.class);
		return objectList;
	}

	public List<FilesObject> listObjects(String container, int limit) throws Exception {
		return listObjectsStaringWith(container, null, null, limit, null);
	}

	public List<FilesObject> listObjects(String container, String path) throws Exception {
		return listObjectsStaringWith(container, null, path, -1, null);
	}

	public List<FilesObject> listObjects(String container, String path, int limit) throws Exception {
		return listObjectsStaringWith(container, null, path, limit, null);
	}


	public List<FilesObject> listObjects(String container, String path, int limit, String marker) throws Exception {
		return listObjectsStaringWith(container, null, path, limit, marker);
	}

	public List<FilesObject> listObjects(String container, int limit, String marker) throws Exception {
		return listObjectsStaringWith(container, null, null, limit, marker);
	}

	public static String md5Sum(File f) throws IOException {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
			InputStream is = new FileInputStream(f);
			byte[] buffer = new byte[1024];
			int read = 0;

			while ((read = is.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}

			is.close();

			byte[] md5sum = digest.digest();
			BigInteger bigInt = new BigInteger(1, md5sum);

			// Front load any zeros cut off by BigInteger
			String md5 = bigInt.toString(16);
			while (md5.length() != 32) {
				md5 = "0" + md5;
			}
			return md5;
		} catch (NoSuchAlgorithmException e) {
			logger.fatal("The JRE is misconfigured on this computer", e);
			return null;
		}
	}

	public void createContainer(String container) throws Exception {
		validContianerName(container);
		HttpUrl.Builder urlBuilder = HttpUrl.parse(storageURL).newBuilder().addPathSegment(container);
		Request request = new Request.Builder().url(urlBuilder.build()).put(RequestBody.create(MediaType.parse("text/plan"), container)).build();
		Response response = this.doHttp(request);
		if (!response.isSuccessful()) {
			throw new Exception(response.message());
		}
	}

	public boolean deleteContainer(String container) throws Exception {
		validContianerName(container);

		HttpUrl.Builder urlBuilder = HttpUrl.parse(storageURL).newBuilder().addPathSegment(container);
		Request request = new Request.Builder().url(urlBuilder.build()).delete(RequestBody.create(MediaType.parse("text/plan"), container)).build();
		Response response = this.doHttp(request);
		if (!response.isSuccessful()) {
			throw new Exception(response.message());
		}
		return true;
	}

	public byte[] getObject(String container, String objName) throws Exception {
		validContianerName(container);
		validObjectName(objName);
		HttpUrl.Builder urlBuilder = HttpUrl.parse(storageURL).newBuilder().addPathSegment(container).addPathSegment(objName);
		Request request = new Request.Builder().url(urlBuilder.build()).get().build();
		Response response = this.doHttp(request);
		if (!response.isSuccessful()) {
			throw new Exception(response.message());
		}
		return response.body().bytes();
	}

	public InputStream getObjectAsStream(String container, String objName) throws Exception {
		validContianerName(container);
		validObjectName(objName);
		HttpUrl.Builder urlBuilder = HttpUrl.parse(storageURL).newBuilder().addPathSegment(container).addPathSegment(objName);
		Request request = new Request.Builder().url(urlBuilder.build()).get().build();
		Response response = this.doHttp(request);
		if (!response.isSuccessful()) {
			throw new Exception(response.message());
		}
		return response.body().byteStream();
	}


	public void deleteObject(String container, String objName) throws Exception {
		validContianerName(container);
		validObjectName(objName);
		HttpUrl.Builder urlBuilder = HttpUrl.parse(storageURL).newBuilder().addPathSegment(container).addPathSegment(objName);
		Request request = new Request.Builder().url(urlBuilder.build()).delete().build();
		Response response = this.doHttp(request);
		if (!response.isSuccessful()) {
			throw new Exception(response.message());
		}
	}

	public boolean storeObjectAs(String container, File obj, String contentType, String name) throws Exception {
		return storeObjectAs(container, obj, contentType, name, new HashMap<String, String>(), null);
	}

	public boolean storeObjectAs(String container, File obj, String contentType, String name, IFilesTransferCallback callback) throws Exception {
		return storeObjectAs(container, obj, contentType, name, new HashMap<String, String>(), callback);
	}

	public boolean storeObjectAs(String container, File obj, String contentType, String name, Map<String, String> metadata) throws Exception {
		return storeObjectAs(container, obj, contentType, name, metadata, null);
	}

	public boolean storeObjectAs(String container, File obj, String contentType, String name, Map<String, String> metadata, IFilesTransferCallback callback) throws Exception {
		validContianerName(container);
		validObjectName(name);

		if (!obj.exists()) {
			throw new FileNotFoundException(name + " does not exist");
		}

		if (obj.isDirectory()) {
			throw new IOException("The alleged file was a directory");
		}

		Map<String, String> map = new HashMap<>();
		for (String key : metadata.keySet()) {
			map.put(FilesConstants.X_OBJECT_META + key, metadata.get(key));
		}

		HttpUrl.Builder urlBuilder = HttpUrl.parse(storageURL).newBuilder().addPathSegment(container).addPathSegment(name);
		Request request = new Request.Builder()
				.url(urlBuilder.build())
				.put(RequestBody.create(MediaType.parse(contentType), obj))
				.headers(Headers.of(map))
				.build();

		Response response = this.doHttp(request);
		if (!response.isSuccessful()) {
			throw new Exception(response.message());
		}
		return true;
	}

	public FilesObjectMetaData getObjectMetaData(String container, String objName) throws Exception {
		FilesObjectMetaData metaData = null;
		validContianerName(container);
		validObjectName(objName);

		HttpUrl.Builder urlBuilder = HttpUrl.parse(storageURL).newBuilder().addPathSegment(container).addPathSegment(objName);
		Request request = new Request.Builder()
				.url(urlBuilder.build())
				.get()
				.build();
		Response response = this.doHttp(request);
		if (response.isSuccessful()) {

			String mimeType = response.body().contentType().type();
			String lastModified = response.header("Last-Modified");
			String eTag = response.header(FilesConstants.E_TAG);
			String contentLength = response.header("Content-Length");

			metaData = new FilesObjectMetaData(mimeType, contentLength, eTag, lastModified);

			Map<String, String> map = new HashMap<>();
			Headers headers = response.headers();
			for (String str : headers.names()) {
				map.put(str, headers.get(str));
			}
			metaData.setMetaData(map);
		}
		return metaData;
	}

	private String guessMimeType(String path) {
		FileNameMap fileNameMap = URLConnection.getFileNameMap();
		String contentTypeFor = fileNameMap.getContentTypeFor(path);
		if (contentTypeFor == null) {
			contentTypeFor = "application/octet-stream";
		}
		return contentTypeFor;
	}

	public boolean storeObject(String container, File obj) throws Exception {
		return storeObjectAs(container, obj, guessMimeType(obj.getName()), obj.getName());
	}

	public boolean storeObject(String container, File obj, String contentType) throws Exception {
		return storeObjectAs(container, obj, contentType, obj.getName());
	}

	public boolean storeObject(String container, byte obj[], String contentType, String name) throws Exception {
		return storeObject(container, obj, contentType, name, new HashMap<String, String>());
	}

	public boolean storeObject(String container, byte obj[], String contentType, String name, Map<String, String> metadata) throws Exception {
		return storeObject(container, obj, contentType, name, metadata, null);
	}

	public boolean storeObject(String container, byte obj[], String contentType, String name, Map<String, String> metadata, IFilesTransferCallback callback) throws Exception {
		validContianerName(container);
		validObjectName(name);

		Map<String, String> map = new HashMap<>();
		for (String key : metadata.keySet()) {
			map.put(FilesConstants.X_OBJECT_META + key, metadata.get(key));
		}

		HttpUrl.Builder urlBuilder = HttpUrl.parse(storageURL).newBuilder().addPathSegment(container).addPathSegment(name);
		Request request = new Request.Builder()
				.url(urlBuilder.build())
				.put(RequestBody.create(MediaType.parse(contentType), obj))
				.headers(Headers.of(map))
				.build();

		Response response = this.doHttp(request);
		if (!response.isSuccessful()) {
			throw new Exception(response.message());
		}
		return true;
	}

	public boolean storeObject(String container, InputStream data, String contentType, String name, Map<String, String> metadata) throws Exception {
		return this.storeObject(container, org.apache.commons.io.IOUtils.toByteArray(data), contentType, name, metadata);
	}

	public boolean storeStreamedObject(String container, InputStream data, String contentType, String name, Map<String, String> metadata) throws Exception {
		return this.storeObject(container, org.apache.commons.io.IOUtils.toByteArray(data), contentType, name, metadata);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getAuthenticationURL() {
		return authenticationURL;
	}

	public void setAuthenticationURL(String authenticationURL) {
		this.authenticationURL = authenticationURL;
	}
}
