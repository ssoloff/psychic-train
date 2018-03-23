package io.github.ssoloff.psychictrain.engine;

public interface Subscriber {
  void topicChanged(Topic<?> topic);
}
