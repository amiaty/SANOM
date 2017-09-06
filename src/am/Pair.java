/*
 * Copyright (c) 2017.
 *    * Unauthorized copying of this file  (Pair.java), via any medium is strictly prohibited
 *    * Proprietary and confidential
 *    * Written by :
 * 		Amir Ahooye Atashin - FUM <amir.atashin@mail.um.ac.ir>
 * 		Majeed Mohammadi - TBM <M.Mohammadi@tudelft.nl>
 *
 * 																						Last modified: 2017 - 9 - 6
 *
 */

package am;

public class Pair<L,R> {
    private L l;
    private R r;
    public Pair(L l, R r){
        this.l = l;
        this.r = r;
    }
    public L getL(){ return l; }
    public R getR(){ return r; }
    public void setL(L l){ this.l = l; }
    public void setR(R r){ this.r = r; }
}