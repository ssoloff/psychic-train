package io.github.ssoloff.psychictrain.engine.impl;

import io.github.ssoloff.psychictrain.engine.AbstractBrokerTestCase;
import io.github.ssoloff.psychictrain.engine.Broker;

public final class DefaultBrokerAsBrokerTest extends AbstractBrokerTestCase {
  @Override
  protected Broker createBroker() {
    return new DefaultBroker();
  }
}
