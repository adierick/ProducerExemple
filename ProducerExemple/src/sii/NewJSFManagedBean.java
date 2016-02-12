/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sii;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.servlet.http.HttpServletRequest;



/**
 *
 * @author adierick.ext
 */
@Named(value = "newJSFManagedBean")
@RequestScoped
@ManagedBean
public class NewJSFManagedBean {
    @Resource(mappedName = "myQueue")
    private Queue myQueue;
    @Resource(mappedName = "myQueueConnectionFactory")
    private ConnectionFactory myQueueFactory;
    
    private String message;

    /**
     * Creates a new instance of NewJSFManagedBean
     */
    public NewJSFManagedBean() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    public void send() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            
            HttpServletRequest request = (HttpServletRequest)FacesContext.getCurrentInstance().getExternalContext().getRequest();
            message = request.getParameter("myForm:message");            
            
             sendJMSMessageToMyQueue(message);
             FacesMessage facesMessage = new FacesMessage("Message sent: " + message);
             facesMessage.setSeverity(FacesMessage.SEVERITY_INFO);
             facesContext.addMessage(null, facesMessage);
        } catch (JMSException jmse) {
             FacesMessage facesMessage = new FacesMessage("Message NOT sent: " + message);
             facesMessage.setSeverity(FacesMessage.SEVERITY_ERROR);
            facesContext.addMessage(null, facesMessage);
        }
    }

    private Message createJMSMessageForjmsMyQueue(Session session, Object messageData) throws JMSException {
        // TODO create and populate message to send
        TextMessage tm = session.createTextMessage();
        tm.setText(messageData.toString());
        return tm;
    }

    private void sendJMSMessageToMyQueue(Object messageData) throws JMSException {
        Connection connection = null;
        Session session = null;
        try {
            connection = myQueueFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer messageProducer = session.createProducer(myQueue);
            messageProducer.send(createJMSMessageForjmsMyQueue(session, messageData));
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (JMSException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Cannot close session", e);
                }
            }
            if (connection != null) {
                connection.close();
            }
        }
    }
}
