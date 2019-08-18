package composeapi.maven_project.MavenComposeAPI;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.omg.CORBA.portable.InputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
    	HttpClient httpClient = new DefaultHttpClient();
        try {
          HttpGet httpGetRequest = new HttpGet("http://dummy.restapiexample.com/api/v1/employees");
          HttpResponse httpResponse = httpClient.execute(httpGetRequest);

          System.out.println("----------------------------------------");
          System.out.println(httpResponse.getStatusLine());
          String res = EntityUtils.toString(httpResponse.getEntity());
          System.out.println(res);
          ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
          String json = mapper.writeValueAsString(httpResponse.getEntity().getContent());
          System.out.println(json);

          System.out.println("----------------------------------------");

        } catch (Exception e) {
          e.printStackTrace();
        } finally {
          httpClient.getConnectionManager().shutdown();
        }
      
    	System.out.println( "Hello World!" );
    }
}
