package com.v1.pension.controller.system;

import com.v1.pension.dto.pensioner.pensioner_post.PensionerPostRequestDto;
import com.v1.pension.service.system.PensionerPostService;
import com.v1.pension.utils.MyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pensioner-posts")
@RequiredArgsConstructor
public class PensionerPostController {
  private final PensionerPostService postService;

  @GetMapping("/all/details")
  public ResponseEntity<?> getAllPensionerPostDetails() {
    var data = postService.getAllPensionerPostDetails();
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/active/summaries")
  public ResponseEntity<?> getActivePensionerPostSummariesByCategoryAndLevel(
      @RequestParam String pcCode, @RequestParam String plCode) {
    var data = postService.getActivePensionerPostSummariesByCategoryAndLevel(pcCode, plCode);
    return ResponseEntity.ok(MyResponse.data(data));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getById(@PathVariable long id) {
    var data = postService.getResponseById(id);
    return ResponseEntity.ok(data);
  }

  @PostMapping
  public ResponseEntity<?> addPensionerPost(@RequestBody PensionerPostRequestDto dto) {
    postService.addPensionerPost(dto);
    return ResponseEntity.ok(MyResponse.message("Pensioner post has been saved"));
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> updatePensionerPost(@PathVariable long id, @RequestBody PensionerPostRequestDto dto) {
    postService.updatePensionerPost(id, dto);
    return ResponseEntity.ok(MyResponse.message("Pensioner Post Has Been Updated"));
  }
}
