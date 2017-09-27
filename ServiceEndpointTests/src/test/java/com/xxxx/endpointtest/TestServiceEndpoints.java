package com.xxxx.endpointtest;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.collections.map.MultiValueMap;

/*
 * Example use class
 */
public class TestServiceEndpoints extends BaseServiceEndpointTest {

	String soapRequestBody;
	String fullUrl;
	String baselineFileFqn;

	@Test(alwaysRun=true)
	public void getUserByUserId() {
		fullUrl = baseURL + "/services/XXXXService";
		soapRequestBody = "<soapenv:Envelope " +
		                         "   xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
		                         "   xmlns:xxxx=\"http://xxxx.xxxx.com/\">" +
		                         "<soapenv:Header/>" +
		                         "<soapenv:Body>" +
		                         "<xxxx:getUserByUserId>" +
		                         "<userid>xxxxxx</userid>" +
		                         "</xxxx:getUserByUserId>" +
		                         "</soapenv:Body>" +
		                         "</soapenv:Envelope>";
		baselineFileFqn = baselinesPath + "getUserByUserId_baseline.txt";
		int result = doSoapTest(fullUrl, soapRequestBody, baselineFileFqn);
		assert evalForAssert(result);
	}

  @Test(alwaysRun=true)
	public void getUserByUserId() {
		fullUrl = baseURL + "/services/XXXXService";
		soapRequestBody = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xxxx=\"http://xxxx.xxxx.com/\">\n" +
		                         "   <soapenv:Header/>\n" +
		                         "   <soapenv:Body>\n" +
		                         "      <xxxx:getUserByUserId2>\n" +
		                         "         <userId>xxxxxx</userId>\n" +
		                         "      </xxxx:getUserByUserId2>\n" +
		                         "   </soapenv:Body>\n" +
		                         "</soapenv:Envelope>";
		baselineFileFqn = baselinesPath + "getUserByUserId2_baseline.txt";
		int result = doSoapTest(fullUrl, soapRequestBody, baselineFileFqn);
		assert evalForAssert(result);
	}

	/**
	 * Important thing here is to check standard out or the log file to look at the URLs that got sent to the server
	 * specified in the 'url' variable to be sure the logic handling them created correctly-formed URLs to
	 * send to the server.
	 */
	@SuppressWarnings("unchecked")
	@Test(alwaysRun=true)
	public void REST_URLs_Format_Test() {
		ArrayList<String> urls = new ArrayList<>();
		urls.add("http://localhost:9000/#/");
		urls.add("http://localhost:9000/#/?");
		urls.add("http://localhost:9000/#/?&");
		urls.add("http://localhost:9000/#");
		urls.add("http://localhost:9000/#?");
		urls.add("http://localhost:9000/#/?key1000=");
		urls.add("http://localhost:9000/#/?key1000=&");
		urls.add("http://localhost:9000/#/?key1000=8987");
		urls.add("http://localhost:9000/#/?key1000=8987&");

		Map<String, String> urlParams = MultiValueMap.decorate(new HashMap<>());
		urlParams.put("", "val1");
		urlParams.put("", "val2");
		urlParams.put("key3", "val3");
		urlParams.put("key3", "val3");
		urlParams.put("key4", "&");
		urlParams.put("key4", "");
		urlParams.put("key4", "");
		urlParams.put("key4", "");
		urlParams.put("key5", "abc");
		urlParams.put("key5", null);
		urlParams.put("key6", null);
		urlParams.put("", "");
		urlParams.put("", "");
		urlParams.put(null, null);
		urlParams.put(null, null);

		Iterator<String> urlsIter = urls.iterator();
		urlsIter.forEachRemaining( url -> {
			doRestTest(url, urlParams, NOBASELINE, false);
		});
		assert true;
	}

	@SuppressWarnings("unchecked")
	@Test(alwaysRun=true)
	public void XXXX_Test() {
		fullUrl = "http://xxxxxx.xxxxxxx.com/infoService/getData";
		Map<String, String> urlParams = MultiValueMap.decorate(new HashMap<>());
		urlParams.put("key1", "val1");
		urlParams.put("key2", "val2");
		baselineFileFqn = baselinesPath + "XXXX_Test.txt";
		int result = doRestTest(fullUrl, urlParams, baselineFileFqn);
		assert evalForAssert(result);
	}

}

