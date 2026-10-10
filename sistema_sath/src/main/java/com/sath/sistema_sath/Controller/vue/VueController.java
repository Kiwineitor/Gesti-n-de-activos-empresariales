package com.sath.sistema_sath.Controller.vue;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class VueController {


    @GetMapping({
            "/",
            "/login",
            "/{path:[^\\.]*}",
            "/{path1:[^\\.]*}/{path2:[^\\.]*}"
    })
    public String forwardVue() {
        return "forward:/index.html";
    }
}
