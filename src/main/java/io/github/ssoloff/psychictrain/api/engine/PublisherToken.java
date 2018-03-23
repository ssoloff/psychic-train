package io.github.ssoloff.psychictrain.api.engine;

import org.eclipse.jdt.annotation.NonNull;

public interface PublisherToken<@NonNull P extends Publisher> {
  P getPublisher();

  void unregister();
}
