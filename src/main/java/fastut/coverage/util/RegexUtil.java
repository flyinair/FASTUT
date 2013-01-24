package fastut.coverage.util;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * Abstract, not to be instantiated utility class for Regex functions.
 *
 * @author John Lewis (logic copied from MethodInstrumenter)
 */
public abstract class RegexUtil {

    private static final Logger       logger = Logger.getLogger(RegexUtil.class);

    private final static Perl5Matcher pm     = new Perl5Matcher();

    /**
     * <p>
     * Check to see if one of the regular expressions in a collection match an input string.
     * </p>
     *
     * @param regexs The collection of regular expressions.
     * @param str The string to check for a match.
     * @return True if a match is found.
     */
    public static boolean matches(Collection regexs, String str) {
        Iterator iter = regexs.iterator();
        while (iter.hasNext()) {
            Pattern regex = (Pattern) iter.next();
            if (pm.matches(str, regex)) {
                return true;
            }
        }

        return false;
    }

    public static void addRegex(Collection list, String regex) {
        try {
            Perl5Compiler pc = new Perl5Compiler();
            Pattern pattern = pc.compile(regex);
            list.add(pattern);
        } catch (MalformedPatternException e) {
            logger.warn("The regular expression " + regex + " is invalid: " + e.getLocalizedMessage());
        }
    }

}
