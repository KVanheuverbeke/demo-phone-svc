package be.demo.normalizephone.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Phone {

    @Schema(description = "Input")
    InputPhone inputPhone;
    @Schema(description = "Output")
    OutputPhone outputPhone;

}