package be.demo.normalizephone;

import be.demo.normalizephone.beans.InputPhone
import be.demo.normalizephone.business.NormalizePhone
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

class NormalizePhoneSpec extends Specification {

    @Subject
    NormalizePhone normalizePhone = new NormalizePhone()
    static final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    def 'valid local phone normalization'() {

        given: 'Pass valid local phone number'
        def inputPhone = new InputPhone("0479445566", "BE")

        when: 'We normalize the input'
        def outputPhone = normalizePhone.normalize(inputPhone)

        then: 'We expect the following result'
        with(outputPhone) {
            internationalPrefix == Integer.valueOf(32)
            zonalPrefix == "479"
            number == "445566"
            phoneDedupKey == Long.valueOf(479445566)
            phoneNormalized == "0479/445566"
            countryCode == "BE"
            validity.flag == Short.valueOf((short) 0)
            validity.message == "phone number OK"
            validity.detailedFlag == "00"
            validity.detailedMessage == "PHONE_OK"
            phoneType == Short.valueOf((short) 1)
            phoneIntNormalized == "+32479445566"
            phoneIntNormalizedE123 == "+32 479 44 55 66"
            phoneIntNormalizedSopres == "+32 (0479) 445566"
        }

    }

    def 'valid international phone normalization'() {

        given: 'Pass valid international phone number'
        def inputPhone = new InputPhone("+32479445566", "BE")

        when: 'We normalize the input'
        def outputPhone = normalizePhone.normalize(inputPhone)

        then: 'We expect the following result'
        with(outputPhone) {
            internationalPrefix == Integer.valueOf(32)
            zonalPrefix == "479"
            number == "445566"
            phoneDedupKey == Long.valueOf(479445566)
            phoneNormalized == "0479/445566"
            countryCode == "BE"
            validity.flag == Short.valueOf((short) 0)
            validity.message == "phone number OK"
            validity.detailedFlag == "00"
            validity.detailedMessage == "PHONE_OK"
            phoneType == Short.valueOf((short) 1)
            phoneIntNormalized == "+32479445566"
            phoneIntNormalizedE123 == "+32 479 44 55 66"
            phoneIntNormalizedSopres == "+32 (0479) 445566"
        }

    }

