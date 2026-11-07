package livr.Rules;

import livr.FunctionKeeper;
import livr.LIVRUtils;
import livr.RuleArguments;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The format rules of LIVR 2.5 (3.8): they check the string form of a primitive value and output
 * it. A boolean gives the error code of the rule, an object or a list gives FORMAT_ERROR.
 * Every grammar matches the whole string with ASCII classes
 */
public class Formats {
    private static final String HEX = "[0-9a-fA-F]";
    private static final Pattern UUID = Pattern.compile(HEX + "{8}-" + HEX + "{4}-(" + HEX + ")" + HEX + "{3}-(" + HEX + ")" + HEX + "{3}-" + HEX + "{12}");
    private static final Pattern IPV4 = Pattern.compile("(?:25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])(?:\\.(?:25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])){3}");
    private static final Pattern IPV6_GROUP = Pattern.compile(HEX + "{1,4}");
    private static final Pattern PREFIX_LENGTH = Pattern.compile("0|[1-9][0-9]{0,2}");
    private static final Pattern HOSTNAME_LABEL = Pattern.compile("[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?");
    private static final Pattern DIGITS = Pattern.compile("[0-9]+");
    private static final Pattern BASE64 = Pattern.compile("(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?");
    private static final Pattern BASE64URL_PADDED = Pattern.compile("(?:[A-Za-z0-9_-]{4})*(?:[A-Za-z0-9_-]{2}==|[A-Za-z0-9_-]{3}=)");
    private static final Pattern BASE64URL = Pattern.compile("[A-Za-z0-9_-]*");
    private static final Pattern HEX_STRING = Pattern.compile(HEX + "+");
    private static final Pattern MONGO_ID = Pattern.compile(HEX + "{24}");
    private static final Pattern HEX_COLOR = Pattern.compile("#(?:" + HEX + "{3,4}|" + HEX + "{6}|" + HEX + "{8})");
    private static final Pattern MAC48 = Pattern.compile(HEX + "{2}([:-])" + HEX + "{2}(?:\\1" + HEX + "{2}){4}");
    private static final Pattern MAC64 = Pattern.compile(HEX + "{2}([:-])" + HEX + "{2}(?:\\1" + HEX + "{2}){6}");
    private static final Pattern E164 = Pattern.compile("\\+[1-9][0-9]{6,14}");
    private static final String SEMVER_NUMBER = "(?:0|[1-9][0-9]*)";
    private static final String SEMVER_PRE_ID = "(?:0|[1-9][0-9]*|[0-9]*[A-Za-z-][0-9A-Za-z-]*)";
    private static final Pattern SEMVER = Pattern.compile(SEMVER_NUMBER + "\\." + SEMVER_NUMBER + "\\." + SEMVER_NUMBER
            + "(?:-" + SEMVER_PRE_ID + "(?:\\." + SEMVER_PRE_ID + ")*)?"
            + "(?:\\+[0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*)?");

    private static final Map<String, Integer> DIGEST_LENGTHS = new HashMap<>();

    static {
        DIGEST_LENGTHS.put("md5", 32);
        DIGEST_LENGTHS.put("sha1", 40);
        DIGEST_LENGTHS.put("sha256", 64);
        DIGEST_LENGTHS.put("sha384", 96);
        DIGEST_LENGTHS.put("sha512", 128);
    }

    /**
     * A UUID (RFC 9562): any version 1-8 and the nil and max UUIDs, or {"uuid": ["v4", "v7"]}
     */
    public static Function<List<Object>, Function> uuid = objects -> {
        Set<Character> versions = new HashSet<>();
        for (String version : RuleArguments.get(objects).isEmpty() ? Arrays.<String>asList() : RuleArguments.strings(objects, "uuid")) {
            if (!version.matches("v[1-8]")) throw RuleArguments.schemaError("unknown version \"" + version + "\" of \"uuid\"");
            versions.add(version.charAt(1));
        }

        return format("WRONG_UUID", value -> {
            Matcher matcher = UUID.matcher(value);
            if (!matcher.matches()) return false;
            if (versions.isEmpty() && (value.equals("00000000-0000-0000-0000-000000000000")
                    || value.equalsIgnoreCase("ffffffff-ffff-ffff-ffff-ffffffffffff"))) return true;

            char version = matcher.group(1).charAt(0);
            boolean isVersion = versions.isEmpty() ? version >= '1' && version <= '8' : versions.contains(version);
            return isVersion && "89abAB".indexOf(matcher.group(2).charAt(0)) >= 0;
        });
    };

