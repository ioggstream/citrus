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

package com.consol.citrus.actions;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.consol.citrus.DefaultTestCase;
import com.consol.citrus.TestActor;
import com.consol.citrus.TestCase;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.context.TestContextFactory;
import com.consol.citrus.endpoint.Endpoint;
import com.consol.citrus.endpoint.EndpointConfiguration;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.functions.DefaultFunctionLibrary;
import com.consol.citrus.message.DefaultMessage;
import com.consol.citrus.message.Message;
import com.consol.citrus.message.MessageDirection;
import com.consol.citrus.message.MessageHeaders;
import com.consol.citrus.message.MessageProcessor;
import com.consol.citrus.messaging.Producer;
import com.consol.citrus.testng.AbstractTestNGUnitTest;
import com.consol.citrus.validation.DefaultMessageHeaderValidator;
import com.consol.citrus.validation.builder.PayloadTemplateMessageBuilder;
import com.consol.citrus.validation.context.HeaderValidationContext;
import com.consol.citrus.validation.matcher.DefaultValidationMatcherLibrary;
import com.consol.citrus.validation.script.GroovyScriptMessageBuilder;
import com.consol.citrus.variable.MessageHeaderVariableExtractor;
import com.consol.citrus.variable.dictionary.DataDictionary;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Christoph Deppisch
 */
public class SendMessageActionTest extends AbstractTestNGUnitTest {

    private Endpoint endpoint = Mockito.mock(Endpoint.class);
    private Producer producer = Mockito.mock(Producer.class);
    private EndpointConfiguration endpointConfiguration = Mockito.mock(EndpointConfiguration.class);

    @Override
    protected TestContextFactory createTestContextFactory() {
        TestContextFactory factory = super.createTestContextFactory();
        factory.getFunctionRegistry().addFunctionLibrary(new DefaultFunctionLibrary());
        factory.getValidationMatcherRegistry().addValidationMatcherLibrary(new DefaultValidationMatcherLibrary());
        return factory;
    }

    @Test
    @SuppressWarnings("rawtypes")
	public void testSendMessageWithMessagePayloadData() {
		TestActor testActor = new TestActor();
        testActor.setName("TESTACTOR");

		PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
		messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

		final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .actor(testActor)
                .messageBuilder(messageBuilder)
                .build();
		sendAction.execute(context);

	}

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessagePayloadResource() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadResourcePath("classpath:com/consol/citrus/actions/test-request-payload.xml");

        final Message controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<TestRequest>" + System.lineSeparator() +
                "    <Message>Hello World!</Message>" + System.lineSeparator() +
                "</TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
	public void testSendMessageWithMessageBuilderScriptData() {
		StringBuilder sb = new StringBuilder();
		sb.append("markupBuilder.TestRequest(){\n");
		sb.append("Message('Hello World!')\n");
		sb.append("}");

		GroovyScriptMessageBuilder scriptMessageBuidler = new GroovyScriptMessageBuilder();
		scriptMessageBuidler.setScriptData(sb.toString());

		final Message controlMessage = new DefaultMessage("<TestRequest>" + System.lineSeparator() +
                "  <Message>Hello World!</Message>" + System.lineSeparator() +
                "</TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(scriptMessageBuidler)
                .build();
		sendAction.execute(context);

	}

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessageBuilderScriptDataVariableSupport() {
        context.setVariable("text", "Hello World!");

        StringBuilder sb = new StringBuilder();
        sb.append("markupBuilder.TestRequest(){\n");
        sb.append("Message('${text}')\n");
        sb.append("}");

        GroovyScriptMessageBuilder scriptMessageBuidler = new GroovyScriptMessageBuilder();
        scriptMessageBuidler.setScriptData(sb.toString());

        final Message controlMessage = new DefaultMessage("<TestRequest>" + System.lineSeparator() +
                "  <Message>Hello World!</Message>" + System.lineSeparator() +
                "</TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(scriptMessageBuidler)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessageBuilderScriptResource() {
        GroovyScriptMessageBuilder scriptMessageBuidler = new GroovyScriptMessageBuilder();
        scriptMessageBuidler.setScriptResourcePath("classpath:com/consol/citrus/actions/test-request-payload.groovy");

        final Message controlMessage = new DefaultMessage("<TestRequest>" + System.lineSeparator() +
                "  <Message>Hello World!</Message>" + System.lineSeparator() +
                "</TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(scriptMessageBuidler)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessagePayloadDataVariablesSupport() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>${myText}</Message></TestRequest>");

        context.setVariable("myText", "Hello World!");

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessagePayloadResourceVariablesSupport() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadResourcePath("classpath:com/consol/citrus/actions/test-request-payload-with-variables.xml");

        context.setVariable("myText", "Hello World!");

        final Message controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<TestRequest>" + System.lineSeparator() +
                "    <Message>Hello World!</Message>" + System.lineSeparator() +
                "</TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessagePayloadResourceFunctionsSupport() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadResourcePath("classpath:com/consol/citrus/actions/test-request-payload-with-functions.xml");

        final Message controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.lineSeparator() +
                "<TestRequest>" + System.lineSeparator() +
                "    <Message>Hello World!</Message>" + System.lineSeparator() +
                "</TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageOverwriteMessageElements() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>?</Message></TestRequest>");

        MessageProcessor processor = (message, context) -> {
            message.setPayload(message.getPayload(String.class).replaceAll("\\?", "Hello World!"));
        };

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .process(processor)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessageHeaders() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        final Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        messageBuilder.setMessageHeaders(headers);

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithHeaderValuesVariableSupport() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        context.setVariable("myOperation", "sayHello");

        final Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "${myOperation}");
        messageBuilder.setMessageHeaders(headers);

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    public void testSendMessageWithUnknownVariableInMessagePayload() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>${myText}</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        try {
            sendAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Unknown variable 'myText'");
            return;
        }

        Assert.fail("Missing " + CitrusRuntimeException.class + " with unknown variable error message");
    }

