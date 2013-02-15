/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.processor.aggregator;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.processor.aggregate.UseLatestAggregationStrategy;

/**
 * Unit test to verify that aggregate by interval only also works.
 *
 * @version 
 */
public class DistributedCompletionIntervalTest extends AbstractDistributedTest {

    public void testAggregateInterval() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        MockEndpoint mock2 = getMockEndpoint2("mock:result");
        // by default the use latest aggregation strategy is used so we get message 18 and message 19
        mock.expectedBodiesReceived("Message 18");
        mock2.expectedBodiesReceived("Message 19");

        // ensure messages are send after the 1s
        Thread.sleep(2000);
        
        for (int i = 0; i < 20; i++) {
            int choice = i % 2;
            if (choice == 0) {
                template.sendBodyAndHeader("direct:start", "Message " + i, "id", "1");
            } else {
                template2.sendBodyAndHeader("direct:start", "Message " + i, "id", "1");
            }
        }

        mock.assertIsSatisfied();
        mock2.assertIsSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                // START SNIPPET: e1
                from("direct:start")
                    .aggregate(header("id"), new UseLatestAggregationStrategy())
                        // trigger completion every 5th second
                        .completionInterval(5000)
                    .to("mock:result");
                // END SNIPPET: e1
            }
        };
    }
}
