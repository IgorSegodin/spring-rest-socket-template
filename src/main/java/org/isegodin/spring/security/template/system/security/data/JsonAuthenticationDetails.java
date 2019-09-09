package org.isegodin.spring.security.template.system.security.data;

import lombok.Builder;
import lombok.Data;

/**
 * @author isegodin
 */
@Data
@Builder
public class JsonAuthenticationDetails {

    private final String login;
}
