package other_stuff;

import java.util.LinkedList;
import java.util.List;

public class PowerSet {

    public static void main(String[] args){
        int[] set = {1,2,3,4,20};

        for(List<Integer> subset : powerSet(set)){
            for(Integer i : subset){
                System.out.print(i + " ");
            }
            System.out.println();
        }
    }

    public static List<List<Integer>> powerSet(int[] set){
        String[] mask = new String[((int) Math.pow(2,set.length))];

        for(int i = 0; i < mask.length; i++){
            String binary = Integer.toBinaryString(i);
            while(binary.length() < set.length){
                binary = "0".concat(binary);
            }
            mask[i] = binary;
        }

        List<List<Integer>> powerSet = new LinkedList<>();

        for(int i = 0; i < mask.length; i++){
            List<Integer> subset = new LinkedList<>();
            for(int j = 0; j < set.length; j++){
                if(mask[i].toCharArray()[j]  == '1'){
                    subset.add(set[j]);
                }
            }
            powerSet.add(subset);
        }
        return powerSet;
    }
}
