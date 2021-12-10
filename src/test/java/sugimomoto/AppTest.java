package sugimomoto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.print.attribute.standard.Media;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.apache.hc.core5.http.ContentType;
import org.junit.Rule;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8081);

    private final String url = "http://localhost:8081/";
    
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }

    @Test
    public void mockServerTest() throws IOException{
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url + "foo").header("Accept", "text/plain").get().build();

        Response response = client.newCall(request).execute();
        assertEquals("{\"bar\":\"buzz\"}",response.body().string());
    }

    @Test
    public void basicAuthenticationTest() throws IOException{
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
            .url(url + "foo_basic_authentication")
            .header("Accept", "application/json")
            .header("Authorization", Credentials.basic("userName", "password"))
            .get().build();

        Response response = client.newCall(request).execute();
        assertEquals("{\"bar\":\"buzz\"}",response.body().string());
    }

    @Test
    public void queryParametersTest() throws IOException{
        
        OkHttpClient client = new OkHttpClient();        
        Request request = new Request.Builder().url(url + "foo_query_parameters?$top=10&$select=id,name").header("Accept", "application/json").get().build();

        Response response = client.newCall(request).execute();
        assertEquals("{\"bar\":\"buzz\"}",response.body().string());
    }

    @Test
    public void externalFileTest() throws IOException{
        
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url + "foo_external_file_with_body").header("Accept", "text/plain").get().build();

        Response response = client.newCall(request).execute();
        assertEquals("{\"bar\":\"buzz\"}",response.body().string());
    }

    
    @Test
    public void jsonBodyTest() throws IOException{
        
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url + "foo_json_body").header("Accept", "text/plain").get().build();

        Response response = client.newCall(request).execute();
        assertTrue("match", response.body().string().contains("hello"));
    }

    @Test
    public void postRequestTest() throws IOException{
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), "{\"bar\":\"buzz\"}");
        Request request = new Request
        .Builder()
        .url(url + "foo_json_post")
        .header("Accept", "application/json")
        .header("Content-Type", "application/json")
        .post(body).build();

        Response response = client.newCall(request).execute();
        assertEquals(200, response.code());
    }

    @Test
    public void postJsonRequestTest() throws IOException{

        List<User> users = new ArrayList();
        // "ignoreArrayOrder" : true をつけると配列の順番が変わってもOK
        users.add(new User(1,"kazuya"));
        users.add(new User(2,"hitomi"));

        String postJson = new ObjectMapper().writeValueAsString(users);

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), postJson);
        Request request = new Request
            .Builder()
            .url(url + "foo_json_user_post")
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .post(body).build();

        Response response = client.newCall(request).execute();
        assertEquals(200, response.code());
    }



    @Test
    public void PostmanEchoTest() throws IOException{
        /*
        PostmanEcho echo = new PostmanEcho("https://postman-echo.com/post");

        
        assertEquals("name=hello", echo.request());
        */

    }
}
