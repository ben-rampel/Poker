package other_stuff;

import java.util.LinkedList;
import java.util.List;

public class SetOperations {
    public static <T> List<List<T>> powerSet(T[] set) {
        String[] mask = new String[((int) Math.pow(2, set.length))];

        for (int i = 0; i < mask.length; i++) {
            String binary = Integer.toBinaryString(i);
            while (binary.length() < set.length) {
                binary = "0".concat(binary);
            }
            mask[i] = binary;
        }

        List<List<T>> powerSet = new LinkedList<>();

        for (String s : mask) {
            List<T> subset = new LinkedList<>();
            for (int j = 0; j < set.length; j++) {
                if (s.toCharArray()[j] == '1') {
                    subset.add(set[j]);
                }
            }
            powerSet.add(subset);
        }
        return powerSet;
    }
}
