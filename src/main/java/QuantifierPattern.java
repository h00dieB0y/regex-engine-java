/**
 * Base class for quantified patterns like +, ?, *.
 */
public abstract class QuantifierPattern implements PatternMatcher {
    protected final PatternMatcher element;
    
    protected QuantifierPattern(PatternMatcher element) {
        this.element = element;
    }
    
    @Override
    public boolean matches(String input) {
        for (int i = 0; i <= input.length(); i++) {
            if (matchesAt(input, i)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean matchesAt(String input, int position) {
        return matchLength(input, position) >= 0;
    }
}