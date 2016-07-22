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

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.google.common.collect.ImmutableList;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.JobNotFoundException;

public class DeprovisionerYarnClientTest {

  private final YarnClient yarnClientMock = mock(YarnClient.class);
  private List<ApplicationReport> yarnReportWithOneJob;
  private List<ApplicationReport> yarnReportWithNoJobs;
  private List<ApplicationReport> yarnReportWithTwoJobs;
  private Set<String> expectedApplicationType = new HashSet<>(Arrays.asList("MAPREDUCE"));
  private EnumSet<YarnApplicationState> expectedApplicationState =
      EnumSet.of(YarnApplicationState.RUNNING);
  private final String testServiceInstanceId = "29ujhdfl-jvhfds-08";
  private ApplicationId expectedYarnJobId1;
  private String expectedH2oJobName =
      DeprovisionerYarnClient.JOB_NAME_PREFIX + testServiceInstanceId;
  private ApplicationId expectedYarnJobId2;



  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  @Before
  public void setUp() {
    expectedYarnJobId1 = mock(ApplicationId.class);
    ApplicationReport yarnJobMetadata1 = mock(ApplicationReport.class);
    when(yarnJobMetadata1.getName()).thenReturn(expectedH2oJobName);
    when(yarnJobMetadata1.getApplicationId()).thenReturn(expectedYarnJobId1);

    // another YarnJob with the same name
    expectedYarnJobId2 = mock(ApplicationId.class);
    ApplicationReport yarnJobMetadata2 = mock(ApplicationReport.class);
    when(yarnJobMetadata2.getName()).thenReturn(expectedH2oJobName);
    when(yarnJobMetadata2.getApplicationId()).thenReturn(expectedYarnJobId2);

    yarnReportWithOneJob = ImmutableList.of(yarnJobMetadata1);
    yarnReportWithNoJobs = ImmutableList.of();
    yarnReportWithTwoJobs = ImmutableList.of(yarnJobMetadata1, yarnJobMetadata2);
  }

  @Test
  public void start_invokesStartOnYarnClient() throws Exception {
    // given
    DeprovisionerYarnClient sut = new DeprovisionerYarnClient(yarnClientMock);

    // when
    sut.start();

    // then
    verify(yarnClientMock).start();
  }

  @Test
  public void getH2oJobId_oneJobReturnedByYarnClient_JobIdReturned() throws Exception {
    // given
    when(yarnClientMock.getApplications(expectedApplicationType, expectedApplicationState))
        .thenReturn(yarnReportWithOneJob);
    DeprovisionerYarnClient sut = new DeprovisionerYarnClient(yarnClientMock);

    // when
    ApplicationId actualH2oJobId = sut.getH2oJobId(testServiceInstanceId);

    // then
    assertEquals(expectedYarnJobId1, actualH2oJobId);
  }

  @Test
  public void getH2oJobId_noJobsReturnedByYarnClient_ExceptionThrown() throws Exception {
    // given
    when(yarnClientMock.getApplications(expectedApplicationType, expectedApplicationState))
        .thenReturn(yarnReportWithNoJobs);
    DeprovisionerYarnClient sut = new DeprovisionerYarnClient(yarnClientMock);

    // when
    // then
    thrown.expect(JobNotFoundException.class);
    thrown.expectMessage("No such H2O job on YARN exists");
    sut.getH2oJobId(testServiceInstanceId);
  }

  @Test
  public void getH2oJobId_twoJobsReturnedByYarnClient_ExceptionThrown() throws Exception {
    // given
    when(yarnClientMock.getApplications(expectedApplicationType, expectedApplicationState))
        .thenReturn(yarnReportWithTwoJobs);
    DeprovisionerYarnClient sut = new DeprovisionerYarnClient(yarnClientMock);

    // when
    // then
    thrown.expect(YarnException.class);
    thrown.expectMessage("Found 2 apps with name " + expectedH2oJobName);
    sut.getH2oJobId(testServiceInstanceId);
  }

  @Test
  public void getH2oJobId_ExceptionThrownFromYarnClient_ExceptionThrown() throws Exception {
    // given
    when(yarnClientMock.getApplications(expectedApplicationType, expectedApplicationState))
        .thenThrow(new YarnException());
    DeprovisionerYarnClient sut = new DeprovisionerYarnClient(yarnClientMock);

    // when
    // then
    thrown.expect(YarnException.class);
    thrown.expectMessage("Error obtaining H2O job id from YARN");
    sut.getH2oJobId(testServiceInstanceId);

  }

  @Test
  public void killApplication_invokesKillOnYarnClient() throws Exception {
    // given
    DeprovisionerYarnClient sut = new DeprovisionerYarnClient(yarnClientMock);
    ApplicationId applicationIdMock = mock(ApplicationId.class);

    // when
    sut.killApplication(applicationIdMock);

    // then
    verify(yarnClientMock).killApplication(applicationIdMock);

  }

  @Test
  public void h2oJobName_ReturnsExpectedJobName() throws Exception {
    // given

    // when
    String actualH2oJobName = DeprovisionerYarnClient.h2oJobName(testServiceInstanceId);

    // then
    assertEquals(expectedH2oJobName, actualH2oJobName);
  }

  @Test
  public void getH2oJobId_logLevelSetToDebugAndMoreThanOneJobFound_allJobIdsLogged()
      throws Exception {
    // given
    Logger.getRootLogger().setLevel(Level.DEBUG);
    Writer log = new StringWriter();
    Logger.getRootLogger().addAppender(new WriterAppender(new SimpleLayout(), log));

    int jobId1 = 122139831;
    int jobId2 = 90324830;
    when(expectedYarnJobId1.getId()).thenReturn(jobId1);
    when(expectedYarnJobId2.getId()).thenReturn(jobId2);
    when(yarnClientMock.getApplications(expectedApplicationType, expectedApplicationState))
        .thenReturn(yarnReportWithTwoJobs);
    DeprovisionerYarnClient sut = new DeprovisionerYarnClient(yarnClientMock);

    // when
    try {
      sut.getH2oJobId(testServiceInstanceId);
    } catch (YarnException e) {
      // ignoring exception cause we are testing if logger works correctly in case of exception
    }

    // then
    assertThat(log.toString(), containsString(String.valueOf(jobId1)));
    assertThat(log.toString(), containsString(String.valueOf(jobId2)));
  }

}
