package com.ascendcorp.exam.service;

import com.ascendcorp.exam.model.InquiryServiceResultDTO;
import com.ascendcorp.exam.model.TransferResponse;
import com.ascendcorp.exam.proxy.BankProxyGateway;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;

import java.util.Date;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;

@TestConfiguration
@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
public class InquiryServiceTest {

    @InjectMocks
    private InquiryService inquiryService;
    @Mock
    private BankProxyGateway bankProxyGateway;

    private static TransferResponse getTransferResponse() {
        TransferResponse mockResponse = new TransferResponse();
        mockResponse.setBankTransactionID("1");
        mockResponse.setResponseCode("unknown");
        mockResponse.setTranDateTime(new Date());
        mockResponse.setChannel("1");
        mockResponse.setLocationCode("1");
        mockResponse.setBankCode("1");
        mockResponse.setBankNumber("1");
        mockResponse.setDescription("501:Unknown Error");
        mockResponse.setReferenceCode1("REF001");
        mockResponse.setReferenceCode2("REF002");
        mockResponse.setAmount(1000.0);
        mockResponse.setFirstName("testFirst");
        mockResponse.setLastName("testLast");
        return mockResponse;
    }

    @Test
    public void testValidInquiryApprovedResponse() {

        TransferResponse mockResponse = getTransferResponse();
        mockResponse.setResponseCode("approved");
        mockResponse.setDescription("Transaction approved");
        mockResponse.setReferenceCode1("REF001");
        mockResponse.setReferenceCode2("REF002");
        mockResponse.setAmount(1000.0);

        when(bankProxyGateway.requestTransfer(any(TransferResponse.class)))
                .thenReturn(mockResponse);

        InquiryServiceResultDTO result = inquiryService.inquiry(mockResponse);

        assertNotNull(result);
        assertEquals("200", result.getReasonCode());
        assertEquals("Transaction approved", result.getReasonDesc());
        assertEquals("REF001", result.getRef_no1());
        assertEquals("REF002", result.getRef_no2());
        assertEquals("1000.0", result.getAmount());
        assertEquals("1", result.getTranID());

        verify(bankProxyGateway, times(1)).requestTransfer(any(TransferResponse.class));
    }

    @Test
    public void testInvalidDataResponse() {

        TransferResponse mockResponse = getTransferResponse();

        mockResponse.setResponseCode("invalid_data");
        mockResponse.setDescription("400:Invalid:Invalid Account Number");

        when(bankProxyGateway.requestTransfer(any(TransferResponse.class)))
                .thenReturn(mockResponse);

        InquiryServiceResultDTO result = inquiryService.inquiry(mockResponse);

        assertNotNull(result);
        assertEquals("400", result.getReasonCode());
        assertEquals("Invalid Account Number", result.getReasonDesc());

        verify(bankProxyGateway, times(1)).requestTransfer(any(TransferResponse.class));
    }

    @Test
    public void testTransactionErrorResponse() {

        TransferResponse mockResponse = getTransferResponse();
        mockResponse.setResponseCode("transaction_error");
        mockResponse.setDescription("98:Transaction Failed");


        when(bankProxyGateway.requestTransfer(any(TransferResponse.class)))
                .thenReturn(mockResponse);

        InquiryServiceResultDTO result = inquiryService.inquiry(mockResponse);

        assertNotNull(result);
        assertEquals("98", result.getReasonCode());
        assertEquals("Transaction Failed", result.getReasonDesc());

        verify(bankProxyGateway, times(1)).requestTransfer(any(TransferResponse.class));
    }

    @Test
    public void testUnknownErrorResponse() {

        TransferResponse mockResponse = getTransferResponse();
        mockResponse.setResponseCode("unknown");
        mockResponse.setDescription("501:Unknown Error");

        when(bankProxyGateway.requestTransfer(any(TransferResponse.class)))
                .thenReturn(mockResponse);

        InquiryServiceResultDTO result = inquiryService.inquiry(mockResponse);

        assertNotNull(result);
        assertEquals("501", result.getReasonCode());
        assertEquals("Unknown Error", result.getReasonDesc());

        verify(bankProxyGateway, times(1)).requestTransfer(any(TransferResponse.class));
    }

