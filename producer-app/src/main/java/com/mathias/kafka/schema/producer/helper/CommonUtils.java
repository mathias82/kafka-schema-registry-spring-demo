package com.mathias.kafka.schema.producer.helper;

import com.mathias.kafka.schema.producer.entities.GeneralResponse;
import com.mathias.kafka.schema.producer.enums.GeneralResponseStatus;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

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
