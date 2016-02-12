package org.commonjava.propulsor.boot;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Random;

public final class PortFinder
{
    private static final Random RANDOM = new Random();

    private PortFinder()
    {
    }

    public static <T> T findPortFor( final int maxTries, PortConsumer<T> consumer )
    {
        for ( int i = 0; i < maxTries; i++ )
        {
            final int port = 1024 + ( Math.abs( RANDOM.nextInt() ) % 30000 );
            T result = null;
            try
            {
                return consumer.call( port );
            }
            catch ( RuntimeException e )
            {
                // handle Undertow BindException runtime wrapper...
                if ( !e.getMessage().contains( "Address already in use" ) )
                {
                    throw e;
                }
            }
            catch ( final IOException e )
            {
            }
        }

        throw new IllegalStateException( "Cannot find open port after " + maxTries + " attempts." );
    }

    public static int findOpenPort( final int maxTries )
    {
        for ( int i = 0; i < maxTries; i++ )
        {
            final int port = 1024 + ( Math.abs( RANDOM.nextInt() ) % 30000 );
            ServerSocket sock = null;
            try
            {
                sock = new ServerSocket( port );
                return port;
            }
            catch ( final IOException e )
            {
            }
            finally
            {
                IOUtils.closeQuietly( sock );
            }
        }

        throw new IllegalStateException( "Cannot find open port after " + maxTries + " attempts." );
    }

    public interface PortConsumer<T>
    {
        T call(int port) throws IOException;
    }

}
