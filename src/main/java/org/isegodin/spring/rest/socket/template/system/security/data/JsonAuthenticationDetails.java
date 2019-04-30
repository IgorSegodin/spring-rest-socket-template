package org.isegodin.spring.rest.socket.template.system.security.data;

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