    @Test
    public void testSendMessageWithUnknownVariableInHeaders() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "${myOperation}");
        messageBuilder.setMessageHeaders(headers);

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        try {
            sendAction.execute(context);
        } catch(CitrusRuntimeException e) {
            Assert.assertEquals(e.getMessage(), "Unknown variable 'myOperation'");
            return;
        }

        Assert.fail("Missing " + CitrusRuntimeException.class + " with unknown variable error message");
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithExtractHeaderValues() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        final Map<String, Object> controlHeaders = new HashMap<String, Object>();
        controlHeaders.put("Operation", "sayHello");
        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>", controlHeaders);

        final Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("Operation", "sayHello");
        messageBuilder.setMessageHeaders(headers);

        Map<String, String> extractVars = new HashMap<String, String>();
        extractVars.put("Operation", "myOperation");
        extractVars.put(MessageHeaders.ID, "correlationId");

        MessageHeaderVariableExtractor variableExtractor = new MessageHeaderVariableExtractor.Builder()
                .headers(extractVars)
                .build();

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .process(variableExtractor)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

        Assert.assertNotNull(context.getVariable("myOperation"));
        Assert.assertNotNull(context.getVariable("correlationId"));

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testMissingMessagePayload() {
        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), new DefaultMessage(""));
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithUTF16Encoding() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<?xml version=\"1.0\" encoding=\"UTF-16\"?><TestRequest><Message>Hello World!</Message></TestRequest>");

        final Message controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"UTF-16\"?><TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithISOEncoding() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><TestRequest><Message>Hello World!</Message></TestRequest>");

        final Message controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithUnsupportedEncoding() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<?xml version=\"1.0\" encoding=\"MyUnsupportedEncoding\"?><TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        try {
            sendAction.execute(context);
        } catch (CitrusRuntimeException e) {
            Assert.assertTrue(e.getCause() instanceof UnsupportedEncodingException);
        }

        verify(producer).send(any(Message.class), any(TestContext.class));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void testSendMessageWithMessagePayloadResourceISOEncoding() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadResourcePath("classpath:com/consol/citrus/actions/test-request-iso-encoding.xml");

        final Message controlMessage = new DefaultMessage("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" + System.lineSeparator() +
                "<TestRequest>" + System.lineSeparator() +
                "    <Message>Hello World!</Message>" + System.lineSeparator() +
                "</TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);

    }

    @Test
    public void testDisabledSendMessage() {
        TestCase testCase = new DefaultTestCase();

        TestActor disabledActor = new TestActor();
        disabledActor.setDisabled(true);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .actor(disabledActor)
                .messageBuilder(messageBuilder)
                .build();
        testCase.addTestAction(sendAction);
        testCase.execute(context);

    }

    @Test
    public void testDisabledSendMessageByEndpointActor() {
        TestCase testCase = new DefaultTestCase();

        TestActor disabledActor = new TestActor();
        disabledActor.setDisabled(true);

        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>Hello World!</Message></TestRequest>");

        reset(endpoint, producer, endpointConfiguration);
        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);
        when(endpoint.getActor()).thenReturn(disabledActor);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        testCase.addTestAction(sendAction);
        testCase.execute(context);

    }

    @Test
    public void testWithExplicitDataDictionary() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>?</Message></TestRequest>");

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello Citrus!</Message></TestRequest>");

        DataDictionary<String> dictionary = Mockito.mock(DataDictionary.class);
        reset(endpoint, producer, endpointConfiguration);
        when(dictionary.getDirection()).thenReturn(MessageDirection.OUTBOUND);
        when(dictionary.isGlobalScope()).thenReturn(false);
        doAnswer(invocationOnMock -> {
            Message message = invocationOnMock.getArgument(0);
            message.setPayload("<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
            return null;
        }).when(dictionary).process(any(Message.class), eq(context));

        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .dictionary(dictionary)
                .build();
        sendAction.execute(context);
    }

    @Test
    public void testWithExplicitAndGlobalDataDictionary() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>?</Message></TestRequest>");

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello Citrus!</Message></TestRequest>");

        DataDictionary<String> dictionary = Mockito.mock(DataDictionary.class);
        DataDictionary<String> globalDictionary = Mockito.mock(DataDictionary.class);
        reset(endpoint, producer, endpointConfiguration);
        when(dictionary.getDirection()).thenReturn(MessageDirection.OUTBOUND);
        when(globalDictionary.getDirection()).thenReturn(MessageDirection.OUTBOUND);
        when(dictionary.isGlobalScope()).thenReturn(false);
        when(globalDictionary.isGlobalScope()).thenReturn(true);
        doAnswer(invocationOnMock -> {
            Message message = invocationOnMock.getArgument(0);
            message.setPayload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            return null;
        }).when(globalDictionary).process(any(Message.class), eq(context));

        doAnswer(invocationOnMock -> {
            Message message = invocationOnMock.getArgument(0);
            message.setPayload("<TestRequest><Message>Hello Citrus!</Message></TestRequest>");
            return null;
        }).when(dictionary).process(any(Message.class), eq(context));

        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        context.getMessageProcessors().setMessageProcessors(Collections.singletonList(globalDictionary));

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .dictionary(dictionary)
                .build();
        sendAction.execute(context);
    }

    @Test
    public void testWithGlobalDataDictionary() {
        PayloadTemplateMessageBuilder messageBuilder = new PayloadTemplateMessageBuilder();
        messageBuilder.setPayloadData("<TestRequest><Message>?</Message></TestRequest>");

        final Message controlMessage = new DefaultMessage("<TestRequest><Message>Hello World!</Message></TestRequest>");

        DataDictionary<String> inboundDictionary = Mockito.mock(DataDictionary.class);
        DataDictionary<String> outboundDictionary = Mockito.mock(DataDictionary.class);
        reset(endpoint, producer, endpointConfiguration);
        when(inboundDictionary.getDirection()).thenReturn(MessageDirection.INBOUND);
        when(outboundDictionary.getDirection()).thenReturn(MessageDirection.OUTBOUND);
        when(inboundDictionary.isGlobalScope()).thenReturn(true);
        when(outboundDictionary.isGlobalScope()).thenReturn(true);
        doAnswer(invocationOnMock -> {
            Message message = invocationOnMock.getArgument(0);
            message.setPayload("<TestRequest><Message>Hello World!</Message></TestRequest>");
            return null;
        }).when(outboundDictionary).process(any(Message.class), eq(context));

        doThrow(new CitrusRuntimeException("Unexpected call of inbound data dictionary"))
                .when(inboundDictionary).process(any(Message.class), eq(context));

        when(endpoint.createProducer()).thenReturn(producer);
        when(endpoint.getEndpointConfiguration()).thenReturn(endpointConfiguration);

        doAnswer(invocation -> {
            validateMessageToSend(invocation.getArgument(0), controlMessage);
            return null;
        }).when(producer).send(any(Message.class), any(TestContext.class));

        when(endpoint.getActor()).thenReturn(null);

        context.getMessageProcessors().setMessageProcessors(Arrays.asList(inboundDictionary, outboundDictionary));

        SendMessageAction sendAction = new SendMessageAction.Builder()
                .endpoint(endpoint)
                .messageBuilder(messageBuilder)
                .build();
        sendAction.execute(context);
    }

    private void validateMessageToSend(Message toSend, Message controlMessage) {
        Assert.assertEquals(toSend.getPayload(String.class).trim(), controlMessage.getPayload(String.class).trim());
        DefaultMessageHeaderValidator validator = new DefaultMessageHeaderValidator();
        validator.validateMessage(toSend, controlMessage, context, new HeaderValidationContext());
    }
}