    /**
     * An IPv4 or an IPv6 address, {"ip": "v4"} or {"ip": "v6"} for one of them
     */
    public static Function<List<Object>, Function> ip = objects -> {
        String version = ipVersion(objects, "ip");
        return format("WRONG_IP", value -> isIp(value, version));
    };

    /**
     * An IP address, "/" and a prefix length; host bits may be set
     */
    public static Function<List<Object>, Function> cidr = objects -> {
        String version = ipVersion(objects, "cidr");
        return format("WRONG_CIDR", value -> {
            int slash = value.lastIndexOf('/');
            if (slash < 0 || !PREFIX_LENGTH.matcher(value.substring(slash + 1)).matches()) return false;

            String address = value.substring(0, slash);
            int prefixLength = Integer.parseInt(value.substring(slash + 1));
            if (isIp(address, "v4")) return !"v6".equals(version) && prefixLength <= 32;
            if (isIp(address, "v6")) return !"v4".equals(version) && prefixLength <= 128;
            return false;
        });
    };

    /**
     * A hostname (RFC 1123): labels of 1-63 characters, at most 253 characters in total; the last
     * label is not all digits
     */
    public static Function<List<Object>, Function> hostname = objects -> {
        RuleArguments.none(objects, "hostname");
        return format("WRONG_HOSTNAME", value -> {
            if (value.length() > 253) return false;
            String[] labels = value.split("\\.", -1);
            for (String label : labels) {
                if (!HOSTNAME_LABEL.matcher(label).matches()) return false;
            }
            return !DIGITS.matcher(labels[labels.length - 1]).matches();
        });
    };

    /**
     * Base64 (RFC 4648) with padding, or {"base64": "url"}: the URL-safe alphabet, padding optional
     */
    public static Function<List<Object>, Function> base64 = objects -> {
        List<Object> args = RuleArguments.get(objects);
        boolean isUrl = !args.isEmpty() && RuleArguments.string(args.get(0), "base64").equals("url");
        if (!args.isEmpty() && !isUrl) throw RuleArguments.schemaError("unknown variant of \"base64\"");

        if (!isUrl) return format("WRONG_BASE64", value -> BASE64.matcher(value).matches());
        return format("WRONG_BASE64", value -> value.indexOf('=') < 0
                ? BASE64URL.matcher(value).matches() && value.length() % 4 != 1
                : BASE64URL_PADDED.matcher(value).matches());
    };

    public static Function<List<Object>, Function> hex = objects -> {
        RuleArguments.none(objects, "hex");
        return format("WRONG_HEX", value -> HEX_STRING.matcher(value).matches());
    };

    /**
     * The hex digest of one of the given algorithms: {"digest": ["sha256", "sha512"]}
     */
    public static Function<List<Object>, Function> digest = objects -> {
        Set<Integer> lengths = new HashSet<>();
        for (String algorithm : RuleArguments.strings(objects, "digest")) {
            if (!DIGEST_LENGTHS.containsKey(algorithm)) throw RuleArguments.schemaError("unknown algorithm \"" + algorithm + "\" of \"digest\"");
            lengths.add(DIGEST_LENGTHS.get(algorithm));
        }
        return format("WRONG_DIGEST", value -> lengths.contains(value.length()) && HEX_STRING.matcher(value).matches());
    };

    public static Function<List<Object>, Function> mongo_id = objects -> {
        RuleArguments.none(objects, "mongo_id");
        return format("WRONG_MONGO_ID", value -> MONGO_ID.matcher(value).matches());
    };

    public static Function<List<Object>, Function> hex_color = objects -> {
        RuleArguments.none(objects, "hex_color");
        return format("WRONG_HEX_COLOR", value -> HEX_COLOR.matcher(value).matches());
    };

