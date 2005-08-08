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
 
Copyright 2003 by ==Quiet==
 */
package net.sf.borg.common.util;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
/* XTree is a poor mans DOM tree. It parses XML into a tree that can be manipulated
 * and written back to text form.
 *
 * XTree is used when simple XML processing is needed and the goal is NOT to
 * have to include an overblown 3rd party parser to do simple stuff
 *
 * ****** XTree is not a full XML parser. It only supports elements with no children
 *  and elements with children but no data. CDATA, attributes, PI, comments and prologs
    are not supported
 */

// each XTree object is an XML element in the XML tree
public class XTree
{
    
    static
    {
        Version.addVersion("$Id$");
    }
    
    // the element's name and optional value
    private String name_, value_;
    
    // pointers to other nodes in the tree
    private XTree firstChild_, sibling_, parent_, lastChild_;
    
    // create a new element as a child of this one with no name or value
    XTree newChild()
    { return( appendChild( "", "" ));}
    
    // return the element's name
    public String name()
    { return( name_ ); }
    
    // return the element's value
    public String value()
    { return( value_ );}
    
    // when stringing certain function calls that traverse a path of the tree
    // it would not be good to have one call return a null. Otherwise, this
    // would crash - root.child("A").child("B").child("C")
    // therefore, there is the concept of a null node. This null node will
    // be returned whenever a child requested by name does not exist.
    // the exists() method will indicate that this node does not exist - this
    // is the only know for which exists is false.
    // chained calls such as the A/B/C one above will wind up returning the null
    // node as final result if A,B, or C does not exist
    public boolean exists()
    { return( this != null_one );  }
    
    // the actual null node
    private static final XTree null_one = new XTree();
    
    // initialize a node with name=ROOT
    public XTree()
    {
        name_ =  "ROOT";
        sibling_ = null;
        parent_ = null;
        firstChild_ = null;
        lastChild_ = null;
        value_ = "";
    }
    
    // get the nth parent (ancestor), starting at n=1 for the direct parent
    public XTree parent(int n)
    {
        XTree ret;
        for( ret = parent_; ret != null && n > 1;
        ret = ret.parent_, n--);
        return( ret );
    }
    
    // get the nth child with the first being n=1. This
    // method WILL return null if no child(n) exists
    public XTree child(int n)
    {
        XTree ret;
		for (ret = firstChild_; ret != null && n > 1; ret = ret.sibling_, n--)
			;
		return (ret);
    }
    
    // get element name
    public XTree name(String newt)
    {
        name_ = newt;
        return( this );
    }
    
    // get element value
    public XTree value(String newt)
    {
        value_ = newt;
        return( this );
    }
    
    public XTree valueUnEscape(String s)
    {
        if( s.indexOf('&') != -1 )
        {
            s = s.replaceAll("&amp;", "&" );
            s = s.replaceAll("&lt;", "<" );
            s = s.replaceAll("&gt;", ">" );
        }           
        value_ = s; 
        return(this);
    }
    
    // remove an element and all children (descendants) under it
    public XTree remove()
    {
        XTree par = parent_;
        if( parent_ != null)
        {
            
            // remove the node from any lists
            if( par.firstChild_ == this )
            {
                par.firstChild_ = sibling_;
                if( par.lastChild_ == this )
                {
                    par.lastChild_ = null;
                }
            }
            else
            {
                XTree c;
                for( c = par.firstChild_; c != null ; c = c.sibling_)
                {
                    if( c.sibling_ == this )
                    {
                        c.sibling_ = sibling_;
                        if( par.lastChild_ == this )
                        {
                            par.lastChild_ = c;
                        }
                        break;
                    }
                }
            }
            
            return( par );
        }
        
        return( null );
    }
    
    // determine the index of this element among its parents children (its siblings)
    public int index()
    {
        int i;
        XTree t;
        if( parent_ == null )
            return(0);
        
        for( i = 0, t = parent_.firstChild_;  t != this; t = t.sibling_, i++ );
        
        return(i);
    }
    
    // return the number of children of an element
    public int numChildren()
    {
        int i;
        XTree t;
        for( i = 0, t = firstChild_; t != null; i++, t = t.sibling_ );
        return(i);
    }
    
    // add a child element to the current node with name=t
    public XTree appendChild(String t)
    {
        if( t == null )
            return null;
        XTree l = lastChild_;
        XTree n = new XTree();
        if( !t.equals("") ) n.name( t );
        
        n.parent_ = this;
        if( l != null )
            l.sibling_ = n;
        else
            firstChild_ = n;
        
        lastChild_ = n;
        
        return( n );
    }
    
    // add a child to the current element with name=t and value=v
    public XTree appendChild(String t, String v)
    {
        if( t == null || v == null || v.equals("") )
            return null;
        XTree l = lastChild_;
        XTree n = new XTree();
        if( !t.equals("") ) n.name( t );
        n.value( v );
        n.parent_ = this;
        if( l != null )
            l.sibling_ = n;
        else
            firstChild_ = n;
        
        lastChild_ = n;
        
        return( n );
    }
    
