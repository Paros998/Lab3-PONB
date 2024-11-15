package psk.ponb;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

@Getter
@RequiredArgsConstructor
public class HttpClientApp {
    private final static Logger log = LogManager.getLogger(HttpClientApp.class);
    private final String externalApi = "https://6737cf384eb22e24fca635b9.mockapi.io/api/http/v1";
    private final String endpoint = "/posts";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private final ObjectMapper objectMapper = new JsonMapper();

    public static void main(String[] args) {
        HttpClientApp app = new HttpClientApp();
        String externalUrl = app.getExternalApi() + app.getEndpoint();
        String externalId;

        Optional<HttpRequest> getAllPosts = Optional.ofNullable(
                app.prepareGetRequest(externalUrl, Map.of())
        );

        if (getAllPosts.isPresent()) {
            Optional<HttpResponse<String>> response = Optional.ofNullable(app.callService(getAllPosts.get()));
            if (response.isPresent()) {
                final ArrayList<Post> postsBoxType = new ArrayList<Post>();
                new TypeReference(postsBoxType.getClass())
                ArrayList<Post> posts = app.deserializeResult(response.get().body(), postsBoxType.getClass());
                log.info("Called posts and got: {}", posts);
            }
        }


    }

    private HttpResponse<String> callService(final HttpRequest request) {
        log.info("Calling api: {} with request: {}", request.uri(), request);
        try {
            final HttpResponse<String> response = this.getHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Received response: {} {}", response, response.body());
            return response;
        }
        catch (IOException | InterruptedException e) {
            log.error("Error occurred during calling request", e);
            return null;
        }
    }

    private HttpRequest prepareGetRequest(final String url, final Map<String, String> queryParams) {
        try {
            URI uri;
            if (!queryParams.isEmpty()) {
                StringBuilder params = new StringBuilder();
                queryParams.forEach((key, value) -> params.append("&").append(key).append("=").append(value));
                params.delete(0, 1);
                uri = new URI(url + "?" + params);
            } else {
                uri = new URI(url);
            }
            return HttpRequest.newBuilder()
                    .uri(uri)
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .GET()
                    .build();
        } catch (final URISyntaxException e) {
            log.error("Error occurred during preparing request.");
            return null;
        }
    }

    private <T> T deserializeResult(final String o, final Class<?> clazz) {
        try {
            T result = (T) objectMapper.readValue(o, clazz);
            log.info("Deserialized response object from: {} to: {}", o, result);
            return result;
        } catch (final Exception e) {
            log.error("Error occurred during deserializing response", e);
            return null;
        }
    }
}
