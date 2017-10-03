package com.xxxx.endpointtest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.http.util.EntityUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import org.apache.log4j.PropertyConfigurator;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.builder.Input;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

import org.w3c.dom.Document;


/**
 *
 * Developed using JDK 1.8.  This class uses 1.8 syntax features.
 *
 * Class meant to be useful only when extended, hence it is abstract.  This class is extended and
 * with a few lines of code, a servicepoint is testable.  See current examples of classes that extend
 * this class to see how to use it.  It is designed to be very easy to utilize such that the programmer
 * can make a test without writing any functional code.   I.e., the programmer's edits are changes to
 * parameter values only, which are then passed into a single method (doXXXXTest(), eg: doSoapTest())
 * defined herein that does the actual test work.  Thus no functional programming code need
 * be written by the programmer to create a new service endpoint test unless a very specific kind of
 * test is needed.
 *
 * IntelliJ config note: If you get class not found errors at runtime while running in IntelliJ, add
 * the following paths under Dependencies: F4 (Open Module Settings)-under Project Settings-select Modules-in Dependencies tab:
 *
 * <projectpath>\target\classes
 * <projectpath>\target\test-classes
 *
 * Specifying Overrides:
 *
 * A properties file, baseserviceendpointtest.properties, located at /src/main/resources can be created and edited
 * so that the baseline files path is specifiable vs. using the default, which is defined above.  The base URL for
 * the service endpoints (http[s]://host:port/) can also be specified, also defined above.
 *
 * In baseserviceendpointtest.properties, to specify overrides, use as follows:
 *
 * --- Top of File ---
 * baselinesPath=<relative or absolute path to the baseline files>
 * baseURL=<base URL to the service endpoints you are testing>
 * --- End of File ---
 *
 * Relative paths are OK.  Bear in mind they are relative to the project root, not the location of the properties file.
 *
 * Example:
 *
 * --- Top of File ---
 * baselinesPath=C:/Users/ggliddy/watergate/baselineFiles
 * baseURL=http://services.watergateplumbers.com:786/
 * --- End of File ---
 *
 * ==============================
 *
 * Running from ServiceEndpointTestsRunner, you can specify the location and name of this file as an override of the default
 * name and location of the file.  Likewise for the log4j props file and the name and location of the test suite file(s)
 *
 * Logging:
 *
 * Configure the log name and log location, format, etc., with /src/main/resources/log4j.properties.  Extending classes
 * use the 'log' object they inherit from this class.  Again, note that this path and file name can be overridden
 * when running tests using ServiceEndpointTestsRunner to run tests.
 *
 */
public abstract class BaseServiceEndpointTest {

	// Used in dev for debug if desired. //////////
	private static final boolean writeToOutWithLog = false; // Keep as false in source control and in all envs except developer's local installation.
	private static final boolean writeToLogWithLog = true; // Keep as true in source control and in all envs except developer's local installation.
	// ////////////////////////////////////////////

	protected static Logger       log           = Logger.getLogger(BaseServiceEndpointTest.class);
	protected static String       baselinesPath = "baseline/";

	protected static       String baseURL       = "http://localhost:15080/";

	protected static final String NOBASELINE    = "NOBASELINE";

	protected static final int    ERROR         = 0;
	protected static final int    FAIL          = 1;
	protected static final int    SUCCESS       = 2;

	private static final String tab             = "\t";
	private static final String newline         = "\n";
	private static final String newline2        = newline + newline;
	private static final String headerFmt       = dashes(15) + " %20s " + dashes(15) + newline;
	private static final String testHeader      = dashes(25) + " Running Test " + dashes(25);
	private static final String testFooter      = dashes(testHeader.length());

	// Keeping these two variables in case we implement with readable files instead of embedded SOAP request bodies:
	/* protected static String testFilesNameStartText = "test"; // Text the test file names start with
	   protected static String testFilesExt           = ".txt"; // File ext. of the test files names
	*/

	private static final String BASELINES_PATH_KEY         = "baselinesPath";
	private static final String BASE_URL_KEY               = "baseURL";
	private static final String CONTENT_TYPE_KEY           = "Content-type";
	private static final String UTF8_TEXT                  = "UTF-8";
	private static final String CONTENT_TYPE_SOAP_HEADER   = "application/soap+xml; charset=" + UTF8_TEXT;
	private static final String CONTENT_TYPE_TEXT_HEADER   = "text; charset=" + UTF8_TEXT;

	private boolean addBreaksAtGreaterThans = true; // ATM, always true.  Not yet a user option but may be one day.

	private final static String propsFileName = "main/resources/baseserviceendpointtest.properties"; // Can specify a different file name on a per-run basis via ServiceEndpointTestDefaultsSetter in ServiceEndpointTestsRunner

