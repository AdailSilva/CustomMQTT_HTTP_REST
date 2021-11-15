package com.adailsilva.pahomqtt.configurations;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;

@Configuration
public class AppConfig {

	@Bean
	public MqttPahoClientFactory mqttPahoClientFactory() {
		DefaultMqttPahoClientFactory defaultMqttPahoClientFactory = new DefaultMqttPahoClientFactory();
		MqttConnectOptions connectOptions = new MqttConnectOptions();

		connectOptions.setServerURIs(new String[] { "tcp://localhost:1883" });
		connectOptions.setUserName("adailsilva");
		connectOptions.setPassword("@Hacker101".toCharArray());
		connectOptions.setCleanSession(true);

		defaultMqttPahoClientFactory.setConnectionOptions(connectOptions);

		return defaultMqttPahoClientFactory;
	}

	@Bean
	public MessageChannel mqttInputChannel() {
		return new DirectChannel();
	}

	@Bean
	public MessageProducer inbound() {
		MqttPahoMessageDrivenChannelAdapter mqttPahoMessageDrivenChannelAdapter = new MqttPahoMessageDrivenChannelAdapter(
				"serverIn", mqttPahoClientFactory(), "#");

		mqttPahoMessageDrivenChannelAdapter.setCompletionTimeout(5000);
		mqttPahoMessageDrivenChannelAdapter.setConverter(new DefaultPahoMessageConverter());
		mqttPahoMessageDrivenChannelAdapter.setQos(2);
		mqttPahoMessageDrivenChannelAdapter.setOutputChannel(mqttInputChannel());

		return mqttPahoMessageDrivenChannelAdapter;
	}

	@Bean
	@ServiceActivator(inputChannel = "mqttInputChannel")
	public MessageHandler handler() {
		return new MessageHandler() {
			@Override
			public void handleMessage(Message<?> message) throws MessagingException {
				String topic = message.getHeaders().get(MqttHeaders.RECEIVED_TOPIC).toString();
				if (topic.equals("/adailsilva/notifications")) {
					System.out.println("This is the topic");
				}
				System.out.println(message.getPayload());
			}
		};
	}

	@Bean
	public MessageChannel mqttOutboundChannel() {
		return new DirectChannel();
	}

	@Bean
	@ServiceActivator(inputChannel = "mqttOutboundChannel")
	public MessageHandler mqttOutbound() {
		// clientId is generated using a random number
		MqttPahoMessageHandler messageHandler = new MqttPahoMessageHandler("serverOut", mqttPahoClientFactory());

		messageHandler.setAsync(true);
		messageHandler.setDefaultTopic("#");
		messageHandler.setDefaultRetained(false);

		return messageHandler;
	}
}
