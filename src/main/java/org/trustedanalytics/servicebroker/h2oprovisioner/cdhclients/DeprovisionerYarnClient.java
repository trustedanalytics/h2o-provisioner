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
package org.trustedanalytics.servicebroker.h2oprovisioner.cdhclients;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeprovisionerYarnClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeprovisionerYarnClient.class);
  static final String JOB_NAME_PREFIX = "H2O_BROKER_";

  private final YarnClient tapYarnClient;

  public DeprovisionerYarnClient(YarnClient tapYarnClient) {
    this.tapYarnClient = tapYarnClient;
  }

  public void start() {
    tapYarnClient.start();
  }

  public ApplicationId getH2oJobId(String serviceInstanceId) throws YarnException {
    String h2oJobName = DeprovisionerYarnClient.h2oJobName(serviceInstanceId);

    List<ApplicationReport> foundJobs;
    try {
      foundJobs = getJobListByName(h2oJobName);
    } catch (YarnException | IOException e) {
      throw new YarnException("Error obtaining H2O job id from YARN: ", e);
    }

    if (foundJobs.size() != 1) {
      throw new YarnException("Error obtaining H2O job id from YARN. Found " + foundJobs.size()
          + " apps with name " + h2oJobName);
    } else {
      return foundJobs.get(0).getApplicationId();
    }
  }

  private List<ApplicationReport> getJobListByName(String name) throws YarnException, IOException {
    Set<String> applicationTypes = new HashSet<>();
    applicationTypes.add("MAPREDUCE");

    EnumSet<YarnApplicationState> applicationState = EnumSet.of(YarnApplicationState.RUNNING);

    LOGGER.debug("Getting yarn jobs...");
    List<ApplicationReport> applicationsMetadata =
        tapYarnClient.getApplications(applicationTypes, applicationState);
    LOGGER.debug("Done.");

    if (LOGGER.isDebugEnabled()) {
      String message = applicationsMetadata.stream()
          .map(report -> String.valueOf(report.getApplicationId().getId()))
          .collect(Collectors.joining(" "));
      LOGGER.debug("Jobs found: " + message);
    }

    List<ApplicationReport> foundApps = applicationsMetadata.stream()
        .filter(x -> x.getName().equals(name)).collect(Collectors.toList());

    return foundApps;
  }

  public void killApplication(ApplicationId applicationId) throws YarnException, IOException {
    tapYarnClient.killApplication(applicationId);
  }

  public static String h2oJobName(String serviceInstanceId) {
    return JOB_NAME_PREFIX + serviceInstanceId;
  }

}