    @Test
    public void testProcessResponse_NullResponse() {

        TransferResponse response = null;

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                inquiryService.processResponse(response)
        );
        assertEquals("Unable to inquiry from service.", exception.getMessage());
    }

    @Test
    public void testProcessResponse_UnsupportedResponseCode() {

        TransferResponse response = new TransferResponse();
        response.setResponseCode("unsupported_code");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                inquiryService.processResponse(response)
        );
        assertEquals("Unsupported error reason code", exception.getMessage());
    }

    @Test
    public void testHandleInvalidData_NullDescription() {

        TransferResponse response = new TransferResponse();
        InquiryServiceResultDTO resultDTO = new InquiryServiceResultDTO();

        // set Description Null
        response.setDescription(null);

        inquiryService.handleInvalidData(response, resultDTO);

        assertEquals("400", resultDTO.getReasonCode());
        assertEquals("General Invalid Data", resultDTO.getReasonDesc());
    }

    @Test
    public void testHandleInvalidData_DescriptionWithLessThan3Parts() {

        TransferResponse response = new TransferResponse();
        InquiryServiceResultDTO resultDTO = new InquiryServiceResultDTO();

        response.setDescription("INVALID:123");

        inquiryService.handleInvalidData(response, resultDTO);

        assertEquals("400", resultDTO.getReasonCode());
        assertEquals("General Invalid Data", resultDTO.getReasonDesc());
    }

    @Test
    public void testHandleInvalidData_DescriptionWith3Parts() {

        TransferResponse response = new TransferResponse();
        InquiryServiceResultDTO resultDTO = new InquiryServiceResultDTO();

        response.setDescription("INVALID:CODE:Specific reason");

        inquiryService.handleInvalidData(response, resultDTO);

        assertEquals("INVALID", resultDTO.getReasonCode());
        assertEquals("Specific reason", resultDTO.getReasonDesc());
    }

    @Test
    public void testHandleTransactionError_NullDescription() {

        TransferResponse response = new TransferResponse();
        InquiryServiceResultDTO resultDTO = new InquiryServiceResultDTO();

        // set Description Null
        response.setDescription(null);

        inquiryService.handleTransactionError(response, resultDTO);

        assertEquals("500", resultDTO.getReasonCode());
        assertEquals("General Transaction Error", resultDTO.getReasonDesc());
    }

    @Test
    public void testHandleTransactionError_DescriptionWithLessThan2Parts() {

        TransferResponse response = new TransferResponse();
        InquiryServiceResultDTO resultDTO = new InquiryServiceResultDTO();

        response.setDescription("ERROR");

        inquiryService.handleTransactionError(response, resultDTO);

        assertEquals("500", resultDTO.getReasonCode());
        assertEquals("General Transaction Error", resultDTO.getReasonDesc());
    }

    @Test
    public void testHandleTransactionError_SubIdx1Equals98() {

        TransferResponse response = new TransferResponse();
        InquiryServiceResultDTO resultDTO = new InquiryServiceResultDTO();

        response.setDescription("98:Transaction issue");

        inquiryService.handleTransactionError(response, resultDTO);

        assertEquals("98", resultDTO.getReasonCode());
        assertEquals("Transaction issue", resultDTO.getReasonDesc());
    }

    @Test
    public void testHandleTransactionError_DescriptionWith3Parts() {

        TransferResponse response = new TransferResponse();
        InquiryServiceResultDTO resultDTO = new InquiryServiceResultDTO();

        response.setDescription("ERROR:CODE:Specific issue");

        inquiryService.handleTransactionError(response, resultDTO);

        assertEquals("CODE", resultDTO.getReasonCode());
        assertEquals("Specific issue", resultDTO.getReasonDesc());
    }

    @Test
    public void testHandleTransactionError_DescriptionWith2Parts() {

        TransferResponse response = new TransferResponse();
        InquiryServiceResultDTO resultDTO = new InquiryServiceResultDTO();

        response.setDescription("ERROR:General issue");

        inquiryService.handleTransactionError(response, resultDTO);

        assertEquals("ERROR", resultDTO.getReasonCode());
        assertEquals("General issue", resultDTO.getReasonDesc());
    }

    @Test
    public void testHandleUnknownError_NullDescription() {

        TransferResponse response = new TransferResponse();
        InquiryServiceResultDTO resultDTO = new InquiryServiceResultDTO();

        // set Description Null
        response.setDescription(null);

        inquiryService.handleUnknownError(response, resultDTO);

        // Assert
        assertEquals("501", resultDTO.getReasonCode());
        assertEquals("General Invalid Data", resultDTO.getReasonDesc());
    }

    @Test
    public void testHandleUnknownError_DescriptionWithLessThan2Parts() {

        TransferResponse response = new TransferResponse();
        InquiryServiceResultDTO resultDTO = new InquiryServiceResultDTO();

        response.setDescription("ERROR");

        inquiryService.handleUnknownError(response, resultDTO);

        assertEquals("501", resultDTO.getReasonCode());
        assertEquals("General Invalid Data", resultDTO.getReasonDesc());
    }

    @Test
    public void testHandleUnknownError_DescriptionWith2Parts() {

        TransferResponse response = new TransferResponse();
        InquiryServiceResultDTO resultDTO = new InquiryServiceResultDTO();

        response.setDescription("ERROR:Unknown issue");

        inquiryService.handleUnknownError(response, resultDTO);

        assertEquals("ERROR", resultDTO.getReasonCode());
        assertEquals("Unknown issue", resultDTO.getReasonDesc());
    }

    @Test
    public void testNullTransactionIdThrowsException() {
        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setResponseCode("00");
        transferResponse.setTranDateTime(new Date());
        transferResponse.setChannel("ATM");
        transferResponse.setLocationCode("LOC001");
        transferResponse.setBankCode("B001");
        transferResponse.setBankNumber("123456");
        transferResponse.setAmount(1000.0);
        transferResponse.setFirstName("John");
        transferResponse.setLastName("Doe");

        assertThrows(NullPointerException.class, () -> inquiryService.inquiry(transferResponse));
    }

    @Test
    public void testValidateRequest_NullTransactionId() {

        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setBankTransactionID(null); // Setting transaction ID to null
        transferResponse.setResponseCode("00");
        transferResponse.setTranDateTime(new Date());
        transferResponse.setChannel("ATM");
        transferResponse.setLocationCode("LOC001");
        transferResponse.setBankCode("B001");
        transferResponse.setBankNumber("123456");
        transferResponse.setAmount(1000.0);
        transferResponse.setFirstName("John");
        transferResponse.setLastName("Doe");

        NullPointerException exception = assertThrows(NullPointerException.class, () -> inquiryService.validateRequest(transferResponse));
        assertEquals("Transaction id Invalid Data", exception.getMessage());
    }

    @Test
    public void testValidateRequest_NullResponseCode() {
        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setBankTransactionID("123");
        transferResponse.setResponseCode(null); // Setting response code to null
        transferResponse.setTranDateTime(new Date());
        transferResponse.setChannel("ATM");
        transferResponse.setLocationCode("LOC001");
        transferResponse.setBankCode("B001");
        transferResponse.setBankNumber("123456");
        transferResponse.setAmount(1000.0);
        transferResponse.setFirstName("John");
        transferResponse.setLastName("Doe");

        NullPointerException exception = assertThrows(NullPointerException.class, () -> inquiryService.validateRequest(transferResponse));
        assertEquals("Response Code is required!", exception.getMessage());
    }

    @Test
    public void testValidateRequest_NullTranDateTime() {

        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setBankTransactionID("123");
        transferResponse.setResponseCode("00");
        transferResponse.setTranDateTime(null); // Setting transaction date to null
        transferResponse.setChannel("ATM");
        transferResponse.setLocationCode("LOC001");
        transferResponse.setBankCode("B001");
        transferResponse.setBankNumber("123456");
        transferResponse.setAmount(1000.0);
        transferResponse.setFirstName("John");
        transferResponse.setLastName("Doe");

        NullPointerException exception = assertThrows(NullPointerException.class, () -> inquiryService.validateRequest(transferResponse));
        assertEquals("Transaction DateTime is required!", exception.getMessage());
    }

    @Test
    public void testValidateRequest_NullChannel() {

        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setBankTransactionID("123");
        transferResponse.setResponseCode("00");
        transferResponse.setTranDateTime(new Date());
        transferResponse.setChannel(null); // Setting channel to null
        transferResponse.setLocationCode("LOC001");
        transferResponse.setBankCode("B001");
        transferResponse.setBankNumber("123456");
        transferResponse.setAmount(1000.0);
        transferResponse.setFirstName("John");
        transferResponse.setLastName("Doe");

        NullPointerException exception = assertThrows(NullPointerException.class, () -> inquiryService.validateRequest(transferResponse));
        assertEquals("Channel is required!", exception.getMessage());
    }

    @Test
    public void testValidateRequest_NullLocationCode() {

        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setBankTransactionID("123");
        transferResponse.setResponseCode("00");
        transferResponse.setTranDateTime(new Date());
        transferResponse.setChannel("ATM");
        transferResponse.setLocationCode(null); // Setting location code to null
        transferResponse.setBankCode("B001");
        transferResponse.setBankNumber("123456");
        transferResponse.setAmount(1000.0);
        transferResponse.setFirstName("John");
        transferResponse.setLastName("Doe");

        NullPointerException exception = assertThrows(NullPointerException.class, () -> inquiryService.validateRequest(transferResponse));
        assertEquals("locationCode is required!", exception.getMessage());
    }

    @Test
    public void testValidateRequest_NullOrEmptyBankCode() {

        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setBankTransactionID("123");
        transferResponse.setResponseCode("00");
        transferResponse.setTranDateTime(new Date());
        transferResponse.setChannel("ATM");
        transferResponse.setLocationCode("LOC001");
        transferResponse.setBankCode(""); // Empty bank code
        transferResponse.setBankNumber("123456");
        transferResponse.setAmount(1000.0);
        transferResponse.setFirstName("John");
        transferResponse.setLastName("Doe");

        NullPointerException exception = assertThrows(NullPointerException.class, () -> inquiryService.validateRequest(transferResponse));
        assertEquals("Bank Code is required!", exception.getMessage());

        // Test with null bank code
        transferResponse.setBankCode(null); // Null bank code
        exception = assertThrows(NullPointerException.class, () -> inquiryService.validateRequest(transferResponse));
        assertEquals("Bank Code is required!", exception.getMessage());
    }

    @Test
    public void testValidateRequest_NullOrEmptyBankNumber() {

        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setBankTransactionID("123");
        transferResponse.setResponseCode("00");
        transferResponse.setTranDateTime(new Date());
        transferResponse.setChannel("ATM");
        transferResponse.setLocationCode("LOC001");
        transferResponse.setBankCode("B001");
        transferResponse.setBankNumber(""); // Empty bank number
        transferResponse.setAmount(1000.0);
        transferResponse.setFirstName("John");
        transferResponse.setLastName("Doe");

        NullPointerException exception = assertThrows(NullPointerException.class, () -> inquiryService.validateRequest(transferResponse));
        assertEquals("Bank Number is required!", exception.getMessage());

        // Test with null bank number
        transferResponse.setBankNumber(null); // Null bank number
        exception = assertThrows(NullPointerException.class, () -> inquiryService.validateRequest(transferResponse));
        assertEquals("Bank Number is required!", exception.getMessage());
    }

    @Test
    public void testValidateRequest_AmountLessThanOrEqualZero() {

        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setBankTransactionID("123");
        transferResponse.setResponseCode("00");
        transferResponse.setTranDateTime(new Date());
        transferResponse.setChannel("ATM");
        transferResponse.setLocationCode("LOC001");
        transferResponse.setBankCode("B001");
        transferResponse.setBankNumber("123456");
        transferResponse.setAmount(0); // Amount is zero
        transferResponse.setFirstName("John");
        transferResponse.setLastName("Doe");

        // Act & Assert
        NullPointerException exception = assertThrows(NullPointerException.class, () -> inquiryService.validateRequest(transferResponse));
        assertEquals("Amount must more than zero!", exception.getMessage());
    }

    @Test
    public void testValidateRequest_NullFirstName() {

        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setBankTransactionID("123");
        transferResponse.setResponseCode("00");
        transferResponse.setTranDateTime(new Date());
        transferResponse.setChannel("ATM");
        transferResponse.setLocationCode("LOC001");
        transferResponse.setBankCode("B001");
        transferResponse.setBankNumber("123456");
        transferResponse.setAmount(1000.0);
        transferResponse.setFirstName(null); // Null first name
        transferResponse.setLastName("Doe");

        NullPointerException exception = assertThrows(NullPointerException.class, () -> inquiryService.validateRequest(transferResponse));
        assertEquals("FirstName is required!", exception.getMessage());
    }

    @Test
    public void testValidateRequest_NullLastName() {

        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setBankTransactionID("123");
        transferResponse.setResponseCode("00");
        transferResponse.setTranDateTime(new Date());
        transferResponse.setChannel("ATM");
        transferResponse.setLocationCode("LOC001");
        transferResponse.setBankCode("B001");
        transferResponse.setBankNumber("123456");
        transferResponse.setAmount(1000.0);
        transferResponse.setFirstName("John");
        transferResponse.setLastName(null); // Null last name

        NullPointerException exception = assertThrows(NullPointerException.class, () -> inquiryService.validateRequest(transferResponse));
        assertEquals("LastName is required!", exception.getMessage());
    }

    @Test
    public void testValidateRequest_ValidInputs() {

        TransferResponse transferResponse = new TransferResponse();
        transferResponse.setBankTransactionID("123");
        transferResponse.setResponseCode("00");
        transferResponse.setTranDateTime(new Date());
        transferResponse.setChannel("ATM");
        transferResponse.setLocationCode("LOC001");
        transferResponse.setBankCode("B001");
        transferResponse.setBankNumber("123456");
        transferResponse.setAmount(1000.0);
        transferResponse.setFirstName("John");
        transferResponse.setLastName("Doe");

        // Act & Assert (No exception should be thrown with valid inputs)
        assertDoesNotThrow(() -> inquiryService.validateRequest(transferResponse));
    }
}
