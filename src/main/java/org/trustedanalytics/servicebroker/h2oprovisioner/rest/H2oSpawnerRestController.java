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

package org.trustedanalytics.servicebroker.h2oprovisioner.rest;

import org.trustedanalytics.servicebroker.h2oprovisioner.rest.api.H2oCredentials;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.api.H2oProvisionerRequestData;
import org.trustedanalytics.servicebroker.h2oprovisioner.service.H2oDeprovisioner;
import org.trustedanalytics.servicebroker.h2oprovisioner.service.H2oSpawner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class H2oSpawnerRestController {

  private static final Logger LOGGER = LoggerFactory.getLogger(H2oSpawnerRestController.class);

  @Autowired
  private H2oSpawner h2oSpawner;

  @Autowired
  private H2oDeprovisioner h2oDeprovisioner;

  @RequestMapping(value = "/rest/instances/{instanceId}/create", method = RequestMethod.POST)
  public H2oCredentials provisionH2o(@PathVariable String instanceId,
      @RequestParam String nodesCount, @RequestParam String memory,
      @RequestParam(required = false, defaultValue = "on") String kerberos,
      @RequestBody H2oProvisionerRequestData parameters) throws H2oSpawnerException {

    //Make use of this parameter while doing task DPNG-6358
    LOGGER.debug("User token passed: " + parameters.getUserToken());

    return h2oSpawner.provisionInstance(instanceId, memory, nodesCount, "on".equals(kerberos),
        parameters.getYarnConfig());
  }

  @RequestMapping(value = "rest/instances/{instanceId}/delete", method = RequestMethod.POST)
  public String deprovisionH2o(@PathVariable String instanceId,
      @RequestBody Map<String, String> hadoopConf,
      @RequestParam(required = false, defaultValue = "on") String kerberos)
      throws H2oDeprovisioningException, JobNotFoundException {
    return h2oDeprovisioner.deprovisionInstance(instanceId, hadoopConf, "on".equals(kerberos));
  }
}
