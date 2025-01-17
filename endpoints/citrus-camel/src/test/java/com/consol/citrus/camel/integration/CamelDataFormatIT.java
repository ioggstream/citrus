/*
 * Copyright 2006-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.camel.integration;

import com.consol.citrus.annotations.CitrusTest;
import com.consol.citrus.message.MessageType;
import com.consol.citrus.testng.spring.TestNGCitrusSpringSupport;
import org.apache.camel.CamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static com.consol.citrus.actions.ReceiveMessageAction.Builder.receive;
import static com.consol.citrus.actions.SendMessageAction.Builder.send;
import static com.consol.citrus.camel.dsl.CamelSupport.camel;
import static org.apache.camel.builder.endpoint.StaticEndpointBuilders.seda;

/**
 * @author Christoph Deppisch
 */
public class CamelDataFormatIT extends TestNGCitrusSpringSupport {

    @Autowired
    private CamelContext camelContext;

    @Test
    @CitrusTest
    public void shouldApplyDataFormat() {
        when(send(camel().endpoint(seda("data")::getUri))
                .message()
                .body("Citrus rocks!")
                .transform(camel(camelContext)
                        .marshal()
                        .base64())
        );

        then(receive("camel:" + camel().endpoints().seda("data").getUri())
                .transform(camel(camelContext)
                        .unmarshal()
                        .base64())
                .transform(camel(camelContext)
                        .convertBodyTo(String.class))
                .message()
                .type(MessageType.PLAINTEXT)
                .body("Citrus rocks!"));
    }

}
