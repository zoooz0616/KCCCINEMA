package com.example.kcccinema.controller.mypage;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.example.kcccinema.model.MovieVO;
import com.example.kcccinema.model.ReservedInfoVO;
import com.example.kcccinema.service.mypage.CheckReserveService;
import com.example.kcccinema.service.mypage.ICheckReserveService;
import com.example.kcccinema.vo.mypage.CheckReserveVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class CheckReserveController {
	static final Logger logger = LoggerFactory.getLogger(CheckReserveController.class);
	
	@Autowired
	ICheckReserveService checkReserveService;
	
	@RequestMapping(value="/checkReserve")
	public String checkReserve(HttpServletRequest request,HttpSession session, Model model) {
		
		//reservedInfoVO 리스트로 생성해서 userid로 예매한 정보들 가져오기
		List<ReservedInfoVO> reservedInfoVOList = new ArrayList<>();
		reservedInfoVOList =checkReserveService.getReservedInfo(String.valueOf(session.getAttribute("userId")));
		
		//System.out.println(reservedInfoVOList.get(0));
		
		// for문을 사용하여 각 예매 정보에 해당하는 영화 포스터 이미지를 가져와서 저장
		for (ReservedInfoVO reservedInfoVO : reservedInfoVOList) {
		    Long movieId = reservedInfoVO.getMovieId();
		    
		    //service.getMoviePoster(movieId)를 호출하여 이미지 byte를 가져와서 setmoviePoster로 저장
		    
		    MovieVO movieVO = new MovieVO();
		    movieVO = checkReserveService.getMovie(movieId);
		    
		    byte[] moviePoster = movieVO.getMoviePoster();
		    reservedInfoVO.setMoviePoster(moviePoster);
		    
		    //****reservedInfoVO에 moviePoster byte값 이용해서 String base64Image;여기에 변환해서 저장.
		    String base64Image = Base64.getEncoder().encodeToString(reservedInfoVO.getMoviePoster());
		    reservedInfoVO.setBase64Image(base64Image);
		}

		// model에 reservedInfoVO 리스트를 넣어서 HTML로 보냄
		model.addAttribute("reservedInfoVOList", reservedInfoVOList);

		return "mypage/checkReservation";
	}
	
	@RequestMapping(value="checkreserve/delete", method=RequestMethod.GET)
	public String deleteReserve(HttpSession session, Model model) {
		String userid = (String)session.getAttribute("userId");
		if(userid != null && !userid.equals("")) {
			CheckReserveVO checkreservevo = checkReserveService.selectReserve(userid);
			model.addAttribute("userId",userid);
			model.addAttribute("message", "MEMBER_PW_RE");
			return "mypage/checkReservation";
		}else {
			model.addAttribute("message","NOT_LOGIN_USER");
			return "user/login";
		}
	}
	@RequestMapping(value="checkreserve/delete", method=RequestMethod.POST)
	public String deleteReserve(String password, HttpSession session, Model model) {
		try {
			CheckReserveVO checkreservevo = new CheckReserveVO();
			checkreservevo.setUserId((String)session.getAttribute("userId"));
			String dbpw = (String)checkReserveService.getPassword(checkreservevo.getUserId());
			if(password != null && password.equals(dbpw)) {
				checkreservevo.setPassword(password);
				checkReserveService.deleteReserve(checkreservevo);
				return "mypage/checkReservation";
			}else {
				model.addAttribute("message","WRONG_PASSWORD");
				return "mypage/checkReservation";
			}
		}catch(Exception e) {
			model.addAttribute("messsage", "DELETE_FAIL");
			e.printStackTrace();
			return "mypage/checkReservation";
		}
	}

}
