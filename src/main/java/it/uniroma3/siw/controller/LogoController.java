package it.uniroma3.siw.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogoController {

    @GetMapping("/logo")
    public String showLogoPage() {
        // Restituisce il template appena creato
        return "logo/index";
    }
}
