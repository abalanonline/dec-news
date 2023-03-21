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

public class App {

  public static final String TAGLINE = "Should you quit being a pigeon? Ornithologists say yes!";

  public static void main(String[] args) throws Exception {
    GptThree gptThree = new GptThree();
    // CBC NEWS NETWORK is Canada's most trusted 24-hour news channel
    String xml = GptThree.httpGet("https://www.cbc.ca/cmlink/rss-canada-montreal");
    JsonNode items = new XmlMapper().readTree(xml).get("channel").get("item");
    for (JsonNode item : items) {
      String description = item.get("description").textValue().replaceAll("<[^>]*>", "").trim();
      String s = gptThree.makeNews(item.get("title").textValue(), description);

      System.out.println(s);
      break; // circuit
    }
  }
}
