/**
 * Base class for all pattern elements.
 */
public abstract class PatternElement implements PatternMatcher {
    
    /**
     * Matches this element against a single character.
     */
    public abstract boolean matchesCharacter(char c);
    
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
        return matchLength(input, position) > 0;
    }
    
    @Override
    public int matchLength(String input, int position) {
        if (position >= input.length()) {
            return -1;
        }
        
        return matchesCharacter(input.charAt(position)) ? 1 : -1;
    }
}