こんにちは。最近またダイエットに励んでいる杉本です。

というわけで Withings というブランドの体重計をこの機会に買ってしまいました。

https://twitter.com/sugimomoto/status/1461873799092224000?s=20

https://www.withings.com/jp/ja/scales

この体重計の一つのポイントがAPIの存在です。

https://developer.withings.com/

そこで、いそいそとJavaでAPI連携のプログラムを趣味で書いているのですが、API連携レイヤーを担うライブラリを書くにあたって、やっぱり面倒なのがAPIの実際のリクエストを検証するテストですね。

色々と調べていたらJavaのJUnitに組み込むならWiremock というのが便利な感じだったので、ちょっと検証した結果をまとめたいと思います。

http://wiremock.org/

# Wiremock のいいところ

以下のようなリクエストの定義ファイルを作るだけで、テスト用のMock API Server が組み込める感じです。

```json
{
    "mappings": [
        {
            "request": {
                "method": "GET",
                "urlPath": "/foo",
                "headers": {
                    "Accept": {
                        "equalTo": "application/json"
                    }
                }
            },
            "response": {
                "status": 200,
                "body": "{\"bar\":\"buzz\"}",
                "headers": {
                    "Content-Type": "application/json"
                }
            }
        }
    ]
}
```

さらにナイスなのはリクエストが間違っていた時にいかのような差分を返してくれるところですね！

ただのMock APIだと、リクエストの失敗だけ、みたいな感じになりがちですが、これだとかなりプログラムの修正が捗る感じです。

```
                                               Request was not matched
                                               =======================
-----------------------------------------------------------------------------------------------------------------------
| Closest stub                                             | Request                                                  |
-----------------------------------------------------------------------------------------------------------------------
                                                           |
GET                                                        | GET
[path] /foo                                                | /foo
                                                           |
Accept: application/json                                   | Accept: application/jso                             <<<<< Header does not match
                                                           |
                                                           |
-----------------------------------------------------------------------------------------------------------------------
```

# インストール

Wiremockですが、スタンドアローンで利用するパターンとJUnit等Javaに組み込んで利用するパターンの2種類の利用方法があります。

スタンドアローンはWiremock自身がホスティングしているWebサービスのMocklab バージョンか、シンプルにJarを実行するもの、Docker などにも配置できるイメージなどでも提供されています。

https://get.mocklab.io/

http://wiremock.org/docs/running-standalone/

http://wiremock.org/docs/docker/

スタンダードはマイクロサービスなどのテストで利用するのも良いですね。

今回はシンプルなライブラリのテストで利用したいので JUnit ベースで利用してみました。

Mavenのリポジトリは以下から。

https://mvnrepository.com/artifact/com.github.tomakehurst/wiremock

# 使い方

JUnitで利用する場合、まずJUnitのテストクラスにWireMockRuleのインスタンスを追加します。

http://wiremock.org/docs/junit-extensions/

第一引数で指定しているのはPort Numberですね。

```java
import com.github.tomakehurst.wiremock.junit.WireMockRule;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8081);
```

そして必要になるのが、APIリクエストを検証するための設定ファイルです。

「\src\test\resources\mappings\my-mapping.json」という形でJSONファイルで定義します。デフォルトではこのパスに配置されたものが参照されるみたいですね。

例えばOkHTTPを使って「http://localhost:8081/foo」にGETリクエストを送り、「{"bar":"buzz"}」というJSONのレスポンスを取得するテストを書いてみましょう。（JSONはわかりやすいように文字列扱いでテスト書いてます）

```java
import okhttp3.*;

/* 省略 */

    private static final OkHttpClient client = new OkHttpClient();

    @Test
    public void mockServerTest() throws IOException{
        Request request = new Request.Builder().url(url + "foo").header("Accept", "application/json").get().build();

        Response response = client.newCall(request).execute();
        assertEquals("{\"bar\":\"buzz\"}",response.body().string());
    }
```

「my-mapping.json」のサンプルは以下のような感じです。

```json
{
    "mappings": [
        {
            "request": {
                "method": "GET",
                "urlPath": "/foo",
                "headers": {
                    "Accept": {
                        "equalTo": "application/json"
                    }
                }
            },
            "response": {
                "status": 200,
                "body": "{\"bar\":\"buzz\"}",
                "headers": {
                    "Content-Type": "application/json"
                }
            }
        }
    ]
}
```

