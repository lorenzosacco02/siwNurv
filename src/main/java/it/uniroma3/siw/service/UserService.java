package it.uniroma3.siw.service;

import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.Image;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CredentialsService credentialsService;
    @Autowired
    private ImageService imageService;

    @Transactional
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return credentialsService.getCredentials(userDetails.getUsername()).getUser();
    }
    @Transactional
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Transactional
    public void save(User user) {
        userRepository.save(user);
    }

    @Transactional
    public void updateProfilePic(User user, MultipartFile img) throws IOException {
        Image image = new Image();
        image.setName(img.getOriginalFilename());
        image.setContent(img.getBytes());
        imageService.save(image);
        Long oldImgId = null;
        if(user.getProfilePicture() != null) {
            oldImgId = user.getProfilePicture().getId();
        }
        user.setProfilePicture(image);
        if(oldImgId != null) {
            imageService.deleteImage(oldImgId);
        }
    }

    @Transactional
    public void updateProfilePic(User user, String stock) {
        try {
            ClassPathResource classPathResource = new ClassPathResource("/static/images/stock/" + stock);
            byte[] imageBytes = StreamUtils.copyToByteArray(classPathResource.getInputStream());

            Image image = new Image();
            image.setName(stock);
            image.setContent(imageBytes);
            imageService.save(image);

            Long oldImgId = null;
            if(user.getProfilePicture() != null) {
                oldImgId = user.getProfilePicture().getId();
            }
            user.setProfilePicture(image);
            if(oldImgId != null) {
                imageService.deleteImage(oldImgId);
            }

            user.setProfilePicture(image);
        } catch (IOException e) {
            e.printStackTrace(); // oppure loggalo
            // Puoi anche aggiungere un messaggio all'utente tipo "Errore nel caricamento immagine stock"
        }
    }

    @Transactional
    public List<String> loadStockImages() {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream("stock-images.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines()
                    .filter(line -> !line.isBlank())
                    .collect(Collectors.toList());
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Transactional
    public List<User> getAllDefaultUsers() {
        return credentialsService.getAllByRole(Credentials.DEFAULT_ROLE)
                .stream()
                .map(Credentials::getUser)
                .collect(Collectors.toList());
    }
}
