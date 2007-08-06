/* 
This file is part of BORG.

    BORG is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    BORG is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with BORG; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

Copyright 2003 by Mike Berger
*/
package net.sf.borg.common;

import java.util.HashMap;
import java.util.Random;

/*
 * mcrypt.java
 *
 * Created on February 4, 2002, 1:21 PM
 */

/**
 *
 * @author  MBERGER
 * @version
 */
public class Crypt {
    
    private Random r_;
    private HashMap dmap_;
    private char[][] left;
    private char[] right;
    

    /** Creates new mcrypt */
    // mcrypt encrypts strings using a string to numeric code that I learned as
    // a kid and used to do with paper and pencil. 
    // Not unbreakable by a long shot, but will protect data from 
    // casual snooping
    // the interesting thing about the code that makes it non-trivial is
    // how an input letter can encrypt to 3 different numbers at random.
    public Crypt(String key) throws Exception {
        
        left = new char[3][3];
        right = new char[9];
        dmap_ = new HashMap();
        r_ = new Random();
        if( key.length() != 18 )
            throw new Exception( "Invalid Key" );
        
        for( int i = 0; i < 17; i++ ) {
            if( !Character.isDigit(key.charAt(i)) )
                throw new Exception( "Invalid Key" );
            if( key.charAt(i) == '0' )
                throw new Exception( "Invalid Key" );
        }
        
        for( int i = 0; i < 3; i++ ) {
            for( int j = 0; j < 3; j++ ) {
                left[i][j] = key.charAt(i*3+j);
            }
        }
        
        for( int i = 0; i < 9; i++ ) {
            right[i] = key.charAt(i+9);
        }
        
        // build decrypt map
        for( int i = 0; i < 3; i++ ) {
            for( int j = 0; j < 3; j++ ) {
                
                char l = left[i][j];
                
                for( int k = 0; k < 9; k++ ) {
                    String ky = "";
                    ky += l;
                    ky += right[k];
                    int v = i*9+k+10;
                    Character value = new Character( Character.forDigit(v,Character.MAX_RADIX));
                    
                    dmap_.put( ky, value );
                }
            }
        }
    }
    
    public String encrypt( String in ) {
        String result = "";
        
        int aval = Character.digit('a',Character.MAX_RADIX );
        
        for( int i = 0; i < in.length(); i++ ) {
            char c = in.charAt(i);
            if( Character.isDigit(c)) {
                char cn = Character.forDigit( aval + Character.digit(c,Character.MAX_RADIX) ,Character.MAX_RADIX);
                result += cn;
            }
            else if( !Character.isLetter(c) ) {
                result += c;
            }
            else {
                if( Character.isUpperCase(c) ) {
                    result += "U";
                }
                // letter
                int d = Character.digit(c,Character.MAX_RADIX );
                if( d == -1 )
                {
                	result += c;
                	continue;
                }
                d = d - 10;
                //System.out.println(d);
                int ran = r_.nextInt(3);
                //System.out.println(ran);
                result += left[d/9][ran];
                result += right[d%9];
            }
        }
        return( result );
        
    }
    
    public String decrypt( String in ) {
        String result = "";
        int aval = Character.digit('a',Character.MAX_RADIX );
        boolean upper = false;
        for( int i = 0; i < in.length(); i++ ) {
            char c1 = in.charAt(i);
            if( in.charAt(i) == 'U' ) {
                // upper case
                upper = true;
                continue;
            }
            if( Character.isLetter(c1) ) {
                int d = Character.digit(c1,Character.MAX_RADIX);
                if( d == -1 )
                {
                	result += c1;
                	continue;
                }
                d = d - aval;
                result += Character.forDigit(d, Character.MAX_RADIX);
            }
            else if( !Character.isDigit(c1) ) {
                result += c1;
            }
            else {
                i++;
                if( i == in.length() ) {
                    break;
                }
                char c2 = in.charAt(i);
                
                // lookup
                String key = "";
                key += c1;
                key += c2;
                Object o = dmap_.get(key);
                if( o != null ) {
                    Character ch = (Character)o;
                    char c3 = ch.charValue();
                    if( upper ) {
                        upper = false;
                        c3 = Character.toUpperCase(c3);
                    }
                    result += c3;
                }
                
                
                
            }
        }
        
        return( result );
    }
    
    static public void main( String[] args ) {
        try{
            Crypt mc = new Crypt("123456789123456789");
            String in = "abcdefghijklmnopqrstuvwxyz";
            String res = mc.encrypt(in);
            System.out.println(res);
            System.out.println( mc.decrypt(res) );
            in = "Feb 4,2002";
            res = mc.encrypt(in);
            System.out.println(res);
            System.out.println( mc.decrypt(res) );
            in = "œŸæó¹ê³AaBbCcDd";
            res = mc.encrypt(in);
            System.out.println(res);
            System.out.println( mc.decrypt(res) );
            
        }
        catch( Exception e ) {
            System.out.println(e.toString());
        }
        
    }
}