    // launch multiple tests on the normalizer with some different kind of input values and expected results
    @Unroll
    def 'test with #inputPhone and #inputDefaultCountry should give the expected result'() {

        given: 'Pass valid international phone number'
        def input = new InputPhone(inputPhone, inputDefaultCountry)

        when: 'We normalize the input'
        def outputPhone = normalizePhone.normalize(input)

        then: 'We expect the following result'
        outputPhone.internationalPrefix == internationalPrefix
        outputPhone.zonalPrefix == zonalPrefix
        outputPhone.number == number
        outputPhone.phoneDedupKey == phoneDedupKey
        outputPhone.phoneNormalized == phoneNormalized
        outputPhone.countryCode == countryCode
        outputPhone.validity.flag == validityFlag
        outputPhone.validity.message == validityMessage
        outputPhone.validity.detailedFlag == validityDetailedFlag
        outputPhone.validity.detailedMessage == validityDetailedMessage
        outputPhone.phoneType == phoneType
        outputPhone.phoneIntNormalized == phoneIntNormalized
        outputPhone.phoneIntNormalizedE123 == phoneIntNormalizedE123
        outputPhone.phoneIntNormalizedSopres == phoneIntNormalizedSopres

        where:
        inputPhone      | inputDefaultCountry || internationalPrefix | zonalPrefix | number     | phoneDedupKey | phoneNormalized | countryCode | validityFlag       | validityMessage                        | validityDetailedFlag | validityDetailedMessage            | phoneType          | phoneIntNormalized | phoneIntNormalizedE123 | phoneIntNormalizedSopres
        // Better version than with spphone, google libphonenumber detects valid phone number via isValid function
        "+3221445566"   | "BE"                || null                | null        | null       | null          | null            | null        | Short.valueOf("8") | "invalid number for Zonal def"         | "04"                 | "ERROR_NOT_LOCAL_IN_ASKED_COUNTRY" | null               | null               | null                   | null
        "+3225559656"   | "BE"                || 32                  | "2"         | "5559656"  | 25559656L     | "02/5559656"    | "BE"        | Short.valueOf("0") | "phone number OK"                      | "00"                 | "PHONE_OK"                         | Short.valueOf("0") | "+3225559656"      | "+32 2 555 96 56"      | "+32 (02) 5559656"
        // This is more correct than before, zonal number "2" is correct !
        "+322555965"    | "BE"                || null                | null        | null       | null          | null            | null        | Short.valueOf("1") | "invalid length for National def"      | "05"                 | "ERROR_TOO_SHORT_FOR_CC_COUNTRY"   | null               | null               | null                   | null
        // Too long national number - Detailed Flag more correct
        "+32255596566"  | "BE"                || null                | null        | null       | null          | null            | null        | Short.valueOf("1") | "invalid length for National def"      | "01"                 | "ERROR_OUTPUT_TOO_LONG"            | null               | null               | null                   | null
        // Too short mobile number
        "+3247944556"   | "BE"                || null                | null        | null       | null          | null            | null        | Short.valueOf("1") | "invalid length for National def"      | "05"                 | "ERROR_TOO_SHORT_FOR_CC_COUNTRY"   | null               | null               | null                   | null
        // Too long mobile number
        "+324794455660" | "BE"                || null                | null        | null       | null          | null            | null        | Short.valueOf("1") | "invalid length for National def"      | "01"                 | "ERROR_OUTPUT_TOO_LONG"            | null               | null               | null                   | null
        // One letter in telephone number
        "+3247944556A"  | "BE"                || null                | null        | null       | null          | null            | null        | Short.valueOf("3") | "invalid character (contains letters)" | "10"                 | "ERROR_INVALID_CHARACTERS"         | null               | null               | null                   | null
        // local number checking with country FR
        "0612271893"    | "FR"                || 33                  | "6"         | "12271893" | 612271893L    | "06/12271893"   | "FR"        | Short.valueOf("0") | "phone number OK"                      | "00"                 | "PHONE_OK"                         | Short.valueOf("1") | "+33612271893"     | "+33 6 12 27 18 93"    | "+33 (06) 12271893"
        // local number checking with country LU
        "337004"        | "LU"                || 352                 | "33"        | "7004"     | 337004L       | "033/7004"      | "LU"        | Short.valueOf("0") | "phone number OK"                      | "00"                 | "PHONE_OK"                         | Short.valueOf("0") | "+352337004"       | "+352 33 70 04"        | "+352 (033) 7004"
        // local number checking with country FR
        "0678912345"    | "FR"                || 33                  | "6"         | "78912345" | 678912345L    | "06/78912345"   | "FR"        | Short.valueOf("0") | "phone number OK"                      | "00"                 | "PHONE_OK"                         | Short.valueOf("1") | "+33678912345"     | "+33 6 78 91 23 45"    | "+33 (06) 78912345"
        // local number checking with country DE
        "06654321"      | "DE"                || 49                  | "6654"      | "321"      | 6654321L      | "06654/321"     | "DE"        | Short.valueOf("0") | "phone number OK"                      | "00"                 | "PHONE_OK"                         | Short.valueOf("0") | "+496654321"       | "+49 6654 321"         | "+49 (06654) 321"

    }

    // launch multiple tests on the function getValidityFlag() with input values and expected results
    @Unroll
    def 'test method getValidityFlag() with validityDetailedFlag "#validityDetailedFlag" should give the expected result #validityFlag'() {
        when: 'Result back from function'
        def output = normalizePhone.getValidityFlag(validityDetailedFlag)

        then: 'We expect the following result'
        output == validityFlag

        where:
        validityDetailedFlag || validityFlag
        "00"                 || 0
        "01"                 || 1
        "02"                 || 5
        "03"                 || 9 // Normally not used
        "04"                 || 8
        "05"                 || 1
        "06"                 || 9  // Normally not used
        "07"                 || 2
        "08"                 || 4
        "09"                 || 6
        "10"                 || 3
        // empty
        ""                   || 9 // Normally not used
    }

