package it.uniroma3.siw.controller;

import it.uniroma3.siw.controller.validator.CredentialsValidator;
import it.uniroma3.siw.model.Credentials;
import it.uniroma3.siw.model.TipoDiAnomalia;
import it.uniroma3.siw.model.Tratta;
import it.uniroma3.siw.model.User;
import it.uniroma3.siw.service.AnomaliaService;
import it.uniroma3.siw.service.CredentialsService;
import it.uniroma3.siw.service.TrattaService;
import it.uniroma3.siw.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import it.uniroma3.siw.model.Tratta;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AuthController {

	@Autowired
	private CredentialsService credentialsService;
    @Autowired
    private UserService userService;
    @Autowired
    private TrattaService trattaService;
	@Autowired
	private CredentialsValidator credentialsValidator;
	@Autowired
	private AnomaliaService anomaliaService;

	@GetMapping("/accessDenied")
	public String accessDenied(Model model) {
		model.addAttribute("user", userService.getCurrentUser());
		return "accessDenied";
	}

	@GetMapping(value = "/register")
	public String showRegisterForm(Model model) {
		model.addAttribute("user", new User());
		model.addAttribute("credentials", new Credentials());
		return "register";
	}

	@GetMapping(value = "/login")
	public String showLoginForm(Model model) {
		return "login";
	}

	@GetMapping(value = "/" )
	public String index(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof AnonymousAuthenticationToken) {
			return "index";
		}
		User currentUser = userService.getCurrentUser();
		Credentials credentials = credentialsService.getCredentialsByUser(currentUser);
		if (credentials != null && credentials.getRole().equals(Credentials.ADMIN_ROLE)) {
			model.addAttribute("user", currentUser);
			return "user/admin/index";
		}
		if (credentials != null && credentials.getRole().equals(Credentials.SUPERVISOR_ROLE)) {
			Tratta tratta = trattaService.getBySupervisor(currentUser);
			model.addAttribute("user", currentUser);
			model.addAttribute("tratta", tratta);
			model.addAttribute("anomalie", anomaliaService.getByTratta(tratta));
			return "user/supervisor/index";
		}
		if (credentials != null && credentials.getRole().equals(Credentials.DEFAULT_ROLE)) {
			Tratta trattaOperatore = trattaService.getByOperatore(currentUser);
			/*
			if(trattaOperatore!=null){
					// operatore assegnato ad una tratta
					model.addAttribute("trattaOperatore", trattaOperatore);
				}

			 */
				model.addAttribute("user", currentUser);
				model.addAttribute("tratta", trattaOperatore);
				model.addAttribute("anomalie", anomaliaService.getByTratta(trattaOperatore));
				return "user/operatore/index";
			}
		return "index";
	}


	@GetMapping("/search")
	public String search(@RequestParam(value = "nome", required = false) String nome,
						 @RequestParam(value = "anomalia" , required = false) TipoDiAnomalia anomalia,
						 @RequestParam(value = "sort", required = false) String sort,
						 Model model) {
		model.addAttribute("user", userService.getCurrentUser());
		model.addAttribute("tratte", trattaService.getFilteredSorted(sort, nome, anomalia));
		return "user/index";
	}

	@GetMapping(value = "/success")
	public String defaultAfterLogin() {
		return "redirect:/";
	}

	@PostMapping(value = {"/register"})
	public String registerUser(@ModelAttribute("user") User user,
							   BindingResult userBindingResult,
							   @Valid @ModelAttribute("credentials") Credentials credentials,
							   BindingResult credentialsBindingResult,
							   Model model) {

		credentialsValidator.validate(credentials, credentialsBindingResult);
		if (userBindingResult.hasErrors() || credentialsBindingResult.hasErrors()) {
			model.addAttribute("credentials", credentials);
			model.addAttribute("user", user);
			return "register";
		}
		credentials.setUser(user);
		credentialsService.saveCredentials(credentials);
		model.addAttribute("user", user);
		return "login";
	}
	/*
	@GetMapping("/profile")
	public String getAllBooks(Model model) {
		User currentUser = userService.getCurrentUser();
		model.addAttribute("user", currentUser);

		// Passa la tratta dell'operatore se esiste
		Tratta trattaOperatore = trattaService.getByOperatore(currentUser);
		if (trattaOperatore != null) {
			model.addAttribute("trattaOperatore", trattaOperatore);
		}

		return "user/profile";
	}
	 */
}
