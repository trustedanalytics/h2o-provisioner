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

package org.trustedanalytics.servicebroker.h2oprovisioner.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.trustedanalytics.servicebroker.h2oprovisioner.credentials.CredentialsSupplier;
import org.trustedanalytics.servicebroker.h2oprovisioner.credentials.RandomAlphanumericCredentialsSupplier;
import org.trustedanalytics.servicebroker.h2oprovisioner.ports.PortsPool;
import org.trustedanalytics.servicebroker.h2oprovisioner.service.H2oSpawner;
import org.trustedanalytics.servicebroker.h2oprovisioner.service.externals.H2oDriverExec;
import org.trustedanalytics.servicebroker.h2oprovisioner.service.externals.H2oUiFileParser;
import org.trustedanalytics.servicebroker.h2oprovisioner.service.externals.KinitExec;

import java.io.IOException;

@Configuration
public class H2oSpawnerConfig {

  @Autowired
  private ExternalConfiguration config;

  @Autowired
  private KerberosProperties kerberosProperties;

  @Autowired
  private PortsPool portsPool;

  @Bean
  public H2oSpawner getH2oSpawner(CredentialsSupplier usernameSupplier,
      CredentialsSupplier passwordSupplier, KinitExec kinitExec, H2oDriverExec h2oDriverExec,
      H2oUiFileParser h2oUiFileParser) {

    return new H2oSpawner(config, portsPool, usernameSupplier, passwordSupplier, kinitExec,
        h2oDriverExec, h2oUiFileParser);
  }

  @Bean
  @Profile({"cloud", "default"})
  public CredentialsSupplier usernameSupplier() {
    return new RandomAlphanumericCredentialsSupplier(
        Integer.parseInt(config.getH2oUsernameLength()));
  }

  @Bean
  @Profile({"cloud", "default"})
  public CredentialsSupplier passwordSupplier() {
    return new RandomAlphanumericCredentialsSupplier(
        Integer.parseInt(config.getH2oPasswordLength()));
  }

  @Bean
  @Profile({"cloud", "default"})
  public KinitExec kinitExec() throws IOException {
    return new KinitExec(kerberosProperties);
  }

  @Bean
  @Profile({"cloud", "default"})
  public H2oDriverExec h2oDriverExec() {
    return new H2oDriverExec(config.getYarnConfDir());
  }

  @Bean
  @Profile({"cloud", "default"})
  public H2oUiFileParser h2oUiFileParser() {
    return new H2oUiFileParser();
  }
}
