/*
 * Created on Dec 18, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package net.sf.borg.common.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * @author Owner
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class XSLTransform {

    static public String transform( String xml, String xsl ) throws Exception
    {
        URL inurl = (new XSLTransform().getClass()).getResource(xsl);
        if( inurl == null )
            throw new Exception("Transform " + xsl + " not found");
        Source insrc = new StreamSource(inurl.openStream());
        Transformer trans = TransformerFactory.newInstance().newTransformer(insrc);
        StreamResult res = new StreamResult( new StringWriter());
        trans.transform(new StreamSource(new StringReader(xml)), res );
        return( res.getWriter().toString());
    }
    
    static public void main( String args[]) throws Exception
    {
        System.out.println( transform( "<ADDRESSES><Address><KEY>13</KEY><FirstName>Wedding</FirstName><LastName>Anniversary</LastName><Nickname>LietRaša - lietuviško teksto rašyklė</Nickname><Birthday>05/29/88 12:00 AM</Birthday></Address></ADDRESSES>", 
                "/resource/addr.xsl"));
    }
}