ポイントはリクエストのマッチングパターンでしょう。

Acceptの検証を行っている箇所は、現在「equalTo」で完全一致ですが、例えば以下のような部分一致の形にもできますし

```json
      "Accept" : {
        "contains" : "json"
      }
```

URLも正規表現でマッチングをかけることができます。例えばURLパスにリソースのIDなどが動的に入る場合は、このアプローチが活用できますね。

```json
{
  "request": {
    "urlPattern": "/your/([a-z]*)\\?and=query"
    ...
  },
  ...
}
```

# Query Parameters 

APIリクエストでチェックが面倒なものと言えば、Query Parametersではないでしょうか。

固定で構成しているならまだしも、動的に組み立てる側面が強いプログラムであれば、柔軟なチェック方法が欲しいですよね。

```java
@Test
public void queryParametersTest() throws IOException{
    
            
    Request request = new Request.Builder().url(url + "foo_query_parameters?$top=10&$select=id,name").header("Accept", "application/json").get().build();

    Response response = client.newCall(request).execute();
    assertEquals("{\"bar\":\"buzz\"}",response.body().string());
}
```

WireMockでは以下の「queryParameters」でそれぞれのパラメータを指定して、検証することができるので、便利です。もちろん、equalToの部分は「contains」や「matches」を使ってもいいですね。

```json
{
    "method": "GET",
    "urlPath": "/foo_query_parameters",
    "queryParameters": {
        "$select": {
            "equalTo": "id,name"
        },
        "$top": {
            "equalTo": "10"
        }
    },
    "headers": {
        "Accept": {
            "equalTo": "application/json"
        }
    }
}
```

# Request Body

Request Body にJSONを使うパターンも多いかと思います。

```java
@Test
public void postJsonRequestTest() throws IOException{

    List<User> users = new ArrayList();
    users.add(new User(1,"kazuya"));
    users.add(new User(2,"hitomi"));

    String postJson = new ObjectMapper().writeValueAsString(users);

    RequestBody body = RequestBody.create(postJson,MediaType.parse("application/json"));
    Request request = new Request
        .Builder()
        .url(url + "foo_json_user_post")
        .header("Accept", "application/json")
        .header("Content-Type", "application/json")
        .post(body).build();

    Response response = client.newCall(request).execute();
    assertEquals(200, response.code());
}
```

以下のように「bodyPatterns」を使って、JSONを表現し、マッチングがかけられます。

ちなみに「"ignoreArrayOrder" : true」をつけると配列の順番が変わってもOKと細かな点が優しくできます。

```json
{
    "method": "POST",
    "urlPattern": "/foo_json_user_post",
    "headers": {
        "Content-Type": {
            "contains": "application/json"
        }
    },
    "bodyPatterns": [
        {
            "equalToJson": [
                {
                    "id": 2,
                    "name": "hitomi"
                },
                {
                    "id": 1,
                    "name": "kazuya"
                }
            ],
            "ignoreArrayOrder" : true
        }
    ]
}
```


# Authentication

認証周りはCookieやOAuth TokenだとそのままHeaderをチェックすればいいだけと言えばだけですが、例えばBasic認証だと専用のアプローチが存在していたりします。

```java
@Test
public void basicAuthenticationTest() throws IOException{
    
    Request request = new Request.Builder()
        .url(url + "foo_basic_authentication")
        .header("Accept", "application/json")
        .header("Authorization", Credentials.basic("userName", "password"))
        .get().build();

    Response response = client.newCall(request).execute();
    assertEquals("{\"bar\":\"buzz\"}",response.body().string());
}
```

以下の「basicAuthCredentials」で検証できます。もちろんHeaderにBase64エンコードしたものを入れておいて、検証しても問題ないですが、こういう専用のものがあると、いちいち変換の手間が無いので良いですね。

```json
{
    "method": "GET",
    "urlPath": "/foo_basic_authentication",
    "headers": {
        "Accept": {
            "equalTo": "application/json"
        }
    },
    "basicAuthCredentials": {
        "username": "userName",
        "password": "password"
    }
}












