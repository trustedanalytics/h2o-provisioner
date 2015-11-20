/**
 * Copyright (c) 2015 Intel Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.trustedanalytics.servicebroker.h2oprovisioner.integration;

import com.google.common.collect.ImmutableMap;
import org.apache.hadoop.conf.Configuration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestOperations;
import org.trustedanalytics.servicebroker.h2oprovisioner.Application;
import org.trustedanalytics.servicebroker.h2oprovisioner.config.ExternalConfiguration;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.api.H2oCredentials;
import org.trustedanalytics.servicebroker.h2oprovisioner.service.externals.H2oDriverExec;
import org.trustedanalytics.servicebroker.h2oprovisioner.service.externals.H2oUiFileParser;
import org.trustedanalytics.servicebroker.h2oprovisioner.service.externals.KinitExec;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.MatcherAssertionErrors.assertThat;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {Application.class, TestConfig.class})
@WebAppConfiguration
@IntegrationTest
@ActiveProfiles("test")
public class H2oProvisionerIntegrationTest {

    @Value("http://localhost:${local.server.port}")
    private String baseUrl;

    private RestOperations rest = new TestRestTemplate();

    @Autowired
    private ExternalConfiguration conf;

    @Autowired
    public KinitExec kinitExec;

    @Autowired
    public H2oDriverExec h2oDriverExec;

    @Autowired
    public H2oUiFileParser h2oUiFileParser;

    @Test
    public void testCreateServiceInstance_success_shouldReturnCreatedInstance() throws Exception {
        //arrange
        final String INSTANCE_ID = "instanceId0";
        final String MEMORY = "256m";
        final String NODES_COUNT = "4";
        final Map<String, String> YARN_CONF = ImmutableMap.of("key1", "value1", "key2", "value2");
        when(h2oUiFileParser.getFlowUrl("h2o_ui_" + INSTANCE_ID)).thenReturn("qwerty.com:80");

        //act
        ResponseEntity<H2oCredentials> h2oCredentialsEntity =
            rest.postForEntity(
                baseUrl + "/rest/instances/" + INSTANCE_ID + "/create?nodesCount=" + NODES_COUNT
                    + "&memory=" + MEMORY,
                YARN_CONF,
                H2oCredentials.class);

        //assert
        assertThat(h2oCredentialsEntity.getStatusCode(),
            equalTo(HttpStatus.OK));
        assertThat(h2oCredentialsEntity.getBody().getHostname(),
            equalTo("qwerty.com"));
        assertThat(h2oCredentialsEntity.getBody().getPort(),
            equalTo("80"));
        assertThat(h2oCredentialsEntity.getBody().getUsername(),
            equalTo(TestConfig.FAKE_H2O_INSTANCE_USERNAME));
        assertThat(h2oCredentialsEntity.getBody().getPassword(),
            equalTo(TestConfig.FAKE_H2O_INSTANCE_PASSWORD));

        verify(kinitExec, times(1)).loginToKerberos();

        ArgumentCaptor<Configuration> hadoopConfCaptor =
            ArgumentCaptor.forClass(Configuration.class);
        verify(h2oDriverExec, times(1))
            .spawnH2oOnYarn(eq(new String[] {
                "hadoop",
                "jar",
                conf.getH2oDriverJarpath(),
                "-driverif", conf.getH2oDriverIp(),
                "-driverport", String.valueOf(TestConfig.FAKE_DRIVER_CALLBACK_PORT),
                "-mapperXmx", MEMORY,
                "-nodes", NODES_COUNT,
                "-output", "/tmp/h2o/" + INSTANCE_ID,
                "-jobname", "H2O_BROKER_" + INSTANCE_ID,
                "-notify", "h2o_ui_" + INSTANCE_ID,
                "-username", TestConfig.FAKE_H2O_INSTANCE_USERNAME,
                "-password", TestConfig.FAKE_H2O_INSTANCE_PASSWORD,
                "-disown",
            }), hadoopConfCaptor.capture());
        Configuration yarnConf = hadoopConfCaptor.getValue();
        assertThat(yarnConf.get("key1"), equalTo("value1"));
        assertThat(yarnConf.get("key2"), equalTo("value2"));

        verify(h2oUiFileParser, times(1)).getFlowUrl("h2o_ui_" + INSTANCE_ID);
    }
}
