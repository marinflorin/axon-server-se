/*
 * Copyright (c) 2017-2019 AxonIQ B.V. and/or licensed to AxonIQ B.V.
 * under one or more contributor license agreements.
 *
 *  Licensed under the AxonIQ Open Source License Agreement v1.0;
 *  you may not use this file except in compliance with the license.
 *
 */

package io.axoniq.axonserver.metric;

import io.micrometer.core.instrument.distribution.HistogramSnapshot;

/**
 * Created by Sara Pellegrini on 18/04/2018.
 * sara.pellegrini@gmail.com
 */
public class SnapshotMetric implements ClusterMetric {

    private final HistogramSnapshot snapshot;

    public SnapshotMetric(HistogramSnapshot snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public long size() {
        return snapshot.count();
    }

    @Override
    public long min() {
        return 0L;
    }

    @Override
    public long max() {
        return (long)snapshot.max();
    }

    @Override
    public double mean() {
        return snapshot.mean();
    }
}
