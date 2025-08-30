import java.util.Set;
import java.util.stream.Collectors;

/**
 * Matches character groups like [abc] or [^abc].
 */
public class CharacterGroupPattern extends PatternElement {
    private final Set<Integer> allowedChars;
    private final boolean negated;
    
    public CharacterGroupPattern(String group) {
        this.negated = group.startsWith("^");
        String chars = negated ? group.substring(1) : group;
        this.allowedChars = chars.chars().boxed().collect(Collectors.toSet());
    }
    
    @Override
    public boolean matchesCharacter(char c) {
        boolean inSet = allowedChars.contains((int) c);
        return negated ? !inSet : inSet;
    }
}