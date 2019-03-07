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
package org.commonjava.propulsor.boot;

public class BootStatus
{

    public static final int ERR_LOAD_BOOT_OPTIONS = 1;

    public static final int ERR_PARSE_ARGS = 2;

    public static final int ERR_INIT = 3;

    public static final int ERR_LOAD_CONFIG = 4;

    public static final int ERR_START_LIFECYCLE = 5;

    public static final int ERR_DEPLOY = 6;

    public static final int ERR_START = 7;


    private Throwable error;

    private int exit;

    public BootStatus()
    {
        exit = -1;
    }

    public BootStatus( final int exit, final Throwable error )
    {
        markFailed( exit, error );
    }

    public void markFailed( final int exit, final Throwable error )
    {
        this.exit = exit;
        this.error = error;
    }

    public boolean isSet()
    {
        return exit > -1;
    }

    public boolean isFailed()
    {
        return exit > 0;
    }

    public boolean isSuccess()
    {
        return exit == 0;
    }

    public Throwable getError()
    {
        return error;
    }

    public int getExitCode()
    {
        return exit;
    }

    public void markSuccess()
    {
        exit = 0;
    }

}
