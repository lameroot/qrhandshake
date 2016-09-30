package ru.qrhandshake.qrpos.api.binding;

import ru.qrhandshake.qrpos.api.ApiResponse;
import ru.qrhandshake.qrpos.dto.BindingDto;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by lameroot on 02.06.16.
 */
public class GetBindingsResponse extends ApiResponse {

    private Set<BindingDto> bindings = new HashSet<>();

    public Set<BindingDto> getBindings() {
        return bindings;
    }

    public void setBindings(Set<BindingDto> bindings) {
        this.bindings = bindings;
    }
}
