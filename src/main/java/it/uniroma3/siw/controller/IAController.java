package it.uniroma3.siw.controller;

import it.uniroma3.siw.service.AnomaliaService;
import it.uniroma3.siw.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IAController {

    @Autowired
    private AnomaliaService anomaliaService;

    @Autowired
    private UserService userService; // <-- aggiungi questa riga

    @GetMapping("/ia/alerts")
    public String getIAAlerts(Model model) {
        model.addAttribute("anomalie", anomaliaService.getAll());
        model.addAttribute("user", userService.getCurrentUser()); // ora funziona
        return "ia/alerts";
    }
}
