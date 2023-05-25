package be.demo.normalizephone.business;

import be.demo.normalizephone.beans.InputPhone;
import be.demo.normalizephone.beans.OutputPhone;
import be.demo.normalizephone.beans.Validity;
import be.demo.normalizephone.beans.Views;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberType.MOBILE;

@Slf4j
@Service
public class NormalizePhone {

    private static final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    /**
     * Normalize a phone number
     *
     * @param phone phone number and default country
     * @return a parsed phone number with some normalizations and information of the phone number
     * @throws IOException exception
     */
    public OutputPhone normalize(InputPhone phone) throws IOException {
        // use a local variable for the input fields and do a trim on the input
        String phoneNumber = phone.getPhone().trim();
        // use a local variable for the input fields and do a trim on the input + convert to uppercase - expecting a
        // ISO 3166 alpha-2 value in fact
        String countryCode = phone.getDefaultCountryCode().trim().toUpperCase();

        Phonenumber.PhoneNumber parsedNumber;
        OutputPhone outputPhone = new OutputPhone();

        // Try to parse the given phone number
        try {
            parsedNumber = phoneUtil.parse(phoneNumber, countryCode);
        } catch (NumberParseException e) {
            parsedNumber = null;

            log.warn("Could not parse the given number {} with default country code {}", phoneNumber, countryCode);
        }

        // Give the detailed validity flag
        String validityDetailedFlag = getValidityDetailedFlag(parsedNumber, phoneNumber);
        // Give the validity flag based on conditions
        Short validityFlag = getValidityFlag(validityDetailedFlag);
        // Set the validity flag + description AND set the validity detailed flag + description
        outputPhone.setValidity(new Validity(validityFlag, getValidityFlagDescription(validityFlag), validityDetailedFlag, getValidityDetailedFlagDescription(validityDetailedFlag)));

        if (parsedNumber != null) {
            // Give international prefix
            int interNationalPrefix = parsedNumber.getCountryCode();
            outputPhone.setInternationalPrefix(interNationalPrefix);

            // Give national prefix
            String zonalPrefix = String.valueOf(parsedNumber.getNationalNumber()).substring(0, phoneUtil.getLengthOfNationalDestinationCode(parsedNumber));
            outputPhone.setZonalPrefix(zonalPrefix);

            // Give phone number without international and national prefix
            String numberWithoutIntNatPrefix = String.valueOf(parsedNumber.getNationalNumber()).substring(phoneUtil.getLengthOfNationalDestinationCode(parsedNumber));
            outputPhone.setNumber(numberWithoutIntNatPrefix);

            // Give a dedupkey on phone number
            outputPhone.setPhoneDedupKey(parsedNumber.getNationalNumber());

            // Phone normalized
            outputPhone.setPhoneNormalized("0" + zonalPrefix + "/" + numberWithoutIntNatPrefix);

            // Give zoneID - combination of country code and national prefix, f.e. 32479112233 => zoneID : 320479
            if (!zonalPrefix.isBlank()) {
                int zoneID = interNationalPrefix * 10000;
                zoneID = zoneID + Integer.parseInt(zonalPrefix);
                outputPhone.setZoneId(zoneID);
            }

            // Give the country code
            outputPhone.setCountryCode(phoneUtil.getRegionCodeForCountryCode(interNationalPrefix));

            // Is the phone number a mobile number ? Yes => 1 , No => 0, if validity flag 0 or 6
            if (validityFlag == 0 || validityFlag == 6) {
                outputPhone.setPhoneType(isMobileNumber(parsedNumber));
            }

            // International normalized phone number - E.164 - standard
            outputPhone.setPhoneIntNormalized(phoneUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.E164));

            // International normalized phone number - E.123
            outputPhone.setPhoneIntNormalizedE123(phoneUtil.format(parsedNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL));

            // Sopres formatting of telephone number
            outputPhone.setPhoneIntNormalizedSopres("+" + interNationalPrefix + " (0" + zonalPrefix + ") " + numberWithoutIntNatPrefix);
        }

        boolean showLimited;
        switch (validityFlag) {
            case 1:
            case 2:
            case 3:
            case 5:
            case 8:
                showLimited = true;
                break;
            default:
                showLimited = false;
        }