	/* When running from ServiceEndpointTestsRunner, ServiceEndpointTestDefaultsSetter will have had its
	   relevant values set by the time the class gets loaded which won't happen
	   until TestNG_instance.run() is called in ServiceEndpointTestsRunner.  If not, the getXXX()s below
	   will return nulls, which are handled by configureLogger() and assignValuesFromPropertiesFile gracefully. */
	static {
		ServiceEndpointTestDefaultsSetter setds = ServiceEndpointTestDefaultsSetter.getInstance();
		configureLogger(setds.getLog4jPropsFileName());
		assignValuesFromPropertiesFile(setds.getPropsFileName());
	}


	public static void main(String args[]) {
		print("Please run this class using ServiceEndpointTestsRunner or in IntelliJ using a TestNG profile.");
		print("Exiting.");
	}

	/**
	 * Executes SOAP call-requiring test using passed-in values as parameters.  The returned value
	 * indicates the result of the test: ERROR, FAIL, or SUCCESS.  See fields declared in this class
	 * for the values. Note checkXML is passed to reportResults().  See notations for that method for
	 * its purpose.  A null or empty 'url' value throws an Exception.
	 *
	 * @param url
	 * @param soapRequestBody
	 * @param baselineFileFqn
	 * @param checkXML
	 * @return int
	 */
	protected int doSoapTest(String url, String soapRequestBody, String baselineFileFqn, boolean checkXML) {
		try {
		  if(url == null || url.isEmpty()) {
			  throw new Exception("doSoapTest(): 'url' is null or empty.");
		  }
		  doTestOutputHeader(url);
			String response = httpSOAPPost(url, (soapRequestBody==null ? "" : soapRequestBody) );
			String baseline = readFile(baselineFileFqn);
			boolean result = reportResults(baseline, response, checkXML);
			logInfo(testFooter);
			return (result ? SUCCESS : FAIL);
		} catch (Exception e) {
			logError("doSoapTest(): Exception thrown.  Cannot complete test.  Check log for error information.", e);
			return ERROR;
		}
	}

	/**
	 * Convenience call to doSoapTest(String url, String soapRequestBody, String baselineFileFqn, boolean checkXML).
	 * 'checkXML' is passed in as true, unlike when doing REST tests where the default for checkXML is false.
	 *
	 * @param url
	 * @param soapRequestBody
	 * @param baselineFileFqn
	 * @return
	 */
	protected int doSoapTest(String url, String soapRequestBody, String baselineFileFqn) {
		return doSoapTest(url, soapRequestBody, baselineFileFqn, true);
	}


	/**
	 * Executes REST call-requiring test using passed-in values as parameters.  The returned value
	 * indicates the result of the test: ERROR, FAIL, or SUCCESS.  See fields declared in this class
	 * for the values. Note checkXML is passed to reportResults().  See notations for that method for
	 * its purpose.  A null or empty 'url' value throws an Exception.
	 *
	 * @param url
	 * @param params
	 * @param baselineFileFqn
	 * @param checkXML
	 * @return int
	 */
	protected int doRestTest(String url, Map params, String baselineFileFqn, boolean checkXML) {
		try {
			if(url == null || url.isEmpty()) {
				throw new Exception("doRestTest(): 'url' is null or empty.");
			}
			if(params == null) {
				params = new HashMap();
			}
		  doTestOutputHeader(url);
			String response = httpRESTGet(url, params);
			String baseline = readFile(baselineFileFqn);
			boolean result = reportResults(baseline, response, checkXML);
			logInfo(testFooter);
			return (result ? SUCCESS : FAIL);
		} catch (Exception e) {
			logError("doRestTest(): Exception thrown.  Cannot complete test.  Check log for error information.", e);
			return ERROR;
		}
	}


	/**
	 * Convenience call to doRestTest(String url, Map params, String baselineFileFqn, boolean checkXML).
	 * 'checkXML' is passed in as false, unlike when doing SOAP tests where the default for checkXML is true.
	 *
	 * @param url
	 * @param params
	 * @param baselineFileFqn
	 * @return int
	 */
	protected int doRestTest(String url, Map params, String baselineFileFqn) {
     return doRestTest(url, params, baselineFileFqn, false);
	}


	/**
	 * For the tests, result value must be SUCCESS.  This is a method b/c if one day another
	 * state is added that implies SUCCESS but doesn't fit in with the current definition,
	 * or if we add a new state the test can come back with that needs evaluating further to
	 * determine success, we can change the logic for all the tests here vs. in every
	 * test method.
	 *
	 * @param result
	 * @return boolean
	 */
	protected boolean evalForAssert(int result) {
		return (result == SUCCESS);
	}


