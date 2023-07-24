package com.example.kcccinema.controller.admin;

import java.io.File; 
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.example.kcccinema.dao.IMovieRepository;
import com.example.kcccinema.model.MovieVO;
import com.example.kcccinema.service.movie.MovieService;


@Controller
@RequestMapping("/admin")
public class AdminController {
	// 파일 업로드 폴더 경로
	private static final String UPLOAD_POSTER = "C:/kcccinema/movieposter/";
	@Autowired
	private IMovieRepository movieRepository;
	@Autowired
	private MovieVO movie;
	@Autowired
	private MovieService movieService;


	/* 기능1: 영화관리*/

	/* 상영 영화 조회 */
	@RequestMapping(value="/movies", method=RequestMethod.GET)
	public ModelAndView getMovieList() {
		List<MovieVO> movieList = movieService.getAllMovieList();
		ModelAndView modelAndView = new ModelAndView("admin/movies"); // movieList.html 템플릿을 사용할 것을 지정
		// 추가적인 영화 데이터를 원하는 만큼 리스트에 추가
		modelAndView.addObject("movieList", movieList); // 영화 데이터를 "movieList"라는 이름으로 전달
		return modelAndView;
	}


	/* 상영 상태에 따른 영화 조회 */
	@PostMapping("/movies/filterByStatus")
	@ResponseBody // 응답 데이터를 JSON 형태로 반환
	public List<MovieVO> filterMoviesByStatus(@RequestParam("status") String status) {
		System.out.println(status);
		List<MovieVO> movieList = new ArrayList<MovieVO>();
		if (status.equals("all-movies")) {
			movieList = movieService.getAllMovieList();
		} else {
			movieList = movieService.getMoviesByStatus(status);
		}
		System.out.println("ajax가 요청한 movieList의 길이: " + movieList.size());
		for(MovieVO movie : movieList) {
			System.out.println(movie.getScreening());
		}
		return movieList;
	}
	
	/* 영화 정보 수정 */
	@PostMapping("/admin/movies/updateMovieData")
	@ResponseBody
	public String updateMovieData(@RequestParam("movieId") String movieId, @RequestParam("newValue") String newValue) {
	    // 영화 데이터 업데이트 로직 수행
	    // ...

	    // 업데이트된 값을 JSON 형태로 응답
	    Map<String, String> responseMap = new HashMap<>();
	    responseMap.put("updatedValue", newValue);
	    return "lee";
	}
	
	
	


	/* 영화 추가 */

	// 영화 추가 페이지
	@RequestMapping(value="/movies/registration", method=RequestMethod.GET)
	public String insertMoviePage()  {
		return "admin/registrationform";
	}

	// 영화 추가 기능
	@RequestMapping(value="/movies/registration", method=RequestMethod.POST)
	public String insertMovie(@RequestParam("moviePoster") MultipartFile file, MultipartHttpServletRequest  multipartRequest, ModelAndView modelAndView) throws Exception{
		multipartRequest.setCharacterEncoding("utf-8");

		// 이미지파일 처리
		if(file!=null && !file.isEmpty()) {
			try {
				// 바이트 변환은 임시파일은 .tmp가 사라지기전에 해주어야 하므로 Multipartfile작업 전에 해준다.
				byte[] imageData = file.getBytes();

				String filePath = UPLOAD_POSTER + file.getOriginalFilename();
				file.transferTo(new File(filePath));
				System.out.println("file: " + file);



				// ImageEntity 생성 및 데이터 저장
				movie.setOriginalFilename(file.getOriginalFilename());
				movie.setContentType(file.getContentType());
				movie.setMoviePoster(imageData);
			} catch (IOException e) {
				System.out.println("로컬 저장 실패!");
				e.printStackTrace();
			}
			/*
			 * // 1. 아이디
			 * movie.setMovieId(Integer.parseInt(multipartRequest.getParameter("movieId")));
			 */
			// 2. 제목
			String movieTitle = multipartRequest.getParameter("movieTitle");
			movie.setMovieTitle(movieTitle);
			// 3. 카테고리
			movie.setMovieCategory(multipartRequest.getParameter("movieCategory"));
			// 4. 상영 시작일
			Date openingDate = Date.valueOf(multipartRequest.getParameter("openingDate"));
			movie.setOpeningDate(openingDate);
			// 5. 상영 종료일
			Date closingDate = Date.valueOf(multipartRequest.getParameter("closingDate"));
			movie.setClosingDate(closingDate);
			// 6. 상영 소요시간
			movie.setRunningTime(Integer.parseInt(multipartRequest.getParameter("runningTime")));
			// 7. 영화감독
			movie.setMovieDirector(multipartRequest.getParameter("movieDirector"));
			// 8. 영화 줄거리
			movie.setMovieSynopsis(multipartRequest.getParameter("movieSynopsis"));
			// 9. 출연진
			movie.setPerformer(multipartRequest.getParameter("performer"));
			// 10. 시청연령
			movie.setIsAdultMovie(multipartRequest.getParameter("isAdultMovie"));

			movieService.insertMovie(movie);

			//			movieRepository.insertMovie(movie);
			//			movieRepository.insertMoviePoster(movie);

			System.out.println(movie.getMovieTitle() + "영화가 신규 등록되었습니다!");
		}
		return "home";
	}


	/* 영화 수정 */
	@RequestMapping(value="/movies/{movieId}", method=RequestMethod.PUT)
	public void updateMovie()  {

	}

	/* 영화 삭제 */
	@RequestMapping(value="/movies/{movieId}", method=RequestMethod.DELETE)
	public void deleteMovie()  {

	}


	/* 기능2: 상영일 관리 */


	/* 상영 시간 관리 페이지 */
	@RequestMapping(value="/schedule", method=RequestMethod.GET)
	public String movieSchedule() {
		return "admin/schedule";
	}

}
