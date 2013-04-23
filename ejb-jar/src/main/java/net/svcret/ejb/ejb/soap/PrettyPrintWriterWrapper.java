package net.svcret.ejb.ejb.soap;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang3.StringUtils;

public class PrettyPrintWriterWrapper implements XMLEventWriter {

	private XMLEventWriter myTarget;
	private int depth = 0;
	private Map<Integer, Boolean> hasChildElement = new HashMap<Integer, Boolean>();
	private XMLEventFactory myXmlEventFactory;

	private static final String INDENT_CHAR = " ";
	private static final String LINEFEED_CHAR = "\n";

	public PrettyPrintWriterWrapper(XMLEventFactory theXmlEventFactory, XMLEventWriter target) {
		myXmlEventFactory = theXmlEventFactory;
		myTarget = target;
	}

	private String repeat(int d, String s) {
		return StringUtils.repeat(s, d * 3);
	}

	@Override
	public void flush() throws XMLStreamException {
		myTarget.flush();
	}

	@Override
	public void close() throws XMLStreamException {
		myTarget.close();
	}

	@Override
	public void add(XMLEvent theEvent) throws XMLStreamException {
        
        if( theEvent.isStartElement())
        {
            // update state of parent node
            if( depth > 0 )
            {
                hasChildElement.put( depth-1, true );
            }
            
            // reset state of current node
            hasChildElement.put( depth, false );
            
            // indent for current depth
            myTarget.add(myXmlEventFactory.createCharacters( LINEFEED_CHAR + repeat( depth, INDENT_CHAR )));
            
            depth++;
            
        } else if( theEvent.isEndElement())
        {
            depth--;
            
            if( hasChildElement.get( depth) == true )
            {
                // indent for current depth
                myTarget.add(myXmlEventFactory.createCharacters( LINEFEED_CHAR + repeat( depth, INDENT_CHAR )));
            }
            
        }
        
//        if( "writeEmptyElement".equals(m ))
//        {
//            // update state of parent node
//            if( depth &gt; 0 )
//            {
//                hasChildElement.put( depth-1, true );
//            }
//            
//            // indent for current depth
//            target.writeCharacters( LINEFEED_CHAR );
//            target.writeCharacters( repeat( depth, INDENT_CHAR ));
//            
//        }
        
        myTarget.add(theEvent);
	}

	@Override
	public void add(XMLEventReader theReader) throws XMLStreamException {
		myTarget.add(theReader);
	}

	@Override
	public String getPrefix(String theUri) throws XMLStreamException {
		return myTarget.getPrefix(theUri);
	}

	@Override
	public void setPrefix(String thePrefix, String theUri) throws XMLStreamException {
		myTarget.setPrefix(thePrefix, theUri);
	}

	@Override
	public void setDefaultNamespace(String theUri) throws XMLStreamException {
		myTarget.setDefaultNamespace(theUri);
	}

	@Override
	public void setNamespaceContext(NamespaceContext theContext) throws XMLStreamException {
		myTarget.setNamespaceContext(theContext);
	}

	@Override
	public NamespaceContext getNamespaceContext() {
		return myTarget.getNamespaceContext();
	}

}
