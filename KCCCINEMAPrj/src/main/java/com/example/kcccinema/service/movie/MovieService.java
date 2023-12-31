package com.example.kcccinema.service.movie;

import java.awt.Graphics2D; 
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import com.example.kcccinema.dao.IMovieRepository;
import com.example.kcccinema.model.MovieVO;

@Service
public class MovieService{
	private static final String UPLOAD_POSTER = "C:/kcccinema/movieposter/";
	@Autowired
	private IMovieRepository movieRepository;
	@Autowired
	MovieVO movie;

	/* 영화 추가 기능 */
	public void insertMovie(MultipartFile file, MultipartHttpServletRequest  multipartRequest, ModelAndView modelAndView) throws Exception {
		
		multipartRequest.setCharacterEncoding("utf-8");

		// 이미지파일이 있는 경우
		if(file!=null && !file.isEmpty()) {
			try {
				// 바이트 변환은 임시파일은 .tmp가 사라지기전에 해주어야 하므로 Multipartfile작업 전에 처리
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
		}

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
			
		movieRepository.insertMovie(movie);
		movieRepository.insertMoviePoster(movie);
	}


	/* 전체 영화 조회 */
	public List<MovieVO> getAllMovieList() {
		List<MovieVO> allMovieList = movieRepository.selectAllMovieList();

		Date currentDate = new Date(System.currentTimeMillis());
		for (MovieVO movie : allMovieList) {
			String base64Image = Base64.getEncoder().encodeToString(movie.getMoviePoster());
			movie.setBase64Image(base64Image);

			if (movie.getOpeningDate().after(currentDate)) {
				movie.setScreening("상영예정");
			} else if (movie.getClosingDate().after(currentDate)) {
				movie.setScreening("상영중");
			} else {
				movie.setScreening("상영종료");
			}
		}
		return allMovieList;
	}

	/* 영화 날짜순 조회 */
	public List<MovieVO> getMovieListByDate() {
		List<MovieVO> movieListDate = movieRepository.selectMovieListByDate();

		Date currentDate = new Date(System.currentTimeMillis());
		for (MovieVO movie : movieListDate) {
			String base64Image = Base64.getEncoder().encodeToString(movie.getMoviePoster());
			movie.setBase64Image(base64Image);

			if (movie.getOpeningDate().after(currentDate)) {
				movie.setScreening("상영예정");
			} else if (movie.getClosingDate().after(currentDate)) {
				movie.setScreening("상영중");
			} else {
				movie.setScreening("상영종료");
			}
		}
		return movieListDate;
	}

	/* 영화 상영 상태에 따른 조회 */
	public List<MovieVO> getMoviesByStatus(String status) {
		List<MovieVO> movieListByStatus = new ArrayList<MovieVO>();
		List<MovieVO> allMovieList = getAllMovieList();
		for (MovieVO movie : allMovieList) {
			if (movie.getScreening().equals(status)) {
				movieListByStatus.add(movie);
			}
		}
		return movieListByStatus;
	}

	/* 영화 포스터 이미지 압축 */
	private static byte[] compressImage(byte[] imageData, double compressionQuality) {
		try (InputStream inputStream = new ByteArrayInputStream(imageData)) {
			BufferedImage originalImage = ImageIO.read(inputStream);
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			// Create a new BufferedImage with the same dimensions and type as the original
			// image
			BufferedImage compressedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(),
					originalImage.getType());

			// Get the Graphics2D object of the compressed image
			Graphics2D g = compressedImage.createGraphics();

			// Draw the original image on the compressed image with the specified
			// compression quality
			g.drawImage(originalImage, 0, 0, originalImage.getWidth(), originalImage.getHeight(), null);

			// Encode the compressed image to JPEG format with the specified compression
			// quality
			ImageIO.write(compressedImage, "jpeg", outputStream);

			return outputStream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			return imageData; // Return the original image data if compression fails
		}
	}

	/* 영화 수정 */

	/* 카테고리, 제목 수정 */
	public boolean updateMovieData(int movieId, String fieldName, String newValue) throws Exception {
		Map<String, Object> updateInformation = new HashMap<>();
		updateInformation.put("movieId", movieId);
		if (fieldName.equals("movieCategory")) {
			fieldName = "MOVIE_CATEGORY";
		} else {
			fieldName = "MOVIE_TITLE";
		} 
		updateInformation.put("fieldName", fieldName);
		updateInformation.put("newValue", newValue);

		int resultNumber = movieRepository.updateMovie(updateInformation);
		if (resultNumber == 1) {
			return true;
		} else {
			return false;
		}
	}

	/* 상영 시작일, 상영 종료일 수정 */
	public boolean updateMovieData(int movieId, String fieldName, Date newDate) throws Exception {
		Map<String, Object> updateInformation = new HashMap<>();
		updateInformation.put("movieId", movieId);
		if(fieldName.equals("openingDate")) {
			fieldName = "OPENING_DATE";
		} else {
			fieldName = "CLOSING_DATE";
		}
		updateInformation.put("fieldName", fieldName);
		updateInformation.put("newValue", newDate);

		int resultNumber = movieRepository.updateMovie(updateInformation);
		if (resultNumber == 1) {
			return true;
		} else {
			return false;
		}
	}

	/* 영화 검색 */
	public List<MovieVO> searchMovies(String searchWord) throws Exception {
		List<MovieVO> movieList = movieRepository.selectMoviesBySearchWord(searchWord);
		for (MovieVO movie : movieList) {
			String base64Image = Base64.getEncoder().encodeToString(movie.getMoviePoster());
			movie.setBase64Image(base64Image);
		}
		return movieList;
	}

	/* 영화명 자동완성 */
	public List<String> searchMoviesTitle(String searchWord) throws Exception {
		List<MovieVO> movieList = movieRepository.selectMoviesTitle(searchWord);
		List<String> movieTitleList = new ArrayList<>();
		for (MovieVO movie : movieList) {
			movieTitleList.add(movie.getMovieTitle());
		}
		return movieTitleList;
	}

	/* 영화 삭제 */
	public void deleteMovies(List<Integer> ids) {
		movieRepository.deleteMoviesByIds(ids);
	}

}
	
