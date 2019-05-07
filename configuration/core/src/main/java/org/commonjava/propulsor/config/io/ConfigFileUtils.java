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
package org.commonjava.propulsor.config.io;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.PropertiesBasedValueSource;
import org.codehaus.plexus.interpolation.StringSearchInterpolator;
import org.commonjava.propulsor.config.ConfigurationException;

import static org.apache.commons.io.FileUtils.readLines;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ConfigFileUtils
{

    private static final String LS = System.getProperty( "line.separator", "\n" );

    private static final String INCLUDE_COMMAND = "Include ";

    private static final String VARIABLES_COMMAND = "Variables ";

    private static final String GLOB_BASE_PATTERN = "([^\\?\\*]+)[\\\\\\/]([\\*\\?]+.+)";

    private static final String GLOB_IDENTIFYING_PATTERN = ".*[\\?\\*].*";

    private ConfigFileUtils()
    {
    }

    public static InputStream readFileWithIncludes( final String path )
            throws IOException, ConfigurationException
    {
        return readFileWithIncludes( new File( path ) );
    }

    public static InputStream readFileWithIncludes( final File f )
            throws IOException, ConfigurationException
    {
        final List<String> lines = readLinesWithIncludes( f, false );

        return new ByteArrayInputStream( join( lines, LS ).getBytes() );
    }

    private static String join( List<String> lines, String ls )
    {
        StringBuilder sb = new StringBuilder();
        lines.forEach( line->{
            if ( sb.length() > 0 )
            {
                sb.append( ls );
            }
            sb.append( line );
        } );

        return sb.toString();
    }

    public static List<String> readLinesWithIncludes( final File f )
            throws IOException, ConfigurationException
    {
        return readLinesWithIncludes( f, false );
    }

    public static List<String> readLinesWithIncludes( final File f, boolean ignoreVariables )
            throws IOException, ConfigurationException
    {
        Properties vars = new Properties();

        final List<String> lines = new ArrayList<String>();
        final File dir = f.getParentFile();
        for ( final String line : readLines( f ) )
        {
            if ( line.startsWith( INCLUDE_COMMAND ) )
            {
                final String glob = line.substring( INCLUDE_COMMAND.length() );
                for ( final File file : findMatching( dir, glob ) )
                {
                    lines.addAll( readLinesWithIncludes( file, true ) );
                }
            }
            else if ( !ignoreVariables && line.startsWith( VARIABLES_COMMAND ) )
            {
                final String glob = line.substring( VARIABLES_COMMAND.length() );
                InputStream fs = null;
                for ( final File file : findMatching( dir, glob ) )
                {
                    try
                    {
                        fs = new FileInputStream( file );
                        vars.load( fs );
                    }
                    finally
                    {
                        IOUtils.closeQuietly( fs );
                        fs = null;
                    }
                }
            }
            else
            {
                lines.add( line );
            }
        }

        return lines;
    }

    public static File[] findMatching( final File dir, String glob )
        throws IOException
    {
        if ( !glob.matches( GLOB_IDENTIFYING_PATTERN ) )
        {
            File f = new File( glob );
            if ( !f.isAbsolute() )
            {
                f = new File( dir, glob );
            }

            return new File[] { f };
        }

        final Matcher m = Pattern.compile( GLOB_BASE_PATTERN )
                                 .matcher( glob );
        String base = null;
        String pattern = null;
        if ( m.matches() )
        {
            base = m.group( 1 );
            pattern = m.group( 2 );

            if ( !new File( base ).isAbsolute() )
            {
                base = new File( dir, base ).getAbsolutePath();
            }
        }
        else
        {
            base = dir.getAbsolutePath();
            pattern = glob;
        }

        if ( pattern.length() < 1 )
        {
            return new File[] { new File( base ).getCanonicalFile() };
        }

        final StringBuilder regex = new StringBuilder();
        for ( int i = 0; i < pattern.length(); i++ )
        {
            final char c = pattern.charAt( i );
            switch ( c )
            {
                case '*':
                {
                    if ( i + 1 < pattern.length() && pattern.charAt( i + 1 ) == '*' )
                    {
                        regex.append( ".+" );
                        i++;
                    }
                    else
                    {
                        regex.append( "[^\\\\\\/]*" );
                    }
                    break;
                }
                case '.':
                {
                    regex.append( "\\." );
                    break;
                }
                case '?':
                {
                    regex.append( "." );
                    break;
                }
                default:
                {
                    regex.append( c );
                }
            }
        }

        final boolean dirsOnly = pattern.endsWith( "/" ) || pattern.endsWith( "\\" );
        final String globRegex = regex.toString();
        final File bdir = new File( base ).getCanonicalFile();
        final int bdirLen = bdir.getPath()
                                .length() + 1;

        final List<File> allFiles = listRecursively( bdir );
        for ( final Iterator<File> it = allFiles.iterator(); it.hasNext(); )
        {
            final File f = it.next();
            if ( dirsOnly && !f.isDirectory() )
            {
                it.remove();
                continue;
            }

            final String sub = f.getAbsolutePath()
                                .substring( bdirLen );

            if ( !sub.matches( globRegex ) )
            {
                it.remove();
            }
        }

        return allFiles.toArray( new File[] {} );
    }

    private static List<File> listRecursively( final File dir )
        throws IOException
    {
        final List<File> files = new ArrayList<File>();
        final File d = dir.getCanonicalFile();

        recurse( d, files );

        return files;
    }

    private static void recurse( final File dir, final List<File> files )
        throws IOException
    {
        if ( !dir.isDirectory() )
        {
            return;
        }

        for ( final String name : dir.list() )
        {
            final File f = new File( dir, name ).getCanonicalFile();

            files.add( f );
            if ( f.isDirectory() )
            {
                recurse( f, files );
            }
        }
    }

}
