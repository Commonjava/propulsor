package org.commonjava.propulsor.deploy.undertow;

public interface UndertowBootOptions
{

    UndertowBootOptions DEFAULT = new UndertowBootOptions()
    {
        @Override
        public String getContextPath()
        {
            return "/";
        }

        @Override
        public String getDeploymentName()
        {
            return "Web (Undertow)";
        }

        @Override
        public int getPort()
        {
            return 8080;
        }

        @Override
        public String getBind()
        {
            return "0.0.0.0";
        }
    };

    String getContextPath();

    String getDeploymentName();

    int getPort();

    String getBind();

}
