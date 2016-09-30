package ru.qrhandshake.qrpos.api.merchant;

import ru.qrhandshake.qrpos.api.ApiAuth;

import javax.validation.constraints.NotNull;

/**
 * Created by lameroot on 24.05.16.
 */
public class MerchantRegisterRequest extends ApiAuth {

    @NotNull
    private String name;
    private String description;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
