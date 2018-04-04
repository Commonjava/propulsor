/**
 * Copyright (C) 2011-2017 Red Hat, Inc. (https://github.com/Commonjava/indy)
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
package org.commonjava.propulsor.metrics.zabbix.reporter;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metered;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;
import org.commonjava.propulsor.metrics.ManagedMetricsException;
import org.commonjava.propulsor.metrics.conf.MetricsConfig;
import org.commonjava.propulsor.metrics.zabbix.conf.ZabbixReporterConfig;
import org.commonjava.propulsor.metrics.zabbix.socket.DataObject;
import org.commonjava.propulsor.metrics.zabbix.socket.SocketResult;
import org.commonjava.propulsor.metrics.zabbix.socket.ZabbixJsonRpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by xiabai on 3/29/17.
 */
@ApplicationScoped
public class ZabbixReporter
                extends ScheduledReporter
{
    private static final Logger logger = LoggerFactory.getLogger( ZabbixReporter.class );

    private final ZabbixJsonRpcClient jsonRpcClient;

    private final ZabbixReporterConfig config;

    private final MetricsConfig metricsConfig;

    public ZabbixReporter( ZabbixJsonRpcClient jsonRpcClient, final MetricRegistry registry, final MetricFilter filter,
                           final ScheduledExecutorService executor, ZabbixReporterConfig config, MetricsConfig metricsConfig )
    {
        super( registry, "Zabbix", filter, config.getRateUnit(), config.getDurationUnit(), executor );
        this.jsonRpcClient = jsonRpcClient;
        this.config = config;
        this.metricsConfig = metricsConfig;
    }

    private DataObject toDataObject( String key, String keySuffix, Object value, long clock )
    {
        return DataObject.builder()
                         .host( config.getHost() )
                         .key( metricsConfig.getInstancePrefix() + key + keySuffix )
                         .clock( clock )
                         .value( String.valueOf( value ) )
                         .build();
    }

    /**
     * for histograms.
     *
     * @param key
     * @param snapshot
     * @param dataObjectList
     */
    private void addSnapshotDataObject( String key, Snapshot snapshot, long clock, List<DataObject> dataObjectList )
    {
        dataObjectList.add( toDataObject( key, ".min", snapshot.getMin(), clock ) );
        dataObjectList.add( toDataObject( key, ".max", snapshot.getMax(), clock ) );
        dataObjectList.add( toDataObject( key, ".mean", snapshot.getMean(), clock ) );
        dataObjectList.add( toDataObject( key, ".stddev", snapshot.getStdDev(), clock ) );
        dataObjectList.add( toDataObject( key, ".median", snapshot.getMedian(), clock ) );
        dataObjectList.add( toDataObject( key, ".75th", snapshot.get75thPercentile(), clock ) );
        dataObjectList.add( toDataObject( key, ".95th", snapshot.get95thPercentile(), clock ) );
        dataObjectList.add( toDataObject( key, ".98th", snapshot.get98thPercentile(), clock ) );
        dataObjectList.add( toDataObject( key, ".99th", snapshot.get99thPercentile(), clock ) );
        dataObjectList.add( toDataObject( key, ".99.9th", snapshot.get999thPercentile(), clock ) );
    }

    /**
     * for timer.
     *
     * @param key
     * @param snapshot
     * @param dataObjectList
     */
    private void addSnapshotDataObjectWithConvertDuration( String key, Snapshot snapshot, long clock,
                                                           List<DataObject> dataObjectList )
    {
        dataObjectList.add( toDataObject( key, ".min", convertDuration( snapshot.getMin() ), clock ) );
        dataObjectList.add( toDataObject( key, ".max", convertDuration( snapshot.getMax() ), clock ) );
        dataObjectList.add( toDataObject( key, ".mean", convertDuration( snapshot.getMean() ), clock ) );
        dataObjectList.add( toDataObject( key, ".stddev", convertDuration( snapshot.getStdDev() ), clock ) );
        dataObjectList.add( toDataObject( key, ".median", convertDuration( snapshot.getMedian() ), clock ) );
        dataObjectList.add( toDataObject( key, ".75th", convertDuration( snapshot.get75thPercentile() ), clock ) );
        dataObjectList.add( toDataObject( key, ".95th", convertDuration( snapshot.get95thPercentile() ), clock ) );
        dataObjectList.add( toDataObject( key, ".98th", convertDuration( snapshot.get98thPercentile() ), clock ) );
        dataObjectList.add( toDataObject( key, ".99th", convertDuration( snapshot.get99thPercentile() ), clock ) );
        dataObjectList.add( toDataObject( key, ".99.9th", convertDuration( snapshot.get999thPercentile() ), clock ) );
    }

    private void addMeterDataObject( String key, Metered meter, long clock, List<DataObject> dataObjectList )
    {
        dataObjectList.add( toDataObject( key, ".count", meter.getCount(), clock ) );
        dataObjectList.add( toDataObject( key, ".meanRate", convertRate( meter.getMeanRate() ), clock ) );
        dataObjectList.add( toDataObject( key, ".1-minuteRate", convertRate( meter.getOneMinuteRate() ), clock ) );
        dataObjectList.add( toDataObject( key, ".5-minuteRate", convertRate( meter.getFiveMinuteRate() ), clock ) );
        dataObjectList.add( toDataObject( key, ".15-minuteRate", convertRate( meter.getFifteenMinuteRate() ), clock ) );
    }

    @SuppressWarnings( "rawtypes" )
    @Override
    public void report( SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
                        SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters,
                        SortedMap<String, Timer> timers )
    {
        final long clock = System.currentTimeMillis() / 1000;
        List<DataObject> dataObjectList = new LinkedList<DataObject>();
        for ( Map.Entry<String, Gauge> entry : gauges.entrySet() )
        {
            DataObject dataObject =
                            toDataObject( entry.getKey(), "", String.valueOf( entry.getValue().getValue() ), clock );
            dataObjectList.add( dataObject );
        }

        for ( Map.Entry<String, Counter> entry : counters.entrySet() )
        {
            DataObject dataObject =
                            toDataObject( entry.getKey(), "", String.valueOf( entry.getValue().getCount() ), clock );
            dataObjectList.add( dataObject );
        }

        for ( Map.Entry<String, Histogram> entry : histograms.entrySet() )
        {
            Histogram histogram = entry.getValue();
            Snapshot snapshot = histogram.getSnapshot();
            addSnapshotDataObject( entry.getKey(), snapshot, clock, dataObjectList );
        }

        for ( Map.Entry<String, Meter> entry : meters.entrySet() )
        {
            Meter meter = entry.getValue();
            addMeterDataObject( entry.getKey(), meter, clock, dataObjectList );
        }

        for ( Map.Entry<String, Timer> entry : timers.entrySet() )
        {
            Timer timer = entry.getValue();
            addMeterDataObject( entry.getKey(), timer, clock, dataObjectList );
            addSnapshotDataObjectWithConvertDuration( entry.getKey(), timer.getSnapshot(), clock, dataObjectList );
        }

        try
        {
            SocketResult senderResult = jsonRpcClient.send( dataObjectList, clock );
            if ( !senderResult.success() )
            {
                logger.warn( "report metrics to zabbix not success!" + senderResult );
            }
            else if ( logger.isDebugEnabled() )
            {
                logger.info( "report metrics to zabbix success. " + senderResult );
            }
        }
        catch ( IOException | ManagedMetricsException e )
        {
            logger.error( String.format( "Could not send metrics to Zabbix: %s", e.getMessage() ), e );
        }
    }

}
