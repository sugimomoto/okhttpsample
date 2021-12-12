package sugimomoto;

import java.io.IOException;

import okhttp3.*;

public class PostmanEcho {

    private String url;
    
    public PostmanEcho(String url){
        this.url = url;
    }

    public String request() throws IOException{

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"), "name=hello");
        Request request = new Request.Builder().url(this.url).post(body).build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

}
