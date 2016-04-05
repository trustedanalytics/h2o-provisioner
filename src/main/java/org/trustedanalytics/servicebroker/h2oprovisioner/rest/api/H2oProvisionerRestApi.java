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

import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface H2oProvisionerRestApi {
  /**
   * Generates url to REST method of H2O-provisioner. It can be used in any REST client.
   * 
   * @param serviceInstanceId service instance unique id
   * @param nodesCount number of H2O nodes to be spawned
   * @param memory amount of memory for a single H2O node, e.g. 256m, 1g
   * @param kerberos true if kerberos authentication should be performed
   * @return url to REST method of H2O-provisioner with given parameters
   */
  String prepareUrl(String serviceInstanceId, String nodesCount, String memory, boolean kerberos);

  /**
   * Returns credentials required to connect with H2O instance wrapped with ResponseEntity
   * 
   * @param serviceInstanceId service instance unique id
   * @param nodesCount number of H2O nodes to be spawned
   * @param memory amount of memory for a single H2O node, e.g. 256m, 1g
   * @param yarnConf YARN configuration map
   * @param kerberos true if kerberos authentication should be performed
   * @return credentials required to connect with H2O instance
   */
  ResponseEntity<H2oCredentials> createH2oInstance(String serviceInstanceId, String nodesCount,
      String memory, boolean kerberos, Map<String, String> yarnConf);
}
