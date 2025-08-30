import java.util.ArrayList;
import java.util.List;

/**
 * Factory for creating pattern matchers from regex strings.
 */
public class PatternFactory {
    
    public PatternMatcher createPattern(String regex) {
        if (regex == null || regex.isEmpty()) {
            return new LiteralCharacterPattern('\0'); // Empty pattern
        }
        
        PatternMatcher pattern = parsePattern(regex);
        
        // Handle anchors
        if (regex.startsWith("^") && regex.endsWith("$")) {
            String innerRegex = regex.substring(1, regex.length() - 1);
            PatternMatcher innerPattern = parsePattern(innerRegex);
            return new StartAnchorPattern(new EndAnchorPattern(innerPattern));
        } else if (regex.startsWith("^")) {
            String innerRegex = regex.substring(1);
            PatternMatcher innerPattern = parsePattern(innerRegex);
            return new StartAnchorPattern(innerPattern);
        } else if (regex.endsWith("$")) {
            String innerRegex = regex.substring(0, regex.length() - 1);
            PatternMatcher innerPattern = parsePattern(innerRegex);
            return new EndAnchorPattern(innerPattern);
        }
        
        return pattern;
    }
    
    private PatternMatcher parsePattern(String regex) {
        if (regex.isEmpty()) {
            return new EmptyPattern();
        }
        
        List<PatternMatcher> sequence = new ArrayList<>();
        int i = 0;
        
        while (i < regex.length()) {
            PatternMatcher element = parseElement(regex, i);
            ElementParseResult result = parseElementWithQuantifier(regex, i);
            
            sequence.add(result.pattern);
            i = result.nextPosition;
        }
        
        if (sequence.size() == 1) {
            return sequence.get(0);
        }
        
        return new SequencePattern(sequence);
    }
    
    private ElementParseResult parseElementWithQuantifier(String regex, int position) {
        ElementParseResult baseResult = parseElementBase(regex, position);
        int nextPos = baseResult.nextPosition;
        
        // Check for quantifiers
        if (nextPos < regex.length()) {
            char quantifier = regex.charAt(nextPos);
            switch (quantifier) {
                case '+':
                    return new ElementParseResult(
                        new OneOrMorePattern(baseResult.pattern),
                        nextPos + 1
                    );
                case '?':
                    return new ElementParseResult(
                        new ZeroOrOnePattern(baseResult.pattern),
                        nextPos + 1
                    );
            }
        }
        
        return baseResult;
    }
    
    private ElementParseResult parseElementBase(String regex, int position) {
        char c = regex.charAt(position);
        
        switch (c) {
            case '\\':
                return parseEscapeSequence(regex, position);
            case '.':
                return new ElementParseResult(new AnyCharacterPattern(), position + 1);
            case '[':
                return parseCharacterGroup(regex, position);
            case '(':
                return parseGroup(regex, position);
            default:
                return new ElementParseResult(new LiteralCharacterPattern(c), position + 1);
        }
    }
    
    private ElementParseResult parseEscapeSequence(String regex, int position) {
        if (position + 1 >= regex.length()) {
            return new ElementParseResult(new LiteralCharacterPattern('\\'), position + 1);
        }
        
        char escaped = regex.charAt(position + 1);
        switch (escaped) {
            case 'd':
                return new ElementParseResult(new DigitClassPattern(), position + 2);
            case 'w':
                return new ElementParseResult(new WordClassPattern(), position + 2);
            default:
                return new ElementParseResult(new LiteralCharacterPattern(escaped), position + 2);
        }
    }
    
    private ElementParseResult parseCharacterGroup(String regex, int position) {
        int endPos = regex.indexOf(']', position + 1);
        if (endPos == -1) {
            return new ElementParseResult(new LiteralCharacterPattern('['), position + 1);
        }
        
        String group = regex.substring(position + 1, endPos);
        return new ElementParseResult(new CharacterGroupPattern(group), endPos + 1);
    }
    
    private ElementParseResult parseGroup(String regex, int position) {
        int endPos = findMatchingParen(regex, position);
        if (endPos == -1) {
            return new ElementParseResult(new LiteralCharacterPattern('('), position + 1);
        }
        
        String groupContent = regex.substring(position + 1, endPos);
        
        if (groupContent.contains("|")) {
            return parseAlternation(groupContent, endPos + 1);
        } else {
            PatternMatcher groupPattern = parsePattern(groupContent);
            return new ElementParseResult(groupPattern, endPos + 1);
        }
    }
    
    private ElementParseResult parseAlternation(String content, int nextPosition) {
        String[] alternatives = content.split("\\|");
        List<PatternMatcher> patterns = new ArrayList<>();
        
        for (String alt : alternatives) {
            patterns.add(parsePattern(alt));
        }
        
        return new ElementParseResult(new AlternationPattern(patterns), nextPosition);
    }
    
    private int findMatchingParen(String regex, int openPos) {
        int depth = 0;
        for (int i = openPos; i < regex.length(); i++) {
            if (regex.charAt(i) == '(') {
                depth++;
            } else if (regex.charAt(i) == ')') {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    private PatternMatcher parseElement(String regex, int position) {
        return parseElementWithQuantifier(regex, position).pattern;
    }
    
    private static class ElementParseResult {
        final PatternMatcher pattern;
        final int nextPosition;
        
        ElementParseResult(PatternMatcher pattern, int nextPosition) {
            this.pattern = pattern;
            this.nextPosition = nextPosition;
        }
    }
}