    /**
     * A MAC-48 address, or {"mac": 64} for EUI-64: pairs of hex digits separated by ":" or "-"
     */
    public static Function<List<Object>, Function> mac = objects -> {
        List<Object> args = RuleArguments.get(objects);
        BigDecimal bits = args.isEmpty() ? BigDecimal.valueOf(48) : RuleArguments.number(args.get(0));
        boolean is48 = bits != null && bits.compareTo(BigDecimal.valueOf(48)) == 0;
        boolean is64 = bits != null && bits.compareTo(BigDecimal.valueOf(64)) == 0;
        if (!is48 && !is64) throw RuleArguments.schemaError("\"mac\" takes 48 or 64");

        Pattern pattern = is48 ? MAC48 : MAC64;
        return format("WRONG_MAC", value -> pattern.matcher(value).matches());
    };

    public static Function<List<Object>, Function> e164 = objects -> {
        RuleArguments.none(objects, "e164");
        return format("WRONG_E164", value -> E164.matcher(value).matches());
    };

    /**
     * Semantic Versioning 2.0.0, without a "v" prefix
     */
    public static Function<List<Object>, Function> semver = objects -> {
        RuleArguments.none(objects, "semver");
        return format("WRONG_SEMVER", value -> SEMVER.matcher(value).matches());
    };

    /**
     * The structure of a JWS compact serialization: base64url header.payload.signature, the
     * signature may be empty. Nothing is decoded
     */
    public static Function<List<Object>, Function> jwt = objects -> {
        RuleArguments.none(objects, "jwt");
        return format("WRONG_JWT", value -> {
            String[] segments = value.split("\\.", -1);
            if (segments.length != 3 || segments[0].isEmpty() || segments[1].isEmpty()) return false;
            for (String segment : segments) {
                if (!BASE64URL.matcher(segment).matches() || segment.length() % 4 == 1) return false;
            }
            return true;
        });
    };

    /**
     * A format rule: the string form must pass the test; a boolean gives the error code too
     */
    static Function<FunctionKeeper, Object> format(String errorCode, Predicate<String> isValid) {
        return (wrapper) -> {
            Object value = wrapper.getValue();
            if (LIVRUtils.isEmpty(value)) return "";
            if (!LIVRUtils.isPrimitive(value)) return "FORMAT_ERROR";
            if (value instanceof Boolean) return errorCode;

            String string = LIVRUtils.stringForm(value);
            if (!isValid.test(string)) return errorCode;

            wrapper.getFieldResultArr().add(string);
            return "";
        };
    }

    /**
     * none, "v4" or "v6"
     */
    private static String ipVersion(List<Object> objects, String ruleName) {
        List<Object> args = RuleArguments.get(objects);
        if (args.isEmpty()) return null;

        String version = RuleArguments.string(args.get(0), ruleName);
        if (!version.equals("v4") && !version.equals("v6")) throw RuleArguments.schemaError("\"" + ruleName + "\" takes \"v4\" or \"v6\"");
        return version;
    }

    private static boolean isIp(String value, String version) {
        if (!"v6".equals(version) && IPV4.matcher(value).matches()) return true;
        return !"v4".equals(version) && isIpv6(value);
    }

    /**
     * The text forms of RFC 4291, 2.2: eight groups of 1-4 hex digits, one "::" for one or more
     * groups of zeros, an IPv4 address in the last two groups
     */
    private static boolean isIpv6(String value) {
        int gap = value.indexOf("::");
        if (gap >= 0 && value.indexOf("::", gap + 1) >= 0) return false;

        // The groups before and after "::"; an IPv4 address only at the end of the address
        List<String> parts = new ArrayList<>();
        if (gap < 0) {
            parts.addAll(Arrays.asList(value.split(":", -1)));
        } else {
            String head = value.substring(0, gap);
            String tail = value.substring(gap + 2);
            if (!head.isEmpty()) parts.addAll(Arrays.asList(head.split(":", -1)));
            if (!tail.isEmpty()) parts.addAll(Arrays.asList(tail.split(":", -1)));
        }
        boolean mayEndWithIpv4 = gap < 0 || gap + 2 < value.length();

        int groups = 0;
        for (int i = 0; i < parts.size(); i++) {
            if (mayEndWithIpv4 && i == parts.size() - 1 && IPV4.matcher(parts.get(i)).matches()) {
                groups += 2;
            } else if (IPV6_GROUP.matcher(parts.get(i)).matches()) {
                groups++;
            } else {
                return false;
            }
        }

        return gap < 0 ? groups == 8 : groups < 8;
    }
}
