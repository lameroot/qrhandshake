package ru.qrhandshake.qrpos.api.binding;

import ru.qrhandshake.qrpos.api.ApiResponse;
import ru.qrhandshake.qrpos.dto.BindingDto;

import java.util.HashSet;
import java.util.Set;

/**
 * User: Krainov
 * Date: 06.10.2016
 * Time: 12:04
 */
public class BindingDeleteResponse extends ApiResponse {

    private Set<BindingDto> bindings = new HashSet<>();

    public Set<BindingDto> getBindings() {
        return bindings;
    }

    public void setBindings(Set<BindingDto> bindings) {
        this.bindings = bindings;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BindingDeleteResponse{");
        sb.append("bindings=").append(bindings);
        sb.append('}');
        return sb.toString();
    }
}
