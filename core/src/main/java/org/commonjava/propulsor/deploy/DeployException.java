package org.commonjava.propulsor.deploy;

import org.commonjava.propulsor.boot.BootException;

public class DeployException extends BootException
{
    public DeployException( String message, Object... params )
    {
        super( message, params );
    }

    public DeployException( String message, Throwable cause, Object... params )
    {
        super( message, cause, params );
    }
}
