package com.tisd.c4change.Controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tisd.c4change.CustomException.EmailAlreadyExistsException;
import com.tisd.c4change.DTO.IndividualDTO.IndividualRegistrationDto;
import com.tisd.c4change.DTO.IndividualDTO.IndividualResponseDto;
import com.tisd.c4change.DTO.NgoDTO.NGORegistrationDto;
import com.tisd.c4change.DTO.NgoDTO.NGOResponseDto;
import com.tisd.c4change.Entity.Availability;
import com.tisd.c4change.Service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    UserService userService;

    @PostMapping(value = "/register-individual", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerIndividual(
            @RequestPart("name") String name,
            @RequestPart("email") String email,
            @RequestPart("password") String password,
            @RequestPart(value = "location", required = false) String location,
            @RequestPart(value = "phone", required = false) String phone,
            @RequestPart(value = "address", required = false) String address,
            @RequestPart(value = "bio", required = false) String bio,
            @RequestPart("skills") String skills,
            @RequestPart("interests") String interests,
            @RequestPart("availability") String availability,
            @RequestPart("resume") MultipartFile resume) {

        try {
            ObjectMapper mapper = new ObjectMapper();

            IndividualRegistrationDto dto = new IndividualRegistrationDto();
            dto.setName(name);
            dto.setEmail(email);
            dto.setPassword(password);
            dto.setLocation(location);
            dto.setPhone(phone);
            dto.setAddress(address);
            dto.setBio(bio);
            dto.setSkills(mapper.readValue(skills, new TypeReference<List<String>>() {
            }));
            dto.setInterests(mapper.readValue(interests, new TypeReference<List<String>>() {
            }));
            dto.setAvailability(Availability.valueOf(availability.toUpperCase()));
            dto.setResume(resume);

            IndividualResponseDto response = userService.registerIndividual(dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Registration failed",
                    "message", e.getMessage(),
                    "details", e.getClass().getSimpleName()
            ));
        }
    }

    @PostMapping(value = "/register-ngo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> registerNGO(
            @RequestPart("orgName") String orgName,
            @RequestPart("email") String email,
            @RequestPart("regNumber") String regNumber,
            @RequestPart("password") String password,
            @RequestPart(value = "phone", required = false) String phone,
            @RequestPart(value = "address", required = false) String address,
            @RequestPart(value = "mission", required = false) String mission,
            @RequestPart(value = "website", required = false) String website,
            @RequestPart("volNeeds") String volNeeds,
            @RequestPart("verificationDocs") MultipartFile verificationDocs) {

        try {
            logger.info("Starting NGO registration for: {}", email);

            // Convert comma-separated string to List
            ObjectMapper objectMapper = new ObjectMapper();
            NGORegistrationDto dto = new NGORegistrationDto();
            dto.setOrgName(orgName);
            dto.setEmail(email);
            dto.setPassword(password);
            dto.setRegNumber(regNumber);
            dto.setPhone(phone);
            dto.setAddress(address);
            dto.setMission(mission);
            dto.setWebsite(website);
            dto.setVolNeeds(objectMapper.readValue(volNeeds, new TypeReference<List<String>>() {}));
            dto.setVerificationDocs(verificationDocs);

            NGOResponseDto response = userService.registerNGO(dto);
            return ResponseEntity.ok(response);

        } catch (EmailAlreadyExistsException e) {
            logger.warn("Registration failed - email exists: {}", email);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    Map.of("error", "Email already registered"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Registration failed",
                    "message", e.getMessage(),
                    "details", e.getClass().getSimpleName()
            ));
        }
    }

    @PostMapping("/debug-form")
    public ResponseEntity<String> debugForm(@RequestParam Map<String, String> allParams) {
        return ResponseEntity.ok("Received params: " + allParams.toString());
    }

    @PostMapping("/debug-file")
    public ResponseEntity<String> debugFile(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok("Received file: " + file.getOriginalFilename() +
                ", size: " + file.getSize() +
                ", type: " + file.getContentType());
    }

//    @GetMapping("/u/{id}")
//    public ResponseEntity<IndividualResponseDto> getIndividualUser(@PathVariable("id") Long id ) {
//        IndividualResponseDto getIndUser = userService.
//    }
}
