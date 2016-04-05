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
import org.trustedanalytics.servicebroker.h2oprovisioner.config.KerberosProperties;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.util.MatcherAssertionErrors.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class KinitExecTest {

  @Test
  public void fillKrbConfTemplate_templateAsInput_returnsFilledTemplate() {
    // arrange
    final String INPUT_FILE = "\"<kdcPLACEHOLDER>\"\n" + "\"<realmPLACEHOLDER>\"\n"
        + "\"some line\"\n" + "\"<kdcPLACEHOLDER>\"\n";

    final String EXPECTED_OUTPUT_FILE =
        "\"kdc\"\n" + "\"realm\"\n" + "\"some line\"\n" + "\"kdc\"\n";

    KerberosProperties kerberosProperties = new KerberosProperties();
    kerberosProperties.setKdc("kdc");
    kerberosProperties.setRealm("realm");

    // act
    String actualOutputFile = KinitExec.fillKrbConfTemplate(INPUT_FILE, kerberosProperties);

    // assert
    assertThat(actualOutputFile, equalTo(EXPECTED_OUTPUT_FILE));
  }
}
