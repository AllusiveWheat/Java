package CodingBat;

public class StringX {
    public String stringX(String str) {
        // Given a string, return a version where all the "x" have been removed. Except an "x" at
        // the very start or end should not be removed.
        // stringX("xxHxix") → "xHix"
        // stringX("abxxxcd") → "abcd"
        // stringX("xabxxxcdx") → "xabcdx"
        String result = "";
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != 'x' || i == 0 || i == str.length() - 1) {
                result += str.charAt(i);
            }
        }
        return result;
    }
}
