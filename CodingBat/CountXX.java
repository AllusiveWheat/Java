package CodingBat;

public class CountXX {
    int countXX(String str) {
        int count = 0;
        for (int i = 0; i < str.length() - 1; i++) {
            if (str.substring(i, i + 2).equals("xx")) {
                count++;
            }
        }
        return count;
    }

    boolean doubleX(String str) {
        // doubleX("axxbb") → true

    }
}
