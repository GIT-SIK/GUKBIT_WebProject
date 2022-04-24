package com.gukbit.service;

import com.gukbit.domain.Academy;
import com.gukbit.domain.Board;
import com.gukbit.domain.Course;
import com.gukbit.dto.AcademyDto;
import com.gukbit.repository.AcademyRepository;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.transaction.Transactional;

import com.gukbit.repository.CourseRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Service
public class AcademyService {

  private AcademyRepository academyRepository;
  private CourseRepository courseRepository;
  private IWordAnalysisService wordAnalysisService;

  public AcademyService(AcademyRepository academyRepository,CourseRepository courseRepository,IWordAnalysisService wordAnalysisService) {
    this.academyRepository = academyRepository;
    this.courseRepository = courseRepository;
    this.wordAnalysisService = wordAnalysisService;
  }

  public List<Academy> searchAllAcademy(){
    List<Academy> academyList = academyRepository.findAll();
    return academyList;
  }

  @Transactional
  public List<AcademyDto> searchAcademy(String keyword) {
    List<Academy> academies = academyRepository.findByNameContaining(keyword);
    List<AcademyDto> academyDtoList = new ArrayList<>();
    List<Academy> academiesTemp = new ArrayList<>();

    for(int imgCount=0; imgCount<academies.size(); imgCount++){
      academiesTemp.add(isImage(academies.get(imgCount)));
    }

    if(academiesTemp.isEmpty()) return academyDtoList;
    for(Academy academy : academiesTemp){
      academyDtoList.add(this.convertEntityToDto(academy));
    }
    return academyDtoList;
  }

  private AcademyDto convertEntityToDto(Academy academy){
    return AcademyDto.builder()
        .code(academy.getCode())
        .name(academy.getName())
        .home_url(academy.getHome_url())
        .region(academy.getRegion())
        .addr(academy.getAddr())
        .eval(academy.getEval())
        .tel(academy.getTel())
        .imageUrl(academy.getImageUrl())
        .build();
  }

  public Academy getAcademyInfo(String code){
    Academy academy_info = academyRepository.findByCode(code);
    academy_info = isImage(academy_info);
    return academy_info;

  }

  public Page<Course> expectedCoursePageList(String code, Pageable pageable){
    List<Course> list = courseRepository.findAllByAcademyCode(code);

    LocalDate now = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    String formatedNow = now.format(formatter);

    List<Course> expectedList = new ArrayList<>();

    for (int i = 0; i < list.size(); i++) {
      int startDate = Integer.parseInt(list.get(i).getStart().replaceAll("-",""));
      if((startDate - Integer.parseInt(formatedNow)) > 0){
        expectedList.add(list.get(i));
      }
    }

    pageable = PageRequest.of(pageable.getPageNumber() <= 0 ? 0 : pageable.getPageNumber() - 1, 5);

    final int start = (int)pageable.getOffset();
    final int end = Math.min((start + pageable.getPageSize()), expectedList.size());
    final Page<Course> page = new PageImpl<>(expectedList.subList(start, end), pageable, expectedList.size());
    return page;
  }



  /* 이미지 입력 및 이미지 확인 여부 */
  public Academy isImage(Academy academy){

    String[] fne = {".jpg", ".png", ".gif", ".bmp"};

    System.out.println("******************************* isImage *******************************");

    for(String fnet : fne) {
      String url = "static/images/academy/";
      String fileName = academy.getCode() + fnet;
      url += fileName;
      try {
        File file = new ClassPathResource(url).getFile();
        if (file.isFile()) {
          academy.setImageUrl(fileName);
          break;
        }
      }catch (IOException e){
        academy.setImageUrl("NoAcademyImage.png");
      }
    }

    return academy;
  }



  public List<Map<String,Integer>> analysis(String academyCode) throws Exception {


    //분석할 문장
    //String text = "아침에 밥을 꼭 먹고 점심엔 점심 밥을 꼭 먹고 저녁엔 저녁 밥을 꼭 먹자!";

    //신조어 및 새롭게 생겨난 가수 및 그룹명은 제대로 된 분석이 불가능합니다.
    // 새로운 명사 단어들은 어떻게 데이터를 처리해야 할까?? => 데이터사전의 주기적인 업데이트


    List<Map<String,Integer>> list = new ArrayList<>(wordAnalysisService.doWordAnalysis(academyCode));


    return list;
  }
}
