package sugimomoto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.junit.Rule;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8081);
    
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
        Request request = new Request.Builder().url("http://localhost:8081/foo").header("Accept", "text/plain").get().build();

        Response response = client.newCall(request).execute();
        assertEquals("{\"bar\":\"buzz\"}",response.body().string());
    }

    @Test
    public void PostmanEchoTest() throws IOException{
        /*
        PostmanEcho echo = new PostmanEcho("https://postman-echo.com/post");

        
        assertEquals("name=hello", echo.request());
        */

    }
}
