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

package org.trustedanalytics.servicebroker.h2oprovisioner.service.externals;

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class H2oUiFileParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(H2oUiFileParser.class);

    public String getFlowUrl(String h2oUiFilePath) throws IOException {
        try (FileReader reader = new FileReader(h2oUiFilePath)) {
            return getFlowUrl(new BufferedReader(reader));
        }
    }

    @VisibleForTesting
    String getFlowUrl(BufferedReader h2oUiFile) throws IOException {
        LOGGER.info("Try to read UI file.");

        String line = h2oUiFile.readLine();
        LOGGER.debug(line);
        String address = line;

        if(LOGGER.isDebugEnabled()) {
            while ((line = h2oUiFile.readLine()) != null) {
                LOGGER.debug(line);
            }
        }

        LOGGER.info("UI address = " + address);
        return address;
    }
}
