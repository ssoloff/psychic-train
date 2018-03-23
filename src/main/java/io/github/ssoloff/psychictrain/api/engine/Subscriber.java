package io.github.ssoloff.psychictrain.api.engine;

public interface Subscriber {
  void topicChanged(Topic<?> topic);
}
