package com.tisd.c4change.Service;

import com.tisd.c4change.DTO.IndividualDTO.*;
import com.tisd.c4change.DTO.NgoDTO.*;

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
    //
}
