package server;

import com.thoughtworks.xstream.XStream;
import server.controller.Server;
import server.model.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        Server server = Server.getInstance();
        new Thread(server).start();
        /*XmlConfiguration configuration = XmlConfiguration.getInstance();

        String answer = configuration.configuration("<command type=\"registration\" login=\"log1\" password=\"pass1\" name=\"n1\" />");

        System.out.println(answer);
        answer = configuration.configuration("<command type=\"addMessage\" sender=\"log\" chat_id=\"0\" text=\"text10\" />");
        System.out.println(answer);
        answer = configuration.configuration("<command type=\"get_chat_users\" chat_id=\"0\" />");
        System.out.println(answer);
        ModelImpl.getInstance().save();*/

        /*XStream xstream = new XStream();
        List<Message> messageList = new ArrayList<>();
        String buf = XmlConfiguration.getInstance().configuration("<command type=\"addMessage\" sender=\"my_nick\" chat_id = \"0\" text =\"sdfgdsf\"/>");
        System.out.println(buf);

        //    messageList.add(new Message(user, "1"));
      //  messageList.add(new Message(user, "2"));
        System.out.println(xstream.toXML(messageList));
        System.out.println(xstream.fromXML(xstream.toXML(messageList)));
        /*JAXBContext jaxbContext = JAXBContext.newInstance(User.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter sw = new StringWriter();

        jaxbMarshaller.marshal(user, sw);
        String xmlString = sw.toString();
        System.out.println(xmlString);*/
    }
}
