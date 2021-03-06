/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.siddhi.extension.io.websocket.sink.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Class to retain results received by JMS client so that tests can poll the result and assert against.
 */
public class ResultContainer {
    private static Log log = LogFactory.getLog(ResultContainer.class);
    private int eventCount;
    private List<String> results;
    private CountDownLatch latch;
    private int timeout = 90;
    private int expectedEventCount = 0;


    public ResultContainer(int expectedEventCount) {
        this.expectedEventCount = expectedEventCount;
        eventCount = 0;
        results = new ArrayList<>(expectedEventCount);
        latch = new CountDownLatch(expectedEventCount);
    }

    public void eventReceived(String message) {
        eventCount++;
        results.add(message);
        latch.countDown();
    }

    public void eventReceived(byte[] message) {
        try {
            eventCount++;
            String text = new String(message, "utf-8");
            results.add(text);
            latch.countDown();
        } catch (UnsupportedEncodingException e) {
            log.error("UnsupportedEncodingException is thrown while converting the message to string.", e);
        }
    }

    public Boolean assertMessageContent(String content) {
        try {
            if (latch.await(timeout, TimeUnit.SECONDS)) {
                for (String message : results) {
                    if (message.contains(content)) {
                        return true;
                    }
                }
                return false;
            } else {
                log.error("ExpectedNumber : " + expectedEventCount + " of results not received. Only received " +
                         eventCount + " events.");
                return false;
            }
        } catch (InterruptedException e) {
            log.error("InterruptedException occurred while asserting the message content.");
        }
        return false;
    }


}
