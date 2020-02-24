package de.bonndan.nivio.output;

import de.bonndan.nivio.model.LandscapeItem;
import de.bonndan.nivio.output.icons.Icons;
import de.bonndan.nivio.util.URLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static de.bonndan.nivio.output.icons.Icons.DEFAULT_ICON;

@Component
public class LocalServer implements EnvironmentAware {

    public static final String VENDORICONS_PATH = "/vendoricons";
    private static Environment env;
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalServer.class);
    public static final String VENDOR_PREFIX = "vendor://";

    private final Map<String, URL> vendorIcons = new HashMap<>();
    private final URL defaultIcon;

    private String imageProxy;

    /**
     * without slash
     */
    private final String baseUrl;

    public LocalServer(@Value("${nivio.baseUrl:}") String baseUrl) {
        if (!StringUtils.isEmpty(baseUrl)) {
            this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length()-1) : baseUrl;
        } else {
            this.baseUrl = "http://" + host() + ":" + port();
        }

        try {
            //http://www.apache.org/foundation/marks/
            vendorIcons.put("apache/httpd", new URL("http://www.apache.org/logos/res/httpd/httpd.png"));
            vendorIcons.put("redhat/keycloak", new URL("https://raw.githubusercontent.com/keycloak/keycloak-misc/master/logo/keycloak_icon_256px.png"));
            vendorIcons.put("k8s", new URL("https://raw.githubusercontent.com/kubernetes/kubernetes/master/logo/logo.png"));
            //https://redis.io/topics/trademark
            vendorIcons.put("redis", new URL("http://download.redis.io/logocontest/82.png"));

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        defaultIcon = getUrl(DEFAULT_ICON.getName());
    }

    /**
     * Returns the current publically visible url.
     *
     * @param path path to add
     * @return url with host, port
     */
    public URL getUrl(String path) {
        try {
            return new URL(baseUrl + (path.startsWith("/") ? path : "/" + path));
        } catch (MalformedURLException ignored) {
            LOGGER.warn("Failed to build url for {}", path);
            return defaultIcon;
        }
    }


    public URL getIconUrl(LandscapeItem item) {

        if (!StringUtils.isEmpty(item.getIcon())) {

            if (item.getIcon().startsWith(VENDOR_PREFIX)) {
                String key = item.getIcon().replace(VENDOR_PREFIX, "").toLowerCase();
                return proxiedUrl(vendorIcons.get(key));
            }

            URL iconUrl = getIconUrl(item.getIcon());
            return iconUrl != null ? iconUrl : getUrl(item.getIcon());
        }


        if (StringUtils.isEmpty(item.getType())) {
            return getUrl(DEFAULT_ICON.getName());
        }

        //fallback to service
        Icons icon = Icons.of(item.getType().toLowerCase()).orElse(DEFAULT_ICON);
        return getUrl(icon.getName());
    }


    /**
     * Provides an URL for a locally served icon.
     */
    private URL getIconUrl(String icon) {
        URL url = URLHelper.getURL(icon);

        //local icon urls are not supported
        if (url != null && URLHelper.isLocal(url)) {
            url = null;
        }

        if (url == null) {
            return getUrl("/icons/" + icon + ".png");
        }

        return proxiedUrl(url);
    }

    private URL proxiedUrl(URL url) {

        if (imageProxy == null){
            URL imageProxy = getUrl(VENDORICONS_PATH);
            setImageProxy(imageProxy.toString());
        }

        if (!StringUtils.isEmpty(imageProxy)) {
            try {
                return new URL(imageProxy + "//" + url.toString());
            } catch (MalformedURLException e) {
                LOGGER.error("Failed to build image proxy url from {}", imageProxy + "//" + url.getPath());
            }
        }
        return url;
    }

    public void setImageProxy(String imageProxy) {
        this.imageProxy = imageProxy;
    }

    private static String host() {
        return InetAddress.getLoopbackAddress().getHostName();
    }

    private static String port() {

        if (env != null) {
            String port = env.getProperty("local.server.port");
            if (port != null && Integer.valueOf(port) != 0)
                return port;
        }

        return "8080";
    }

    @Override
    public void setEnvironment(Environment environment) {
        env = environment;
    }
}
