package com.example.mobile_scratch.adapter;

public class PaymentRequest {
    private String cardNumber;
    private String expirationDate;
    private String cvc;

    // Getters and setters for the payment details

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getCvc() {
        return cvc;
    }

    public void setCvc(String cvc) {
        this.cvc = cvc;
    }

    public void setCvv(String cvv) {
    }
}

