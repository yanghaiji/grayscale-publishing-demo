package com.javayh.api.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * -Dserver.port=8092
 * </p>
 *
 * @author hai ji
 * @version 1.0.0
 * @since 2023-03-27
 */
@RestController
@RequestMapping(value = "/system/api/")
public class GraLbController {

    @Value("${server.port}")
    private Integer port;

    @GetMapping(value = "test")
    public String test() {
        return String.format("port=%s", port);
    }
}