	/**
	 * Returns the name of the method that called this method.  The method in the call
	 * hierarchy is returned based on its location in the call hierarchy, which is given
	 * with input parameter 'stackframe'.  Useful for informational messages, etc.
	 *
	 * @param stackframe
	 * @return String
	 */
	protected String getCallingMethodName(int stackframe) {
		return Thread.currentThread().getStackTrace()[stackframe].getMethodName();
	}


	/**
	 * Convenience call to System.out.println().  Pass in a null String parameter, it prints an empty string.
	 *
	 * @param s
	 */
	protected static void print(String s) {
		System.out.println((s == null ? "" : s));
	}


	// /////////////////////// private methods start here ///////////////////////////////////

	/**
	 * Convenience call to getCallingMethodName(int stackframe).  Passes a value of 5 to the
	 * method.  Intent is to return the name of the method that called doXXXXTest(), hence it
	 * is private.
	 *
	 * @return String
	 */
	private String getCallingMethodName() {
		return getCallingMethodName(5);
	}


	/**
	 * Reports any diffs between the content of 'baseline' and 'response'.  If an error
	 * occurs, it throws the exception upward for the calling method to determine how to
	 * handle the problem.  Otherwise it returns true when the two strings are equal
	 * (via XMLUnit.DetailedDiff()) or false when not. 'checkXML' indicates to try to
	 * convert the input strings into standard org.w3c.Document objects and examine them
	 * for differences as such.  If either input string cannot be converted to a Document
	 * object, an exception is thrown.  Otherwise, Google Code diff utilities are used, as
	 * these do not check for XML compliance first.  If either 'baseline' or 'response' is
	 * either null or empty, an Exception is thrown.
	 *
	 * If 'baseline' ends with the value of NOBASELINE, no baseline comparison is done and the
	 * method returns true.
	 *
	 * @param baseline
	 * @param response
	 * @param checkXML
	 * @return boolean
	 */
	private boolean reportResults(String baseline, String response, boolean checkXML) throws Exception {
		if (baseline == null || baseline.isEmpty()) {
			Exception e = new Exception("reportResults(): 'baseline' was null or empty.  Cannot run test.");
			logError("reportResults(): 'baseline' value is null.", e);
			throw e;
		}
		if(baseline.startsWith(NOBASELINE)) {
			logInfo("reportResults(): 'baseline' value starts with '" + NOBASELINE + "'.  Returning true.");
			return true;
		}
		if (response == null || response.isEmpty()) {
			Exception e = new Exception("reportResults(): 'response' was null or empty.  Cannot run test.");
			logError("reportResults(): 'response' value is null.", e);
			throw e;
		}
		Diff diff = null;
		String diffMessage = null;
		Patch patch = null;
		if(checkXML) {
			diff = DiffBuilder.compare(Input.fromDocument(getDocument(baseline)))
			                  .withTest(Input.fromDocument(getDocument(response)))
			                  .checkForSimilar().ignoreComments().build();
		} else {
			/* For when checkXML is false because DiffBuilder only wants to work with validated HTML/XML documents.
			   If they cannot be validated, it throws an exception.  If the user does not want strict document
			   conformance checking applied to the test, we have to use the less insistent way to do the diff. */
			patch = DiffUtils.diff(splitLines(baseline), splitLines(response));
			if(patch.getDeltas().size() > 0) {
				diffMessage = patchString(patch);
			}
		}
		if ((diff != null && diff.hasDifferences()) || diffMessage != null) {
			StringBuilder report = new StringBuilder();
			report.append("The response and baseline did not match.  Diffs follow." + newline);
			if(diff != null) {
				int numdiffs = 0;
				for (Difference difference : diff.getDifferences()) {
					numdiffs++;
					report.append(dashes(40) + newline);
					report.append(difference.toString() + newline);
					report.append(dashes(40) + newline);
				}
				report.append("Total differences: " + numdiffs + newline2);
			}
			if(diffMessage != null) {
				report.append(diffMessage + newline);
				report.append("Total differences: " + patch.getDeltas().size() + newline2);
			}
			report.append(newline + "End of differences." + newline2);
			logInfo(report.toString());
			return false;
		} else {
			logInfo("The response and baseline matched.");
			return true;
		}
	}


