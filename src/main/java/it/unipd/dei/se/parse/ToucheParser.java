package it.unipd.dei.se.parse;

import org.apache.lucene.benchmark.quality.QualityQuery;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a specific {@code TopicParser} that parses topics encoded in XML files.
 */
public class ToucheParser extends TopicParser {

    public static final XMLInputFactory xmlFactory = XMLInputFactory.newInstance();
    private final XMLEventReader in;
    private QualityQuery parsedTopic;
    private boolean topicPending;

    public ToucheParser(Reader in) throws XMLStreamException {
        super(in);
        this.in = xmlFactory.createXMLEventReader(in);
    }

    @Override
    protected QualityQuery parse() {
        if (topicPending) {
            topicPending = false;
            return parsedTopic;
        }
        try {
            Map<String, String> fields = new HashMap<>();
            String number = "";

            while (in.hasNext()) {
                XMLEvent event = in.nextEvent();
                if (event.isStartElement()) {
                    StartElement element = event.asStartElement();
                    switch (element.getName().getLocalPart()) {
                        case ToucheFields.NUMBER:
                            number = in.getElementText();
                            break;
                        case ToucheFields.TITLE:
                            fields.put(ToucheFields.TITLE, in.getElementText());
                            break;
                    }
                }
                if (event.isEndElement()) {
                    EndElement element = event.asEndElement();
                    if (element.getName().getLocalPart().equals(ToucheFields.TOPIC)) {
                        parsedTopic = new QualityQuery(number, fields);
                        return parsedTopic;
                    }
                }
            }

            return null;
        } catch (XMLStreamException e) {
            throw new IllegalStateException("Unable to parse the document.", e);
        }
    }

    @Override
    public boolean hasNext() {
        if (topicPending) return true;
        if (parse() == null) return false;
        topicPending = true;
        return true;
    }
}
