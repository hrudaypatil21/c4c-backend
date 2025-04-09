package com.tisd.c4change.Service;

import com.tisd.c4change.Controller.UserController;
import com.tisd.c4change.CustomException.*;
import com.tisd.c4change.DTO.IndividualDTO.*;
import com.tisd.c4change.DTO.NgoDTO.*;
import com.tisd.c4change.Entity.*;
import com.tisd.c4change.Mapper.DtoConverter;
import com.tisd.c4change.Password.PasswordUtil;
import com.tisd.c4change.Repository.NGORepository;
import com.tisd.c4change.Repository.IndividualRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.ValidationException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService{
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final IndividualRepository individualRepository;
    private final NGORepository ngoRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public UserServiceImpl(IndividualRepository individualRepository,
                           NGORepository ngoRepository,
                           FileStorageService fileStorageService) {
        this.individualRepository = individualRepository;
        this.ngoRepository = ngoRepository;
        this.fileStorageService = fileStorageService;
    }

    @Override
    @Transactional
    public IndividualResponseDto registerIndividual(IndividualRegistrationDto registrationDto) {
        if (individualRepository.existsByEmail(registrationDto.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        IndividualUser user = new IndividualUser();
        user.setEmail(registrationDto.getEmail());
        // Make sure to hash the password before saving
        user.setPassword(registrationDto.getPassword()); // This should call setPassword() which hashes it
        user.setName(registrationDto.getName());
        user.setLocation(registrationDto.getLocation());
        user.setPhone(registrationDto.getPhone());
        user.setAddress(registrationDto.getAddress());
        user.setBio(registrationDto.getBio());
        user.setSkills(registrationDto.getSkills());
        user.setInterests(registrationDto.getInterests());
        user.setAvailability(registrationDto.getAvailability());

        if (registrationDto.getResume() != null && !registrationDto.getResume().isEmpty()) {
            byte[] resumeBytes = fileStorageService.storeFile(registrationDto.getResume());
            user.setResumePath(resumeBytes);
        }
        IndividualUser savedUser = individualRepository.save(user);
        return DtoConverter.toIndividualResponseDto(savedUser);
    }

    @Override
    public NGOResponseDto registerNGO(NGORegistrationDto registrationDto) {
        if(ngoRepository.existsByEmail(registrationDto.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered.");
        }

        if (ngoRepository.existsByRegNumber(registrationDto.getRegNumber())) {
            logger.warn("Registration attempt with existing reg number: {}", registrationDto.getRegNumber());
            throw new ConflictException("Registration number already exists");
        }

        try {
            // Map DTO to entity
            NGOProfile ngo = new NGOProfile();
            ngo.setEmail(registrationDto.getEmail());
            ngo.setPassword(registrationDto.getPassword());
            ngo.setOrgName(registrationDto.getOrgName());
            ngo.setRegNumber(registrationDto.getRegNumber());
            ngo.setOrgPhone(registrationDto.getPhone());
            ngo.setOrgAddress(registrationDto.getAddress());
            ngo.setOrgMission(registrationDto.getMission());
            ngo.setOrgWebsite(registrationDto.getWebsite());
            ngo.setVolNeeds(registrationDto.getVolNeeds());

            if (registrationDto.getVerificationDocs() != null && !registrationDto.getVerificationDocs().isEmpty()) {
                byte[] verificationDocs = fileStorageService.storeFile(registrationDto.getVerificationDocs());
                ngo.setVerificationDocsPath(verificationDocs);
            }

            // Save to database
            NGOProfile savedNGO = ngoRepository.save(ngo);

            // Return response DTO
            return DtoConverter.toNGOResponseDto(savedNGO);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ConflictException("Failed to register NGO");
        }
    }

    private String generateToken(String email) {
        // Simple token - replace with JWT in production
        return "token-" + UUID.randomUUID() + "-" + email.hashCode();
    }

    @Override
    public IndividualResponseDto loginIndividual(IndividualLoginDto loginDto) {
        IndividualUser user = individualRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Invalid credentials"));

        if(!user.verifyPassword(loginDto.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        IndividualResponseDto response = DtoConverter.toIndividualResponseDto(user);
        return response;
    }

    @Override
    public NGOResponseDto loginNGO(NGOLoginDto loginDto) {
        NGOProfile user = ngoRepository.findByEmail(loginDto.getEmail()).
                orElseThrow(() -> new UserNotFoundException("Invalid email/password"));

        if(!user.verifyPassword(loginDto.getPassword())) {
            throw new InvalidCredentialsException("Invalid email/password");
        }

        return DtoConverter.toNGOResponseDto(user);

    }

    @Override
    public IndividualResponseDto updateIndividual(Long id, IndividualUpdateDto updateDto) {

        IndividualUser user = individualRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (updateDto.getName() != null) user.setName(updateDto.getName());
        if (updateDto.getLocation() != null) user.setLocation(updateDto.getLocation());
        if (updateDto.getPhone() != null) user.setPhone(updateDto.getPhone());
        if (updateDto.getAddress() != null) user.setAddress(updateDto.getAddress());
        if (updateDto.getBio() != null) user.setBio(updateDto.getBio());
        if (updateDto.getSkills() != null) user.setSkills(updateDto.getSkills());
        if (updateDto.getInterests() != null) user.setInterests(updateDto.getInterests());
        if (updateDto.getAvailability() != null) user.setAvailability(updateDto.getAvailability());

        // Handle password change
        if (updateDto.isPasswordChangeRequested()) {
            if (!updateDto.isNewPasswordMatching()) {
                throw new InvalidCredentialsException("New passwords don't match");
            }
            if (!user.verifyPassword(updateDto.getCurrentPassword())) {
                throw new InvalidCredentialsException("Current password is incorrect");
            }
            user.setPassword(updateDto.getNewPassword());
        }

        if (updateDto.getResume() != null && !updateDto.getResume().isEmpty()) {
            byte[] resumeBytes = fileStorageService.storeFile(updateDto.getResume());
            user.setResumePath(resumeBytes);
        }

        IndividualUser updatedUser = individualRepository.save(user);

        return DtoConverter.toIndividualResponseDto(updatedUser);
    }

    @Override
    public NGOResponseDto updateNGO(Long id, NGOUpdateDto updateDto) {
        NGOProfile ngo = ngoRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("NGO not found"));

        if (updateDto.getOrgName() != null) ngo.setOrgName(updateDto.getOrgName());
        if (updateDto.getOrgPhone() != null) ngo.setOrgPhone(updateDto.getOrgPhone());
        if (updateDto.getOrgAddress() != null) ngo.setOrgAddress(updateDto.getOrgAddress());
        if (updateDto.getOrgMission() != null) ngo.setOrgMission(updateDto.getOrgMission());
        if (updateDto.getOrgWebsite() != null) ngo.setOrgWebsite(updateDto.getOrgWebsite());
        if (updateDto.getVolNeeds() != null) ngo.setVolNeeds(updateDto.getVolNeeds());

        if (updateDto.isPasswordChangeRequested()) {
            if (!updateDto.isNewPasswordMatching()) {
                throw new InvalidCredentialsException("New passwords don't match");
            }
            if (!ngo.verifyPassword(updateDto.getCurrentPassword())) {
                throw new InvalidCredentialsException("Current password is incorrect");
            }
            ngo.setPassword(updateDto.getNewPassword());
        }

        NGOProfile updatedNGO = ngoRepository.save(ngo);

        return DtoConverter.toNGOResponseDto(updatedNGO);
    }

    //Search
    @Override
    public List<IndividualResponseDto> searchIndividuals(String query) {
        List<IndividualUser> users = individualRepository.searchIndividuals(query);
        return users.stream()
                .map(DtoConverter::toIndividualResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<NGOResponseDto> searchNGOs(String query) {
        List<NGOProfile> ngos = ngoRepository.searchNGOs(query);
        return ngos.stream()
                .map(DtoConverter::toNGOResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public IndividualUser authenticateIndividual(IndividualLoginDto loginDto) {
        IndividualUser user = individualRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new UserNotFoundException("Invalid credentials"));

        if(!user.verifyPassword(loginDto.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        return user;
    }

    @Override
    public NGOProfile authenticateNGO(NGOLoginDto loginDto) {
        // More detailed logging
        logger.info("Attempting to authenticate NGO with email: {}", loginDto.getEmail());

        NGOProfile user = ngoRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> {
                    logger.warn("NGO not found with email: {}", loginDto.getEmail());
                    return new UserNotFoundException("Invalid email or password");
                });

        logger.debug("Found NGO profile: {}", user.getEmail());

        if(!user.verifyPassword(loginDto.getPassword())) {
            logger.warn("Password verification failed for NGO: {}", loginDto.getEmail());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        logger.info("NGO authenticated successfully: {}", user.getEmail());
        return user;
    }

    }