	/**
	 * Converts passed-in String to a standard XML org.w3c.dom.Document.  It is on the calling method
	 * to supply a string that is convertible to a canonical XML document.  'normalizedocument',
	 * if true, causes Document.normalizeDocument() to be applied to the returnable Document
	 * object before it is returned.  If there is problem with creating the new Document, the
	 * method throws an Exception.
	 *
	 * @param xmltext
	 * @param normalizeDocument
	 * @return Document
	 */
	private Document getDocument(String xmltext, boolean normalizeDocument) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringComments(true);
		Document doc;
		try {
			doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xmltext.getBytes()));
			if(normalizeDocument) {
				doc.normalizeDocument();
			}
			return doc;
		} catch (Exception e) {
			logError("getDocument(): Exception thrown building baseline or response XML document.  See log for details.", e);
			throw e;
		}
	}


	/**
	 * Convenience call to getDocument(String xmltext, boolean normalizedocument).
	 * 'normalizedocument' is set to true.
	 *
	 * @param xmltext
	 * @return Document
	 */
	private Document getDocument(String xmltext) throws Exception {
		return getDocument(xmltext, true);
	}


	/**
	 * Gets the content of the file designated by the parameter 'path'.  If 'path' is null or empty, the method
	 * throws an exception.  If 'path' ends with the value of NOBASELINE, the value of NOBASELINE" is returned.
	 *
	 * When 'addBreaksAtGreaterThans' is true, the returned text has line breaks after each ">" character.
	 * This is done to make evaluating differences between the returned response text and the baseline file
	 * text easier to report to the user.
	 *
	 * @param path
	 * @return String
	 */
	private String readFile(String path) throws Exception {
		if(path==null || path.isEmpty()) {
			throw new Exception("readFile(): 'path' was null or empty.  Cannot continue test.");
		}
		if(path.endsWith(NOBASELINE)) {
			return NOBASELINE;
		}
		try {
			String retn = cleanWhitespaceBetweenTags(FileUtils.readFileToString(new File(path), UTF8_TEXT));
			return (addBreaksAtGreaterThans ? addBreaksAtGreaterThans(retn) : retn);
		} catch (Exception e) {
			logError("readFile(): Exception thrown.  Cannot complete test.", e);
			throw e;
		}
	}


	/**
	 * Returns the result from the POST action against the given server as represented by the value
	 * of parameter 'url'.  The POST action contains a SOAP request ('soapRequestBody').  Throws an
	 * Exception if it cannot process the action for any reason.
	 *
	 * When 'addBreaksAtGreaterThans' is true, the returned text has line breaks after each ">" character.
	 * This is done to make evaluating differences between the returned response text and the baseline file
	 * text easier to report to the user.
	 *
	 * @param url
	 * @param soapRequestBody
	 * @return String
	 */
	private String httpSOAPPost(String url, String soapRequestBody) throws Exception {
		try {
			String retn = cleanWhitespaceBetweenTags(EntityUtils.toString(invokeHttpSOAPPost(url, (soapRequestBody == null ? "" : soapRequestBody)).getEntity()));
			return (addBreaksAtGreaterThans ? addBreaksAtGreaterThans(retn) : retn);
		} catch (Exception e) {
			logError("httpSOAPPost(): Exception thrown.  Cannot return request.", e);
			throw e;
		}
	}


	/**
	 * Returns the result from the GET action against the given server as represented by the value
	 * of parameter 'url'.  'params' can be null if the URL passed in has all the params on it it
	 * needs, or the method invokeHttpRESTGet() will add them.  You can also pass in a url with
	 * params as part of it, and pass in a populated 'params' Map.  The params in the Map will
	 * be added to the 'url' value.  Throws an Exception if it cannot process the action for any reason.
	 *
	 * When 'addBreaksAtGreaterThans' is true, the returned text has line breaks after each ">" character.
	 * This is done to make evaluating differences between the returned response text and the baseline file
	 * text easier to report to the user.
	 *
	 * @param url
	 * @param params
	 * @return String
	 */
	private String httpRESTGet(String url, Map params) throws Exception {
		try {
			String retn = cleanWhitespaceBetweenTags(EntityUtils.toString(invokeHttpRESTGet(url, params).getEntity()));
			return (addBreaksAtGreaterThans ? addBreaksAtGreaterThans(retn) : retn);
		} catch (Exception e) {
			logError("httpRESTGet(): Exception thrown.  Cannot return request.", e);
			throw e;
		}
	}


	/**
	 * Convenience call to httpRESTGet(String url, Map params)
	 * 'params' is passed in as null.
	 *
	 * @param url
	 * @return String
	 */
	private String httpRESTGet(String url) throws Exception {
		return httpRESTGet(url, null);
	}


	/**
	 * Creates the HTTP POST request and adds the SOAP request body ('soapRequestBody') to it, then
	 * executes the request, returning the HttpResponse object from the server.  Throws an Exception
	 * if it cannot process the action for any reason.
	 *
	 * @param url
	 * @param soapRequestBody
	 * @return HttpResponse
	 */
	private HttpResponse invokeHttpSOAPPost(String url, String soapRequestBody) throws Exception {
		try {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost(url);
			httpPost.setHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_SOAP_HEADER);
			httpPost.setEntity(new StringEntity((soapRequestBody == null ? "" : soapRequestBody)));
			return httpclient.execute(httpPost);
		} catch (Exception e) {
			logError("invokeHttpSOAPPost(): Exception thrown.  Cannot retrieve request.", e);
			throw e;
		}
	}


	/**
	 * Creates the HTTP GET request and adds any key-value pairs found in 'params' to the 'url',
	 * then executes the request, returning the HttpResponse object from the server.  Throws an
	 * Exception of it cannot process the action for any reason.
	 *
	 * @param url
	 * @param params
	 * @return HttpResponse
	 */
	@SuppressWarnings("unchecked")
	private HttpResponse invokeHttpRESTGet(String url, Map params) throws Exception {
		try {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			if(params != null && !params.isEmpty()) {
				url = buildRequestURL(url, convertMapToBasicNameValuePairList(params));
			}
			logInfo(newline + "invokeHttpRESTGet(): Making request against URL:");
			logInfo(tab + tab + url + newline);
			HttpGet httpGet = new HttpGet(url);
			httpGet.setHeader(CONTENT_TYPE_KEY, CONTENT_TYPE_TEXT_HEADER);
			return httpclient.execute(httpGet);
		} catch (Exception e) {
			logError("invokeHttpRESTGet(): Exception thrown.  Cannot retrieve request.", e);
			throw e;
		}
	}


	/**
	 * Given String as URL and paramsList as key-value pairs for the URL, returns a String
	 * with a URL containing the passed-in parameter as key-value pairs in its query string.  Note the method assumes
	 * that the passed-in key values are already URL-encoded.  Furthermore, if a value has a comma in
	 * it, it is assumed to be a separator for same-keyed values, not meant to be part of the query string,
	 * as already-encoded values will have any literal commas in them encoded.  Thus a value in a
	 * BasicNameValuePair object that reads: "A,b,C" with a key of "letter" is converted to the following
	 * string: letter=A&letter=b&letter=C and added to the URL, as in: http://www.host.com/?letter=A&letter=b&letter=C
	 *
	 * @param url
	 * @param paramsList
	 * @return String
	 */
	private String buildRequestURL(String url, ArrayList<BasicNameValuePair> paramsList) {
		url += (!url.contains("?") && !url.endsWith("/")) ? "/?" : "";
		url += (!url.contains("?") ? "?" : "");
		url += (!url.endsWith("?") && !url.endsWith("&")) ? "&" : "";
		return (url += buildQueryString(paramsList));
	}


	/**
	 * Builds a key-value pair query string for a URL out of the 'paramsList' passed in.
	 * Resulting String is in the form: [{key=val&}...] with the trailing ampersand removed
	 * keys are never null or blank; and BasicNameValuePair instance with an empty or null
	 * key is discarded.  Keys may repeat, as of course may values.
	 *
	 * @param paramsList
	 * @return
	 */
	@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
	private String buildQueryString(ArrayList<BasicNameValuePair> paramsList) {

		StringBuilder querysb = new StringBuilder();
		final String standInForBlankValue = "&";

		paramsList.forEach(it -> {
			String key = it.getName();
			String value = it.getValue();
			value = (value == null) ? "" : value;
			if(key != null && !key.isEmpty()) {
			 if(!value.contains(",")) {
				  querysb.append(key + "=" + value + "&");
			 } else {
				value = getNormalizedCsvString(value, standInForBlankValue);
				ArrayList<String> vals = new ArrayList<>(Arrays.asList(value.split(",")));
				vals.forEach(val -> {
					querysb.append(key + "=" + (val.equals(standInForBlankValue) ? "" : val) + standInForBlankValue);
				});
			 }
			}
		});
		String querystring = querysb.toString();
		return (!querystring.isEmpty() ? querystring.substring(0, querystring.length()-1) : querystring);
	}


	/**
	 * We have to control for cases where there are multiple vals associated with the same key.  In that case,
	 * we got back a comma-delimited list from convertMapToBasicNameValuePairList().  But marginal cases will
	 * burn us.  We have to control for when a key-value pair has a null or empty value for the first key-value
	 * pair added to the map for the multi-value key.  Likewise if it was the last one.  And what of any time
	 * in between?  For example, adding "" four times for the key "mykey" yields the following
	 * comma-separated string listing: ",,,".  String.split() will make that out to be two blank values, so we'd get back
	 * "myKey=&myKey=" instead of "myKey=&myKey=&myKey=&myKey=".  To protect against this, if the value string
	 * should start or end with a comma, a flag (standInForBlankValue) must be pre-pended or post-pended to the string to indicate
	 * that it is an intentionally blank value.  Likewise for the nothingness between commas, as in: ",,".  The quesiton is,
	 * what should be the flag?  Theoretically, the user can supply ANY typable value as the value side of a key-value pair.
	 * Since the values have all already been URL-encoded by now, one set of chars we can be sure will not be among
	 * the value-side values are chars encoded by URLEncoder.encode().  So any HTTP-reserved char will work.  (An ampersand
	 * is a good value to use since it will not appear as a literal in any value that has been HTTP URL-encoded.)
	 *
	 * @param value
	 * @param standInForBlankValue
	 * @return
	 */
	private String getNormalizedCsvString(String value, String standInForBlankValue) {
		if(value.startsWith(",")) {
			value = standInForBlankValue + value;
		}
		if(value.endsWith(",")) {
			value += standInForBlankValue;
		}
		return value.replace(",,", "," + standInForBlankValue + ",");
	}


	/**
	 * Converts a Map to ArrayList<BasicNameValuePair>.  If 'map' is null or empty, an ArrayList<BasicNameValuePair>
	 *   object with no entries in it is returned.  Note that any given value side of a BasicNameValuePair object in the
	 *   list contains either a single URL-encoded value fit for using as the value side of a key-value pair in a URL
	 *   query string or else a comma-delimited String of such values concatenated.  The calling method is responsible
	 *   for parsing the value side via comma delimiter to pull back the individual values associated with the repeating
	 *   key.  Example BasicNameValuePair objects returned in the ArrayList:
	 *
	 *   key:             value:
	 *   legalname        O%27Hare%2CJohn               // URL-encoded value of: "O'Hare,John"
	 *   age              32
	 *   kid              Amy,Becky,Sedrick             // In this case, eventually a URL would be formed with the following in its query string: kid=Amy&kid=Becky&kid=Sedrick
	 *   alias            Smith%2CJohn,Anderson%2CJohn  // In this case, eventually a URL would be formed with the following in its query string: alias=Smith%2CJohn&alias=Anderson%2CJohn
	 *
	 *   @param map
	 *   @return ArrayList<BasicNameValuePair>
	 */
	@SuppressWarnings("unchecked")
	private ArrayList<BasicNameValuePair> convertMapToBasicNameValuePairList(Map map) {
		ArrayList<BasicNameValuePair> pairsList = new ArrayList<>();
		if (map != null && !map.isEmpty()) {
			Collection<BasicNameValuePair> pairsColln =
					Collections2.transform(map.entrySet(), new Function<Map.Entry, BasicNameValuePair>() {
				      @Override
				      public BasicNameValuePair apply(Map.Entry me) {
					      String key = (me.getKey() == null ? "" : ((String) me.getKey()).trim());
					      // URL-conveyed key-value pairs cannot have a blank key value
					      if (key.length() == 0) {
						      return null;
					      }
					      Object val = (me.getValue() == null ? "" : me.getValue());
					      /* Since URL params could be repeated due to the use of a MultiValueMap object, we need to flatten the
					       * value side of the incoming 'map' object.  Converting the value side from an ArrayList to a comma-delimited
					       * String is easy but the values must be URL-encoded since they may contain commas, and as literals we'd want
					       * to preserve them.  For consistency of output we also must encode the value side if it is just a String
					       * object. */
					      try {
						      String value = ((val instanceof String) ? URLEncoder.encode(((String) val), UTF8_TEXT): null);
						      StringBuilder sb = new StringBuilder();
						      if (val instanceof List) {
							      ((List) val).forEach(it -> {
								      try {
									      sb.append(URLEncoder.encode((String) (it == null ? "" : it), UTF8_TEXT)).append(",");
								      } catch (Exception e) {
									      logFatal("BaseServiceEndpointTest.convertMapToBasicNameValuePairList(): Programming error detected: Specified encoding '" + UTF8_TEXT + "' unsupported.", e);
									      System.exit(1);
								      }
							      });
						      }
						      String flattenedvalues = sb.toString();
						      flattenedvalues = (!flattenedvalues.isEmpty() ? flattenedvalues.substring(0, flattenedvalues.length() - 1) : flattenedvalues);
						      return new BasicNameValuePair(key, ((value != null) ? value : flattenedvalues));
					      } catch (Exception e) {
						      logFatal("BaseServiceEndpointTest.convertMapToBasicNameValuePairList(): Programming error detected: Specified encoding '" + UTF8_TEXT + "' unsupported.", e);
						      System.exit(1);
					      }
					      return null;
				      }
			     }
			);

			pairsColln.iterator().forEachRemaining( bnvp ->
			 {
				if(bnvp != null) {
				 pairsList.add(bnvp);
				}
			 });
		}
		return pairsList;
	}


	/**
	 * Utility method used by Google Code diff utility to show differences between compared
	 * file content.
	 *
	 * @param patch
	 * @return String
	 */
	@SuppressWarnings("unchecked")
	private String patchString(Patch patch) {
		StringBuilder sb = new StringBuilder();
		for(Delta delta : patch.getDeltas()) {
			sb.append(String.format(headerFmt, "Baseline"));
			for(String line : (List<String>)delta.getOriginal().getLines()) {
				sb.append(line).append(newline);
			}
			sb.append(String.format(headerFmt, "New"));
			for(String line : (List<String>)delta.getRevised().getLines()) {
				sb.append(line).append(newline);
			}
			sb.append(String.format(headerFmt, "END")).append(newline);
		}
		return sb.toString();
	}


	/**
	 * Utility method to split lines of text at Windows new line character and returns a List
	 * containing these lines.
	 *
	 * @param text
	 * @return List
	 */
	private List<String> splitLines(String text) {
		List<String> result = Lists.newLinkedList();
		String[] winLines = text.split("\r" + newline);
		for(String s : winLines) {
			for(String linLine : s.split(newline)) {
				result.add(linLine);
			}
		}
		return result;
	}


	/**
	 * Writes output header to log file.
	 *
	 * @param url
	 */
	private void doTestOutputHeader(String url) {
		logInfo(testHeader);
		logInfo("Test Method Name: " + getCallingMethodName());
		logInfo("Test URL        : " + url);
		logInfo("");
	}


	/**
	 * Looks for file with name assigned to variable 'propsFileName' at main/resources and reads in
	 * the content as a key-value pair property list.  If no such file exists, the default values for the
	 * settable variables are used.  Otherwise the file entries override them.  If the properties file
	 * appears to have a bad entry in it, the method reports a fatal error and the VM exits so the user knows
	 * the properties file has a bad entry in it that needs investigating.
	 */
	private static void assignValuesFromPropertiesFile(String propsFileNm) {

		Properties props = new Properties();

		if(propsFileNm == null || propsFileNm.isEmpty()) {
			propsFileNm = propsFileName; // Use class default value if we have no user-specified file.
		}

		try {
			File propsfile = new File(propsFileNm);
			// Handling for when we run in IntelliJ
			if(!propsfile.exists()) {
				propsfile = new File("src/" + propsFileNm);
			}
			props.load(new FileInputStream(propsfile));
			logInfo("BaseServiceEndpointTest.assignValuesFromPropertiesFile(): Properties file '" + propsFileNm + "' loaded.");
		} catch (NullPointerException e) {
			logInfo("BaseServiceEndpointTest.assignValuesFromPropertiesFile(): Properties file '" + propsFileNm + "' not found.  Using defaults.");
		} catch (Exception e) {
			logFatal("BaseServiceEndpointTest.assignValuesFromPropertiesFile(): Fatal error occurred reading the '" + propsFileNm + "' file by stream.  Check log.  Program exiting.", e);
			System.exit(1);
		}

		if (props.isEmpty()) {
			return;
		}

		File baselineFile;
		if(props.containsKey(BASELINES_PATH_KEY)) {
			String entry = props.getProperty(BASELINES_PATH_KEY);
			entry += (!entry.endsWith("/") ? "/" : "");
			baselineFile = new File(entry);
			if(!baselineFile.exists() || !baselineFile.isDirectory()) {
				logInfo("BaseServiceEndpointTest.assignValuesFromPropertiesFile(): Read '" + entry + "' from properties file as the baselines directory path, " +
				              "but no such path appears to exist.");
				logFatal("BaseServiceEndpointTest.assignValuesFromPropertiesFile(): The baselines directory entry was interpreted to resolve to the following " +
				               "location: " + baselineFile.getAbsolutePath(), new Exception("Specified directory does not exist."));
				System.exit(1);
			}
			baselinesPath = entry;
			logInfo("BaseServiceEndpointTest.assignValuesFromPropertiesFile(): The baseline files path has been set to '" + baselineFile.getAbsolutePath() + "' from the properties file.");
		} else {
			baselineFile = new File(baselinesPath);
			logInfo("BaseServiceEndpointTest.assignValuesFromPropertiesFile(): Default baseline files path of '" + baselineFile.getAbsolutePath() + "' is in use.");
		}

		boolean condition = props.containsKey(BASE_URL_KEY);
		baseURL = condition ? props.getProperty(BASE_URL_KEY) : baseURL;
		String msg = condition ? "BaseServiceEndpointTest.assignValuesFromPropertiesFile(): The base URL for requests has been set to '" + baseURL + "' from the properties file." :
		             "BaseServiceEndpointTest.assignValuesFromPropertiesFile(): Default base URL for requests of '" + baseURL + "' is in use.";
		logInfo(msg);
	}


	/**
	 * Configures log4j.  If it fails, the tests are not run.  Logging has to be working for tests to run.  Exception and status
	 * info is sent to std-out until logging is known to be working, then it is logged and sent to std-out via logInfo().
	 * log4jPropsFileName is the relative or absolute path name of the .properties file to use.  If it is null or empty,
	 * the default file for log4j instances is used (in our case that is found at: /main/resources )
	 *
	 * @param log4jPropsFileName
	 */
	private static void configureLogger(String log4jPropsFileName) {
		print("BaseServiceEndpointTest.configureLogger(): Configuring log4j...");
		if(log4jPropsFileName != null && !log4jPropsFileName.isEmpty()) {
		 Properties props = new Properties();
		 try {
		 	 File logprops = new File(log4jPropsFileName);
			 // Handling for when we run in IntelliJ
		 	 if(!logprops.exists()) {
			   logprops = new File("src/" + log4jPropsFileName);
		   }
			 props.load(new FileInputStream(logprops));
		 } catch (Exception e) {
		 	 print("BaseServiceEndpointTest.configureLogger: Exception on class startup, could not load specified .properties file at " + log4jPropsFileName);
			 print("BaseServiceEndpointTest.configureLogger(): Tests cannot be run without a properly-configured log.  Exiting.");
			 e.printStackTrace();
			 System.exit(1);
		 }
		 PropertyConfigurator.configure(props);
		} else {
		 BasicConfigurator.configure();
		}
		print("BaseServiceEndpointTest.configureLogger(): log4j configured.");
		try {
			if(log != null) {
				logInfo("BaseServiceEndpointTest.configureLogger(): Writing to log at: " +
				              (new File(((FileAppender) Logger.getRootLogger().
						          getAppender("baseserviceendpointtest")).getFile())).getAbsolutePath());
			} else {
				throw new Exception("BaseServiceEndpointTest.configureLogger(): Unexpected: 'log' object is null.");
			}
		} catch (Exception e) {
			print("BaseServiceEndpointTest.configureLogger(): Exception on class startup, failed to obtain root logger reference to report location of log file.");
			print("BaseServiceEndpointTest.configureLogger(): Tests cannot be run without a properly-configured log.  Exiting.");
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Convenience call to configureLogger(String) with null as the passed-in log4j .properties file name value
	 */
	private static void configureLogger() {
		configureLogger(null);
	}

	/**
	 * Adds newline character after each instance of ">" in the line.
	 *
	 * @param text
	 * @return String
	 */
	private String addBreaksAtGreaterThans(String text) {
		if(text == null || text.isEmpty()) {
			return "";
		}
		return text.replaceAll(">", ">" + newline);
	}


	/**
	 * Cleans all whitespace from between instances of ">" and "<".  Necessary because
	 * diffs may be done using Google Code method which doesn't control for whitespace between
	 * close and open tags in XML.
	 *
	 * @param text
	 * @return String
	 */
	private String cleanWhitespaceBetweenTags(String text) {
		if(text == null || text.isEmpty()) {
			return "";
		}
		return text.replaceAll(">\\s+<", "><");
	}


	/**
	 * Utility method to return a dashed line equal in length to the number passed in.  If 0 or < 0
	 * is passed in, it returns and empty String.
	 *
	 * @param numDashes
	 * @return
	 */
	private static String dashes(int numDashes) {
		StringBuilder sb = new StringBuilder();
		if (numDashes > 0) {
			for (int i = 0; i < numDashes; i++) {
				sb.append("-");
			}
		}
		return sb.toString();
	}


	/**
	 * Writes input string to std-out and the log as an .info-level addition.
	 * @param msg
	 */
	private static void logInfo(String msg) {
		if (writeToOutWithLog) {
			print(msg);
		}
		if(writeToLogWithLog) {
			log.info(msg);
		}
	}


	/**
	 * Writes input string and stacktrace of 'e' to std out and the log as an .error-level addition.
	 *
	 * @param msg
	 * @param e
	 */
	private static void logError(String msg, Exception e) {
		if (writeToOutWithLog) {
			print(msg);
			print(e.getCause().toString());
		}
		if(writeToLogWithLog) {
			log.error(msg, e);
		}
	}


	/**
	 * Writes input string and stacktrace of 'e' to std out and the log as a fatal-level addition.
	 *
	 * @param msg
	 * @param e
	 */
	private static void logFatal(String msg, Exception e) {
		if(writeToOutWithLog) {
			print(msg);
			print(e.getCause().toString());
		}
		if(writeToLogWithLog) {
			log.fatal(msg, e);
		}
	}


}
