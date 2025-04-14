package com.tisd.c4change.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
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
import com.tisd.c4change.Mapper.DtoConverter;
import com.tisd.c4change.Repository.IndividualRepository;
import com.tisd.c4change.Repository.NGORepository;
import com.tisd.c4change.Service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.security.sasl.AuthenticationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:5173", allowedHeaders = "*")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    UserService userService;

    @Autowired
    IndividualRepository individualRepository;

    @Autowired
    NGORepository ngoRepository;

    @Autowired
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Transactional
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
            @RequestPart(value = "resume") MultipartFile resume) {

        UserRecord userRecordInd = null;

        try {
            // Validate required fields
            if (name == null || name.isEmpty() ||
                    email == null || email.isEmpty() ||
                    password == null || password.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Validation failed",
                        "message", "Name, email, and password are required"
                ));
            }

            // 1. Create Firebase account
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password);

            userRecordInd = firebaseAuth.createUser(request);

            // 2. Process your existing DTO
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
            dto.setResume(resume);  // Can be null

            // 3. Save to your database with Firebase UID
            IndividualResponseDto response = userService.registerIndividual(dto, userRecordInd.getUid());

            return ResponseEntity.ok(response);

        } catch (FirebaseAuthException e) {
            logger.error("Firebase registration failed", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Registration failed",
                    "message", e.getMessage()
            ));
        } catch (JsonProcessingException e) {
            logger.error("JSON processing error", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid skills or interests format",
                    "message", "Please provide skills and interests as valid JSON arrays"
            ));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid availability value", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid availability",
                    "message", "Availability must be one of: WEEKLY, MONTHLY, ONE_TIME, INDEFINITE"
            ));
        } catch (Exception e) {
            logger.error("Unexpected error during registration", e);
            // Cleanup Firebase user if created
            if (userRecordInd != null) {
                try {
                    firebaseAuth.deleteUser(userRecordInd.getUid());
                } catch (FirebaseAuthException ex) {
                    logger.error("Failed to cleanup Firebase user", ex);
                }
            }
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Registration failed",
                    "message", "An unexpected error occurred"
            ));
        }
    }

    @Transactional
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
            @RequestPart(value = "verificationDocs") MultipartFile verificationDocs) {

        UserRecord userRecordNgo = null;

        try {
            if (orgName == null || orgName.isEmpty() ||
                    email == null || email.isEmpty() ||
                    password == null || password.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Validation failed",
                        "message", "Name, email, and password are required"
                ));
            }

            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(password);

            userRecordNgo = firebaseAuth.createUser(request);

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
            dto.setVolNeeds(objectMapper.readValue(volNeeds, new TypeReference<List<String>>() {
            }));
            dto.setVerificationDocs(verificationDocs);

            NGOResponseDto response = userService.registerNGO(dto, userRecordNgo.getUid());
            return ResponseEntity.ok(response);

        } catch (FirebaseAuthException e) {
            logger.error("Firebase registration failed", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Registration failed",
                    "message", e.getMessage()
            ));
        } catch (JsonProcessingException e) {
            logger.error("JSON processing error", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid skills or interests format",
                    "message", "Please provide skills and interests as valid JSON arrays"
            ));
        } catch (IllegalArgumentException e) {
            logger.error("Invalid availability value", e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid availability",
                    "message", "Availability must be one of: WEEKLY, MONTHLY, ONE_TIME, INDEFINITE"
            ));
        } catch (Exception e) {
            logger.error("Unexpected error during registration", e);
            // Cleanup Firebase user if created
            if (userRecordNgo != null) {
                try {
                    firebaseAuth.deleteUser(userRecordNgo.getUid());
                } catch (FirebaseAuthException ex) {
                    logger.error("Failed to cleanup Firebase user", ex);
                }
            }
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Registration failed",
                    "message", "An unexpected error occurred"
            ));
        }
    }

    @PostMapping("/login-individual")
    public ResponseEntity<?> loginIndividual(
            @RequestHeader("Authorization") String authHeader) {

        try {
            // 1. Verify Firebase token
            String firebaseToken = authHeader.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);

            // 2. Get user from database
            IndividualUser user = individualRepository.findByEmail(decodedToken.getEmail())
                    .orElseThrow(() -> new UserNotFoundException("User not registered"));

            // 3. Create response
            Map<String, Object> response = new HashMap<>();
            response.put("uid", decodedToken.getUid());
            response.put("email", user.getEmail());
            response.put("name", user.getName());
            response.put("type", "individual");
            response.put("token", firebaseToken);

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + firebaseToken)
                    .body(response);

        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "Invalid token",
                    "message", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Login failed",
                    "message", e.getMessage()
            ));
        }
    }

    @PostMapping("/login-ngo")
    public ResponseEntity<?> loginNGO(
            @RequestHeader("Authorization") String authHeader) {

        try {
            // 1. Verify Firebase token (same as individual)
            String firebaseToken = authHeader.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseToken);

            // 2. Get user from database (changed to NGO)
            NGOProfile user = ngoRepository.findByEmail(decodedToken.getEmail())
                    .orElseThrow(() -> new UserNotFoundException("User not registered"));

            // 3. Create response (with NGO-specific fields)
            Map<String, Object> response = new HashMap<>();
            response.put("uid", decodedToken.getUid());
            response.put("email", user.getEmail());
            response.put("orgName", user.getOrgName());
            response.put("type", "ngo");
            response.put("isVerified", user.getIsVerified());
            response.put("token", firebaseToken);

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + firebaseToken)
                    .body(response);

        } catch (FirebaseAuthException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "Invalid token",
                    "message", e.getMessage()
            ));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "error", "NGO not found",
                    "message", "Please register first"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Login failed",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/user-profile")
    public ResponseEntity<?> getUserProfile(Authentication authentication) {
        try {
            // Safely get the principal as a Map
            @SuppressWarnings("unchecked")
            Map<String, String> principal = (Map<String, String>) authentication.getPrincipal();
            String uid = principal.get("uid");

            // Check both individual and NGO repositories
            Optional<IndividualUser> individual = individualRepository.findByFirebaseUid(uid);
            if (individual.isPresent()) {
                return ResponseEntity.ok(DtoConverter.toIndividualResponseDto(individual.get()));
            }

            Optional<NGOProfile> ngo = ngoRepository.findByFirebaseUid(uid);
            if (ngo.isPresent()) {
                return ResponseEntity.ok(DtoConverter.toNGOResponseDto(ngo.get()));
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        } catch (ClassCastException e) {
            // Fallback for cases where principal is just the UID string
            String uid = (String) authentication.getPrincipal();

            Optional<IndividualUser> individual = individualRepository.findByFirebaseUid(uid);
            if (individual.isPresent()) {
                return ResponseEntity.ok(DtoConverter.toIndividualResponseDto(individual.get()));
            }

            Optional<NGOProfile> ngo = ngoRepository.findByFirebaseUid(uid);
            if (ngo.isPresent()) {
                return ResponseEntity.ok(DtoConverter.toNGOResponseDto(ngo.get()));
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @GetMapping("/ngo-profile")
    public ResponseEntity<?> getNGOProfile(Authentication authentication) {
        try {
            // Safely get the principal as a Map
            @SuppressWarnings("unchecked")
            Map<String, String> principal = (Map<String, String>) authentication.getPrincipal();
            String uid = principal.get("uid");

            Optional<NGOProfile> ngo = ngoRepository.findByFirebaseUid(uid);
            if (ngo.isPresent()) {
                return ResponseEntity.ok(DtoConverter.toNGOResponseDto(ngo.get()));
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("NGO not found");
        } catch (ClassCastException e) {
            // Fallback for cases where principal is just the UID string
            String uid = (String) authentication.getPrincipal();

            Optional<NGOProfile> ngo = ngoRepository.findByFirebaseUid(uid);
            if (ngo.isPresent()) {
                return ResponseEntity.ok(DtoConverter.toNGOResponseDto(ngo.get()));
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("NGO not found");
        }
    }

    @GetMapping("/ngo-profile/{identifier}")
    public ResponseEntity<?> getNGOProfile(
            @PathVariable String identifier,
            Authentication authentication) {

        try {
            // Get current user UID from authentication
            String currentUserUid;
            if (authentication.getPrincipal() instanceof Map) {
                currentUserUid = ((Map<String, String>) authentication.getPrincipal()).get("uid");
            } else {
                currentUserUid = (String) authentication.getPrincipal();
            }

            // Try to find by Firebase UID first
            Optional<NGOProfile> ngo = ngoRepository.findByFirebaseUid(identifier);

            // If not found by UID, try by ID (if identifier is numeric)
            if (!ngo.isPresent() && identifier.matches("\\d+")) {
                ngo = ngoRepository.findById(Long.parseLong(identifier));
            }

            if (ngo.isPresent()) {
                // Verify ownership
                if (!ngo.get().getFirebaseUid().equals(currentUserUid)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized access");
                }
                return ResponseEntity.ok(DtoConverter.toNGOResponseDto(ngo.get()));
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("NGO not found");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error fetching NGO profile");
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
}


//    @GetMapping("/u/{id}")
//    public ResponseEntity<IndividualResponseDto> getIndividualUser(@PathVariable("id") Long id ) {
//        IndividualResponseDto getIndUser = userService.
//    }

