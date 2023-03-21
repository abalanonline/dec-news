/*
 * Copyright 2022 Aleksei Balan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ab.december;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GptThree {

  private static final Logger log = Logger.getLogger(GptThree.class.getName());

  public static final String[] GPT3_MODELS = {"text-curie-001", "text-babbage-001", "text-ada-001"};
  public static final int GPT3_ATTEMPTS = 3;
  public static final int GPT3_SYMBOLS_THRESHOLD = 512; // about 30 seconds of reading
  public static final double GPT3_TEMPERATURE = 1.2;
  public static final int GPT3_TOKENS = 200;
  public static final String URL_LOCAL = "http://localhost:2741/";
  public static final String URL_OPENAI = "https://api.openai.com/v1/";
  private ObjectMapper objectMapper;
  private String apiKey;
  private final String gptUrl;

  public GptThree() {
    this.apiKey = System.getenv("OPENAI_API_KEY");
    if (apiKey == null) {
      throw new IllegalStateException("API key not defined. use \"export OPENAI_API_KEY=sk-eA3Ov43M...\"");
    }
    this.objectMapper = new ObjectMapper();
    this.gptUrl = isLocal() ? URL_LOCAL : URL_OPENAI;
    log.info("GPT url: " + gptUrl);
  }

  public static String httpGet(String url) throws IOException, InterruptedException {
    return HttpClient.newBuilder().build().send(
        HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
        HttpResponse.BodyHandlers.ofString()).body();
  }

  public static boolean isLocal() {
    try {
      String s = httpGet(URL_LOCAL + "moderations");
      return !s.isEmpty() && s.charAt(0) == '{';
    } catch (IOException | InterruptedException e) {
      return false;
    }
  }

  private HttpRequest.Builder newHttpRequestBuilder(String endpoint) {
    return HttpRequest.newBuilder()
        .uri(URI.create(gptUrl + endpoint))
        .header("Authorization", "Bearer " + apiKey)
        .header("Content-Type", "application/json");
  }

  private JsonNode send(HttpRequest.Builder request) {
    HttpClient client = HttpClient.newBuilder().build();
    HttpResponse<byte[]> send;
    JsonNode jsonNode;
    try {
      send = client.send(request.build(), HttpResponse.BodyHandlers.ofByteArray());
      jsonNode = objectMapper.readTree(send.body());
    } catch (IOException | InterruptedException e) {
      throw new IllegalStateException(e);
    }
    JsonNode error = jsonNode.get("error");
    if (error != null) {
      throw new IllegalStateException(error.get("message").textValue());
    }
    return jsonNode;
  }

  public boolean isFlagged(String s) {
    byte[] input;
    try {
      input = objectMapper.writeValueAsBytes(Collections.singletonMap("input", s));
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
    JsonNode response = send(newHttpRequestBuilder("moderations")
        .POST(HttpRequest.BodyPublishers.ofByteArray(input)));
    JsonNode results = response.get("results");
    boolean flagged = IntStream.range(0, results.size()).mapToObj(results::get)
        .anyMatch(a -> a.get("flagged").booleanValue());
    //if (flagged) System.out.println(response.toPrettyString());
    return flagged;
  }

  private List<String> completions(String model, String prompt) {
    Map<String, Object> request = new HashMap<>();
    request.put("model", model);
    request.put("prompt", prompt);
    request.put("temperature", GPT3_TEMPERATURE);
    request.put("max_tokens", GPT3_TOKENS);
    request.put("n", GPT3_ATTEMPTS);
    byte[] input;
    try {
      input = objectMapper.writeValueAsBytes(request);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
    JsonNode response = send(newHttpRequestBuilder("completions")
        .POST(HttpRequest.BodyPublishers.ofByteArray(input)));
    JsonNode choices = response.get("choices");
    return IntStream.range(0, choices.size()).mapToObj(choices::get)
        .map(choice -> choice.get("text").textValue()).collect(Collectors.toList());
  }

  public static final String[] CONTEXT_KEY = {"Headline", "Description", "Tagline"};
  public String makeNews(String... context) {
    for (String text : context) {
      if (isFlagged(text)) return null;
    }

    StringBuilder prompt = new StringBuilder();
    prompt.append("Latest news.\n\n");
    for (int i = 0; i < Math.min(CONTEXT_KEY.length, context.length); i++) {
      prompt.append(CONTEXT_KEY[i]).append(": ").append(context[i]).append("\n\n");
    }
    prompt.append("Text:");

    List<String> results = completions(GPT3_MODELS[0], prompt.toString()).stream()
        .map(u -> u.replaceFirst("[^.]*\\z", "").replace("Text:", "").trim())
        .sorted(Comparator.comparingInt(s -> -s.length()))
        .collect(Collectors.toList());
    String result = results.get(0);
    if (result.length() > GPT3_SYMBOLS_THRESHOLD) return result;
    result = results.get(0) + " " + results.get(1);
    if (result.length() > GPT3_SYMBOLS_THRESHOLD) return result;
    return null;
  }

}
