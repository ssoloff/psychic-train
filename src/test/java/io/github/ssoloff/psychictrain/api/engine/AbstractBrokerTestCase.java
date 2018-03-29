package io.github.ssoloff.psychictrain.api.engine;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

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
import com.google.common.collect.Multimap;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractBrokerTestCase {
  private static final Topic<Integer> TOPIC_1 = Topic.of("topic1", Integer.class);
  private static final Topic<String> TOPIC_2 = Topic.of("topic2", String.class);
  private static final Topic<Double> TOPIC_3 = Topic.of("topic3", Double.class);

  private final Broker broker = createBroker();

  @Inject
  @Mock
  private Consumer<Set<Topic<?>>> topicsChangedConsumer;

  protected AbstractBrokerTestCase() {
  }

  protected abstract Broker createBroker();

  private <@NonNull T> PublisherToken<FakePublisher<T>> registerPublisher(final Topic<T> topic) {
    return broker.registerPublisher(topic, FakePublisher.newFactory());
  }

  private SubscriberToken<FakeSubscriber> registerSubscriber(
      final Topic<?> topic1,
      final @NonNull Topic<?>... otherTopics) {
    return broker.registerSubscriber(
        TopicMatcher.forTopics(topic1, otherTopics),
        FakeSubscriber.newFactory(topicsChangedConsumer));
  }

  @Nested
  public final class PublisherContextTest {
    @Nested
    public final class PublishTest {
      @Test
      public void shouldThrowExceptionWhenValueTypeDoesNotMatchTopicType() {
        final PublisherContext publisherContext = registerPublisher(TOPIC_1).getPublisher().getContext();

        assertThrows(ClassCastException.class, () -> publisherContext.publish("notAnInteger"));
      }
    }
  }

  @Nested
  public final class SubscriberContextTest {
    @Nested
    public final class GetValuesForMatchingTopicsTest {
      private Multimap<Topic<?>, ?> getValuesForMatchingTopics(
          final Topic<?> topic1,
          final @NonNull Topic<?>... otherTopics) {
        return registerSubscriber(topic1, otherTopics)
            .getSubscriber()
            .getContext()
            .getValuesForMatchingTopics();
      }

      @Test
      public void shouldReturnImmutableCollection() {
        final Multimap<Topic<?>, ?> valuesByTopic = getValuesForMatchingTopics(TOPIC_1, TOPIC_2);

        assertThrows(UnsupportedOperationException.class, () -> valuesByTopic.clear());
      }

      @Test
      public void shouldReturnEmptyCollectionWhenNoValuesHaveBeenPublished() {
        registerPublisher(TOPIC_1);
        registerPublisher(TOPIC_2);

        final Multimap<Topic<?>, ?> valuesByTopic = getValuesForMatchingTopics(TOPIC_1, TOPIC_2);

        assertThat(valuesByTopic.keySet(), is(empty()));
      }

      @Test
      public void shouldReturnPublishedValuesWhenValuesHaveBeenPublished() {
        registerPublisher(TOPIC_1).getPublisher().publish(42);
        registerPublisher(TOPIC_1).getPublisher().publish(2112);
        registerPublisher(TOPIC_2).getPublisher().publish("foo");
        registerPublisher(TOPIC_2);
        registerPublisher(TOPIC_3);

        final Multimap<Topic<?>, ?> valuesByTopic = getValuesForMatchingTopics(TOPIC_1, TOPIC_2);

        assertThat(valuesByTopic.keySet(), hasSize(2));
        assertThat(valuesByTopic.get(TOPIC_1), hasSize(2));
        assertThat(valuesByTopic.get(TOPIC_1), containsInAnyOrder(42, 2112));
        assertThat(valuesByTopic.get(TOPIC_2), hasSize(1));
        assertThat(valuesByTopic.get(TOPIC_2), containsInAnyOrder("foo"));
      }
    }

    @Nested
    public final class GetValuesForTopicTest {
      private <@NonNull T> Collection<T> getValuesForTopic(final Topic<T> topic) {
        return registerSubscriber(topic)
            .getSubscriber()
            .getContext()
            .getValuesForTopic(topic);
      }

      @Test
      public void shouldReturnImmutableCollection() {
        final Collection<?> topic1Values = getValuesForTopic(TOPIC_1);

        assertThrows(UnsupportedOperationException.class, () -> topic1Values.clear());
      }

      @Test
      public void shouldReturnEmptyCollectionWhenNoValuesHaveBeenPublished() {
        registerPublisher(TOPIC_1);

        final Collection<?> topic1Values = getValuesForTopic(TOPIC_1);

        assertThat(topic1Values, is(empty()));
      }

      @Test
      public void shouldReturnPublishedValuesWhenValuesHaveBeenPublished() {
        registerPublisher(TOPIC_1).getPublisher().publish(42);
        registerPublisher(TOPIC_1).getPublisher().publish(2112);

        final Collection<?> topic1Values = getValuesForTopic(TOPIC_1);

        assertThat(topic1Values, hasSize(2));
        assertThat(topic1Values, containsInAnyOrder(42, 2112));
      }
    }
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
    public void initializeTopicsChangedConsumer() {
      doNothing().when(topicsChangedConsumer).accept(topicsCaptor.capture());
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
      clearInvocations(new Object[] { topicsChangedConsumer });
      action.run();
    }

    private void thenSubscriberShouldBeNotifiedOfTopicChanges(final @NonNull Topic<?>... topics) {
      verify(topicsChangedConsumer).accept(ImmutableSet.copyOf(topics));
    }

    private void thenSubscriberShouldNotBeNotifiedOfAnyTopicChanges() {
      verify(topicsChangedConsumer, never()).accept(any());
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
      final SubscriberToken<FakeSubscriber> subscriberToken = registerSubscriber(TOPIC_1);

      when(() -> {
        subscriberToken.unregister();
        publisherToken.getPublisher().publish(42);
      });

      thenSubscriberShouldNotBeNotifiedOfAnyTopicChanges();
    }
  }
}
