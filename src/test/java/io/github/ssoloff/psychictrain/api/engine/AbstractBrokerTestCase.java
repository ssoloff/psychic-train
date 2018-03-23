package io.github.ssoloff.psychictrain.api.engine;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import javax.inject.Inject;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import com.example.mockito.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractBrokerTestCase {
  private static final Topic<Integer> TOPIC_1 = Topic.of("topic1", Integer.class);
  private static final Topic<String> TOPIC_2 = Topic.of("topic2", String.class);

  private final Broker broker = createBroker();

  @Inject
  @Mock
  private Subscriber subscriber;

  private final SubscriberFactory<Subscriber> subscriberFactory = subscriberContext -> subscriber;

  protected AbstractBrokerTestCase() {
  }

  protected abstract Broker createBroker();

  private <@NonNull T> PublisherToken<FakePublisher<T>> registerPublisher(final Topic<T> topic) {
    return broker.registerPublisher(topic, FakePublisher.newFactory());
  }

  private SubscriberToken<Subscriber> registerSubscriber(
      final Topic<?> topic1,
      final @NonNull Topic<?>... otherTopics) {
    return broker.registerSubscriber(TopicMatcher.forTopics(topic1, otherTopics), subscriberFactory);
  }

  private void when(final Runnable action) {
    clearInvocations(subscriber);
    action.run();
  }

  private void thenSubscriberShouldBeNotifiedOfTopicChange(final Topic<?> topic) {
    verify(subscriber).topicChanged(topic);
  }

  private void thenSubscriberShouldNotBeNotifiedOfAnyTopicChange() {
    verify(subscriber, never()).topicChanged(any(Topic.class));
  }

  @Test
  public final void shouldNotifySubscriberUponRegistrationWhenMatchingTopicIsCurrentlyPublished() {
    registerPublisher(TOPIC_1);
    registerPublisher(TOPIC_2);

    when(() -> registerSubscriber(TOPIC_1, TOPIC_2));

    thenSubscriberShouldBeNotifiedOfTopicChange(TOPIC_1);
    thenSubscriberShouldBeNotifiedOfTopicChange(TOPIC_2);
  }

  @Test
  public final void shouldNotNotifySubscriberUponRegistrationWhenMatchingTopicIsNotCurrentlyPublished() {
    registerPublisher(TOPIC_1);

    when(() -> registerSubscriber(TOPIC_2));

    thenSubscriberShouldNotBeNotifiedOfAnyTopicChange();
  }

  @Test
  public final void shouldNotifySubscriberWhenMatchingTopicChanged() {
    final PublisherToken<FakePublisher<Integer>> publisherToken = registerPublisher(TOPIC_1);
    registerSubscriber(TOPIC_1);

    when(() -> publisherToken.getPublisher().publish(42));

    thenSubscriberShouldBeNotifiedOfTopicChange(TOPIC_1);
  }

  @Test
  public final void shouldNotNotifySubscriberWhenNonMatchingTopicChanged() {
    final PublisherToken<FakePublisher<Integer>> publisherToken = registerPublisher(TOPIC_1);
    registerSubscriber(TOPIC_2);

    when(() -> publisherToken.getPublisher().publish(42));

    thenSubscriberShouldNotBeNotifiedOfAnyTopicChange();
  }

  @Test
  public final void shouldNotifySubscriberWhenMatchingTopicIsPublished() {
    registerSubscriber(TOPIC_1);

    when(() -> registerPublisher(TOPIC_1));

    thenSubscriberShouldBeNotifiedOfTopicChange(TOPIC_1);
  }

  @Test
  public final void shouldNotNotifySubscriberWhenNonMatchingTopicIsPublished() {
    registerSubscriber(TOPIC_1);

    when(() -> registerPublisher(TOPIC_2));

    thenSubscriberShouldNotBeNotifiedOfAnyTopicChange();
  }

  @Test
  public final void shouldNotifySubscriberWhenMatchingTopicIsUnpublished() {
    final PublisherToken<FakePublisher<Integer>> publisherToken = registerPublisher(TOPIC_1);
    registerSubscriber(TOPIC_1);

    when(() -> publisherToken.unregister());

    thenSubscriberShouldBeNotifiedOfTopicChange(TOPIC_1);
  }

  @Test
  public final void shouldNotNotifySubscriberWhenNonMatchingTopicIsUnpublished() {
    final PublisherToken<FakePublisher<Integer>> publisherToken = registerPublisher(TOPIC_1);
    registerSubscriber(TOPIC_2);

    when(() -> publisherToken.unregister());

    thenSubscriberShouldNotBeNotifiedOfAnyTopicChange();
  }

  @Test
  public final void shouldNotNotifySubscriberWhenMatchingTopicChangedByUnregisteredPublisher() {
    final PublisherToken<FakePublisher<Integer>> publisherToken = registerPublisher(TOPIC_1);
    registerSubscriber(TOPIC_1);

    when(() -> {
      publisherToken.unregister();
      publisherToken.getPublisher().publish(42);
    });

    thenSubscriberShouldBeNotifiedOfTopicChange(TOPIC_1);
  }

  @Test
  public final void shouldNotNotifyUnregisteredSubscriberWhenMatchingTopicChanged() {
    final PublisherToken<FakePublisher<Integer>> publisherToken = registerPublisher(TOPIC_1);
    final SubscriberToken<Subscriber> subscriberToken = registerSubscriber(TOPIC_1);

    when(() -> {
      subscriberToken.unregister();
      publisherToken.getPublisher().publish(42);
    });

    thenSubscriberShouldNotBeNotifiedOfAnyTopicChange();
  }
}