    // launch multiple tests on the function getValidityFlag() with input values and expected results
    @Unroll
    def 'test method getValidityDetailedFlag with inputPhone "#inputPhone" and inputDefaultCountry "#inputDefaultCountry" should give the expected result #validityFlagDetail'() {
        when: 'Result back, after pre-treatments, from function'
        Phonenumber.PhoneNumber parsedNumber
        parsedNumber = phoneUtil.parse(inputPhone, inputDefaultCountry)

        then: 'We expect the following result'
        normalizePhone.getValidityDetailedFlag(parsedNumber, inputPhone) == validityFlagDetail

        where:
        // Better version than with spphone, google libphonenumber detects valid phone number via isValid function
        inputPhone      | inputDefaultCountry || validityFlagDetail
        "+3221445566"   | "BE"                || "04"
        "+3225559656"   | "BE"                || "00"
        // This is more correct than before, zonal number "2" is correct !
        "+322555965"    | "BE"                || "05"
        // Too long national number - Detailed Flag more correct
        "+32255596566"  | "BE"                || "01"
        // Too short mobile number
        "+3247944556"   | "BE"                || "05"
        // Too long mobile number
        "+324794455660" | "BE"                || "01"
        // One letter in telephone number
        "+3247944556A"  | "BE"                || "10"
        // local number checking with country FR
        "0612271893"    | "FR"                || "00"
        // local number checking with country LU
        "337004"        | "LU"                || "00"
        // local number checking with country FR
        "0678912345"    | "FR"                || "00"
        // local number checking with country DE
        "06654321"      | "DE"                || "00"
        // incorrect phone number with empty inputDefaultCountry
        "+32255596566"  | ""                  || "01"
        // correct phone number with empty inputDefaultCountry
        "+3225559656"   | ""                  || "00"
    }

    def 'test method Google parse with parsedNumber null should throw a NumberFormatException'() {
        when: 'Calling the Google phone util with null values'
        phoneUtil.parse(null, null)

        then: 'A NumberParseException should be thrown'
        thrown NumberParseException
    }

    // launch multiple tests on the function getValidityFlag() with input values and expected results
    @Unroll
    def 'test method isMobileNumber with inputPhone "#inputPhone" and inputDefaultCountry "#inputDefaultCountry" should give the expected result #isMobileNumber'() {
        when: 'Result back, after pre-treatments, from function'
        Phonenumber.PhoneNumber parsedNumber
        parsedNumber = phoneUtil.parse(inputPhone, inputDefaultCountry)

        then: 'We expect the following result'
        // Instead of putting value in field output, use the function to compare,
        // so you get input information which is handy when having tests that fails !
        normalizePhone.isMobileNumber(parsedNumber) == isMobileNumber

        where:
        inputPhone      | inputDefaultCountry || isMobileNumber
        "+3221445566"   | "BE"                || 0
        "+3225559656"   | "BE"                || 0
        "+322555965"    | "BE"                || 0
        "+32255596566"  | "BE"                || 0
        "+3247944556"   | "BE"                || 1
        "+324794455660" | "BE"                || 1
        "+3247944556A"  | "BE"                || 1
        "0612271893"    | "FR"                || 1
        "337004"        | "LU"                || 0
        "0678912345"    | "FR"                || 1
        "06654321"      | "DE"                || 0
        "+32255596566"  | ""                  || 0
        "+3225559656"   | ""                  || 0
    }

    def 'test method isMobileNumber with parsedNumber null should throw a null'() {
        when: 'Calling the Google phone util with null values'
        def isMobileNumber = normalizePhone.isMobileNumber(null)

        then: 'A NumberParseException should be thrown'
        isMobileNumber == null
    }

    // launch multiple tests on the function isBetweenBelgiumMobileZones() with input values and expected results
    @Unroll
    def 'test with method isBetweenBelgiumMobileZones with zone "#zone" should return #isBetweenBelgiumMobileZones'() {
        when: 'Result back from function isBetweenBelgiumMobileZones'
        def output = normalizePhone.isBetweenBelgiumMobileZones(zone)

        then: 'We expect the following result'
        output == isBetweenBelgiumMobileZones

        where:
        zone  || isBetweenBelgiumMobileZones
        "400" || false
        "456" || true
        "477" || true
        "486" || true
        "499" || true
        "500" || false
        // empty zone
        ""     || false
    }

}