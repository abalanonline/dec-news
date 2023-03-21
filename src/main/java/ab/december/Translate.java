/*
 * Copyright 2023 Aleksei Balan
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

import software.amazon.awssdk.services.translate.TranslateClient;
import software.amazon.awssdk.services.translate.model.TranslateTextRequest;
import software.amazon.awssdk.services.translate.model.TranslateTextResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_REGION
public class Translate {

  private final TranslateClient client;

  public Translate() {
    client = TranslateClient.builder().build();
  }

  public String enToFr(String s) {
    TranslateTextRequest request = TranslateTextRequest.builder()
        .text(s).sourceLanguageCode("en").targetLanguageCode("fr-CA").build();
    return client.translateText(request).translatedText();
  }

  public static void main(String[] args) throws Exception {
    System.out.println(new Translate().enToFr("hello"));
  }
}
