/*
 * Copyright 2006-2010 the original author or authors.
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

package com.consol.citrus.samples.bookregistry;

import org.testng.ITestContext;
import org.testng.annotations.Test;

import com.consol.citrus.samples.CitrusSamplesDemo;
import com.consol.citrus.samples.common.DemoAwareTestNGCitrusTest;

/**
 * TODO: Description
 *
 * @author Christoph Deppisch
 * @since 2010-02-24
 */
public class GetBookDetails_OK_1_Test extends DemoAwareTestNGCitrusTest {
    
    private BookRegistryDemo demo = new BookRegistryDemo();
    
    @Test
    public void getBookDetails_OK_1_Test(ITestContext testContext) {
        executeTest(testContext);
    }

    @Override
    public CitrusSamplesDemo getDemo() {
        return demo;
    }
}
