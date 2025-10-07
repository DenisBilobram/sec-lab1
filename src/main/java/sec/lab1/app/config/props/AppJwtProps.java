package sec.lab1.app.config.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public class AppJwtProps {
    private String secret;
    private int ttlMinutes;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public int getTtlMinutes() { return ttlMinutes; }
    public void setTtlMinutes(int ttlMinutes) { this.ttlMinutes = ttlMinutes; }
}
