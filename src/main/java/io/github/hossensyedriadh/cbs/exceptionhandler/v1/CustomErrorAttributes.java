package io.github.hossensyedriadh.cbs.exceptionhandler.v1;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@Component
public class CustomErrorAttributes extends DefaultErrorAttributes {
    @Override
    @NonNull
    public Map<String, @Nullable Object> getErrorAttributes(@NonNull WebRequest webRequest, @NonNull ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes=  super.getErrorAttributes(webRequest, options);
        errorAttributes.put("timestamp", OffsetDateTime.now(ZoneOffset.UTC));

        return errorAttributes;
    }
}
