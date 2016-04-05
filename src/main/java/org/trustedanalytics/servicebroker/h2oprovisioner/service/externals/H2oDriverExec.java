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

package org.trustedanalytics.servicebroker.h2oprovisioner.service.externals;

import org.apache.hadoop.conf.Configuration;
import org.trustedanalytics.servicebroker.h2oprovisioner.rest.H2oSpawnerException;
import org.trustedanalytics.servicebroker.h2oprovisioner.service.externals.helpers.ExternalProcessExecutor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class H2oDriverExec {

  private final String yarnConfDir;

  public H2oDriverExec(String yarnConfDir) {
    this.yarnConfDir = yarnConfDir;
  }

  public void spawnH2oOnYarn(String[] command, Configuration hadoopConf)
      throws ExternalProcessException, IOException {
    try (FileOutputStream stream = new FileOutputStream(yarnConfDir + "/yarn-site.xml")) {
      hadoopConf.writeXml(new OutputStreamWriter(stream));
    }

    int h2oExitCode = ExternalProcessExecutor.runCommand(command);
    if (h2oExitCode != 0) {
      throw new ExternalProcessException("h2odriver.jar exited with code " + h2oExitCode);
    }
  }
}
