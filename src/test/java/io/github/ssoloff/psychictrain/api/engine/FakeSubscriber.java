package io.github.ssoloff.psychictrain.api.engine;

import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class FakeSubscriber implements Subscriber {
  private final SubscriberContext context;
  private final Consumer<Set<Topic<?>>> topicsChangedConsumer;

  private FakeSubscriber(final SubscriberContext context, final Consumer<Set<Topic<?>>> topicsChangedConsumer) {
    this.context = context;
    this.topicsChangedConsumer = topicsChangedConsumer;
  }

  public SubscriberContext getContext() {
    return context;
  }

  public static SubscriberFactory<FakeSubscriber> newFactory(final Consumer<Set<Topic<?>>> topicsChangedConsumer) {
    return subscriberContext -> new FakeSubscriber(subscriberContext, topicsChangedConsumer);
  }

  @Override
  public void topicsChanged(final Set<Topic<?>> topics) {
    topicsChangedConsumer.accept(topics);
  }
}
