package douex.dou;

import lombok.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains information about DOU company:
 * <ul><li>name,
 * <li>url on the DOU.UA
 * <li>company description
 * <li>url for company offices on the DOU.UA
 * <li>and {@link Office} list
 */
@Value
public class Company {
    private String name;
    private String url;
    private String description;
    private String officesUrl;
    private List<Office> offices = new ArrayList<>();
}
