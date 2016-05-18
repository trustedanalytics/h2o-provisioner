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
package org.trustedanalytics.servicebroker.h2oprovisioner.integration;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestOperations;
import org.trustedanalytics.servicebroker.h2oprovisioner.Application;
import org.trustedanalytics.servicebroker.h2oprovisioner.cdhclients.DeprovisionerYarnClient;
import org.trustedanalytics.servicebroker.h2oprovisioner.cdhclients.DeprovisionerYarnClientProvider;
import org.trustedanalytics.servicebroker.h2oprovisioner.cdhclients.KerberosClient;

import com.google.common.collect.ImmutableMap;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class, TestConfig.class})
@WebAppConfiguration
@IntegrationTest
@ActiveProfiles("test")
public class H2oDeprovisionerIntegrationTest {

  @Value("http://localhost:${local.server.port}")
  private String baseUrl;

  private RestOperations rest = new TestRestTemplate();

  @Autowired
  public KerberosClient kerberosClient;

  @Autowired
  public DeprovisionerYarnClientProvider yarnClientProvider;

  private final Map<String, String> YARN_CONF = ImmutableMap.of("key1", "value1", "key2", "value2");
  private final String INSTANCE_ID = "instanceId0";
  private DeprovisionerYarnClient yarnClient = mock(DeprovisionerYarnClient.class);
  private ApplicationId applicationIdMock = mock(ApplicationId.class);
  private Configuration hadoopConf = new Configuration(false);

  @Before
  public void setUp() throws IOException, YarnException {
    YARN_CONF.forEach(hadoopConf::set);
  }

  @Test
  public void deleteServiceInstance_success_allSubsequentCallsDone() throws Exception {
    // given
    when(yarnClientProvider.getClient(any(), any())).thenReturn(yarnClient);
    when(yarnClient.getH2oJobId(INSTANCE_ID)).thenReturn(applicationIdMock);
    when(kerberosClient.logInToKerberos(any())).thenReturn(hadoopConf);

    // when
    ResponseEntity<String> entity = rest.postForEntity(
        baseUrl + "/rest/instances/" + INSTANCE_ID + "/delete", YARN_CONF, String.class);

    // then
    ArgumentCaptor<Configuration> hadoopConfCaptor = ArgumentCaptor.forClass(Configuration.class);
    verify(kerberosClient).logInToKerberos(hadoopConfCaptor.capture());
    assertEquals("value1", hadoopConfCaptor.getValue().get("key1"));
    assertEquals("value2", hadoopConfCaptor.getValue().get("key2"));

    verify(yarnClient).start();
    verify(yarnClient).getH2oJobId(INSTANCE_ID);
    verify(yarnClient).killApplication(applicationIdMock);
    
    assertEquals(applicationIdMock.toString(), entity.getBody());
  }
}
