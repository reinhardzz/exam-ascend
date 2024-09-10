package com.ascendcorp.exam.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class InquiryServiceResultDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private java.lang.String tranID;

    private String namespace;

    private java.lang.String reasonCode;

    private java.lang.String reasonDesc;

    private java.lang.String balance;

    private java.lang.String ref_no1;

    private java.lang.String ref_no2;

    private java.lang.String amount;

    private String accountName = null;

    @Override
    public String toString() {
        return "InquiryServiceResultDTO [tranID=" + tranID + ",namespace = "+namespace + ", reasonCode="
                + reasonCode + ", reasonDesc=" + reasonDesc + ", balance="
                + balance + ", ref_no1=" + ref_no1 + ", ref_no2=" + ref_no2
                + ", amount=" + amount + " ,account_name="+accountName+"  ]";
    }



}
