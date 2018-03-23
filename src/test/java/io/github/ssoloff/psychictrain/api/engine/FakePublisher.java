package io.github.ssoloff.psychictrain.api.engine;

import javax.annotation.concurrent.Immutable;

import org.eclipse.jdt.annotation.NonNull;

// TODO: consider removing type information; need to see how real publishers
// might use that information and if we're going to add it back to
// registerPublisher() in order to provide compile-time safety.
@Immutable
public final class FakePublisher<@NonNull T> implements Publisher {
  private final PublisherContext context;

  public FakePublisher(final PublisherContext context) {
    this.context = context;
  }

  public static <@NonNull T> PublisherFactory<FakePublisher<T>> newFactory() {
    return publisherContext -> new FakePublisher<>(publisherContext);
  }

  public void publish(final T value) {
    context.publish(value);
  }
}
