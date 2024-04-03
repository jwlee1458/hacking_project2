package com.example.hackingproject.stock;

import com.example.hackingproject.common.JwtTokenUtil;
import com.example.hackingproject.notice.dto.NoticeReq;
import com.example.hackingproject.notice.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class StockRestAPIController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private NoticeService noticeService;

    @RequestMapping(value = {"/main","/"}, method = RequestMethod.GET)
    public ModelAndView main(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView mav = new ModelAndView();

        // 공지사항 목록 가져오기
        List<NoticeReq> noticeList = noticeService.getNoticeList();
        mav.addObject("noticeList", noticeList);

        mav.addObject("login", jwtTokenUtil.JWTTokenCheck(request));

        mav.setViewName("main_menu");
        return mav;
    }
}
