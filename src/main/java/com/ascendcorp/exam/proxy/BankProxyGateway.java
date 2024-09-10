package com.ascendcorp.exam.proxy;

import com.ascendcorp.exam.model.TransferResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
public class BankProxyGateway {

    public TransferResponse requestTransfer(
            TransferResponse transferResponse) {

        TransferResponse response = new TransferResponse();

        response.setBankTransactionID(transferResponse.getBankTransactionID());
        response.setResponseCode(transferResponse.getResponseCode());
        response.setTranDateTime(transferResponse.getTranDateTime());
        response.setChannel(transferResponse.getChannel());
        response.setBankCode(transferResponse.getBankCode());
        response.setBankNumber(transferResponse.getBankNumber());
        response.setDescription(transferResponse.getDescription());
        response.setAmount(transferResponse.getAmount());
        response.setReferenceCode1(transferResponse.getReferenceCode1());
        response.setReferenceCode2(transferResponse.getReferenceCode2());
        response.setFirstName(transferResponse.getFirstName());
        response.setLastName(transferResponse.getLastName());

        return response;
    }
}

