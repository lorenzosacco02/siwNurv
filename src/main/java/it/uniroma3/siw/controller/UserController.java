package it.uniroma3.siw.controller;

import com.nimbusds.jose.util.Resource;
import it.uniroma3.siw.model.Image;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.ImageService;
import it.uniroma3.siw.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import it.uniroma3.siw.model.Tratta;
import it.uniroma3.siw.service.TrattaService;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private TrattaService trattaService;

    @GetMapping("/profile")
    public String getAllBooks(Model model) {
        User currentUser = userService.getCurrentUser();
        model.addAttribute("user", currentUser);

        Tratta trattaOperatore = trattaService.getByOperatore(currentUser);
        if (trattaOperatore != null) {
            model.addAttribute("trattaOperatore", trattaOperatore);
        }

        return "user/profile";
    }

    @GetMapping("/user/{user_id}/profilePic")
    public ResponseEntity<byte[]> getImage(@PathVariable Long user_id) {

        User user = userService.getUserById(user_id);
        Image image = user.getProfilePicture();

        if (image == null || image.getContent() == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG); // o IMAGE_PNG se il formato lo richiede
        return new ResponseEntity<>(image.getContent(), headers, HttpStatus.OK);
    }

    @GetMapping("/user/{id}/editProfile")
    public String showEditProfile(@PathVariable Long id, Model model, Principal principal) {
        User user = userService.getUserById(id);
        if (!Objects.equals(userService.getCurrentUser().getId(), id))
            return "redirect:/accessDenied";


        // Leggi le immagini dalla cartella static
        List<String> stockImages = userService.loadStockImages();

        model.addAttribute("images", stockImages);
        model.addAttribute("user", user);
        return "user/editProfile";
    }

    @PostMapping("/user/{id}/editProfile")
    public String updateProfile(
            @PathVariable Long id,
            @RequestParam("name") String name,
            @RequestParam("surname") String surname,
            @RequestParam("email") String email,
            @RequestParam(value = "profileImageFile", required = false) MultipartFile file,
            @RequestParam(value = "stockImage", required = false) String stockImage,
            Principal principal) throws IOException {

        User user = userService.getUserById(id);
        if (!Objects.equals(userService.getCurrentUser().getId(), id))
            return "redirect:/accessDenied";

        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);

        if (!file.isEmpty()) {
            userService.updateProfilePic(user, file);
        } else if (stockImage != null && !stockImage.isEmpty()) {
            userService.updateProfilePic(user, stockImage);
        }


        userService.save(user);
        return "redirect:/profile";
    }
}
