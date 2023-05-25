package be.demo.normalizephone.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Configuration
@Slf4j
public class SwaggerConfig {

    private static final String[] HTTPS = new String[]{"https"};
    private static final String[] HTTP = new String[]{"http"};

    private final Environment environment;

    @Autowired
    public SwaggerConfig(Environment environment) {
        this.environment = environment;
    }


    @Bean
    public OpenAPI serviceApiDocumentation() {
        final var apiDoc = new OpenAPI();
        defineServers(apiDoc);
        return apiDoc;
    }

    @Bean
    public OpenApiCustomiser sortTagsAlphabetically() {
        return openApi -> {
            if (openApi.getTags() != null) {
                openApi.setTags(openApi.getTags().stream().sorted(Comparator.comparing(tag -> StringUtils.stripAccents(tag.getName()))).collect(Collectors.toList()));
            }
        };
    }

    private void defineServers(final OpenAPI apiDoc) {
        String activeProfile = "local";
        for (String profileName : environment.getActiveProfiles()) {
            log.info("Currently active profile - " + profileName);
            activeProfile = profileName;
        }

        if (activeProfile.equalsIgnoreCase("local")) {
            Arrays.stream(HTTP).map(protocol -> new DeferredServer().protocol(protocol).description(protocol.toUpperCase())).forEach(apiDoc::addServersItem);
        } else {
            Arrays.stream(HTTPS).map(protocol -> new DeferredServer().protocol(protocol).description(protocol.toUpperCase())).forEach(apiDoc::addServersItem);
        }
    }

    private static class DeferredServer extends Server {
        private String protocol;

        private static boolean isValidIPAddress(String ip) {
            // Regex for digit from 0 to 255.
            String zeroTo255 = "(\\d{1,2}|([01])\\" + "d{2}|2[0-4]\\d|25[0-5])";

            // Regex for a digit from 0 to 255 and
            // followed by a dot, repeat 4 times.
            // this is the regex to validate an IP address.
            String regex = zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255 + "\\." + zeroTo255;

            // Compile the ReGex
            Pattern p = Pattern.compile(".*" + regex + ".*");

            // If the IP address is empty
            // return false
            if (ip == null) {
                return false;
            }

            if (ip.toLowerCase().startsWith("http://localhost")) {
                return true;
            }

            // Pattern class contains matcher() method
            // to find matching between given IP address
            // and regular expression.
            Matcher m = p.matcher(ip);

            // Return if the IP address
            // matched the ReGex
            return m.matches();
        }

        @Override
        public String getUrl() {
            String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
            if (!protocol.equals("http") && isValidIPAddress(baseUrl)) {
                protocol = "http";
            }
            return ServletUriComponentsBuilder.fromCurrentContextPath().scheme(protocol).toUriString();
        }

        public DeferredServer protocol(final String protocol) {
            this.protocol = protocol;
            return this;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DeferredServer)) return false;
            if (!super.equals(o)) return false;
            DeferredServer that = (DeferredServer) o;
            return Objects.equals(protocol, that.protocol);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), protocol);
        }

    }

}