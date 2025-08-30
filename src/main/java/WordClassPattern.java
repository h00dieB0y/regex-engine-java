/**
 * Matches word characters (\w) - letters, digits, underscore.
 */
public class WordClassPattern extends PatternElement {
    
    @Override
    public boolean matchesCharacter(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }
}