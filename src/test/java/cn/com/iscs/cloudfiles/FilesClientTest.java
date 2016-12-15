package cn.com.iscs.cloudfiles;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Created by Administrator on 2016/12/14.
 */
public class FilesClientTest {
	FilesClient filesClient;

	@Before
	public void login() throws Exception {
		filesClient = new FilesClient("testuser:swift", "tthksqNaJdS6RvNaOh9p5aTRu5qkMDCLITi7iYFM","","http://192.168.6.93/auth/");
		filesClient.login();
	}

	@Test
	public void listContainersInfo() throws Exception {
		List<FilesContainerInfo> list = filesClient.listContainersInfo();
		for (FilesContainerInfo constants : list) {
			System.out.println(constants.getName());
		}
	}

}