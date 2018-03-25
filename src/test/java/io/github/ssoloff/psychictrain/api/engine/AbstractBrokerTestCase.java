package io.github.ssoloff.psychictrain.api.engine;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Set;

import javax.inject.Inject;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.collect.ImmutableSet;

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

  @Nested
  public final class SubscriberContractTest {
    @Captor
    @Inject
    private ArgumentCaptor<Set<Topic<?>>> topicsCaptor;

    private void thenTopicsPassedInNotificationShouldBeImmutable() {
      assertThrows(UnsupportedOperationException.class, () -> topicsCaptor.getValue().clear());
    }

    @BeforeEach
    public void initializeSubscriber() {
      doNothing().when(subscriber).topicsChanged(topicsCaptor.capture());
    }

    @Test
    public void shouldPassImmutableSetUponNormalTopicsChangedNotification() {
      final PublisherToken<FakePublisher<Integer>> publisherToken = registerPublisher(TOPIC_1);
      registerSubscriber(TOPIC_1);

      publisherToken.getPublisher().publish(42);

      thenTopicsPassedInNotificationShouldBeImmutable();
    }

    @Test
    public void shouldPassImmutableSetUponRegistrationTopicsChangedNotification() {
      registerPublisher(TOPIC_1);

      registerSubscriber(TOPIC_1);

      thenTopicsPassedInNotificationShouldBeImmutable();
    }
  }

  @Nested
  public final class SubscriberNotificationTest {
    private void when(final Runnable action) {
      clearInvocations(subscriber);
      action.run();
    }

    private void thenSubscriberShouldBeNotifiedOfTopicChanges(final @NonNull Topic<?>... topics) {
      verify(subscriber).topicsChanged(ImmutableSet.copyOf(topics));
    }

    private void thenSubscriberShouldNotBeNotifiedOfAnyTopicChanges() {
      verify(subscriber, never()).topicsChanged(any());
    }

    @Test
    public void shouldNotifySubscriberUponRegistrationWhenMatchingTopicIsCurrentlyPublished() {
      registerPublisher(TOPIC_1);
      registerPublisher(TOPIC_2);

      when(() -> registerSubscriber(TOPIC_1, TOPIC_2));

      thenSubscriberShouldBeNotifiedOfTopicChanges(TOPIC_1, TOPIC_2);
    }

    @Test
    public void shouldNotNotifySubscriberUponRegistrationWhenMatchingTopicIsNotCurrentlyPublished() {
      registerPublisher(TOPIC_1);

      when(() -> registerSubscriber(TOPIC_2));

      thenSubscriberShouldNotBeNotifiedOfAnyTopicChanges();
    }

    @Test
    public void shouldNotifySubscriberWhenMatchingTopicChanged() {
      final PublisherToken<FakePublisher<Integer>> publisherToken = registerPublisher(TOPIC_1);
      registerSubscriber(TOPIC_1);

      when(() -> publisherToken.getPublisher().publish(42));

      thenSubscriberShouldBeNotifiedOfTopicChanges(TOPIC_1);
    }

    @Test
    public void shouldNotNotifySubscriberWhenNonMatchingTopicChanged() {
      final PublisherToken<FakePublisher<Integer>> publisherToken = registerPublisher(TOPIC_1);
      registerSubscriber(TOPIC_2);

      when(() -> publisherToken.getPublisher().publish(42));

      thenSubscriberShouldNotBeNotifiedOfAnyTopicChanges();
    }

    @Test
    public void shouldNotifySubscriberWhenMatchingTopicIsPublished() {
      registerSubscriber(TOPIC_1);

      when(() -> registerPublisher(TOPIC_1));

      thenSubscriberShouldBeNotifiedOfTopicChanges(TOPIC_1);
    }

    @Test
    public void shouldNotNotifySubscriberWhenNonMatchingTopicIsPublished() {
      registerSubscriber(TOPIC_1);

      when(() -> registerPublisher(TOPIC_2));

      thenSubscriberShouldNotBeNotifiedOfAnyTopicChanges();
    }

    @Test
    public void shouldNotifySubscriberWhenMatchingTopicIsUnpublished() {
      final PublisherToken<FakePublisher<Integer>> publisherToken = registerPublisher(TOPIC_1);
      registerSubscriber(TOPIC_1);

      when(() -> publisherToken.unregister());

      thenSubscriberShouldBeNotifiedOfTopicChanges(TOPIC_1);
    }

    @Test
    public void shouldNotNotifySubscriberWhenNonMatchingTopicIsUnpublished() {
      final PublisherToken<FakePublisher<Integer>> publisherToken = registerPublisher(TOPIC_1);
      registerSubscriber(TOPIC_2);

      when(() -> publisherToken.unregister());

      thenSubscriberShouldNotBeNotifiedOfAnyTopicChanges();
    }

    @Test
    public void shouldNotNotifySubscriberWhenMatchingTopicChangedByUnregisteredPublisher() {
      final PublisherToken<FakePublisher<Integer>> publisherToken = registerPublisher(TOPIC_1);
      registerSubscriber(TOPIC_1);

      when(() -> {
        publisherToken.unregister();
        publisherToken.getPublisher().publish(42);
      });

      thenSubscriberShouldBeNotifiedOfTopicChanges(TOPIC_1);
    }

    @Test
    public void shouldNotNotifyUnregisteredSubscriberWhenMatchingTopicChanged() {
      final PublisherToken<FakePublisher<Integer>> publisherToken = registerPublisher(TOPIC_1);
      final SubscriberToken<Subscriber> subscriberToken = registerSubscriber(TOPIC_1);

      when(() -> {
        subscriberToken.unregister();
        publisherToken.getPublisher().publish(42);
      });

      thenSubscriberShouldNotBeNotifiedOfAnyTopicChanges();
    }
  }
}