    // get the value of an element
    // if the esc flag is non-zero, then
    // perform XML escaping on the data (for writing out as XML text)
    public String value(boolean esc)
    {
        
        if( esc )
        {
            String ret = value_;
            ret = ret.replaceAll("&", "&amp;" );
            ret = ret.replaceAll(">", "&gt;" );
            ret = ret.replaceAll("<", "&lt;" );
            return(ret);
        }
        
        return( value_ );
    }
    
    // read XML from a file or System.in if filename=""
    // Only the first XML document in the file is read
    public static XTree readFromFile(String filename) throws Exception
    {
        InputStream fp;
        if( !filename.equals("") )
        {
            fp = new FileInputStream( filename );
        }
        else
        {
            fp =  System.in;
        }
        
        XTree tree =  parse_xml( new InputStreamReader(fp, "UTF-8"));
        return(tree);
    }
    
	// read XML from a stream
	public static XTree readFromStream(InputStream istr) throws Exception
	{
        
		XTree tree =  parse_xml( new InputStreamReader(istr, "UTF-8"));
		return(tree);
	}
    
    // read XML from a URL
    public static XTree readFromURL(URL url) throws Exception
    {
        return readFromStream(url.openStream());
    }
    
    // convert the XML tree starting at the current node
    // to a String
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        toString(0, buf, this);
        return(buf.toString());
    }
    
    // recursive part of toString()
    // XML output is indented to be pretty.
    // level keeps track of the indent level
    private void toString(int level, StringBuffer buf, XTree root)
    {
        
        XTree cur;
        
        for( cur = this; cur != null; cur = cur.sibling_ )
        {
            
            // empty simple node
            if( cur.firstChild_ == null && cur.value_.equals("") )
            {
                buf.append( indent(level) + "<" + cur.name() + "/>\n");
                continue;
            }
            
            buf.append(indent(level) + "<" + cur.name() + ">");
            buf.append( cur.value(true));
            
            if( cur.firstChild_ != null )
            {
             buf.append("\n");
                cur.firstChild_.toString(level+1, buf, root );
                buf.append(indent(level) + "</" + cur.name() + ">\n");
            }
            else
            {
             buf.append("</" + cur.name() + ">\n");
            }
            
            if( root == cur)  break;
        }
        return;
    }
    
    
    // indent XML output 4 spaces per nesting level
    static String indent( int lv )
    {
        int sp = lv * 4;
        StringBuffer s = new StringBuffer(10);
        for(int i = 0; i < sp; i++ )
            s.append(" ");
        return(s.toString());
    }
    
    // read XML from a String
    public static XTree readFromBuffer( String buf ) throws Exception
    {
        return( parse_xml( new StringReader(buf) ));
    }
    
    // return the first child named tg
    // will return the null node if not found - see exists()
    public XTree child( String tg )
    {
        return (child( tg, 1 ));
    }
    
    // return the nth child named tg
    // will return the null node if not found - see exists()
    public XTree child( String tg, int n )
    {
        XTree ret;
        for( ret = firstChild_; ret != null ; ret = ret.sibling_)
        {
            if( tg.equals(ret.name_) )
            {
                if( n == 1 )
                {
                    return( ret );
                }
                n--;
            }
        }
        return(null_one);
        
    }
    
    // delete all children of an element
    public XTree deleteChildren()
    {
        XTree c;
        for( c = firstChild_; c != null ; c = c.sibling_)
        {
            c.remove();
        }
        return(this);
    }
    
    //
    // THE PARSER
    ///
    
    // XML tokens read by the tokenizer
    static final private int T_OPEN = 1;      // <xxx>
    static final private int T_CLOSE = 2;     // </xxx>
    static final private int T_EMPTY = 3;     // <xxx/>
    static final private int T_STRING = 4;    // other string data
    static final private int T_EOF = 5;       // end of input
    
    // the tokenizer (get_token) reads one token at a time from the input
    // and returns it to the main parser state machine
    private static Token get_token( Reader r, StringBuffer buf, boolean open_found )
     throws Exception
    {
        
        buf.setLength(0);
        boolean all_white = true;   // hit only white space for this token
        
        while( true )
        {
            
            int c;
            try
            {
                // read a character
                c = r.read();
            }
            catch( Exception e )
            { throw e; }
            
            // end of data
            if( c == -1 )
             return new Token(T_EOF, open_found);
            
            // not sure why this here - ignore 0 bytes???
            // must have found a problem with 0 bytes in files
            // probably was windows related
            // was copied from C++ version
            if( c == 0 ) continue;
            
            char ch = (char )c;
            if( ch == '>' )
            {
                if( !open_found )
                {
                    throw new Exception("Unmatched close bracket" );
                }
                
                if( buf.length() == 0 )
                {
                    throw new Exception( "Empty element name found" );
                }
                
                open_found = false;
                
                // check if we are closing an empty element
                if( buf.charAt( buf.length() - 1 ) == '/' )
                {
                    // yes, empty element
                    // delete the trailing '/' to return just the
                    // element name
                    buf.deleteCharAt( buf.length() - 1 );
                 return new Token(T_EMPTY, open_found);
                }
                
                // if the beggining of the data was '/', then
                // we have just found the end of a closing element
                // get rid of the '/' to return just the element name
                if( buf.charAt(0) == '/' )
                {
                    buf.deleteCharAt(0);
                 return new Token(T_CLOSE, open_found);
                }
                
                // guess we are just closing an opening element
             return new Token(T_OPEN, open_found);
            }
            else if( ch == '<' )
            {
                if( open_found )
                {
                    throw new Exception( "Illegal Open Bracket" );
                }
                
                open_found = true;
                if( all_white )
                {
                    // string buffer is empty or holding
                    // only whitespace - so ignore
                    // this is the whitespace between
                    // elements
                    buf.setLength(0);
                }
                else
                {
                    // we are holding string data
                    // we cannot start a new token now
                    // so return data. this is element value data
                 return new Token(T_STRING, open_found);
                }
            }
            else
            {
                // add the char to the buffer and update
                // the whitespace flag if needed
                buf.append(ch);
                if( !Character.isWhitespace(ch) )
                {
                    all_white = false;
                }
            }
        }
    }
    
    // THIS IS NOT A FULL XML PARSER!!!
    private static XTree parse_xml( Reader r ) throws Exception
    {
        XTree tree = null;
        String data = "";
        XTree cur = null;
        
        StringBuffer buf = new StringBuffer();

        // this flag is used by the tokenizer to remember that
        // it found an open bracket that ended processing of the
        // last token. I would have liked to rewind the input
        // to put the bracket back - but this was not supported
        // for the input types I support in Java
        // so the tokenizer has this one ugly spot that would
        // have been a call to rewind or unget in C++
        boolean open_found = false;
        
        while( true )
        {
            // get next token
         Token nextToken = get_token(r, buf, open_found);
            int tok = nextToken.tokenType;
            open_found = nextToken.open_found;
            data = buf.toString();
            //System.out.println( tok + " " + data);
            
            // add a new element because we found the opening of one
            if( tok == T_OPEN )
            {
                // the tree does not exist yet - we are adding the root
                if( tree == null )
                {
                    tree = new XTree();
                    tree.name( data );
                    cur = tree;
                }
                else
                {
                    // add the element and move the current pointer to it
                    cur = cur.appendChild( data );
                }
                
            }
            // we found a closing element tag
            else if( tok == T_CLOSE )
            {
                if( tree == null )
                {
                    throw new Exception( "Unexpected element close" );
                }
                
                // make sure the closing tag name matches the name of the current node
                // that we are closing - otherwise error
                if( !data.equals( cur.name() ) )
                {
                    throw new Exception( "Open name [" + cur.name() + "] does not match close name [" +
                    data + "]" );
                }
                
                // closing an element just means that we move our pointer to the parent
                // of the current element
                cur = cur.parent(1);
                
                // when we find the close of the root - exit
                if( cur == null )
                {
                    // closed root - bye
                    return( tree );
                }
            }
            // found an empty element
            else if( tok == T_EMPTY )
            {
                // degenerate case - if the tree is empty, the whole tree is going
                // to be 1 empty element
                if( tree == null )
                {
                    tree = new XTree();
                    tree.name( data );
                    cur = tree;
                    return( tree );
                }
                
                // add the empty element under the current element but don't
                // move the current pointer - the empty element will have no children
                cur.appendChild( data );
                
                
            }
            // got element data - set the current element's value
            else if( tok == T_STRING )
            {
                if( tree == null )
                {
                    throw new Exception( "Illegal non-whitespace before XML start" );
                }
                
                // remove escapes
                
                cur.valueUnEscape(data);
            }
            // end of file - probably an error
            // we should leave the parse before hitting this
            else if( tok == T_EOF )
            {
                if( cur != tree || tree == null )
                {
                    throw new Exception( "Premature end of input" );
                }
                
                return( tree );
            }
        }
    }
    
    // move the children of one element to another
    public void adopt( XTree donor )
    {
        XTree c;
        
        if( donor.firstChild_ == null )
        {
            return;
        }
        
        for( c = donor.firstChild_; c != null ; c = c.sibling_)
        {
            c.parent_ = this;
        }
        
        if( lastChild_ != null )
            lastChild_.sibling_ = donor.firstChild_;
        if( firstChild_ == null )
            firstChild_ = donor.firstChild_;
        donor.firstChild_ = null;
        lastChild_ = donor.lastChild_;
        donor.lastChild_ = null;
    }
    
    public static void main( String argv[] ) throws Exception
    {
        XTree tvo = XTree.readFromFile("");
        String s = tvo.toString();
        System.out.println(s);
    }
    
    ///////////////////////////////////////////////////////////
    // nested class Token
    
    private static class Token
 {
     int tokenType;
     boolean open_found;
     
     Token(int tokenType, boolean open_found)
   {
       this.tokenType = tokenType;
       this.open_found = open_found;
   }
 }
    
    // end nested class Token
    ///////////////////////////////////////////////////////////
}
