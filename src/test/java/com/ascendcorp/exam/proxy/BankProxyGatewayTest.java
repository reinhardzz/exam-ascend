package com.ascendcorp.exam.proxy;

import com.ascendcorp.exam.model.TransferResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@TestConfiguration
@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
class BankProxyGatewayTest {

    private BankProxyGateway bankProxyGateway;

    @BeforeEach
    void setUp() {
        bankProxyGateway = new BankProxyGateway();
    }

    private static TransferResponse getResponse() {
        TransferResponse inputTransferResponse = new TransferResponse();
        inputTransferResponse.setBankTransactionID("TX12345");
        inputTransferResponse.setResponseCode("200");
        inputTransferResponse.setTranDateTime(new Date());
        inputTransferResponse.setChannel("Online");
        inputTransferResponse.setBankCode("BANK123");
        inputTransferResponse.setBankNumber("9876543210");
        inputTransferResponse.setDescription("Transaction Approved");
        inputTransferResponse.setAmount(1000.00);
        inputTransferResponse.setReferenceCode1("REF1");
        inputTransferResponse.setReferenceCode2("REF2");
        inputTransferResponse.setFirstName("testFirst");
        inputTransferResponse.setLastName("testLast");
        return inputTransferResponse;
    }

    @Test
    void testRequestTransfer() {

        TransferResponse inputTransferResponse = getResponse();

        TransferResponse response = bankProxyGateway.requestTransfer(inputTransferResponse);

        assertNotNull(response);
        assertEquals(inputTransferResponse.getBankTransactionID(), response.getBankTransactionID());
        assertEquals(inputTransferResponse.getResponseCode(), response.getResponseCode());
        assertEquals(inputTransferResponse.getTranDateTime(), response.getTranDateTime());
        assertEquals(inputTransferResponse.getChannel(), response.getChannel());
        assertEquals(inputTransferResponse.getBankCode(), response.getBankCode());
        assertEquals(inputTransferResponse.getBankNumber(), response.getBankNumber());
        assertEquals(inputTransferResponse.getDescription(), response.getDescription());
        assertEquals(inputTransferResponse.getAmount(), response.getAmount());
        assertEquals(inputTransferResponse.getReferenceCode1(), response.getReferenceCode1());
        assertEquals(inputTransferResponse.getReferenceCode2(), response.getReferenceCode2());
        assertEquals(inputTransferResponse.getFirstName(), response.getFirstName());
        assertEquals(inputTransferResponse.getLastName(), response.getLastName());
    }

}
