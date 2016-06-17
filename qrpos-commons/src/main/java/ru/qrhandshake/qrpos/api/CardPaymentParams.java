package ru.qrhandshake.qrpos.api;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import ru.qrhandshake.qrpos.util.MaskUtil;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Created by lameroot on 30.05.16.
 */
public class CardPaymentParams extends PaymentParams {

    @JsonIgnore
    private String pan;
    @JsonIgnore
    private String cvc;
    private String month;
    private String year;
    private String cardHolderName;
    private String maskedPan;

    @JsonProperty(value = "masked_pan")
    public String getMaskedPan() {
        return null != maskedPan ? maskedPan : MaskUtil.getMaskedPan(this.pan);
    }
    void setMaskedPan(String maskedPan) {
        this.maskedPan = maskedPan;
    }
    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
        setMaskedPan(MaskUtil.getMaskedPan(pan));
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

    @JsonIgnore
    public boolean isNotBlank() {
        return null != pan && null != month && null != year && null != cardHolderName && null != cvc;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CardPaymentParams that = (CardPaymentParams) o;

        if (maskedPan != null ? !maskedPan.equals(that.maskedPan) : that.maskedPan != null) return false;
        if (!month.equals(that.month)) return false;
        if (!year.equals(that.year)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = month.hashCode();
        result = 31 * result + year.hashCode();
        result = 31 * result + (maskedPan != null ? maskedPan.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CardPaymentParams{");
        sb.append("month='").append(month).append('\'');
        sb.append(", year='").append(year).append('\'');
        sb.append(", cardHolderName='").append(cardHolderName).append('\'');
        sb.append(", maskedPan='").append(maskedPan).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
