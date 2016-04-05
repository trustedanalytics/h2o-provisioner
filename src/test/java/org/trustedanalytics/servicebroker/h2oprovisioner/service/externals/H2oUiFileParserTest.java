/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.trustedanalytics.servicebroker.h2oprovisioner.service.externals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.MatcherAssertionErrors.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class H2oUiFileParserTest {

  private BufferedReader FAKE_H2O_FILE = new BufferedReader(new StringReader(
      // @formatter:off
      "first line\n" + "second line\n" + "third line\n"
  // @formatter:on
  ));

  @Test
  public void getFlowUrl_threeLinesFile_returnsFirstLine() throws IOException {
    H2oUiFileParser h2oUiFileParser = new H2oUiFileParser();
    String actualUrl = h2oUiFileParser.getFlowUrl(FAKE_H2O_FILE);
    assertThat(actualUrl, equalTo("first line"));
  }

  @Test(expected = IOException.class)
  public void getFlowUrl_fileThrowsException_exceptionThrown() throws IOException {
    BufferedReader file = mock(BufferedReader.class);
    when(file.readLine()).thenThrow(new IOException());

    H2oUiFileParser h2oUiFileParser = new H2oUiFileParser();
    h2oUiFileParser.getFlowUrl(file);
  }
}
