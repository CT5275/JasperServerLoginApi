package com.jaspersoft.jasperserver.rest.sample;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class RestAPIUtils {

	private HttpClient httpClient;
	private CookieStore cookieStore;
	private HttpContext httpContext;
	private HttpContext httpContext2;
	protected HttpRequestBase httpReqCE;
	protected HttpRequestBase httpGetKey;
	
	protected HttpRequestBase tempHttpReq;
	protected HttpResponse httpRes;
	protected HttpResponse httpResGetKey;

	static final String BASE_REST_URL = "/jasperserver/rest_v2";
	static final String SERVICEGETKEY = "/jasperserver/GetEncryptionKey";
	static final String SERVICE_LOGIN = "/login";
	private final Log log = LogFactory.getLog(getClass());
	
	public RestAPIUtils() {
		httpClient = new DefaultHttpClient();
		cookieStore = new BasicCookieStore();
		httpContext = new BasicHttpContext();
		httpContext2 = new BasicHttpContext();//without cookie
		httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
	}

	public void loginToServer() {

		try {
			httpGetKey = new HttpGet();
			httpResGetKey = sendRequest(httpGetKey, SERVICEGETKEY, null);
			String jsonString = EntityUtils.toString(httpResGetKey.getEntity());
			JSONObject ja = new JSONObject(jsonString);
			String n_key = ja.getString("n");
			String e_num = ja.getString("e");
			String encryptedPass = encryptPassword(n_key, e_num, "jasperadmin");
			
			//encryptedPass=encryptedPass.replace("a","b"); //mess the output to test failure scenario
			List<NameValuePair> ce_qparams = new ArrayList<NameValuePair>();
			ce_qparams.add(new BasicNameValuePair("j_username", "jasperadmin"));
			ce_qparams.add(new BasicNameValuePair("j_password", encryptedPass));
			httpReqCE = new HttpPost();

			httpRes = sendRequest(httpReqCE, BASE_REST_URL + SERVICE_LOGIN, ce_qparams);
			System.out.println("login status:" + httpRes.getStatusLine().getStatusCode());

			if (httpRes.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + httpRes.getStatusLine().getStatusCode());
				// consuming the content to close the stream
				// IOUtils.toString(httpRes.getEntity().getContent());
			}
			/* Code using Jersey*/
			/*
    		Client client = Client.create();
    		WebResource resource = client.resource("http://localhost:8080/jasperserver/GetEncryptionKey");
    		ClientResponse response = resource.get(ClientResponse.class);

    		String jsonString = response.getEntity(String.class);
    		JSONObject ja = new JSONObject(jsonString);
            String n_key = ja.getString("n");
            String e_num = ja.getString("e");
            String encryptedPass=encryptPassword(n_key,e_num, PASSWORD);

    		resource = client.resource("http://localhost:8080/jasperserver/rest_v2").path("login")
    				 .queryParam("j_username", "jasperadmin")
                     .queryParam("j_password", encryptedPass);

    		ClientResponse response2 = resource.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(ClientResponse.class,loginString);
    		
    		if (response2.getStatus() != 200) {
    			  throw new RuntimeException("Failed : HTTP error code : "
    			      + response.getStatus());	

    		}
			 */
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected HttpResponse sendRequest(HttpRequestBase req, String service, List<NameValuePair> qparams)
			throws Exception {
		URI uri;
		if (qparams != null) {
			uri = URIUtils.createURI("http", "localhost", 8080, service, URLEncodedUtils.format(qparams, "UTF-8"),
					null);
		} else {
			uri = (new URL("http", "localhost", 8080, service)).toURI();
		}

		req.setURI(uri);
		System.out.println("sending Request. url: " + uri.toString() + " req verb: " + req.getMethod());
		httpRes = httpClient.execute(req, httpContext);
		return httpRes;
	}

	protected HttpResponse sendRequest2(HttpRequestBase req, String service, List<NameValuePair> qparams)
			throws Exception {
		URI uri;
		if (qparams != null) {
			uri = URIUtils.createURI("http", "localhost", 8080, service, URLEncodedUtils.format(qparams, "UTF-8"),
					null);
		} else {
			uri = (new URL("http", "localhost", 8080, service)).toURI();
		}

		req.setURI(uri);
		System.out.println("sending Request. url: " + uri.toString() + " req verb: " + req.getMethod());
		httpRes = httpClient.execute(req, httpContext2);
		return httpRes;
	}
	
	public void releaseConnection(HttpResponse res) throws Exception {
		InputStream is = res.getEntity().getContent();
		is.close();
	}

	public String encryptPassword(String n, String e, String plainPw) throws Exception {
		try {
			Security.insertProviderAt(new BouncyCastleProvider(), 1);
			RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(new BigInteger(n, 16), new BigInteger(e, 16));
			PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(publicKeySpec);
			Cipher cipher = Cipher.getInstance("RSA/NONE/NoPadding", "BC");
			// Cipher cipher = Cipher.getInstance("RSA/NONE/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte[] cipherText = cipher.doFinal(plainPw.getBytes("UTF8"));

			return Hex.toHexString(cipherText);
		} catch (Exception ex) {
			throw new Exception(ex.getMessage());
		}
	}

}