        if (showLimited) {
            ObjectMapper mapper = new ObjectMapper();
            String output = mapper.writeValueAsString(outputPhone);

            return mapper
                    .readerWithView(Views.Limited.class)
                    .forType(OutputPhone.class)
                    .readValue(output);
        }

        return outputPhone;
    }

    /**
     * Get validity flag of phone number ( PhoneNumberUtil.ValidationResult + extra checks )
     *
     * @param validityDetailedFl PhoneNumberUtil.ValidationResult
     * @return the converted validity DetailedFlag to a reduced validity flag
     */
    private Short getValidityFlag(String validityDetailedFl) {
        short validityFlag;

        // Convert the validity DetailedFlag to reduced validity flag
        switch (validityDetailedFl) {
            case "00":
                validityFlag = 0;
                break;
            case "01":
            case "05":
                validityFlag = 1;
                break;
            case "02":
                validityFlag = 5;
                break;
            case "04":
                validityFlag = 8;
                break;
            case "07":
                validityFlag = 2;
                break;
            case "08":
                validityFlag = 4;
                break;
            case "09":
                validityFlag = 6;
                break;
            case "10":
                validityFlag = 3;
                break;
            default:
                validityFlag = 9;
        }

        return validityFlag;
    }

    /**
     * Get validity flag of phone number ( PhoneNumberUtil.ValidationResult + extra checks )
     *
     * @param parsedNumber the parsed number by the phone library
     * @param phoneNumber  input phone number
     * @return validity flag
     */
    private String getValidityDetailedFlag(Phonenumber.PhoneNumber parsedNumber, String phoneNumber) {
        // set a default validity detailed flag value
        String validityDetailedFlag = "99";

        // execute first checks related to the parsed number - if it was able to parse
        if (parsedNumber != null) {
            // Check phone number with Google libphonenumber
            PhoneNumberUtil.ValidationResult result = phoneUtil.isPossibleNumberWithReason(parsedNumber);

            // Check if phone number is valid
            boolean isValid = phoneUtil.isValidNumber(parsedNumber);

            switch (result) {
                case IS_POSSIBLE:
                case IS_POSSIBLE_LOCAL_ONLY:
                    // This patch is needed, because the function isPossibleNumberWithReason is not so strict as isValidNumber for length of type phone number
                    // Extra check, isValid = false and validityDetailedFlag = 0 ( IS_POSSIBLE ) AND length for fix or mobile number is too long => validityDetailedFlag = 01 !
                    // Extra check, isValid = false and validityDetailedFlag = 0 ( IS_POSSIBLE ) AND length for mobile number is too short => validityDetailedFlag = 05 !
                    // If not, it would give : isValid = false and validityDetailedFlag = 0 ( IS_POSSIBLE ), not correct ! => validityFlag = 04 ! ( correct length but not valid number )
                    // Note : when fix number is too short => result = TOO_SHORT !
                    if (isValid) {
                        validityDetailedFlag = "00";
                        break;
                    } else if (isMobileNumber(parsedNumber) == 0 && parsedNumber.getNationalNumber() > 99999999) {
                        validityDetailedFlag = "01";
                        break;
                    } else if (isMobileNumber(parsedNumber) == 1 && parsedNumber.getNationalNumber() < 100000000) {
                        validityDetailedFlag = "05";
                        break;
                    } else {
                        validityDetailedFlag = "04";
                        break;
                    }
                case TOO_LONG:
                    validityDetailedFlag = "01";
                    break;
                case TOO_SHORT:
                    validityDetailedFlag = "05";
                    break;
                case INVALID_LENGTH:
                    validityDetailedFlag = "02";
                    break;
                case INVALID_COUNTRY_CODE:
                    validityDetailedFlag = "07"; // "03" "04" "06"
                    break;
            }
        }

        // Extra checks which the google libphonenumber doesn't check !
        // Check if phone number is empty
        if (phoneNumber.isBlank()) {
            validityDetailedFlag = "08";
        }

        // Check if phone number contains one or more letters
        if (phoneNumber.chars().anyMatch(Character::isLetter)) {
            validityDetailedFlag = "10";
        }

        return validityDetailedFlag;
    }

    /**
     * Check if phone number is a mobile number
     *
     * @param parsedNumber the parsed number by the phone library
     * @return true if the phone number is a mobile number else false
     */
    private Short isMobileNumber(Phonenumber.PhoneNumber parsedNumber) {
        PhoneNumberUtil.PhoneNumberType phoneType;
        // Try to get phone type for the given phone number
        try {
            phoneType = phoneUtil.getNumberType(parsedNumber);
        } catch (NullPointerException e) {
            return null;
        }
        String countryCode = phoneUtil.getRegionCodeForCountryCode(parsedNumber.getCountryCode());
        String zone = Long.toString(parsedNumber.getNationalNumber()).substring(0, 3);

        if (phoneType == MOBILE) {
            return 1;
        } else if ("BE".equals(countryCode) && isBetweenBelgiumMobileZones(zone)) {
            // Google libphonenumber doesn't recognize if mobile number when too short/long
            // so methode isBetweenMobileZones verifies Belgium ranges for mobiles
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * Check if the parsed number is in the belgium mobile zone
     * By taking the first 3 digits of the national number
     *
     * @param zone the phone zone (3 digits)
     * @return true if the phone number is in the belgium mobile zone else false
     */
    private boolean isBetweenBelgiumMobileZones(String zone) {
        int minValueInclusive = 456;
        int maxValueInclusive = 499;
        int str2int = Integer.parseInt("0" + zone);

        return (str2int >= minValueInclusive && str2int <= maxValueInclusive);
    }

    /**
     * Get description for the field validityFlag
     *
     * @param validityFl validity flag (short version)
     * @return the description for the validity flag
     */
    private String getValidityFlagDescription(Short validityFl) {
        String validityFlagDescription;

        switch (validityFl) {
            case 0:
                validityFlagDescription = "phone number OK";
                break;
            case 1:
                validityFlagDescription = "invalid length for National def";
                break;
            case 2:
                validityFlagDescription = "invalid prefix";
                break;
            case 3:
                validityFlagDescription = "invalid character (contains letters)";
                break;
            case 4:
                validityFlagDescription = "Blank Phone";
                break;
            case 5:
                validityFlagDescription = "invalid length for Zonal def";
                break;
            case 6:
                validityFlagDescription = "ambiguous country detection";
                break;
            case 7:
                validityFlagDescription = "set to blank because specified dedup format too short";
                break;
            case 8:
                validityFlagDescription = "invalid number for Zonal def";
                break;
            default:
                validityFlagDescription = "Unknown validity flag";
        }

        return validityFlagDescription;
    }

    /**
     * Get description for the field validityDetailedFlag
     *
     * @param validityDetailedFl validity flag (detailed version)
     * @return the description for the validity flag
     */
    private String getValidityDetailedFlagDescription(String validityDetailedFl) {
        String validityDetailedFlagDescription;

        switch (validityDetailedFl) {
            case "00":
                validityDetailedFlagDescription = "PHONE_OK";
                break;
            case "01":
                validityDetailedFlagDescription = "ERROR_OUTPUT_TOO_LONG";
                break;
            case "02":
                validityDetailedFlagDescription = "ERROR_INVALID_LENGTH_NAT";
                break;
            case "04":
                validityDetailedFlagDescription = "ERROR_NOT_LOCAL_IN_ASKED_COUNTRY";
                break;
            case "05":
                validityDetailedFlagDescription = "ERROR_TOO_SHORT_FOR_CC_COUNTRY";
                break;
            case "07":
                validityDetailedFlagDescription = "ERROR_UNKNOWN_INT_PREFIX";
                break;
            case "08":
                validityDetailedFlagDescription = "ERROR_BLANK_PHONE";
                break;
            case "09":
                validityDetailedFlagDescription = "ERROR_COUNTRY_CC_NOT_FOUND";
                break;
            case "10":
                validityDetailedFlagDescription = "ERROR_INVALID_CHARACTERS";
                break;
            default:
                validityDetailedFlagDescription = "Unknown validity flag";
        }

        return validityDetailedFlagDescription;
    }

}
