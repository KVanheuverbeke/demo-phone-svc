package be.demo.normalizephone.beans;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonView(Views.Limited.class)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Validity {

    @Schema(example = "0")
    Short flag;
    @Schema(example = "phone number OK")
    String message;
    @Schema(example = "00")
    String detailedFlag;
    @Schema(example = "PHONE_OK")
    String detailedMessage;

}