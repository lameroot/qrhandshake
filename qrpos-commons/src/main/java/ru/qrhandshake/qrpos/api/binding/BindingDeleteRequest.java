package ru.qrhandshake.qrpos.api.binding;

import org.jetbrains.annotations.NotNull;
import ru.qrhandshake.qrpos.api.ApiAuth;

/**
 * Created by lameroot on 31.05.16.
 */
public class BindingDeleteRequest extends ApiAuth {

    @NotNull
    private String bindingId;
    private boolean returnNewBindingList;

    @NotNull
    public String getBindingId() {
        return bindingId;
    }

    public void setBindingId(@NotNull String bindingId) {
        this.bindingId = bindingId;
    }

    public boolean isReturnNewBindingList() {
        return returnNewBindingList;
    }

    public void setReturnNewBindingList(boolean returnNewBindingList) {
        this.returnNewBindingList = returnNewBindingList;
    }
}
