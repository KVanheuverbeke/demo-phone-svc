package be.demo.normalizephone.beans;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OutputPhone {

    @JsonView(Views.Internal.class)
    @Schema(example = "32")
    Integer internationalPrefix;

    @JsonView(Views.Internal.class)
    @Schema(example = "2")
    String zonalPrefix;

    @JsonView(Views.Internal.class)
    @Schema(example = "5559719")
    String number;

    @JsonView(Views.Internal.class)
    @Schema(example = "25559719")
    Long phoneDedupKey;

    @JsonView(Views.Internal.class)
    @Schema(example = "02/5559719")
    String phoneNormalized;

    @JsonIgnore
    @Schema(hidden = true)
    Integer zoneId;

    // Contains validity flag + description
    // AND validity detailed flag + description
    @JsonView(Views.Limited.class)
    @Schema(example = "1")
    Validity validity;

    @JsonView(Views.Internal.class)
    @Schema(example = "BE")
    String countryCode;

    @JsonView(Views.Limited.class)
    @Schema(example = "0")
    Short phoneType;

    @JsonView(Views.Internal.class)
    @Schema(example = "+3225559719")
    String phoneIntNormalized;

    @JsonView(Views.Internal.class)
    @Schema(example = "+32 2 555 97 19")
    String phoneIntNormalizedE123;

    @JsonView(Views.Internal.class)
    @Schema(example = "+32 (02) 5559719")
    String phoneIntNormalizedSopres;

}