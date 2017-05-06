package douex.dou;

import lombok.Value;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contains information about a {@link Company} office: 'city' and office 'emails'.
 */
@Value
public class Office {
    private String city;
    private Set<String> emails = new HashSet<>();
    
    /**
     * @return {@link String} with coma separated office emails
     */
    public String getEmailsInStr() {
        return emails.stream().collect(Collectors.joining(","));
    }
}
