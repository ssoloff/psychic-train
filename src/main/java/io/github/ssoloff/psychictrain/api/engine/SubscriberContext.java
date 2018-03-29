package io.github.ssoloff.psychictrain.api.engine;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.Multimap;

public interface SubscriberContext {
  Multimap<Topic<?>, ?> getValuesForMatchingTopics();

  <@NonNull T> Collection<T> getValuesForTopic(Topic<T> topic);
}
