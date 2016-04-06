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

package org.trustedanalytics.servicebroker.h2oprovisioner.ports;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;

public class SocketInstantiationPortChecker implements PortChecker {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(SocketInstantiationPortChecker.class);

  @Override
  public boolean isAvailable(int port) {

    LOGGER.info("Checking port: " + port);

    ServerSocket socket;

    try {
      socket = new ServerSocket(port);
    } catch (IOException e) {
      LOGGER.debug("Error when creating ServerSocket object.", e);
      return false;
    }

    try {
      socket.close();
    } catch (IOException e) {
      LOGGER.debug("Error when closing ServerSocket object.", e);
      return false;
    }

    return true;
  }
}
