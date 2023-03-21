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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.logging.Logger;

import static ab.december.GptThree.httpGet;

public class App {

  private static final Logger log = Logger.getLogger(App.class.getName());

  public static final String TAGLINE = "Should you quit being a pigeon? Ornithologists say yes!";
  public static final String THEMATIC_BREAK = "\n\n---\n\n";

  public static void main(String[] args) throws Exception {
    GptThree gptThree = new GptThree();
    Translate translate = new Translate();
    Polly polly = new Polly();
    // CBC NEWS NETWORK is Canada's most trusted 24-hour news channel
    //String xml = httpGet("https://www.cbc.ca/cmlink/rss-canada-montreal");
    String xml = httpGet("https://www.cbc.ca/cmlink/rss-offbeat");
    JsonNode items = new XmlMapper().readTree(xml).get("channel").get("item");
    for (JsonNode item : items) {
      String sPrompt0 = item.get("title").textValue();
      String sPrompt1 = item.get("description").textValue().replaceAll("<[^>]*>", "").trim();
      String sPrompt = sPrompt0 + "\n" + sPrompt1;
      UUID hashCode = UUID.nameUUIDFromBytes(sPrompt.getBytes(StandardCharsets.UTF_8));
      Path pathTxt = Paths.get("news_" + hashCode.toString() + ".txt");
      if (Files.exists(pathTxt)) continue;
      log.info(hashCode + " " + sPrompt0);
      String sEn = gptThree.makeNews(sPrompt0, sPrompt1);
      String sFr = translate.enToFr(sEn);
      Files.writeString(pathTxt, sFr + THEMATIC_BREAK + sEn + THEMATIC_BREAK + sPrompt);
      Files.copy(polly.tts(sFr, hashCode), Paths.get("news_" + hashCode.toString() + ".mp3"));
      //break; // circuit
    }
  }
}
