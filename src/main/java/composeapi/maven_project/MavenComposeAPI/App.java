package composeapi.maven_project.MavenComposeAPI;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
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
    	HttpClient httpClient = new DefaultHttpClient();
        try {
        	String baseUrl="https://api.compose.io/2016-07/";
        	String bearerToken = "58b3b76eaa03495dd117b01155d57df4c3eda3ec3ecf1ca64c5d4dafabafe43a";
        	
        	
		    String getUrl = "http://dummy.restapiexample.com/api/v1/employee/60751";
//		    String getUrl = "http://dummy.restapiexample.com/api/v1/employees";
		    String resp = getRequest(getUrl, bearerToken);
		    System.out.println();
		    
		    System.out.println(resp);

		    JSONObject myResponse = new JSONObject(resp);
		    System.out.println("employee_name- "+myResponse.getString("employee_name"));
//		    JSONArray arr = new JSONArray(resp);
//		    for (int i = 0; i< arr.length()-110;i++) {
//		    	String id = arr.getJSONObject(i).getString("id");
//		    	String name = arr.getJSONObject(i).getString("employee_name");
//		    	System.out.println("id: "+id + " name: "+ name);
//		    }
//		    System.out.println(arr.length());
//		    
		    String postUrl = "http://dummy.restapiexample.com/api/v1/create";
		    JSONObject j = new JSONObject();
		    j.put("name", "motezzzz");
		    j.put("salary", "3422");
		    j.put("age", "12");
		    j.put("id", "2435344");
			String jsonBody = "{\"name\":\"motez\",\"salary\":\"123\",\"age\":\"23\",\"id\":\"1s37\"}";
			System.out.println(postRequest(postUrl, j.toString()));
          
          
//          ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
//          String json = mapper.writeValueAsString(httpResponse.getEntity().getContent());
//          System.out.println(json);

          System.out.println("----------------------------------------");

        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          httpClient.getConnectionManager().shutdown();
        }
      
    	System.out.println( "Hello World!" );
    }

	private static String postRequest(String url, String body) throws UnsupportedEncodingException, IOException, ClientProtocolException {
		CloseableHttpClient client = HttpClients.createDefault();
		
          HttpPost httpPost = new HttpPost(url);
       
          
          StringEntity entity = new StringEntity(body);
          httpPost.setEntity(entity);
          httpPost.setHeader("Accept", "application/json");
          httpPost.setHeader("Content-type", "application/json");
       
       
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
}
