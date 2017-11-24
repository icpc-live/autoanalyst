package io;

import java.io.PrintStream;
import java.util.Map;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class MessageXmlSerializer {
	XMLStreamWriter writer;
	String encoding = "UTF-8"; 

	public MessageXmlSerializer(PrintStream output) throws XMLStreamException{
		XMLOutputFactory factory      = XMLOutputFactory.newInstance();
		writer = factory.createXMLStreamWriter(output, encoding);
	}
	
	public void init() throws XMLStreamException {
		writer.writeStartDocument(encoding ,"1.0");
		writer.writeStartElement("contest");
		writer.writeCharacters("\n");
	}
	
	public void close() throws XMLStreamException {
		writer.writeEndElement();
		writer.writeEndDocument();
	}
	
	public void serialize(SimpleMessage msg) throws XMLStreamException {
		writer.writeStartElement(msg.getName());

		Map<String, String> values = msg.getValues();
		
		for (Map.Entry<String, String> entry : values.entrySet()) {
			writer.writeStartElement(entry.getKey());
			writer.writeCharacters(entry.getValue());
			writer.writeEndElement();
		}
		    
		writer.writeEndElement();
		writer.writeCharacters("\n");
		writer.flush();
	}
	
}


 