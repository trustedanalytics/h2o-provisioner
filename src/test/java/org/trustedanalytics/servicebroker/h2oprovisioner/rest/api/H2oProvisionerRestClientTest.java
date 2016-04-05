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

package org.trustedanalytics.servicebroker.h2oprovisioner.rest.api;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class H2oProvisionerRestClientTest {

  private static final String BASE_URL = "http://baseUrl.com";

  private static final String EFFECTIVE_URL_BASE =
      "http://baseUrl.com/rest/instances/serviceInstanceId/create?nodesCount=2&memory=512m&kerberos=";
  private static final String EFFECTIVE_URL_KRB_ON = EFFECTIVE_URL_BASE + "on";
  private static final String EFFECTIVE_URL_KRB_OFF = EFFECTIVE_URL_BASE + "off";

  private static final ImmutableMap<String, String> YARN_CONF =
      ImmutableMap.of("key1", "value1", "key2", "value2");

  private static final H2oCredentials H2O_CREDENTIALS = new H2oCredentials("a", "b", "c", "d");

  @Mock
  private RestOperations restOperations;

  private H2oProvisionerRestApi h2oRest;

  @Before
  public void setup() {
    h2oRest = new H2oProvisionerRestClient(BASE_URL, restOperations);
  }

  @Test
  public void prepareUrl_parametersGivenKrbTrue_urlGenerated() {
    String createH2oUrl = h2oRest.prepareUrl("serviceInstanceId", "2", "512m", true);
    assertThat(createH2oUrl, equalTo(EFFECTIVE_URL_KRB_ON));
  }

  @Test
  public void prepareUrl_parametersGivenKrbFalse_urlGenerated() {
    String createH2oUrl = h2oRest.prepareUrl("serviceInstanceId", "2", "512m", false);
    assertThat(createH2oUrl, equalTo(EFFECTIVE_URL_KRB_OFF));
  }

  @Test
  public void createH2oInstance_restReturnedResponse_responsePassed() {
    // arrange
    when(restOperations.postForEntity(EFFECTIVE_URL_KRB_OFF, YARN_CONF, H2oCredentials.class))
        .thenReturn(new ResponseEntity<>(H2O_CREDENTIALS, HttpStatus.OK));

    // act
    ResponseEntity<H2oCredentials> h2oInstanceEntity =
        h2oRest.createH2oInstance("serviceInstanceId", "2", "512m", false, YARN_CONF);

    // assert
    assertThat(h2oInstanceEntity.getStatusCode(), equalTo(HttpStatus.OK));
    assertThat(h2oInstanceEntity.getBody(), equalTo(H2O_CREDENTIALS));
    verify(restOperations, times(1)).postForEntity(EFFECTIVE_URL_KRB_OFF, YARN_CONF,
        H2oCredentials.class);
  }
}
