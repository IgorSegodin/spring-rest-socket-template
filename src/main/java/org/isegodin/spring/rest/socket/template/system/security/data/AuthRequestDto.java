package org.isegodin.spring.rest.socket.template.system.security.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author isegodin
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequestDto {

    private String username;
    private String password;
}
