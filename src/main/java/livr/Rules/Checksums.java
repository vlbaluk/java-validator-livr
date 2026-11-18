package livr.Rules;

import livr.RuleArguments;

import java.util.List;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * The checksum rules of LIVR 2.5 (3.9). The string form is checked as it is: no spaces, dashes
 * or lowercase letters are removed. A boolean gives the error code of the rule, an object or a
 * list gives FORMAT_ERROR
 */
public class Checksums {
    private static final Pattern LUHN = Pattern.compile("[0-9]{2,}");
    private static final Pattern CREDIT_CARD = Pattern.compile("[0-9]{12,19}");
    private static final Pattern IBAN = Pattern.compile("[A-Z]{2}[0-9]{2}[A-Z0-9]{11,30}");

    /**
     * At least 2 ASCII digits with a Luhn mod 10 checksum of 0
     */
    public static Function<List<Object>, Function> luhn = objects -> {
        RuleArguments.none(objects, "luhn");
        return Formats.format("WRONG_CHECKSUM", value -> LUHN.matcher(value).matches() && isLuhn(value));
    };

    /**
     * 12-19 ASCII digits (ISO/IEC 7812) and the Luhn checksum; no card network detection
     */
    public static Function<List<Object>, Function> credit_card = objects -> {
        RuleArguments.none(objects, "credit_card");
        return Formats.format("WRONG_CREDIT_CARD_NUMBER", value -> CREDIT_CARD.matcher(value).matches() && isLuhn(value));
    };

    /**
     * ISO 13616: a country code, check digits and 11-30 letters or digits, mod 97 is 1. No
     * per-country length table
     */
    public static Function<List<Object>, Function> iban = objects -> {
        RuleArguments.none(objects, "iban");
        return Formats.format("WRONG_IBAN", value -> IBAN.matcher(value).matches() && isIban(value));
    };

    private static boolean isLuhn(String digits) {
        int sum = 0;
        for (int i = 0; i < digits.length(); i++) {
            int digit = digits.charAt(digits.length() - 1 - i) - '0';
            if (i % 2 == 1) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            sum += digit;
        }
        return sum % 10 == 0;
    }

    /**
     * The first four characters moved to the end, every letter replaced by two digits
     * (A = 10 ... Z = 35), the number modulo 97 is 1
     */
    private static boolean isIban(String iban) {
        String rearranged = iban.substring(4) + iban.substring(0, 4);
        int remainder = 0;
        for (int i = 0; i < rearranged.length(); i++) {
            char c = rearranged.charAt(i);
            remainder = c >= 'A' ? (remainder * 100 + (c - 'A' + 10)) % 97 : (remainder * 10 + (c - '0')) % 97;
        }
        return remainder == 1;
    }
}
