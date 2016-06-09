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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;

import java.util.Map;

public class H2oProvisionerRestClient implements H2oProvisionerRestApi {

  private static final Logger LOGGER = LoggerFactory.getLogger(H2oProvisionerRestClient.class);

  private final String baseUrl;
  private final RestOperations rest;

  public H2oProvisionerRestClient(String baseUrl, RestOperations rest) {
    this.baseUrl = baseUrl;
    this.rest = rest;
  }

  @Override
  public String prepareUrl(String serviceInstanceId, String nodesCount, String memory,
      boolean kerberos) {
    return String.format("%s/rest/instances/%s/create?nodesCount=%s&memory=%s&kerberos=%s", baseUrl,
        serviceInstanceId, nodesCount, memory, kerberos ? "on" : "off");
  }

  @Override
  public ResponseEntity<H2oCredentials> createH2oInstance(String serviceInstanceId,
      String nodesCount, String memory, boolean kerberos, Map<String, String> yarnConf) {
    String url = prepareUrl(serviceInstanceId, nodesCount, memory, kerberos);
    LOGGER.info("calling provisioner with url '" + url + "'");
    return rest.postForEntity(url, yarnConf, H2oCredentials.class);
  }

  @Override
  public ResponseEntity<String> deleteH2oInstance(String serviceInstanceId,
      Map<String, String> yarnConf) {
    String url = String.format("%s/rest/instances/%s/delete", baseUrl, serviceInstanceId);
    LOGGER.info("calling provisioner with url '" + url + "'");

    return rest.postForEntity(url, yarnConf, String.class);
  }
}
