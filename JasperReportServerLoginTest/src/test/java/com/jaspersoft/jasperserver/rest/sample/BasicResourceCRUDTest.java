package com.jaspersoft.jasperserver.rest.sample;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;


public class BasicResourceCRUDTest{
	
	static RestAPIUtils restUtils = new RestAPIUtils();
	protected HttpRequestBase httpReq;
	protected HttpResponse httpRes;
	private final Log log = LogFactory.getLog(getClass());
	
	@BeforeClass 
	public static void setUp() throws Exception {
		restUtils.loginToServer();
	}
	
	@Test 
	public void Resource_IMG_PUT_201() throws Exception{
		log.info("test");
		assert(true);
	}
	

}
