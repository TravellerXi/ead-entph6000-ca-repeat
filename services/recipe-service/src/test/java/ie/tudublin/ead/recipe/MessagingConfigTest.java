package ie.tudublin.ead.recipe;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.MessageConverter;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MessagingConfigTest {

    @Test
    void lifecycleEventsUseJsonRatherThanJavaSerialization() {
        MessageConverter converter = new MessagingConfig().jsonMessageConverter();

        Message message = converter.toMessage(
                Map.of("recipeId", "recipe-1", "occurredAt", "2026-08-18T00:00:00Z"),
                new MessageProperties());

        assertThat(message.getMessageProperties().getContentType()).isEqualTo(MessageProperties.CONTENT_TYPE_JSON);
        assertThat(converter.fromMessage(message)).isInstanceOf(Map.class);
    }
}
