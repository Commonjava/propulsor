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
package org.commonjava.propulsor.metrics.zabbix;

import org.commonjava.propulsor.metrics.ManagedMetricsException;

/**
 * Created by jdcasey on 4/4/18.
 */
public class ZabbixDataException
        extends ManagedMetricsException
{
    public ZabbixDataException( final String format, final Throwable cause, final Object... params )
    {
        super( format, cause, params );
    }

    public ZabbixDataException( final String format, final Object... params )
    {
        super( format, params );
    }
}
