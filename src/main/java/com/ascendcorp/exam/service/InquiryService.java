package com.ascendcorp.exam.service;

import com.ascendcorp.exam.model.InquiryServiceResultDTO;
import com.ascendcorp.exam.model.TransferResponse;
import com.ascendcorp.exam.proxy.BankProxyGateway;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InquiryService {

    @Autowired
    private BankProxyGateway bankProxyGateway;

    final static Logger log = Logger.getLogger(InquiryService.class);

    // Class นี้ มี improve แยกกรณีที่ Function มี if else ตั้งแต่ 2 step ขึ้นไป ไปเป็นอีกฟังก์ชันหนึ่งไว้เรียกใช้ จะได้ดู clean ขึ้น
    // และเปลี่ยนจาก parameter รายตัว เป็นทั้ง model เผื่อว่าอนาคตจะมี field อื่นเพิ่ม
    public InquiryServiceResultDTO inquiry(TransferResponse transferResponse)
    {
        log.info("validate request parameters");
        validateRequest(transferResponse);

        log.info("call bank web service");
        TransferResponse response = bankProxyGateway.requestTransfer(transferResponse);

        return processResponse(response);
    }

    void validateRequest(TransferResponse transferResponse) {
        if(transferResponse.getBankTransactionID() == null) {
            log.info("Transaction id is required!");
            throw new NullPointerException("Transaction id Invalid Data");
        }
        if(transferResponse.getResponseCode() == null) {
            log.info("Response Code is required!");
            throw new NullPointerException("Response Code is required!");
        }
        if(transferResponse.getTranDateTime() == null) {
            log.info("Transaction DateTime is required!");
            throw new NullPointerException("Transaction DateTime is required!");
        }
        if(transferResponse.getChannel() == null) {
            log.info("Channel is required!");
            throw new NullPointerException("Channel is required!");
        }
        if(transferResponse.getLocationCode() == null) {
            log.info("locationCode is required!");
            throw new NullPointerException("locationCode is required!");
        }
        if(transferResponse.getBankCode() == null || transferResponse.getBankCode().equalsIgnoreCase("")) {
            log.info("Bank Code is required!");
            throw new NullPointerException("Bank Code is required!");
        }
        if(transferResponse.getBankNumber() == null || transferResponse.getBankNumber().equalsIgnoreCase("")) {
            log.info("Bank Number is required!");
            throw new NullPointerException("Bank Number is required!");
        }
        if(transferResponse.getAmount() <= 0) {
            log.info("Amount must more than zero!");
            throw new NullPointerException("Amount must more than zero!");
        }
        if(transferResponse.getFirstName() == null) {
            log.info("FirstName is required!");
            throw new NullPointerException("FirstName is required!");
        }
        if(transferResponse.getLastName() == null) {
            log.info("LastName is required!");
            throw new NullPointerException("LastName is required!");
        }
    }

    InquiryServiceResultDTO processResponse(TransferResponse response) {
        InquiryServiceResultDTO respDTO = new InquiryServiceResultDTO();

        if (response != null) {
            respDTO.setRef_no1(response.getReferenceCode1());
            respDTO.setRef_no2(response.getReferenceCode2());
            respDTO.setAmount(String.valueOf(response.getAmount()));
            respDTO.setTranID(response.getBankTransactionID());
            respDTO.setBalance(String.valueOf(response.getAmount()));
            respDTO.setNamespace("testExam");

            // จากเดิมที่เราแมพแต่ละกรณีด้วย equalsIgnoreCase() ไม่ว่าค่า ResponseCode จะเข้ามาพิมพ์ใหญ่พิมพ์เล็กแค่เป็นคำที่ตรงกันก็เข้า if เปลี่ยนเป็น
            // รับค่าเข้ามาโดยใช้ toLowerCase แล้วใช้ switch case แมพแต่ละกรณ๊ไว้เป็นตัวพิมพ์เล็กก็จะได้ผลลัพธ์เหมือนกัน ดูง่ายกว่าด้วย

            switch (response.getResponseCode().toLowerCase()) {
                case "approved":
                    respDTO.setReasonCode("200");
                    respDTO.setReasonDesc(response.getDescription());
                    respDTO.setAccountName(response.getFirstName());
                    break;
                case "invalid_data":
                    handleInvalidData(response, respDTO);
                    break;
                case "transaction_error":
                    handleTransactionError(response, respDTO);
                    break;
                case "unknown":
                    handleUnknownError(response, respDTO);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported error reason code");
            }
        } else {
            throw new IllegalStateException("Unable to inquiry from service.");
        }

        return respDTO;
    }

    void handleInvalidData(TransferResponse response, InquiryServiceResultDTO respDTO) {
        String replyDesc = response.getDescription();
        if (replyDesc != null) {
            String[] respDesc = replyDesc.split(":");
            if (respDesc.length >= 3) {
                respDTO.setReasonCode(respDesc[0]);
                respDTO.setReasonDesc(respDesc[2]);
            } else {
                respDTO.setReasonCode("400");
                respDTO.setReasonDesc("General Invalid Data");
            }
        } else {
            respDTO.setReasonCode("400");
            respDTO.setReasonDesc("General Invalid Data");
        }
    }

    void handleTransactionError(TransferResponse response, InquiryServiceResultDTO respDTO) {
        String replyDesc = response.getDescription();
        if (replyDesc != null) {
            String[] respDesc = replyDesc.split(":");
            if (respDesc.length >= 2) {
                String subIdx1 = respDesc[0];
                String subIdx2 = respDesc[1];
                if ("98".equalsIgnoreCase(subIdx1)) {
                    respDTO.setReasonCode(subIdx1);
                    respDTO.setReasonDesc(subIdx2);
                } else if (respDesc.length >= 3) {
                    respDTO.setReasonCode(subIdx2);
                    respDTO.setReasonDesc(respDesc[2]);
                } else {
                    respDTO.setReasonCode(subIdx1);
                    respDTO.setReasonDesc(subIdx2);
                }
            } else {
                respDTO.setReasonCode("500");
                respDTO.setReasonDesc("General Transaction Error");
            }
        } else {
            respDTO.setReasonCode("500");
            respDTO.setReasonDesc("General Transaction Error");
        }
    }

    void handleUnknownError(TransferResponse response, InquiryServiceResultDTO respDTO) {
        String replyDesc = response.getDescription();
        if (replyDesc != null) {
            String[] respDesc = replyDesc.split(":");
            if (respDesc.length >= 2) {
                respDTO.setReasonCode(respDesc[0]);
                respDTO.setReasonDesc(respDesc[1]);
            } else {
                respDTO.setReasonCode("501");
                respDTO.setReasonDesc("General Invalid Data");
            }
        } else {
            respDTO.setReasonCode("501");
            respDTO.setReasonDesc("General Invalid Data");
        }
    }
}
