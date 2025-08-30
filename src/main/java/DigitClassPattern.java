/**
 * Matches any digit character (\d).
 */
public class DigitClassPattern extends PatternElement {
    
    @Override
    public boolean matchesCharacter(char c) {
        return Character.isDigit(c);
    }
}