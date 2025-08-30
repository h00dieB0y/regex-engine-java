/**
 * Base class for group quantifier patterns like (pattern)+, (pattern)?, etc.
 */
public abstract class GroupQuantifierPattern extends QuantifierPattern {
    
    protected GroupQuantifierPattern(PatternMatcher element) {
        super(element);
    }
    
    /**
     * Tries matching remaining pattern after consuming groups.
     */
    protected boolean tryMatchRemaining(String input, int position, PatternMatcher remaining) {
        if (remaining == null) {
            return true;
        }
        return remaining.matchesAt(input, position);
    }
}