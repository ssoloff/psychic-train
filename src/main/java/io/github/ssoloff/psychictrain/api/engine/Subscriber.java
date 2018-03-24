package io.github.ssoloff.psychictrain.api.engine;

import java.util.Set;

public interface Subscriber {
  void topicsChanged(Set<Topic<?>> topics);
}
