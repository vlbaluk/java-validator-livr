package livr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A reference to another field of "required_if" and "when" (LIVR 2.5, 2.5): "name" is a field of
 * the containing object, "a/b/0" is a path down from it, "~1" stands for "/" and "~0" for "~" as
 * in JSON Pointer (RFC 6901).
 */
public class Reference {
    private static final Pattern INDEX = Pattern.compile("0|[1-9][0-9]*");
    private static final Pattern WRONG_ESCAPE = Pattern.compile("~(?![01])");

    private final List<String> path;

    /**
     * Not a string, an empty reference, a leading or a trailing "/", an empty segment ("a//b") and
     * a wrong escape ("~2", a trailing "~") are schema errors. A leading "/" is reserved for
     * references from the root
     */
    public Reference(Object reference, String ruleName) {
        if (!(reference instanceof String) || ((String) reference).isEmpty()) {
            throw RuleArguments.schemaError("a reference of \"" + ruleName + "\" must be a non-empty string");
        }
        String string = (String) reference;

        // The common case: a field name without escapes
        if (string.indexOf('/') < 0 && string.indexOf('~') < 0) {
            this.path = Collections.singletonList(string);
            return;
        }

        List<String> path = new ArrayList<>();
        for (String segment : string.split("/", -1)) {
            if (segment.isEmpty()) {
                throw RuleArguments.schemaError("the reference \"" + string + "\" of \"" + ruleName + "\" has an empty segment");
            }
            if (WRONG_ESCAPE.matcher(segment).find()) {
                throw RuleArguments.schemaError("the reference \"" + string + "\" of \"" + ruleName + "\" has a wrong escape");
            }
            path.add(segment.replace("~1", "/").replace("~0", "~"));
        }
        this.path = path;
    }

    /**
     * The input value at the path from the containing object, null when a segment is missing or a
     * value on the way is not an object (or a list for an index segment). On a list a segment is
     * an index only in the form "0" or [1-9][0-9]*
     */
    public Object resolve(Map containingObject) {
        Object value = containingObject;

        for (String segment : path) {
            if (value instanceof Map) {
                value = ((Map) value).get(segment);
            } else if (value instanceof List && INDEX.matcher(segment).matches() && segment.length() < 10
                    && Integer.parseInt(segment) < ((List) value).size()) {
                value = ((List) value).get(Integer.parseInt(segment));
            } else {
                return null;
            }
        }

        return value;
    }
}
