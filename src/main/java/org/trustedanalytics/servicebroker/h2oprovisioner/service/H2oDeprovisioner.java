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

import java.io.IOException;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.exceptions.ApplicationNotFoundException;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.trustedanalytics.servicebroker.h2oprovisioner.cdhclients.DeprovisionerYarnClient;
import org.trustedanalytics.servicebroker.h2oprovisioner.cdhclients.DeprovisionerYarnClientProvider;
import org.trustedanalytics.servicebroker.h2oprovisioner.cdhclients.KerberosClient;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.H2oDeprovisioningException;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.JobNotFoundException;

public class H2oDeprovisioner {

  private static final Logger LOGGER = LoggerFactory.getLogger(H2oDeprovisioner.class);

  private final String kerberosUser;
  private final KerberosClient kerberos;
  private final DeprovisionerYarnClientProvider yarnClientProvider;

  public H2oDeprovisioner(String kerberosUser, KerberosClient kerberos,
      DeprovisionerYarnClientProvider yarnClientProvider) {
    this.kerberos = kerberos;
    this.yarnClientProvider = yarnClientProvider;
    this.kerberosUser = kerberosUser;
  }


  public String deprovisionInstance(String serviceInstanceId,
      Map<String, String> hadoopConfiguration, boolean kerberosOn)
          throws H2oDeprovisioningException, JobNotFoundException {
    LOGGER.debug("Reading hadoop configuration...");
    Configuration hadoopConf = new Configuration(false);
    hadoopConfiguration.forEach(hadoopConf::set);
    LOGGER.debug("Configuration read.");

    try {
      DeprovisionerYarnClient yarnClient;
      LOGGER.debug("Creating yarn client...");
      if (kerberosOn) {
        Configuration loggedHadoopConf = logInAndGetConfig(hadoopConf);
        yarnClient = yarnClientProvider.getClient(kerberosUser, loggedHadoopConf);
      } else {
        yarnClient = yarnClientProvider.getClient(kerberosUser, hadoopConf);
      }
      LOGGER.debug("Yarn client created.");

      return deprovisionH2o(yarnClient, serviceInstanceId);
      
    } catch (IOException e) {
      throw new H2oDeprovisioningException(
          "Unable to create yarn client." + e.getMessage(), e);
    }
  }

  private Configuration logInAndGetConfig(Configuration hadoopConf)
      throws H2oDeprovisioningException {
    LOGGER.debug("Logging in to Kerberos...");
    try {
      return kerberos.logInToKerberos(hadoopConf);
    } catch (LoginException | IOException e) {
      throw new H2oDeprovisioningException("Unable to log in: " + e.getMessage(), e);
    }
  }

  private String deprovisionH2o(DeprovisionerYarnClient yarnClient, String serviceInstanceId)
          throws H2oDeprovisioningException, JobNotFoundException {
    try {
      LOGGER.debug("Starting yarn client...");
      yarnClient.start();
      LOGGER.debug("Yarn client started.");

      LOGGER.debug("Extracting job Id...");
      ApplicationId h2oServerJobId = yarnClient.getH2oJobId(serviceInstanceId);
      LOGGER.debug("Extracted job id: " + h2oServerJobId.toString());
      LOGGER.debug("Killing job with id: " + h2oServerJobId.toString());
      yarnClient.killApplication(h2oServerJobId);
      LOGGER.debug("Job " + h2oServerJobId + " killed.");
      return h2oServerJobId.toString();
    } catch (YarnException | IOException e) {
      throw new H2oDeprovisioningException("Unable to deprovision H2O " + e.getMessage(), e);
    }
  }
}
