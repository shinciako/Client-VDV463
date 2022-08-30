package com.vdv463.client;

import org.springframework.core.convert.converter.Converter;
import pl.com.ekoenergetyka.vdv463.messages.AutomaticPreconditioning;

public class StringToEnumConverter implements Converter<String, AutomaticPreconditioning.PreconditioningRequest> {
    @Override
    public AutomaticPreconditioning.PreconditioningRequest convert(String source) {
        return AutomaticPreconditioning.PreconditioningRequest.fromValue(source);
    }
}
