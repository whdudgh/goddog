package tteogipbangbeomdae.goddog.web.volunteer.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tteogipbangbeomdae.goddog.domain.member.dto.Member;
import tteogipbangbeomdae.goddog.domain.reservation.dto.Reservation;
import tteogipbangbeomdae.goddog.domain.reservation.service.ReservationService;
import tteogipbangbeomdae.goddog.domain.shelter.dto.Shelter;
import tteogipbangbeomdae.goddog.domain.shelter.service.ShelterService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tteogipbangbeomdae.goddog.domain.member.dto.Member;
import tteogipbangbeomdae.goddog.domain.reservation.dto.Reservation;
import tteogipbangbeomdae.goddog.domain.reservation.service.ReservationService;
import tteogipbangbeomdae.goddog.domain.shelter.dto.Shelter;
import tteogipbangbeomdae.goddog.domain.shelter.service.ShelterService;


/**
 * 
 * /volunteer요청에 대한 세부 컨트롤러 요청된 url에 따라 해당하는 DB작업을 이루고 템플릿으로 연동시켜줌.
 *
 * @author  떡잎방범대 홍재헌
 * @since   2023. 09. 18.
 * @version 1.0
 */
@Controller
@RequestMapping("/volunteer")
@RequiredArgsConstructor
@Slf4j
public class VolunteerController {
	
	private final ReservationService reservationService;

	private final ShelterService shelterService;
	
	/**
	 * @author 떡잎방범대 홍재헌
	 * @param 봉사예약 컨트롤러
	 * @since 2023. 09. 18.
	 * @version 1.0
	 * @return /volunteer요청에 해당하는 활동과 뷰 반환
	 */
	@GetMapping("")
	public String intro(Model model) {
		return "volunteer/volunteer";
	}
	
	@GetMapping("/agreement")
	public String viewAgreement(Model model) {
		return "volunteer/agreement";
	}
	
	@GetMapping("/map")
	public String viewMap(Model model) {
		return "volunteer/map";
	}
	

	@GetMapping("/calender")
	public String viewCalender(@RequestParam("careNo") int careNo, Model model,HttpSession session) {
		Shelter shelter = shelterService.clickShelter(careNo);
		int maxCount = reservationService.getReservationCount(careNo);
		
		session.setAttribute("careNo", careNo);
		session.setAttribute("shelter", shelter);
		session.setAttribute("maxCount", maxCount);
		return "volunteer/calender";
	}
	
	@GetMapping("/choice")
	public String viewChoice(@RequestParam("regdate") String regdate, Model model, HttpSession session) {
		SimpleDateFormat inputFormat = new SimpleDateFormat("yyyyMMdd");
	    SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy.MM.dd");
	    
	    try {
	        Date date = inputFormat.parse(regdate); // 입력 날짜를 파싱
	        String formattedDate = outputFormat.format(date); // 원하는 형식으로 포맷
	        session.setAttribute("regdate", formattedDate);
	    } catch (ParseException e) {
	        // 파싱 실패 시 예외 처리
	        e.printStackTrace();
	    }
		return "volunteer/choice";
	}
	
	@GetMapping("/result/{id}/{regdate}/{regtime}")
	public String viewResult(@PathVariable("id") String id, @PathVariable("regdate") String regdate, @PathVariable("regtime") String regtime, Model model, HttpSession session) {
		int careNo = (int)session.getAttribute("careNo");
		Reservation reservation = Reservation.builder()
				.memberId(id)
				.regdate(regdate)
				.regtime(regtime)
				.careNo(careNo)
				.build();
		Reservation resultReservation = reservationService.isReservation(reservation);
		Shelter resultShelter = shelterService.clickShelter(careNo);
		model.addAttribute("resultReservation", resultReservation);
		model.addAttribute("resultShelter", resultShelter);

		return "volunteer/result";
	}
	
	@PostMapping("/result")
	public String viewResultProsses(@ModelAttribute Reservation resultReservation, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model, HttpSession session) {
		Member member = (Member)session.getAttribute("loginMember");
		int careNo = (int)session.getAttribute("careNo");
		String memberId = member.getMemberId();
		String shelterName = resultReservation.getShelterName();
		int people = resultReservation.getPeople();
		String regTime = resultReservation.getRegtime();
		String regDate = resultReservation.getRegdate().replace('.', '-');
		
		// 데이터 검증 실패 시 봉사예약 첫 화면으로 Forward
		if (bindingResult.hasErrors()) {
			return "volunteer/map";
		}
		Reservation setReservation = Reservation.builder()
											 .memberId(memberId)
											 .careNo(careNo)
											 .shelterName(shelterName)
											 .people(people)
											 .regdate(regDate)
											 .regtime(regTime)
											 .build();
		reservationService.isReservation(setReservation);
		redirectAttributes.addFlashAttribute("status", true);
		return "redirect:/volunteer/result/" + setReservation.getMemberId() + "/" + setReservation.getRegdate() + "/" + setReservation.getRegtime();
	}
	
	@GetMapping("/list")
	public String viewList(Model model) {
		return "volunteer/list";
	}
	
	@GetMapping("/detail/{reservationNo}")
	public String viewDetail(@PathVariable("reservationNo")int reservationNo, Model model) {
		Reservation reservation = reservationService.getReservation(reservationNo);

		model.addAttribute("reservation", reservation);
		return "volunteer/cancel_detail";
	}
	
}
