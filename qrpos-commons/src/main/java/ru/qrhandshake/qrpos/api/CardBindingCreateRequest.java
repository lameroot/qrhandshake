package ru.qrhandshake.qrpos.api;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by lameroot on 17.08.16.
 */
public class CardBindingCreateRequest extends BindingCreateRequest {

    @NotNull
    private String pan;
    @NotNull
    @Size(min = 2, max = 2)
    private String month;
    @NotNull
    @Size(min = 4, max = 4)
    private String year;
    @NotNull
    private String cardHolderName;
    @NotNull
    private String cvc;

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getCvc() {
        return cvc;
    }

    public void setCvc(String cvc) {
        this.cvc = cvc;
    }
}
