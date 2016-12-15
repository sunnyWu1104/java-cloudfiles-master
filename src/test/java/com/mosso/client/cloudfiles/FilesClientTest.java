package com.mosso.client.cloudfiles;

import junit.framework.Assert;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2016/12/14.
 */
public class FilesClientTest {
	@Test
	public void listContainersInfo() throws Exception {
		List<FilesContainerInfo>  list = filesClient.listContainersInfo();
		for (FilesContainerInfo info : list) {
			System.out.println(info.toString());
		}
	}

	@Test
	public void getObjectMetaData() throws Exception {
		FilesObjectMetaData metaData = filesClient.getObjectMetaData("xuchuntest", "objectWithMeta.ext");
		System.out.println(metaData.toString());

	}
	@Test
	public void storeObjectWithMeta() throws Exception {
		File file = new File("c:\\textMeta.txt");
		if (!file.exists()) file.createNewFile();
		FileUtils.write(file, "ddd");
		Map map = new HashMap<>();
		map.put("maaa", "zidingyi");
		filesClient.storeObjectAs("xuchuntest", file, "ext", "objectWithMeta.ext", map);
		listObjects();
	}

	@Test
	public void deleteObject() throws Exception {
		filesClient.deleteObject("xuchuntest", "destination2.ext");
		listObjects();
	}

	@Test
	public void storeObjectAs() throws Exception {
		File file = new File("c:\\text.txt");
		if (!file.exists()) file.createNewFile();
		FileUtils.write(file, "ddd");
		filesClient.storeObjectAs("xuchuntest", file, "ext","destination2.ext");
		listObjects();
	}

	@Test
	public void storeObject() throws Exception {
		byte[] serverByte = filesClient.getObject("xuchuntest", "destination2.ext");
		Assert.assertTrue(serverByte.length > 0 );
	}

	@Test
	public void deleteContainer() throws Exception {
		filesClient.deleteContainer("xuchuntest2");
		this.listContainers();
	}

	@Test
	public void createContainer() throws Exception {
		filesClient.createContainer("xuchuntest2");
		this.listContainers();
	}

	FilesClient filesClient;

	@Before
	public void login() throws Exception {
		filesClient = new FilesClient("testuser:swift", "tthksqNaJdS6RvNaOh9p5aTRu5qkMDCLITi7iYFM","","http://192.168.6.93/auth/");
		filesClient.login();
	}

	@Test
	public void listContainers() throws Exception {
		List<FilesContainer> list = filesClient.listContainers();
		for (FilesContainer constants : list) {
			System.out.println(constants.getName());
		}
	}
	@Test
	public void listObjects() throws Exception {
		List<FilesObject> list = filesClient.listObjects("xuchuntest");
		for (FilesObject constants : list) {
			System.out.println(constants.getName());
		}
	}
}