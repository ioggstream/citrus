/*
 * Copyright 2006-2011 the original author or authors.
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

package com.consol.citrus.container;

import com.consol.citrus.TestAction;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sequence of Citrus test actions that get executed before a test suite run. Sequence should
 * decide weather to execute according to given suite name and included test groups if any.
 *
 * @author Christoph Deppisch
 */
public class SequenceBeforeSuite extends AbstractSuiteActionContainer {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(SequenceBeforeSuite.class);
    
    @Override
    public void doExecute(TestContext context) {
        log.info("Entering before suite block");

        if (log.isDebugEnabled()) {
            log.debug("Executing " + actions.size() + " actions before suite");
            log.debug("");
        }
        
        for(TestAction action: actions)  {
            try {
                /* Executing test action and validate its success */
                action.execute(context);
            } catch (Exception e) {
                log.error("Task failed " + action.getName() + "Nested exception is: ", e);
                throw new CitrusRuntimeException(e);
            }
        }
    }
}
