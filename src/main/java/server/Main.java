package server;

import com.thoughtworks.xstream.XStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import server.model.FilePath;
import server.model.Message;
import server.model.User;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static XStream xstream = new XStream();

    public static void main(String[] args) {
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Message message = new Message("nameSender", "Text" + i);
            messages.add(message);
        }



        //xstream.alias("message", String.class);
        List<String> list = new ArrayList<>();
        list.add("1");
        list.add("2");

        System.out.println(xstream.toXML(messages));
    }
}
