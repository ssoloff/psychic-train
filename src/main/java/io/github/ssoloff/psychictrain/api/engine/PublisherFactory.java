package io.github.ssoloff.psychictrain.api.engine;

import org.eclipse.jdt.annotation.NonNull;

@FunctionalInterface
public interface PublisherFactory<@NonNull P extends Publisher> {
  P newPublisher(PublisherContext publisherContext);
}
