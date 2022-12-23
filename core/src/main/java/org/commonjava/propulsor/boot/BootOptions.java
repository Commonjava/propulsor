/**
 * Copyright (C) 2014-2022 Red Hat, Inc. (http://github.com/Commonjava/commonjava)
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

import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.PropertiesBasedValueSource;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static org.apache.commons.lang.StringUtils.isEmpty;

/**
 * BootOptions can be instantiated in
 * 1. specify a boot file via -Dboot.properties . The entries will be loaded into an internal properties;
 * 2. parse main's args
 * 3. via constructor and setters
 *
 * BootOptions contains applicationName, homeDir (the application's home) and 4 others
 * bind, port, config dir, context-path.
 *
 * The homeDir is not set in properties file. Rather, it is passed by a constructor parameter.
 * The config is the full path of main.conf. Default is <homeDir>/etc/{application}/main.conf
 */
public class BootOptions
{

    public static final String BIND_PROP = "bind";

    public static final String PORT_PROP = "port";

    public static final String CONFIG_PROP = "config";

    public static final String CONTEXT_PATH_PROP = "context-path";

    public static final String DEFAULT_BIND = "0.0.0.0";

    public static final int DEFAULT_PORT = 8080;

    @Option( name = "-h", aliases = { "--help" }, usage = "Print this and exit" )
    private boolean help;

    @Option( name = "-i", aliases = { "--interface", "--bind",
                    "--listen" }, usage = "Bind to a particular IP address (default: 0.0.0.0, or all available)" )
    private String bind;

    @Option( name = "-p", aliases = { "--port" }, usage = "Use different port (default: 8080)" )
    private Integer port;

    @Option( name = "-c", aliases = {
                    "--config" }, usage = "Use an alternative configuration file (default: <homeDir>/etc/{application}/main.conf)" )
    private String config;

    @Option( name = "-C", aliases = { "--context-path" }, usage = "Specify a root context path for all to use" )
    private String contextPath;

    private StringSearchInterpolator interp;

    private Properties props;

    private String applicationName; // application name, e.g, "indy", used for something like default conf dir

    private String homeDir;

    public String getApplicationName()
    {
        return applicationName;
    }

    public String getHomeSystemProperty()
    {
        return getApplicationName() + ".home"; // e.g. "indy.home"
    }

    /**
     * Environment variables are specified at the OS level.
     */
    public String getHomeEnvar()
    {
        return getHomeSystemProperty();
    }

    public String getConfigSystemProperty()
    {
        return getApplicationName() + ".config"; // e.g., "indy.config"
    }

    public BootOptions()
    {
    }

    public BootOptions( final String applicationName, final String homeDir )
    {
        this.applicationName = applicationName;
        this.homeDir = homeDir;
    }

    public BootOptions( final String application, final String homeDir, final File bootDefaults )
                    throws IOException, InterpolationException
    {
        this( application, homeDir );
        load( bootDefaults );
    }

    protected void loadApplicationOptions()
    {
    }

    protected void setApplicationSystemProperties( final Properties properties )
    {
    }

    public final void copyFrom( final BootOptions options )
    {
        this.applicationName = options.applicationName;
        this.help = options.help;
        this.config = options.config;
        this.interp = options.interp;
        this.props = options.props;
        this.bind = options.bind;
        this.port = options.port;
        this.config = options.config;
        this.contextPath = options.contextPath;
    }

    public void load( final File bootDefaults ) throws IOException, InterpolationException
    {
        this.props = new Properties();

        if ( bootDefaults != null && bootDefaults.exists() )
        {
            try (FileInputStream stream = new FileInputStream( bootDefaults ))
            {
                props.load( stream );
            }
        }

        bind = resolve( props.getProperty( BIND_PROP, DEFAULT_BIND ) );
        port = Integer.parseInt( resolve( props.getProperty( PORT_PROP, Integer.toString( DEFAULT_PORT ) ) ) );

        String defaultConfigPath = new File( homeDir, "etc/" + getApplicationName() + "/main.conf" ).getPath();
        config = resolve( props.getProperty( CONFIG_PROP, defaultConfigPath ) );
        contextPath = normalizeContextPath( props.getProperty( CONTEXT_PATH_PROP, contextPath ) );

        loadApplicationOptions();
    }

    private String normalizeContextPath( String contextPath )
    {
        if ( contextPath == null )
        {
            return "";
        }
        else if ( contextPath.startsWith( "/" ) )
        {
            return contextPath.substring( 1 );
        }
        return contextPath;
    }

    public final void setSystemProperties()
    {
        final Properties properties = System.getProperties();

        setProperty( properties, getHomeSystemProperty(), getHomeDir() );
        setProperty( properties, getConfigSystemProperty(), getConfig() );
        setApplicationSystemProperties( properties );

        System.setProperties( properties );
    }

    private void setProperty( Properties properties, String key, String value )
    {
        if ( isEmpty( value ) )
        {
            throw new IllegalStateException( key + " is not specified." );
        }

        properties.setProperty( key, value );
    }

    public final String resolve( final String value ) throws InterpolationException
    {
        if ( value == null || value.trim().length() < 1 )
        {
            return null;
        }

        if ( props == null )
        {
            if ( homeDir == null )
            {
                return value;
            }
            else
            {
                props = new Properties();
            }
        }

        props.setProperty( getHomeSystemProperty(), homeDir );

        if ( interp == null )
        {
            interp = new StringSearchInterpolator();
            interp.addValueSource( new PropertiesBasedValueSource( props ) );
        }

        return interp.interpolate( value );
    }

    public boolean parseArgs( final String[] args ) throws BootException
    {
        final CmdLineParser parser = new CmdLineParser( this );
        boolean canStart = true;
        try
        {
            parser.parseArgument( args );
        }
        catch ( final CmdLineException e )
        {
            throw new BootException( "Failed to parse command-line args: %s", e, e.getMessage() );
        }

        if ( isHelp() )
        {
            printUsage( parser, null );
            canStart = false;
        }

        return canStart;
    }

    public void printUsage( final CmdLineParser parser, final CmdLineException error )
    {
        if ( error != null )
        {
            System.err.println( "Invalid option(s): " + error.getMessage() );
            System.err.println();
        }
        System.err.println( "Usage: $0 [OPTIONS] [<target-path>]" );
        System.err.println();
        System.err.println();
        parser.printUsage( System.err );
        System.err.println();
    }

    public String getHomeDir()
    {
        return homeDir == null ? System.getProperty( getHomeSystemProperty() ) : homeDir;
    }

    public void setHomeDir( final String homeDir )
    {
        this.homeDir = homeDir;
    }

    protected String getDefaultConfigFile()
    {
        return new File( getHomeDir(), "etc/main.conf" ).getPath();
    }

    public void setProps( Properties props )
    {
        this.props = props;
    }

    public void setPort( Integer port )
    {
        this.port = port;
    }

    public Properties getProps()
    {
        return props;
    }

    public boolean isHelp()
    {
        return help;
    }

    public String getBind()
    {
        return bind;
    }

    public int getPort()
    {
        return port;
    }

    public String getConfig()
    {
        return config == null ? getDefaultConfigFile() : config;
    }

    public void setHelp( final boolean help )
    {
        this.help = help;
    }

    public void setBind( final String bind )
    {
        this.bind = bind;
    }

    public void setPort( final int port )
    {
        this.port = port;
    }

    public void setConfig( final String config )
    {
        this.config = config;
    }

    public String getContextPath()
    {
        return contextPath;
    }

    public void setContextPath( final String contextPath )
    {
        this.contextPath = normalizeContextPath( contextPath );
    }

}
