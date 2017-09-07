/*
 * Copyright (c) 2017.
 *    * Unauthorized copying of this file  (StringUtils.java), via any medium is strictly prohibited
 *    * Proprietary and confidential
 *    * Written by :
 * 		Amir Ahooye Atashin - FUM <amir.atashin@mail.um.ac.ir>
 * 		Majeed Mohammadi - TBM <M.Mohammadi@tudelft.nl>
 *
 * 																						Last modified: 2017 - 9 - 6
 *
 */

package am;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StringUtilsAM {
    private StringUtilsAM() {
    }
    public static final boolean ContrainNumber(String str)
    {
        return str.matches(".*\\d+.*");
    }
    public static final double StringSetSimilarity(String str1, String str2)
    {
        str1 = String.join(" ", str1.split("(?<=\\D)(?=\\d)"));
        str2 = String.join(" ", str2.split("(?<=\\D)(?=\\d)"));

        String[] part1 = str1.split("\\s+");
        String[] part2 = str2.split("\\s+");
        Set<String> intersection  = new HashSet<String>(Arrays.asList(part1));
        intersection .retainAll(Arrays.asList(part2));
        double res = (intersection.size() * 2) / (part1.length + part2.length);
        return res;
    }
}
