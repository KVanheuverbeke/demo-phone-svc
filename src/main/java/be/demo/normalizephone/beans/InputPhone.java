package be.demo.normalizephone.beans;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InputPhone {

    @Schema(example = "025559719")
    String phone;
    @Schema(example = "BE", defaultValue = "BE")
    String defaultCountryCode;

}