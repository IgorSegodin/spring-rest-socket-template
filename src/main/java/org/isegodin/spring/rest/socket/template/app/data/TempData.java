package org.isegodin.spring.rest.socket.template.app.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author isegodin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TempData {
    private String name;
    private long timestamp;
}
