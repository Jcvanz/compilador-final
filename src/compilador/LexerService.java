package compilador;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.Element;
import java.util.*;
import java.util.regex.Pattern;

public class LexerService {

    // vocabulário para classificar tokens por lexema
    private static final String[] RESERVED_WORDS = {
        "add","and","begin","bool","count","delete","do","elementOf","else","end",
        "false","float","if","int","list","not","or","print","read","size",
        "string","true","until","As"
    };
    private static final Set<String> RESERVED_SET =
            new HashSet<>(Arrays.asList(RESERVED_WORDS));

    private static final String[] SPECIAL_SYMBOLS = {
        "==","~=","<-","+","-","*","/","<",">","=","(",")",";",","
    };
    private static final Set<String> SPECIAL_SET =
            new HashSet<>(Arrays.asList(SPECIAL_SYMBOLS));

    private static final Pattern INT_RE   = Pattern.compile("\\d+");
    private static final Pattern FLOAT_RE = Pattern.compile("\\d+\\.\\d+");

    // Roda a análise léxica e devolve um resultado pronto para a UI imprimir. 
    public LexResult analyze(String source, JTextArea editorArea) {
        Lexico lexico = new Lexico();
        Sintatico sintatico = new Sintatico();
        Semantico semantico = new Semantico();
        lexico.setInput(new java.io.StringReader(source));
        
        try {
            sintatico.parse(lexico, semantico);
            return LexResult.successMessage("programa compilado com sucesso");
        }
        catch ( LexicalError e ) {
            int line = lineOfOffset(editorArea, e.getPosition());
            String off = offendingSymbol(source, e.getPosition());

            // classificar o tipo de erro léxico a partir da mensagem do GALS
            String raw = e.getMessage();
            if (isInvalidSymbolMsg(raw)) {
                // se for símbolo inválido (forneçe o próprio símbolo quando possível)
                String offending = (off == null || off.isEmpty()) ? null : off;
                return LexResult.error(LexErrorType.INVALID_SYMBOL, line, offending, raw);
            }
            else if (isInvalidIdentifierMsg(raw)) {
                return LexResult.error(LexErrorType.INVALID_IDENTIFIER, line, off, raw);
            }
            else if (isInvalidStringMsg(raw)) {
                return LexResult.error(LexErrorType.INVALID_STRING, line, off, raw);
            }
            else if (isInvalidCommentMsg(raw)) {
                int startLine = commentStartLineForYourGrammar(source, editorArea, e.getPosition());
                return LexResult.error(LexErrorType.INVALID_COMMENT, startLine, null, raw);
            }
            else {
                return LexResult.error(LexErrorType.OTHER, line, off, raw);
            }
        }
        catch ( SyntaticError e ) {
            int line = lineOfOffset(editorArea, e.getPosition());

            String foundRaw = offendingSymbol(source, e.getPosition());
            String foundNormalized;
            if (foundRaw == null) foundNormalized = "?";
            else if (foundRaw.equals("$") || e.getPosition() >= source.length()) foundNormalized = "EOF";
            else if (classifyToken(foundRaw).equals("constante_string")) foundNormalized = "constante_string";
            else foundNormalized = foundRaw;

            // tentar extrair os símbolos esperados da mensagem do parser
            String expected = null;
            String msgRaw = e.getMessage();
            if (msgRaw != null) {
                String lower = msgRaw.toLowerCase();
                int ix = lower.indexOf("expected");
                if (ix < 0) ix = lower.indexOf("esperado");
                if (ix >= 0) {
                    expected = msgRaw.substring(ix + (lower.indexOf("expected")>=0?8:8)).trim();
                    // remove prefix
                    if (expected.startsWith(":")) expected = expected.substring(1).trim();
                } else {
                    // fallback usa a mensagem inteira
                    expected = msgRaw; 
                }
            }
            if (expected == null || expected.isEmpty()) expected = "símbolo esperado";

            String raw = "encontrado " + foundNormalized + " esperado " + expected;
            return LexResult.error(LexErrorType.OTHER, line, foundNormalized, raw);
        }
        catch ( SemanticError e ) {
            int line = lineOfOffset(editorArea, e.getPosition());
            return LexResult.error(LexErrorType.OTHER, line, null, e.getMessage());
        }
        catch (Exception e) {
            return LexResult.error(LexErrorType.OTHER, 0, null, "erro interno: " + e.getMessage());
        }
    }

    // Helpers de classificação/posição
    private static String classifyToken(String lexeme) {
        if (lexeme == null) return "identificador";
        if (RESERVED_SET.contains(lexeme)) return "palavra reservada";
        if (SPECIAL_SET.contains(lexeme))  return "símbolo especial";
        if (lexeme.length() >= 2 && lexeme.charAt(0) == '"' && lexeme.charAt(lexeme.length()-1) == '"')
            return "constante_string";
        if (FLOAT_RE.matcher(lexeme).matches()) return "constante_float";
        if (INT_RE.matcher(lexeme).matches())   return "constante_int";
        return "identificador";
    }

    private static int lineOfOffset(JTextArea area, int offset) {
        Document doc = area.getDocument();
        offset = Math.max(0, Math.min(offset, doc.getLength()));
        Element root = doc.getDefaultRootElement();
        return root.getElementIndex(offset) + 1;
    }

