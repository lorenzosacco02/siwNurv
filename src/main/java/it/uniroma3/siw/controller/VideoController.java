package it.uniroma3.siw.controller;

import it.uniroma3.siw.controller.validator.VideoValidator;
import it.uniroma3.siw.model.Tratta;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.model.Video;
import it.uniroma3.siw.repository.VideoRepository;
import it.uniroma3.siw.service.TrattaService;
import it.uniroma3.siw.service.UserService;
import it.uniroma3.siw.service.VideoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static it.uniroma3.siw.model.Video.MAX_SIZE;

@Controller
public class VideoController {

    @Autowired
    private VideoService videoService;
    @Autowired
    private UserService userService;
    @Autowired
    private TrattaService trattaService;
    @Autowired
    private VideoValidator videoValidator;

    @GetMapping("/video/{id}/stream")
    public ResponseEntity<byte[]> streamVideo(@PathVariable Long id) {
        Video video = videoService.getById(id); // recupera il video dal DB

        if (video.getFile() == null || video.getFile().length == 0) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("video/mp4"));
        headers.setContentLength(video.getFile().length);

        return new ResponseEntity<>(video.getFile(), headers, HttpStatus.OK);
    }

    @GetMapping("/admin/addVideo")
    public String addVideo(Model model) {
        User currentUser = userService.getCurrentUser();
        boolean isSupervisor = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(a->a.getAuthority().equals("SUPERVISOR"));
        if(isSupervisor){
            Tratta tratta = trattaService.getBySupervisor(currentUser);
            if(tratta == null) return "redirect:/";
            model.addAttribute("video", new Video());
            model.addAttribute("user", currentUser);
//            model.addAttribute("tratta", tratta);
            model.addAttribute("tratta_id", tratta.getId());
            return "user/admin/formNewVideo";
        }
        model.addAttribute("user", currentUser);
        model.addAttribute("tratte" , trattaService.getAll());
        return "user/admin/selectTratta";
    }

    @GetMapping("/admin/addVideo/{tratta_id}")
    public String addVideoTratta(@PathVariable Long tratta_id, Model model) {
        model.addAttribute("video", new Video());
        model.addAttribute("user", userService.getCurrentUser());
        model.addAttribute("tratta_id", tratta_id);
        return "user/admin/formNewVideo";
    }

    @PostMapping("/admin/addVideo/{tratta_id}")
    public String uploadVideo(@Valid @ModelAttribute Video video,
                              BindingResult bindingResult,
                              @PathVariable Long tratta_id,
                              Model model) throws IOException {

        User currentUser = userService.getCurrentUser();
        boolean isSupervisor = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(a->a.getAuthority().equals("SUPERVISOR"));

        if(isSupervisor){
            Tratta trattaSupervisor = trattaService.getBySupervisor(currentUser);
            System.out.println(">>> DEBUG trattaSupervisor=" + (trattaSupervisor == null ? "NULL" : trattaSupervisor.getId()) + " | tratta_id=" + tratta_id);
            if(trattaSupervisor == null || !trattaSupervisor.getId().equals(tratta_id)){
                return "redirect:/accessDenied";
            }
        }
        video.setTratta(trattaService.getById(tratta_id));
        video.setUser(currentUser);

        videoValidator.validate(video, bindingResult);
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", userService.getCurrentUser());
            model.addAttribute("video", video);
            model.addAttribute("tratta", tratta_id);
            return "user/admin/formNewVideo";
        }

        video.setFile(video.getMultipartFile().getBytes());
        videoService.save(video);

        return "redirect:/tratta/" + tratta_id;
    }

    @PostMapping("/admin/deleteVideo/{id}")
    public String deleteVideo(@PathVariable Long id){
        Video video = videoService.getById(id);
        if(video == null){
            return "redirect:/";
        }
        Long trattaId = video.getTratta().getId();
        boolean isSupervisor = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().anyMatch(a->a.getAuthority().equals("SUPERVISOR"));
        if(isSupervisor){
            Tratta trattaSupervisor = trattaService.getBySupervisor(userService.getCurrentUser());
            if(trattaSupervisor==null || !trattaSupervisor.getId().equals(trattaId)){
                return "redirect:/accessDenied";
            }
        }
        videoService.delete(video);
        return "redirect:/tratta/" + trattaId;
    }



}
