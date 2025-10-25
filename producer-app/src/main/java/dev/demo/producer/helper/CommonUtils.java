package dev.demo.producer.helper;


import com.google.common.base.CaseFormat;
import dev.demo.producer.entities.GeneralResponse;
import dev.demo.producer.enums.GeneralResponseStatus;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

@UtilityClass
@Slf4j
public class CommonUtils {

    /**
     * Method general response for controllers
     *
     * @param data
     * @param <T>
     * @return <T> GeneralResponse<T>
     */
    public <T> GeneralResponse<T> buildGeneralResponse(T data) {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null in general response");
        }
        GeneralResponse<T> generalResponse = new GeneralResponse<>();
        generalResponse.setData(data);
        generalResponse.setStatus(GeneralResponseStatus.SUCCESS);
        return generalResponse;
    }

    /**
     * Method general response for controllers with empty data
     *
     * @param <T>
     * @return <T> GeneralResponse<T>
     */
    public <T> GeneralResponse<T> buildResponseWithEmptyData() {
        GeneralResponse<T> generalResponse = new GeneralResponse<>();
        generalResponse.setStatus(GeneralResponseStatus.SUCCESS);
        return generalResponse;
    }
}
