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

import org.trustedanalytics.servicebroker.h2oprovisioner.config.ExternalConfiguration;
import org.trustedanalytics.servicebroker.h2oprovisioner.credentials.CredentialsSupplier;
import org.trustedanalytics.servicebroker.h2oprovisioner.ports.PortsPool;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.H2oSpawnerException;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.api.H2oCredentials;
import org.trustedanalytics.servicebroker.h2oprovisioner.service.externals.H2oDriverExec;
import org.trustedanalytics.servicebroker.h2oprovisioner.service.externals.H2oUiFileParser;
import org.trustedanalytics.servicebroker.h2oprovisioner.service.externals.KinitExec;

import org.apache.hadoop.conf.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

public class H2oSpawner {

  private static final Logger LOGGER = LoggerFactory.getLogger(H2oSpawner.class);

  private final ExternalConfiguration externalConfiguration;
  private final PortsPool portsPool;
  private final CredentialsSupplier usernameSupplier;
  private final CredentialsSupplier passwordSupplier;
  private final KinitExec kinit;
  private final H2oDriverExec h2oDriver;
  private final H2oUiFileParser h2oUiFileParser;

  public H2oSpawner(ExternalConfiguration externalConfiguration, PortsPool portsPool,
      CredentialsSupplier usernameSupplier, CredentialsSupplier passwordSupplier, KinitExec kinit,
      H2oDriverExec h2oDriver, H2oUiFileParser h2oUiFileParser) {

    this.externalConfiguration = externalConfiguration;
    this.portsPool = portsPool;
    this.usernameSupplier = usernameSupplier;
    this.passwordSupplier = passwordSupplier;
    this.kinit = kinit;
    this.h2oDriver = h2oDriver;
    this.h2oUiFileParser = h2oUiFileParser;
  }

  public H2oCredentials provisionInstance(String serviceInstanceId, String memory,
      String nodesCount, boolean kerberos, Map<String, String> hadoopConfiguration)
      throws H2oSpawnerException {

    LOGGER.info("Trying to provision h2o for: " + serviceInstanceId);
    String user = usernameSupplier.get();
    String password = passwordSupplier.get();

    try {
      String[] command = getH2oDriverCommand(serviceInstanceId, user, password, memory, nodesCount);
      LOGGER.info("with such command: " + Arrays.toString(command));

      if (kerberos) {
        kinit.loginToKerberos();
      }

      Configuration hadoopConf = new Configuration(false);
      hadoopConfiguration.forEach(hadoopConf::set);
      h2oDriver.spawnH2oOnYarn(command, hadoopConf);

      // TODO: what if exception will be thrown by getFlowUrl?
      // should we kill h2o on yarn = undo step: spawnH2oOnYarn?

      String host =
          Objects.requireNonNull(h2oUiFileParser.getFlowUrl(h2oUiPath(serviceInstanceId)));

      return new H2oCredentials(getAddress(host), getPort(host), user, password);

    } catch (Exception e) {
      throw new H2oSpawnerException(errorMsg(serviceInstanceId), e);
    }
  }

  private String[] getH2oDriverCommand(String serviceInstanceId, String user, String password,
      String memory, String nodesCount) throws IOException {

    return new String[] {"hadoop", "jar", externalConfiguration.getH2oDriverJarpath(), "-driverif",
        externalConfiguration.getH2oDriverIp(), "-driverport", String.valueOf(portsPool.getPort()),
        "-mapperXmx", memory, "-nodes", nodesCount, "-output", outputPath(serviceInstanceId),
        "-jobname", jobName(serviceInstanceId), "-notify", h2oUiPath(serviceInstanceId),
        "-username", user, "-password", password, "-disown",};
  }

  private String outputPath(String serviceInstanceId) {
    return "/tmp/h2o/" + serviceInstanceId;
  }

  private String jobName(String serviceInstanceId) {
    return "H2O_BROKER_" + serviceInstanceId;
  }

  private String h2oUiPath(String serviceInstanceId) {
    return "h2o_ui_" + serviceInstanceId;
  }

  private String getAddress(String host) {
    return host.split(":")[0];
  }

  private String getPort(String host) {
    return host.split(":")[1];
  }

  private String errorMsg(String serviceInstanceId) {
    return "Unable to provision h2o for: " + serviceInstanceId;
  }
}
