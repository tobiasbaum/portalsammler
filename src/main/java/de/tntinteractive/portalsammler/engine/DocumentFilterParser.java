package de.tntinteractive.portalsammler.engine;

import java.util.ArrayList;
import java.util.List;

public class DocumentFilterParser {

    private static final class TokenConsumer {
        private final List<String> tokens;
        private int curPos;

        public TokenConsumer(List<String> tokens) {
            this.tokens = tokens;
        }

        public String next() {
            if (this.curPos < this.tokens.size()) {
                return this.tokens.get(this.curPos++);
            } else {
                return null;
            }
        }

        public void setCurPos(int curPos) {
            this.curPos = curPos;
        }

        public int getCurPos() {
            return this.curPos;
        }
    }

    public static DocumentFilter parse(String filter) {
        final List<String> tokens = tokenize(filter);
        if (tokens.isEmpty()) {
            return DocumentFilter.NO_FILTER;
        }

        removeAndTokens(tokens);

        final DocumentFilter f = parse(new TokenConsumer(tokens));
        if (f == null) {
            return DocumentFilter.NO_FILTER;
        } else {
            return f;
        }
    }

    private static void removeAndTokens(List<String> tokens) {
        while (tokens.remove("&")) {
        }
    }

    private static DocumentFilter parse(TokenConsumer tokens) {
        DocumentFilter ret;
        ret = parseOr(tokens);
        if (ret != null) {
            return ret;
        }
        ret = parseAnd(tokens);
        if (ret != null) {
            return ret;
        }
        return null;
    }

    private static DocumentFilter parseOr(TokenConsumer tokens) {
        final int startPos = tokens.getCurPos();
        final DocumentFilter first = parseAnd(tokens);
        if (first == null) {
            return null;
        }
        final String cur = tokens.next();
        if (!"|".equals(cur)) {
            tokens.setCurPos(startPos);
            return null;
        }
        final DocumentFilter rest = parse(tokens);
        if (rest == null) {
            tokens.setCurPos(startPos);
            return null;
        }
        return new OrFilter(first, rest);
    }

    private static DocumentFilter parseAnd(TokenConsumer tokens) {
        int startPos = tokens.getCurPos();
        final DocumentFilter first = parseParen(tokens);
        if (first == null) {
            return null;
        }
        startPos = tokens.getCurPos();
        final DocumentFilter rest = parse(tokens);
        if (rest == null) {
            tokens.setCurPos(startPos);
            return first;
        }
        return new AndFilter(first, rest);
    }

    private static DocumentFilter parseParen(TokenConsumer tokens) {
        final int startPos = tokens.getCurPos();
        final String opening = tokens.next();
        if (!"(".equals(opening)) {
            tokens.setCurPos(startPos);
            return parseContains(tokens);
        }
        final DocumentFilter child = parse(tokens);
        if (child == null) {
            tokens.setCurPos(startPos);
            return null;
        }
        //die schließende Klammer wird nur konsumiert, wenn sie da ist; wenn sie fehlt, ist auch OK
        final int endPos = tokens.getCurPos();
        final String ending = tokens.next();
        if (!")".equals(ending)) {
            tokens.setCurPos(endPos);
        }
        return child;
    }

    private static DocumentFilter parseContains(TokenConsumer tokens) {
        final int startPos = tokens.getCurPos();
        final String s = tokens.next();
        if (s == null || s.length() == 1 && isCommandChar(s.charAt(0))) {
            tokens.setCurPos(startPos);
            return null;
        }
        return new ContainsFilter(s);
    }

    private static enum TokenizerState {
        OUT,
        QUOTED
    }

    static List<String> tokenize(String filter) {
        final List<String> tokens = new ArrayList<String>();
        final StringBuilder curToken = new StringBuilder();
        TokenizerState state = TokenizerState.OUT;
        for (final char ch : filter.toCharArray()) {
            switch (state) {
            case OUT:
                if (Character.isWhitespace(ch)) {
                    addAndResetTokenIfNonEmpty(tokens, curToken);
                } else if (ch == '"') {
                    addAndResetTokenIfNonEmpty(tokens, curToken);
                    state = TokenizerState.QUOTED;
                } else if (isCommandChar(ch)) {
                    addAndResetTokenIfNonEmpty(tokens, curToken);
                    tokens.add(Character.toString(ch));
                } else {
                    curToken.append(ch);
                }
                break;
            case QUOTED:
                if (ch == '"') {
                    addAndResetTokenIfNonEmpty(tokens, curToken);
                    state = TokenizerState.OUT;
                } else {
                    curToken.append(ch);
                }
                break;
            default:
                throw new RuntimeException(state.toString());
            }
        }
        addAndResetTokenIfNonEmpty(tokens, curToken);
        return tokens;
    }

    private static void addAndResetTokenIfNonEmpty(final List<String> tokens, final StringBuilder curToken) {
        if (curToken.length() > 0) {
            tokens.add(curToken.toString());
            curToken.setLength(0);
        }
    }

    private static boolean isCommandChar(char ch) {
        return ch == '&' || ch == '|' || ch == '(' || ch == ')';
    }

}
