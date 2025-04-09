package com.tisd.c4change.Service;

import com.tisd.c4change.DTO.IndividualDTO.*;
import com.tisd.c4change.DTO.NgoDTO.*;
import com.tisd.c4change.Entity.IndividualUser;
import com.tisd.c4change.Entity.NGOProfile;

import javax.security.sasl.AuthenticationException;
import java.util.List;

public interface UserService {

    //register, login, update
    IndividualResponseDto registerIndividual(IndividualRegistrationDto registrationDto);
    NGOResponseDto registerNGO(NGORegistrationDto registrationDto);
    IndividualResponseDto loginIndividual(IndividualLoginDto loginDto);
    NGOResponseDto loginNGO(NGOLoginDto loginDto);
    IndividualResponseDto updateIndividual(Long id, IndividualUpdateDto updateDto);
    NGOResponseDto updateNGO(Long id, NGOUpdateDto updateDto);
    List<IndividualResponseDto> searchIndividuals(String query);
    List<NGOResponseDto> searchNGOs(String query);
    IndividualUser authenticateIndividual(IndividualLoginDto loginDto) throws AuthenticationException;
    NGOProfile authenticateNGO(NGOLoginDto loginDto) throws AuthenticationException;
    //
}
