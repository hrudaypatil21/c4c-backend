package com.tisd.c4change.Controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.tisd.c4change.CustomException.EmailAlreadyExistsException;
import com.tisd.c4change.CustomException.InvalidCredentialsException;
import com.tisd.c4change.CustomException.UserNotFoundException;
import com.tisd.c4change.DTO.IndividualDTO.IndividualLoginDto;
import com.tisd.c4change.DTO.IndividualDTO.IndividualRegistrationDto;
import com.tisd.c4change.DTO.IndividualDTO.IndividualResponseDto;
import com.tisd.c4change.DTO.NgoDTO.NGOLoginDto;
import com.tisd.c4change.DTO.NgoDTO.NGORegistrationDto;
import com.tisd.c4change.DTO.NgoDTO.NGOResponseDto;
import com.tisd.c4change.Entity.Availability;
import com.tisd.c4change.Entity.IndividualUser;
import com.tisd.c4change.Entity.NGOProfile;
import com.tisd.c4change.JwtTokenUtil;
import com.tisd.c4change.Mapper.DtoConverter;
import com.tisd.c4change.Service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.security.sasl.AuthenticationException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    UserService userService;
    @Autowired
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

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
            // 1. Create Firebase account
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password);

            UserRecord userRecordInd = firebaseAuth.createUser(request);

            // 2. Process your existing DTO
            ObjectMapper mapper = new ObjectMapper();
            IndividualRegistrationDto dto = new IndividualRegistrationDto();
            dto.setName(name);
            dto.setEmail(email);
            dto.setPassword(password); // Will be hashed by your service
            dto.setLocation(location);
            dto.setPhone(phone);
            dto.setAddress(address);
            dto.setBio(bio);
            dto.setSkills(mapper.readValue(skills, new TypeReference<List<String>>() {}));
            dto.setInterests(mapper.readValue(interests, new TypeReference<List<String>>() {}));
            dto.setAvailability(Availability.valueOf(availability.toUpperCase()));
            dto.setResume(resume);

            // 3. Save to your database with Firebase UID
            IndividualResponseDto response = userService.registerIndividual(dto, userRecordInd.getUid());

            return ResponseEntity.ok(response);
        } catch (FirebaseAuthException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Firebase registration failed",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Registration failed",
                    "message", e.getMessage()
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
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password);

            UserRecord userRecordNgo = firebaseAuth.createUser(request);

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

            NGOResponseDto response = userService.registerNGO(dto, userRecordNgo.getUid());
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

    @PostMapping("/login-individual")
    public ResponseEntity<?> loginIndividual(@RequestBody IndividualLoginDto loginDto) {
        try {
            // 1. Verify with Firebase
            String token = FirebaseAuth.getInstance()
                    .createCustomToken(loginDto.getEmail());

            // 2. Your existing authentication logic
            IndividualUser user = userService.authenticateIndividual(loginDto);

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .body(DtoConverter.toIndividualResponseDto(user));

        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/login-ngo")
    public ResponseEntity<?> loginNGO(@RequestBody NGOLoginDto loginDto) {
        try {
            String token = FirebaseAuth.getInstance()
                    .createCustomToken(loginDto.getEmail());

            NGOProfile user = userService.authenticateNGO(loginDto);

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .body(DtoConverter.toNGOResponseDto(user));

        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
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
