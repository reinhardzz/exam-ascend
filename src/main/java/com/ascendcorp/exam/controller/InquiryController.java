package com.ascendcorp.exam.controller;

import com.ascendcorp.exam.model.InquiryServiceResultDTO;
import com.ascendcorp.exam.model.TransferResponse;
import com.ascendcorp.exam.service.InquiryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inquiry")
public class InquiryController {

    @Autowired
    private InquiryService inquiryService;

    @PostMapping("/transaction")
    public InquiryServiceResultDTO inquireTransaction(@RequestBody TransferResponse transferResponse) {

        return inquiryService.inquiry(transferResponse);
    }
}
