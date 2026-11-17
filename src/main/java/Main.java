import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Main {
    private static final String URL = "https://raw.githubusercontent.com/netology-code/jd-homeworks/master/http/task1/cats";

    public static ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) {
        List<CatFact> catFacts = new ArrayList<>();

        // Получение JSON-строки
        String jsonString = null;
        try {
            jsonString = getJsonString(URL);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(2);
        }

        // Преобразование JSON-строки в объекты CatFact
        try {
            catFacts.addAll(jsonToCatFacts(jsonString));
        } catch (JsonProcessingException e) {
            System.err.println(e.getMessage());
            System.exit(3);
        }

        List<CatFact> filteredCatFacts = catFacts.stream()
                .filter(value -> value.getUpvotes() != null && value.getUpvotes() > 0)
                .toList();

        filteredCatFacts.stream().forEach(System.out::println);
    }

    private static String getJsonString(String url) throws IOException {
        if (url == null || url.isEmpty())
            throw new IllegalArgumentException();

        String jsonString = "";

        try (CloseableHttpClient httpClient = createHttpClient()) {
            HttpGet request = new HttpGet(URL);
            request.setHeader("Accept", "application/json");
            request.setHeader("User-Agent", "CatFactsDemo/0.1");

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                if (response.getStatusLine().getStatusCode() < 200 || response.getStatusLine().getStatusCode() > 299) {
                    System.err.println("Bad response: " + response.getStatusLine());
                    System.exit(1);
                };

                //System.out.println(response.getStatusLine());
                for (int i = 0; i < response.getAllHeaders().length; i++) {
                    Header header = response.getAllHeaders()[i];
                    //System.out.println(header.getName() + ": " + header.getValue());
                }

                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    // Преобразование тела ответа в JSON-строку
                    jsonString = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                }
            }
        }

        return jsonString;
    }


    private static List<CatFact> jsonToCatFacts(String jsonString) throws JsonProcessingException {
        return mapper.readValue(
                jsonString,
                mapper.getTypeFactory().constructCollectionType(List.class, CatFact.class)
                );

    }


    private static CloseableHttpClient createHttpClient() {
        return  HttpClientBuilder.create()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(5000)    // максимальное время ожидание подключения к серверу
                        .setSocketTimeout(30000)    // максимальное время ожидания получения данных
                        .setRedirectsEnabled(false) // возможность следовать редиректу в ответе
                        .build())
                .build();
    }
}
