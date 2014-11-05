package io.searchbox.client.config.idle;

import com.google.common.util.concurrent.AbstractScheduledService;
import io.searchbox.client.config.ClientConfig;

/**
 * Used to reap idle connections from the connection manager.
 */
public class IdleConnectionReaper extends AbstractScheduledService {

    private final ReapableConnectionManager reapableConnectionManager;
    private final ClientConfig clientConfig;

    public IdleConnectionReaper(ClientConfig clientConfig, ReapableConnectionManager reapableConnectionManager) {
        this.reapableConnectionManager = reapableConnectionManager;
        this.clientConfig = clientConfig;
    }

    @Override
    protected void runOneIteration() throws Exception {
        reapableConnectionManager.closeIdleConnections(clientConfig.getMaxConnectionIdleTime(),
                                                       clientConfig.getMaxConnectionIdleTimeDurationTimeUnit());
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedDelaySchedule(0l,
                                               clientConfig.getMaxConnectionIdleTime(),
                                               clientConfig.getMaxConnectionIdleTimeDurationTimeUnit());
    }
}
