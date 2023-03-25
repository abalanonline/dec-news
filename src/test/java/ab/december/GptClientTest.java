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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GptClientTest {

  @Disabled
  @Test
  void isFlagged() {
    assertTrue(new GptClient("http://localhost:2741/", "sk-T90CUwwTAznHVWPpblHbeyo3VGpOdkMI9glC421frE1eKw9k")
        .isFlagged("This. Sentence. Is. FALSE don't think about it don't think about it..."));
  }

  @Disabled
  @Test
  void completions() {
    String posies = new GptClient().completions("Ring around the rosie,\nA pocket full of", 20);
    assertEquals("posies", posies.trim().substring(0, 6).toLowerCase());
  }

}
