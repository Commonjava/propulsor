/**
 * Copyright (C) 2015 John Casey (jdcasey@commonjava.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.commonjava.propulsor.metrics;

public class MetricsConstants
{
    public static final String EXCEPTION = "exception";

    public static final String METER = "meter";

    public static final String TIMER = "timer";

    public static final String DEFAULT = "default";

    public static final String SKIP_METRIC = "skip-this-metric";

    public static final String CUMULATIVELY_METERED = "cumulatively-metered";

    public static final String CUMULATIVE_TIMINGS = "cumulative-timings";

    public static final String CUMULATIVE_COUNTS = "cumulative-counts";

    // for measuring transfer rates...
    public static final double NANOS_PER_SEC = 1E9;

    // for measuring timing in ms...
    public static final double NANOS_PER_MILLISECOND = 1E6;
}
