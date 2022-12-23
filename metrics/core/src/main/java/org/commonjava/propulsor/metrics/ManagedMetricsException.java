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
package org.commonjava.propulsor.metrics;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.text.MessageFormat;

public class ManagedMetricsException
        extends Exception {

    private static final long serialVersionUID = 1L;

    private Object[] params;

    private transient String formattedMessage;

    public ManagedMetricsException( String format, Throwable cause, Object... params) {
        super(format, cause);
        this.params = params;
    }

    public ManagedMetricsException( String format, Object... params) {
        super(format);
        this.params = params;
    }

    @Override
    public synchronized String getMessage() {
        if (formattedMessage == null) {
            final String format = super.getMessage();
            if (params == null || params.length < 1) {
                formattedMessage = format;
            } else {
                final String original = formattedMessage;
                try {
                    formattedMessage = String.format(
                            format.replaceAll("\\{\\}", "%s"), params);
                } catch (final Error e) {
                } catch (final RuntimeException e) {
                } catch (final Exception e) {
                }

                if (formattedMessage == null || original == formattedMessage) {
                    try {
                        formattedMessage = MessageFormat.format(format, params);
                    } catch (final Error e) {
                        formattedMessage = format;
                        throw e;
                    } catch (final RuntimeException e) {
                        formattedMessage = format;
                        throw e;
                    } catch (final Exception e) {
                        formattedMessage = format;
                    }
                }
            }
        }

        return formattedMessage;
    }

    /**
     * Stringify all parameters pre-emptively on serialization, to prevent
     * {@link NotSerializableException}. Since all parameters are used in
     * {@link String#format} or {@link MessageFormat#format}, flattening them to
     * strings is an acceptable way to provide this functionality without making
     * the use of {@link Serializable} viral.
     */
    private Object writeReplace() {
        final Object[] newParams = new Object[params.length];
        int i = 0;
        for (final Object object : params) {
            newParams[i] = String.valueOf(object);
            i++;
        }

        params = newParams;
        return this;
    }
}
