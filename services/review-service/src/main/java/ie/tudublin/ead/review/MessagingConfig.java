package ie.tudublin.ead.review;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessagingConfig {

    @Value("${app.messaging.exchange:ead.events}")
    private String exchangeName;

    @Value("${app.messaging.queue:review.recipe-events}")
    private String queueName;

    @Bean
    TopicExchange eventsExchange() {
        return ExchangeBuilder.topicExchange(exchangeName).durable(true).build();
    }

    @Bean
    Queue recipeEventsQueue() {
        return QueueBuilder.durable(queueName)
                .deadLetterExchange(exchangeName + ".dlx")
                .build();
    }

    @Bean
    Binding recipeEventsBinding(Queue recipeEventsQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(recipeEventsQueue).to(eventsExchange).with("recipe.#");
    }

    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
