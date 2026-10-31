package com.learningplatform.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.learningplatform.backend.entity.User;
import com.learningplatform.backend.repository.UserRepository;
import com.learningplatform.backend.security.JwtService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class ProfileDetailsTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private JwtService jwtService;

	private static final String FULL_DETAILS = """
			{"phone":"+919876543210","dateOfBirth":"1995-04-12","gender":"Female",
			 "addressLine":"12 Mall Road","city":"Manali","state":"Himachal Pradesh",
			 "postalCode":"175131","country":"India","experienceLevel":"Intermediate",
			 "emergencyContactName":"Asha Sethi","emergencyContactPhone":"9812345678",
			 "bio":"Love high passes."}
			""";

	// A fresh user with no phone, plus a login token for them
	private String newUserToken() {
		String email = "profile-" + UUID.randomUUID() + "@example.com";
		User user = new User("Misha", email, null);
		user.setPassword("not-used-in-these-tests");
		userRepository.save(user);
		return "Bearer " + jwtService.generateToken(email);
	}

	private MockHttpServletRequestBuilder putDetails(String token, String json) {
		return put("/api/me/details").header("Authorization", token)
				.contentType(MediaType.APPLICATION_JSON).content(json);
	}

	@Test
	void detailsRequireLogin() throws Exception {
		mockMvc.perform(get("/api/me/details")).andExpect(status().isUnauthorized());
	}

	@Test
	void newUserHasEmptyDetailsAndUnlockedPhone() throws Exception {
		mockMvc.perform(get("/api/me/details").header("Authorization", newUserToken()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Misha"))
				.andExpect(jsonPath("$.phoneLocked").value(false))
				.andExpect(jsonPath("$.city").isEmpty());
	}

	@Test
	void savesDetailsAndLocksPhoneOnceAdded() throws Exception {
		String token = newUserToken();

		mockMvc.perform(putDetails(token, FULL_DETAILS))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.phone").value("+919876543210"))
				.andExpect(jsonPath("$.phoneLocked").value(true))
				.andExpect(jsonPath("$.city").value("Manali"));

		// Saved for real: a fresh read returns the same details
		mockMvc.perform(get("/api/me/details").header("Authorization", token))
				.andExpect(jsonPath("$.postalCode").value("175131"))
				.andExpect(jsonPath("$.experienceLevel").value("Intermediate"));

		// Phone is locked now - a different number is refused
		mockMvc.perform(putDetails(token, FULL_DETAILS.replace("+919876543210", "+911111111111")))
				.andExpect(status().isBadRequest());
	}

	@Test
	void nameCannotBeChangedThroughDetails() throws Exception {
		String token = newUserToken();
		mockMvc.perform(putDetails(token, "{\"name\":\"Hacker\",\"email\":\"hacker@example.com\",\"city\":\"Leh\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("Misha"))
				.andExpect(jsonPath("$.email").value(org.hamcrest.Matchers.startsWith("profile-")));
	}

	@Test
	void rejectsInvalidDetails() throws Exception {
		String token = newUserToken();
		String[] invalid = {
				"{\"postalCode\":\"!!\"}",
				"{\"dateOfBirth\":\"2999-01-01\"}",
				"{\"dateOfBirth\":\"1850-01-01\"}",
				"{\"gender\":\"Robot\"}",
				"{\"experienceLevel\":\"Superhero\"}",
				"{\"city\":\"<script>\"}",
				"{\"emergencyContactPhone\":\"call me\"}",
				"{\"bio\":\"" + "x".repeat(501) + "\"}"
		};
		for (String json : invalid) {
			mockMvc.perform(putDetails(token, json)).andExpect(status().isBadRequest());
		}
	}
}
