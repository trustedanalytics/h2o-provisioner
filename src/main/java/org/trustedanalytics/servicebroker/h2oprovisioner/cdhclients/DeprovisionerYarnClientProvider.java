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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.trustedanalytics.hadoop.config.client.helper.DelegatingYarnClient;
import org.trustedanalytics.hadoop.config.client.helper.UgiWrapper;

public class DeprovisionerYarnClientProvider {

  public DeprovisionerYarnClientProvider() {}
  
  public DeprovisionerYarnClient getClient(String user, Configuration hadoopConf) throws IOException{
    String ticketCachePath = hadoopConf.get("hadoop.security.kerberos.ticket.cache.path");
    UserGroupInformation ugi = UserGroupInformation.getBestUGI(ticketCachePath, user);
    YarnClient client = new DelegatingYarnClient(YarnClient.createYarnClient(), new UgiWrapper(ugi));
    client.init(hadoopConf);
    
    return new DeprovisionerYarnClient(client);
  }
}
