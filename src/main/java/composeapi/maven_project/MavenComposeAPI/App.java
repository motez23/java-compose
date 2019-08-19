package composeapi.maven_project.MavenComposeAPI;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.omg.CORBA.portable.InputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


public class App 
{
    public static void main( String[] args )
    {

    	Properties props = new Properties();	
    	try {
			props.load(new FileInputStream("src/main/resources/config.properties"));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	
    	HttpClient httpClient = new DefaultHttpClient();
        try {
        	String baseUrl = props.getProperty("baseUrl");
        	String bearerToken = props.getProperty("bearerToken");
        	String mongoVersion = props.getProperty("mongoVersion"); 
        	String deploymentType = props.getProperty("deploymentType");
        	String deploymentName = props.getProperty("deploymentName");
		    
        	String resp = "";
		    
		    /*
		     * Get Cluster ID
		     */
		    
		    resp = getRequest(baseUrl+"clusters", bearerToken);
		    JSONObject myResponse = new JSONObject(resp);
		    String clusterID = "nb";
		    String clusterName = "kp1-sjc01-c00";
		    clusterID = getClusterID(myResponse, clusterName);
		    System.out.println("Cluster ID retrieved: " + clusterID);
		    
		    /*
		     * Get Account ID
		     */
		    resp = getRequest(baseUrl+"accounts", bearerToken);
		    myResponse = new JSONObject(resp);
		    String accountID = "nb";
		    String accountName = "";
		    accountID = getAccountID(myResponse, accountName);
		    System.out.println("Account ID retrieved: " + accountID);
		    
		    /*
		     * Post new mongo service
		     */
		    JSONObject j = new JSONObject();
		    
		    j.put("deployment", new JSONObject());
		    j.getJSONObject("deployment").put("name", deploymentName);
		    j.getJSONObject("deployment").put("account_id", accountID);
		    j.getJSONObject("deployment").put("cluster_id", clusterID);
		    j.getJSONObject("deployment").put("type", deploymentType);
		    // older version to showcase upgrade
		    j.getJSONObject("deployment").put("version", mongoVersion);

			resp = postRequest(baseUrl+"deployments", j.toString(), bearerToken);
			System.out.println("Response from deployment creation: " + resp);
			//SLEEP 2 mins
			Thread.sleep(120000);
		    myResponse = new JSONObject(resp);
		    String deploymentID = myResponse.getString("id");
		    String connectionStrings = myResponse.getJSONObject("connection_strings").toString();
		    
		    System.out.println("deployment ID: "+deploymentID);
		    System.out.println("connection strings: " + connectionStrings);
		    /*
		     * Get Version to upgrade to, if any
		     */
		    resp = getRequest(baseUrl+"deployments/"+deploymentID+"/versions",  bearerToken);
	    	myResponse = new JSONObject(resp);
		    String upgradeVersion = getUpgradeVersion(myResponse);
		    if( !upgradeVersion.equalsIgnoreCase("no_upgrade") ) {
			    System.out.println("Highest version available for inplace upgrade: " + upgradeVersion);
			    j = new JSONObject();
			    j.put("deployment", new JSONObject());
			    j.getJSONObject("deployment").put("version", upgradeVersion);
			    
			    resp = patchRequest(baseUrl+"deployments/"+deploymentID+"/versions", j.toString(), bearerToken);

				System.out.println("Response from upgrade request: " + resp);
				Thread.sleep(120000);
		    }
		    
		    /*
		     * Delete newly created deployment
		     */
		    System.out.println("Deletion URL: "+ baseUrl+"deployments/"+deploymentID);
		    /*
		    System.out.println("Excecuting delete for newly created deployment");
		    resp = deleteRequest(baseUrl+"deployments/"+deploymentID, bearerToken);
		    System.out.println("Response from delete request " + resp);
		    myResponse = new JSONObject(resp);
		    String recipeID = myResponse.getString("id");
		    deleteRequest(baseUrl+"deployments/"+deploymentID, bearerToken);
		    System.out.println("Recipe ID from delete request: "+ recipeID);
		    System.out.println("Checking /recipes/"+recipeID+ " for deprovisioning status");
		    resp = getRequest(baseUrl+"recipes/"+recipeID,  bearerToken);
		    System.out.println(resp);
		    */		    
          System.out.println("----------------------------------------");

        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          httpClient.getConnectionManager().shutdown();
        }
    }

	private static String getUpgradeVersion(JSONObject myResponse) {
		JSONArray transitions = myResponse.getJSONObject("_embedded").getJSONArray("transitions");
		if(transitions.length() == 0) {
			return "no_upgrade";
		}
		else {
			return transitions.getJSONObject(transitions.length()-1).getString("to_version");
		}
	}

	private static String getClusterID(JSONObject myResponse, String clusterName) {
		JSONArray clusters = myResponse.getJSONObject("_embedded").getJSONArray("clusters");
		String clusterID = "couldnt find cluster";
		for (int i = 0; i< clusters.length();i++) {
			if(clusters.getJSONObject(i).getString("name").equalsIgnoreCase(clusterName)) {
				clusterID = clusters.getJSONObject(i).getString("id");
			}
		}
		return clusterID;
	}
	
	private static String getAccountID(JSONObject myResponse, String accountName) {
		JSONArray accounts = myResponse.getJSONObject("_embedded").getJSONArray("accounts");
		String accountID = "couldnt find account";
//		for (int i = 0; i< accounts.length();i++) {
//			if(clusters.getJSONObject(i).getString("name").equalsIgnoreCase(accountName)) {
//				accountID = clusters.getJSONObject(i).getString("id");
//			}
//		}
		accountID = accounts.getJSONObject(0).getString("id");
		return accountID;
	}

	private static String postRequest(String url, String body, String bearerToken) throws UnsupportedEncodingException, IOException, ClientProtocolException {
		CloseableHttpClient client = HttpClients.createDefault();
		
          HttpPost httpPost = new HttpPost(url);
          StringEntity entity = new StringEntity(body);
          httpPost.setEntity(entity);
          httpPost.setHeader("Accept", "application/json");
          httpPost.setHeader("Content-type", "application/json");
          httpPost.setHeader("Authorization", "Bearer "+bearerToken);
       
          CloseableHttpResponse response = client.execute(httpPost);
          return EntityUtils.toString(response.getEntity());
	}
	private static String getRequest(String url, String bearerToken) throws UnsupportedEncodingException, IOException, ClientProtocolException {
		CloseableHttpClient client = HttpClients.createDefault();
          HttpGet httpGet = new HttpGet(url);

          httpGet.setHeader("Authorization", "Bearer "+bearerToken);
          CloseableHttpResponse response = client.execute(httpGet);
          return EntityUtils.toString(response.getEntity());
	}
	private static String deleteRequest(String url, String bearerToken) throws UnsupportedEncodingException, IOException, ClientProtocolException {
		CloseableHttpClient client = HttpClients.createDefault();
          HttpDelete httpDelete = new HttpDelete(url);

          httpDelete.setHeader("Authorization", "Bearer "+bearerToken);
          CloseableHttpResponse response = client.execute(httpDelete);
          return EntityUtils.toString(response.getEntity());
	}
	private static String patchRequest(String url, String body, String bearerToken) throws UnsupportedEncodingException, IOException, ClientProtocolException {
			CloseableHttpClient client = HttpClients.createDefault();
			
			HttpPatch httpPatch = new HttpPatch(url);
			StringEntity entity = new StringEntity(body);
			httpPatch.setEntity(entity);
			httpPatch.setHeader("Accept", "application/json");
			httpPatch.setHeader("Content-type", "application/json");
			httpPatch.setHeader("Authorization", "Bearer "+bearerToken);
	        
	        CloseableHttpResponse response = client.execute(httpPatch);
	        return EntityUtils.toString(response.getEntity());
	}
}
