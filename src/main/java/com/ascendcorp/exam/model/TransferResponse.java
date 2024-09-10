package com.ascendcorp.exam.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;


import java.util.Date;

@Data
public class TransferResponse {

    private String bankTransactionID;
    private String responseCode;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date tranDateTime;
    private String channel;
    private String locationCode;
    private String bankCode;
    private String bankNumber;
    private String description;
    private String referenceCode1;
    private String referenceCode2;
    private double amount;
    private String firstName;
    private String lastName;

}
