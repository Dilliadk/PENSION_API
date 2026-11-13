package com.v1.pension.service.system;

import com.v1.pension.dto.pensioner.pensioner_post.PensionerPostRequestDto;
import com.v1.pension.dto.pensioner.pensioner_post.PensionerPostResponseDto;
import com.v1.pension.exception.BadRequestException;
import com.v1.pension.exception.ResourceNotFoundException;
import com.v1.pension.model.system.PensionerPost;
import com.v1.pension.repository.system.PensionerPostRepository;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PensionerPostService {
  private final PensionerPostRepository postRepository;
  private final ModelMapper modelMapper;

  public List<Tuple> getAllPensionerPostDetails() {
    return postRepository.getAllPensionerPostDetails();
  }

  public List<Tuple> getActivePensionerPostSummariesByCategoryAndLevel(String pcCode, String plCode) {
    return postRepository.getActivePensionerPostSummariesByCategoryAndLevel(pcCode, plCode);
  }

  public PensionerPostResponseDto getResponseById(long id) {
    return postRepository.getResponseById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Pensioner Post Not Found"));
  }

  private PensionerPost getById(long id) {
    return postRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Pensioner Post Not Found"));
  }

  public void addPensionerPost(PensionerPostRequestDto dto) {
    if (postRepository.existsByPensionerPostCode(dto.getPensionerPostCode()))
      throw new BadRequestException("This Pensioner Post Code Already Exists");

    var entity = modelMapper.map(dto, PensionerPost.class);
    postRepository.save(entity);
  }

  public void updatePensionerPost(long id, PensionerPostRequestDto dto) {
    var entity = getById(id);

    if (!Objects.equals(entity.getPensionerPostCode(), dto.getPensionerPostCode())) {
      if (postRepository.existsByPensionerPostCode(dto.getPensionerPostCode()))
        throw new BadRequestException("This Pensioner Post Code Already Exists");
    }

    modelMapper.map(dto, entity);
    postRepository.save(entity);
  }
}
