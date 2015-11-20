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

package org.trustedanalytics.servicebroker.h2oprovisioner.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.validation.constraints.NotNull;

@Configuration
@Data
public class ExternalConfiguration {

    @Value("${h2o.driver.ip}")
    @NotNull
    private String h2oDriverIp;

    @Value("${h2o.driver.portLowerBound}")
    @NotNull
    private String h2oDriverPortLowerBound;

    @Value("${h2o.driver.portUpperBound}")
    @NotNull
    private String h2oDriverPortUpperBound;

    @Value("${h2o.driver.jarPath}")
    @NotNull
    private String h2oDriverJarpath;

    @Value("${h2o.credentials.usernameLength}")
    @NotNull
    private String h2oUsernameLength;

    @Value("${h2o.credentials.passwordLength}")
    @NotNull
    private String h2oPasswordLength;

    @Value("${yarn.conf.dir}")
    @NotNull
    private String yarnConfDir;
}
