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

import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.OutputFormat;
import software.amazon.awssdk.services.polly.model.SynthesizeSpeechRequest;
import software.amazon.awssdk.services.polly.model.TextType;
import software.amazon.awssdk.services.polly.model.Voice;
import software.amazon.awssdk.services.polly.model.VoiceId;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

// AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY, AWS_REGION
public class Polly {

  private static final Logger log = Logger.getLogger(Polly.class.getName());

  public static final String[] VOICES_BLACKLIST = new String[]{};
  private final PollyClient client;
  private List<VoiceId> voicesAvailable;

  public Polly() {
    HashSet<String> voicesBlacklist = new HashSet<>(List.of(VOICES_BLACKLIST));
    client = PollyClient.builder().build();
    List<Voice> voices = client.describeVoices().voices().stream()
        .filter(u -> u.languageCode().toString().startsWith("fr-FR"))
        .filter(u -> !voicesBlacklist.contains(u.id().toString()))
        .collect(Collectors.toList());
    log.info("voice: " + voices.stream()
        .map(v -> v.languageCode() + " " + v.gender().toString().charAt(0) + " " + v.id().toString())
        .collect(Collectors.joining(", ")));
    this.voicesAvailable = voices.stream().map(Voice::id).collect(Collectors.toList());
  }

  public InputStream tts(String s, UUID hashCode) {
    VoiceId voiceId = voicesAvailable.get((hashCode.hashCode() << 1 >>> 1) % voicesAvailable.size());
    SynthesizeSpeechRequest request = SynthesizeSpeechRequest.builder()
        .textType(TextType.SSML).text("<speak><prosody rate=\"slow\">" + s + "</prosody></speak>")
        .voiceId(voiceId).outputFormat(OutputFormat.MP3).build();
    return client.synthesizeSpeech(request);
  }

  public String describeVoices() {
    return client.describeVoices().voices().stream()
        .filter(u -> u.languageCode().toString().startsWith("fr"))
        .map(v -> v.languageCode() + " " + v.gender().toString().charAt(0) + " " + v.id().toString() + " " +
            v.supportedEngines().stream().map(u -> u.toString().substring(0, 1)).collect(Collectors.joining()))
        .collect(Collectors.joining("\n"));
  }

  public static void main(String[] args) throws Exception {
    //System.out.println(new Polly().describeVoices());
    InputStream stream = new Polly().tts("bonjour", UUID.randomUUID());
    Files.copy(stream, Paths.get("test.mp3"));
    System.out.println(stream);
  }
}
