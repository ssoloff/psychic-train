package io.github.ssoloff.psychictrain.internal.engine;

import io.github.ssoloff.psychictrain.api.engine.AbstractBrokerTestCase;
import io.github.ssoloff.psychictrain.api.engine.Broker;

public final class DefaultBrokerAsBrokerTest extends AbstractBrokerTestCase {
  @Override
  protected Broker createBroker() {
    return new DefaultBroker();
  }
}
