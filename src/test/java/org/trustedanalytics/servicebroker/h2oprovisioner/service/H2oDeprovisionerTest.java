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
package org.trustedanalytics.servicebroker.h2oprovisioner.service;


import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.trustedanalytics.servicebroker.h2oprovisioner.cdhclients.DeprovisionerYarnClient;
import org.trustedanalytics.servicebroker.h2oprovisioner.cdhclients.DeprovisionerYarnClientProvider;
import org.trustedanalytics.servicebroker.h2oprovisioner.cdhclients.KerberosClient;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.H2oDeprovisioningException;

public class H2oDeprovisionerTest {

  private DeprovisionerYarnClientProvider yarnClientProviderMock =
      mock(DeprovisionerYarnClientProvider.class);
  private DeprovisionerYarnClient yarnClientMock = mock(DeprovisionerYarnClient.class);
  private KerberosClient kerberosClientMock = mock(KerberosClient.class);
  private final String kerberosUser = "askfap";
  private Map<String, String> testHadoopConf = new HashMap<>();
  private Configuration expectedHadoopConf;
  private final String testInstanceId = "sljad-akjdf";
  private ApplicationId applicationIdMock = mock(ApplicationId.class);
  private final String testConfigKey1 = "key1";
  private final String testConfigKey2 = "key2";
  private final String testConfigValue1 = "value1";
  private final String testConfigValue2 = "value2";

  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    testHadoopConf.put(testConfigKey1, testConfigValue1);
    testHadoopConf.put(testConfigKey2, testConfigValue2);

    expectedHadoopConf = new Configuration(false);
    testHadoopConf.forEach(expectedHadoopConf::set);

    when(yarnClientMock.getH2oJobId(testInstanceId)).thenReturn(applicationIdMock);

    when(yarnClientProviderMock.getClient(any(), any())).thenReturn(yarnClientMock);

    when(kerberosClientMock.logInToKerberos(any())).thenReturn(expectedHadoopConf);
  }

  @Test
  public void deprovisionInstanceForKrb_EverythingWorks_AllExternalsCalled() throws Exception {
    // given
    H2oDeprovisioner sut =
        new H2oDeprovisioner(kerberosUser, kerberosClientMock, yarnClientProviderMock);

    // when
    String killedJobId = sut.deprovisionInstance(testInstanceId, testHadoopConf, true);

    // then
    ArgumentCaptor<Configuration> hadoopConfCaptor = ArgumentCaptor.forClass(Configuration.class);
    verify(kerberosClientMock).logInToKerberos(hadoopConfCaptor.capture());
    assertEquals(testConfigValue1, hadoopConfCaptor.getValue().get(testConfigKey1));
    assertEquals(testConfigValue2, hadoopConfCaptor.getValue().get(testConfigKey2));
    verify(yarnClientProviderMock).getClient(kerberosUser, expectedHadoopConf);
    verify(yarnClientMock).start();
    verify(yarnClientMock).getH2oJobId(testInstanceId);
    verify(yarnClientMock).killApplication(applicationIdMock);
    assertEquals(applicationIdMock.toString(), killedJobId);
  }

  @Test
  public void deprovisionInstanceForKrb_KerberosClientThrowsLoginException_ExceptionThrown()
      throws Exception {
    // given
    when(kerberosClientMock.logInToKerberos(any())).thenThrow(new LoginException());
    H2oDeprovisioner sut =
        new H2oDeprovisioner(kerberosUser, kerberosClientMock, yarnClientProviderMock);

    // when
    //then
    thrown.expect(H2oDeprovisioningException.class);
    sut.deprovisionInstance(testInstanceId, testHadoopConf, true);
  }
  
  @Test
  public void deprovisionInstanceForKrb_KerberosClientThrowsIOException_ExceptionThrown()
      throws Exception {
    // given
    when(kerberosClientMock.logInToKerberos(any())).thenThrow(new IOException());
    H2oDeprovisioner sut =
        new H2oDeprovisioner(kerberosUser, kerberosClientMock, yarnClientProviderMock);

    // when
    //then
    thrown.expect(H2oDeprovisioningException.class);
    sut.deprovisionInstance(testInstanceId, testHadoopConf, true);
  }
  
  @Test
  public void deprovisionInstanceForKrb_YarnClientProviderThrowsIOException_ExceptionThrown()
      throws Exception {
    // given
    when(yarnClientProviderMock.getClient(kerberosUser, expectedHadoopConf)).thenThrow(new IOException());
    H2oDeprovisioner sut =
        new H2oDeprovisioner(kerberosUser, kerberosClientMock, yarnClientProviderMock);

    // when
    //then
    thrown.expect(H2oDeprovisioningException.class);
    sut.deprovisionInstance(testInstanceId, testHadoopConf, true);
  }

  
  @Test
  public void deprovisionInstanceForKrb_YarnClientWhenGettingJobIdThrowsYarnException_ExceptionThrown()
      throws Exception {
    // given
    when(yarnClientMock.getH2oJobId(testInstanceId)).thenThrow(new YarnException());
    H2oDeprovisioner sut =
        new H2oDeprovisioner(kerberosUser, kerberosClientMock, yarnClientProviderMock);

    // when
    //then
    thrown.expect(H2oDeprovisioningException.class);
    sut.deprovisionInstance(testInstanceId, testHadoopConf, true);
  }
  
  @Test
  public void deprovisionInstanceForKrb_YarnClientWhenKillingJobThrowsYarnException_ExceptionThrown()
      throws Exception {
    // given
    doThrow(new YarnException()).when(yarnClientMock).killApplication(applicationIdMock);
    H2oDeprovisioner sut =
        new H2oDeprovisioner(kerberosUser, kerberosClientMock, yarnClientProviderMock);

    // when
    //then
    thrown.expect(H2oDeprovisioningException.class);
    sut.deprovisionInstance(testInstanceId, testHadoopConf, true);
  }
  
  @Test
  public void deprovisionInstanceForKrb_YarnClientWhenKillingJobThrowsIOException_ExceptionThrown()
      throws Exception {
    // given
    doThrow(new IOException()).when(yarnClientMock).killApplication(applicationIdMock);
    H2oDeprovisioner sut =
        new H2oDeprovisioner(kerberosUser, kerberosClientMock, yarnClientProviderMock);

    // when
    //then
    thrown.expect(H2oDeprovisioningException.class);
    sut.deprovisionInstance(testInstanceId, testHadoopConf, true);
  }
}
