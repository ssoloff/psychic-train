package io.github.ssoloff.psychictrain.engine;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import com.example.mockito.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractBrokerTestCase {
  private static final Topic<Integer> TOPIC = Topic.of("name", Integer.class);

  private final Broker broker = createBroker();

  @Inject
  @Mock
  private Subscriber subscriber;

  private final SubscriberFactory<Subscriber> subscriberFactory = subscriberContext -> subscriber;

  protected AbstractBrokerTestCase() {
  }

  protected abstract Broker createBroker();

  private PublisherToken<FakePublisher<Integer>> registerPublisher() {
    final PublisherFactory<FakePublisher<Integer>> factory = FakePublisher.newFactory();
    return broker.registerPublisher(TOPIC, factory);
  }

  private SubscriberToken<Subscriber> registerSubscriber() {
    return broker.registerSubscriber(TopicMatcher.forSingle(TOPIC), subscriberFactory);
  }

  // TODO: add tests for:
  // - case where existing subscriber receives notification when publisher added
  // - case where existing subscriber receives notification when publisher removed
  // - case where new subscriber receives notification immediately

  @Test
  public final void shouldNotifySubscriberWhenTopicChanged() {
    final PublisherToken<FakePublisher<Integer>> publisherToken = registerPublisher();
    registerSubscriber();

    publisherToken.getPublisher().publish(42);

    verify(subscriber).topicChanged(TOPIC);
  }

  @Test
  public final void shouldNotNotifySubscriberWhenTopicChangedByUnregisteredPublisher() {
    final PublisherToken<FakePublisher<Integer>> publisherToken = registerPublisher();
    registerSubscriber();

    publisherToken.unregister();
    publisherToken.getPublisher().publish(42);

    verify(subscriber, never()).topicChanged(TOPIC);
  }

  @Test
  public final void shouldNotNotifyUnregisteredSubscriberWhenTopicChanged() {
    final PublisherToken<FakePublisher<Integer>> publisherToken = registerPublisher();
    final SubscriberToken<Subscriber> subscriberToken = registerSubscriber();

    subscriberToken.unregister();
    publisherToken.getPublisher().publish(42);

    verify(subscriber, never()).topicChanged(TOPIC);
  }
}
