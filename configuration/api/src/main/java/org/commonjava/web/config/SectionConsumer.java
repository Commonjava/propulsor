package org.commonjava.web.config;

/**
 * Created by jdcasey on 3/22/18.
 */
public interface SectionConsumer
{
    void configurationParsed()
            throws ConfigurationException;

    boolean sectionStarted( final String name )
            throws ConfigurationException;

    void sectionComplete( final String name )
            throws ConfigurationException;

    void parameter( final String section, final String name, final String value )
            throws ConfigurationException;
}
