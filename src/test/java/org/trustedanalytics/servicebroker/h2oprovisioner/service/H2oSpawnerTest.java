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

import com.google.common.collect.ImmutableMap;
import org.apache.hadoop.conf.Configuration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.trustedanalytics.servicebroker.h2oprovisioner.config.ExternalConfiguration;
import org.trustedanalytics.servicebroker.h2oprovisioner.credentials.CredentialsSupplier;
import org.trustedanalytics.servicebroker.h2oprovisioner.ports.PortsPool;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.api.H2oCredentials;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.H2oSpawnerException;
import org.trustedanalytics.servicebroker.h2oprovisioner.service.externals.H2oDriverExec;
import org.trustedanalytics.servicebroker.h2oprovisioner.service.externals.H2oUiFileParser;
import org.trustedanalytics.servicebroker.h2oprovisioner.service.externals.KinitExec;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class H2oSpawnerTest {

  private static final String DRIVER_JAR_PATH = "driver.jar";
  private static final String DRIVER_IP = "driverIp";
  private static final int DRIVER_CALLBACK_PORT = 1234;
  private static final String H2O_MEMORY = "h2oMemory";
  private static final String H2O_NODES = "h2oNodes";
  private static final Map<String, String> YARN_CONF =
      ImmutableMap.of("key1", "value1", "key2", "value2");
  private static final String H2O_USER = "h2oUser";
  private static final String H2O_PASSWORD = "h2oP4$s";
  private static final String INSTANCE_ID = "instanceId";

  private H2oSpawner h2oSpawner;

  @Mock
  public PortsPool portsPool;

  @Mock
  public CredentialsSupplier usernameSupplier;

  @Mock
  public CredentialsSupplier passwordSupplier;

  @Mock
  public KinitExec kinitExec;

  @Mock
  public H2oDriverExec h2oDriverExec;

  @Mock
  public H2oUiFileParser h2oUiFileParser;

  @Before
  public void setup() throws IOException {
    ExternalConfiguration config = new ExternalConfiguration();
    config.setH2oDriverJarpath(DRIVER_JAR_PATH);
    config.setH2oDriverIp(DRIVER_IP);

    when(portsPool.getPort()).thenReturn(DRIVER_CALLBACK_PORT);
    when(usernameSupplier.get()).thenReturn(H2O_USER);
    when(passwordSupplier.get()).thenReturn(H2O_PASSWORD);

    h2oSpawner = new H2oSpawner(config, portsPool, usernameSupplier, passwordSupplier, kinitExec,
        h2oDriverExec, h2oUiFileParser);
  }

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Test
  public void provisionInstance_kinitFails_exceptionThrown() throws Exception {
    // arrange
    expectedException.expect(H2oSpawnerException.class);
    expectedException.expectMessage("Unable to provision h2o for: " + INSTANCE_ID);
    doThrow(new IOException()).when(kinitExec).loginToKerberos();

    // act
    h2oSpawner.provisionInstance(INSTANCE_ID, H2O_MEMORY, H2O_NODES, true, YARN_CONF);

    // assert
    verifyKinitCalled();
  }

  @Test
  public void provisionInstance_spawnFails_kinitCalledExceptionThrown() throws Exception {
    // arrange
    expectedException.expect(H2oSpawnerException.class);
    expectedException.expectMessage("Unable to provision h2o for: " + INSTANCE_ID);
    doThrow(new IOException()).when(h2oDriverExec).spawnH2oOnYarn(h2oDriverArgs(),
        hadoopConf(YARN_CONF));

    // act
    h2oSpawner.provisionInstance(INSTANCE_ID, H2O_MEMORY, H2O_NODES, true, YARN_CONF);

    // assert
    verifyKinitCalled();
    verifyDriverCalled();
  }

  @Test
  public void provisionInstance_uiFileParserFails_allExternalsCalledExceptionThrown()
      throws Exception {

    // arrange
    expectedException.expect(H2oSpawnerException.class);
    expectedException.expectMessage("Unable to provision h2o for: " + INSTANCE_ID);
    doThrow(new FileNotFoundException()).when(h2oUiFileParser).getFlowUrl("h2o_ui_" + INSTANCE_ID);

    // act
    h2oSpawner.provisionInstance(INSTANCE_ID, H2O_MEMORY, H2O_NODES, true, YARN_CONF);

    // assert
    verifyKinitCalled();
    verifyDriverCalled();
    verifyUiFileParserCalled();
  }

  @Test
  public void provisionInstance_everythingWorks_allExternalsCalledH2oUrlReturned()
      throws Exception {

    // arrange
    when(h2oUiFileParser.getFlowUrl("h2o_ui_" + INSTANCE_ID)).thenReturn("127.0.0.1:54321");

    // act
    H2oCredentials actualH2oCredentials =
        h2oSpawner.provisionInstance(INSTANCE_ID, H2O_MEMORY, H2O_NODES, true, YARN_CONF);

    // assert
    assertThat(actualH2oCredentials.getHostname(), equalTo("127.0.0.1"));
    assertThat(actualH2oCredentials.getPort(), equalTo("54321"));
    assertThat(actualH2oCredentials.getUsername(), equalTo(H2O_USER));
    assertThat(actualH2oCredentials.getPassword(), equalTo(H2O_PASSWORD));
    verifyKinitCalled();
    verifyDriverCalled();
    verifyUiFileParserCalled();
  }

  @Test
  public void provisionInstanceKrbFalse_everythingWorks_allExternalsWithoutKinitCalledH2oUrlReturned()
      throws Exception {

    // arrange
    when(h2oUiFileParser.getFlowUrl("h2o_ui_" + INSTANCE_ID)).thenReturn("127.0.0.1:54321");

    // act
    H2oCredentials actualH2oCredentials =
        h2oSpawner.provisionInstance(INSTANCE_ID, H2O_MEMORY, H2O_NODES, false, YARN_CONF);

    // assert
    assertThat(actualH2oCredentials.getHostname(), equalTo("127.0.0.1"));
    assertThat(actualH2oCredentials.getPort(), equalTo("54321"));
    assertThat(actualH2oCredentials.getUsername(), equalTo(H2O_USER));
    assertThat(actualH2oCredentials.getPassword(), equalTo(H2O_PASSWORD));
    verifyKinitNotCalled();
    verifyDriverCalled();
    verifyUiFileParserCalled();
  }

  private void verifyKinitCalled() throws Exception {
    verify(kinitExec, times(1)).loginToKerberos();
  }

  private void verifyKinitNotCalled() {
    verifyNoMoreInteractions(kinitExec);
  }

  private void verifyDriverCalled() throws Exception {
    ArgumentCaptor<Configuration> hadoopConfCaptor = ArgumentCaptor.forClass(Configuration.class);
    verify(h2oDriverExec, times(1)).spawnH2oOnYarn(eq(h2oDriverArgs()), hadoopConfCaptor.capture());
    Configuration yarnConf = hadoopConfCaptor.getValue();
    assertThat(yarnConf.get("key1"), equalTo("value1"));
    assertThat(yarnConf.get("key2"), equalTo("value2"));
  }

  private void verifyUiFileParserCalled() throws IOException {
    verify(h2oUiFileParser, times(1)).getFlowUrl("h2o_ui_" + INSTANCE_ID);
  }

  private String[] h2oDriverArgs() {
    return new String[] {
        // @formatter:off
        "hadoop", "jar", DRIVER_JAR_PATH, "-driverif", DRIVER_IP, "-driverport",
        String.valueOf(DRIVER_CALLBACK_PORT), "-mapperXmx", H2O_MEMORY, "-nodes", H2O_NODES,
        "-output", "/tmp/h2o/" + INSTANCE_ID, "-jobname", "H2O_BROKER_" + INSTANCE_ID, "-notify",
        "h2o_ui_" + INSTANCE_ID, "-username", H2O_USER, "-password", H2O_PASSWORD, "-disown",
        // @formatter:on
    };
  }

  private Configuration hadoopConf(Map<String, String> yarnConf) {
    Configuration hadoopConf = new Configuration(false);
    yarnConf.forEach(hadoopConf::set);
    return hadoopConf;
  }
}