    private static String offendingSymbol(String src, int pos) {
        if (src == null) return "?";
        int n = src.length();
        if (pos >= n) return "EOF";

        int i = Math.max(0, pos);
        // skip leading whitespace/newlines
        while (i < n && Character.isWhitespace(src.charAt(i))) i++;
        if (i >= n) return "EOF";

        char ch = src.charAt(i);

        // string literal
        if (ch == '"') {
            int j = i + 1;
            while (j < n) {
                char c = src.charAt(j);
                if (c == '"') { j++; break; }
                if (c == '\\' && j + 1 < n) j += 2; else j++;
            }
            return src.substring(i, Math.min(j, n));
        }

        // multi-char special symbols first
        if (i + 1 < n) {
            String two = src.substring(i, i + 2);
            if (SPECIAL_SET.contains(two)) return two;
        }

        // single-char special symbol
        String one = String.valueOf(ch);
        if (SPECIAL_SET.contains(one)) return one;

        // number (int or float)
        if (Character.isDigit(ch)) {
            int j = i;
            while (j < n && Character.isDigit(src.charAt(j))) j++;
            if (j < n && src.charAt(j) == '.') {
                j++;
                while (j < n && Character.isDigit(src.charAt(j))) j++;
            }
            return src.substring(i, j);
        }

        // identifier / reserved word
        if (Character.isLetter(ch) || ch == '_') {
            int j = i;
            while (j < n && (Character.isLetterOrDigit(src.charAt(j)) || src.charAt(j) == '_')) j++;
            return src.substring(i, j);
        }

        // fallback: single char
        return one;
    }

    private static int commentStartLineForYourGrammar(String src, JTextArea area, int errorPos) {
        int start = src.lastIndexOf("*-", Math.max(0, errorPos - 1));
        if (start < 0) start = errorPos;
        return lineOfOffset(area, start);
    }

    // mapeamento de mensagens do GALS 
    private static boolean isInvalidSymbolMsg(String m) {
        return m.contains("símbolo") || m.contains("simbolo")
            || m.contains("invalid symbol") || m.contains("caractere") || m.contains("caracter");
    }
    private static boolean isInvalidIdentifierMsg(String m) {
        return m.contains("identificador");
    }
    private static boolean isInvalidStringMsg(String m) {
        return m.contains("string");
    }
    private static boolean isInvalidCommentMsg(String m) {
        return m.contains("coment");
    }

    // tipos de retorno 
    public enum LexErrorType { INVALID_SYMBOL, INVALID_IDENTIFIER, INVALID_STRING, INVALID_COMMENT, OTHER }

    public static final class TokenInfo {
        public final int line;
        public final String clazz;
        public final String lexeme;
        public TokenInfo(int line, String clazz, String lexeme) {
            this.line = line; this.clazz = clazz; this.lexeme = lexeme;
        }
    }

    public static final class LexErrorInfo {
        public final LexErrorType type;
        public final int line;
        public final String offending;
        public final String rawMessage; 
        public LexErrorInfo(LexErrorType type, int line, String offending, String rawMessage) {
            this.type = type; this.line = line; this.offending = offending; this.rawMessage = rawMessage;
        }
    }

    public static final class LexResult {
        public final boolean ok;                 
        public final List<TokenInfo> tokens;     
        public final LexErrorInfo error;         
        public final String message;            

        private LexResult(boolean ok, List<TokenInfo> tokens, LexErrorInfo error, String message) {
            this.ok = ok;
            this.tokens = (tokens == null) ? Collections.emptyList() : Collections.unmodifiableList(tokens);
            this.error = error;
            this.message = message;
        }

        public static LexResult success(List<TokenInfo> tokens) {
            return new LexResult(true, tokens, null, null);
        }

        public static LexResult error(LexErrorType type, int line, String offending, String raw) {
            LexErrorInfo ei = new LexErrorInfo(type, line, offending, raw);
            String msg = buildErrorMessage(ei);
            return new LexResult(false, Collections.emptyList(), ei, msg);
        }

        public static LexResult successMessage(String msg) {
            return new LexResult(true, Collections.emptyList(), null, (msg == null ? "programa compilado com sucesso" : msg));
        }

        public static LexResult errorMessage(String msg) {
            return new LexResult(false, Collections.emptyList(), null, (msg == null ? "programa apresenta erro" : msg));
        }

        private static String buildErrorMessage(LexErrorInfo ei) {
            if (ei == null) return "programa apresenta erro";
            switch (ei.type) {
                case INVALID_SYMBOL:
                    return "programa apresenta erro\nlinha " + ei.line + ": " + (ei.offending == null ? "símbolo inválido" : ei.offending + " símbolo inválido");
                case INVALID_IDENTIFIER:
                    return "programa apresenta erro\nlinha " + ei.line + ": identificador inválido";
                case INVALID_STRING:
                    return "programa apresenta erro\nlinha " + ei.line + ": constante_string inválida";
                case INVALID_COMMENT:
                    return "programa apresenta erro\nlinha " + ei.line + ": comentário inválido ou não finalizado";
                default:
                    return "programa apresenta erro\nlinha " + ei.line + ": " + (ei.rawMessage == null ? "erro léxico/sintático" : ei.rawMessage);
            }
        }
    }
}
