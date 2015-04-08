package org.commonjava.propulsor.boot;

import java.io.NotSerializableException;
import java.io.Serializable;
import java.text.MessageFormat;

public class BootException extends Exception {

    private static final long serialVersionUID = 1L;

    private Object[] params;

    private transient String formattedMessage;

    public BootException(String format, Throwable cause, Object... params) {
        super(format, cause);
        this.params = params;
    }

    public BootException(String format, Object... params) {
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
