package it.uniroma3.siw.controller;

import it.uniroma3.siw.controller.validator.AnomaliaValidator;
import it.uniroma3.siw.model.Anomalia;
import it.uniroma3.siw.model.TipoDiAnomalia;
import it.uniroma3.siw.model.Video;
import it.uniroma3.siw.service.AnomaliaService;
import it.uniroma3.siw.service.UserService;
import it.uniroma3.siw.service.VideoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Controller
public class AnomaliaController {

    @Autowired
    private VideoService videoService;

    @Autowired
    private AnomaliaService anomaliaService;
    @Autowired
    private UserService userService;
    @Autowired
    private AnomaliaValidator anomaliaValidator;

    @GetMapping("/video/{videoId}/addAnomalia")
    public String aggiungiAnomalia(@PathVariable Long videoId, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isSupervisor = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("SUPERVISOR"));
        if (isSupervisor) {
            return "redirect:/";
        }
        model.addAttribute("anomalia", new Anomalia());
        model.addAttribute("video_id", videoId);
        model.addAttribute("user", userService.getCurrentUser());
        return "user/formNewAnomalia";
    }

    @PostMapping("/video/{videoId}/addAnomalia")
    public String salvaAnomalia(@PathVariable Long videoId,
                                @RequestParam(required = false) TipoDiAnomalia tipoAnomalia,
                                @Valid @ModelAttribute Anomalia anomalia,
                                BindingResult bindingResult,
                                Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isSupervisor = auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("SUPERVISOR"));
        if(isSupervisor){
            return "redirect:/";
        }

        anomalia.setTipoAnomalia(tipoAnomalia); //la setto prima così posso validarla

        anomaliaValidator.validate(anomalia, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", userService.getCurrentUser());
            model.addAttribute("anomalia", anomalia);
            model.addAttribute("video_id", videoId);
            return "user/formNewAnomalia";
        }
        Video video = videoService.getById(videoId);
        if (video == null) {
            return "redirect:/error"; // o pagina custom di errore
        }

        anomalia.setVideo(video);
        anomalia.setUser(userService.getCurrentUser());

        anomaliaService.save(anomalia);

        return "redirect:/tratta/" + video.getTratta().getId();
    }

    @PostMapping("/admin/anomalia/{anomalia_id}/updateRisolta")
    public String aggiornaRisolta(
            @PathVariable Long anomalia_id,
            @RequestParam(value = "risolta", required = false) Boolean risolta) {
        Anomalia anomalia = anomaliaService.getById(anomalia_id);
        if (anomalia != null) {
            anomalia.setRisolta(Boolean.TRUE.equals(risolta)); // imposta false se null
            anomaliaService.save(anomalia);
        }


        return "redirect:/tratta/" + anomalia.getVideo().getTratta().getId() + "#listaAnomalie";
    }

}
