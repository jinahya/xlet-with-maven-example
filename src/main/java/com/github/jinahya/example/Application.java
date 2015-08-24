

package com.github.jinahya.example;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Enumeration;
import java.util.Properties;
import javax.tv.xlet.Xlet;
import javax.tv.xlet.XletContext;
import javax.tv.xlet.XletStateChangeException;
import net.sf.microlog.core.Logger;
import net.sf.microlog.core.LoggerFactory;
import net.sf.microlog.core.PropertyConfigurator;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.simple.parser.JSONParser;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


public class Application implements Xlet {


    static {

        PropertyConfigurator.configure("/microlog.properties");
    }


    private static Properties loadProperties(final String name)
        throws IOException {

        final Logger logger = LoggerFactory.getLogger(Application.class);

        logger.debug("loading properties: " + name);

        final Properties properties = new Properties();

        final InputStream stream = Application.class.getResourceAsStream(name);
        if (stream != null) {
            try {
                properties.load(stream);
            } finally {
                stream.close();
            }
        }

        return properties;
    }


    private static void printProperties(final String name) throws IOException {

        final Logger logger = LoggerFactory.getLogger(Application.class);

        logger.info("printing properties: " + name);

        final Properties properties = loadProperties(name);

        for (final Enumeration e = properties.keys();
             e.hasMoreElements();) {
            final Object key = e.nextElement();
            final Object val = properties.get(key);
            logger.info(key + "=" + val);
        }
    }


    public static void main(final String[] args) throws IOException {

        final Logger logger = LoggerFactory.getLogger(Application.class);

        printProperties("/application.properties");
        printProperties("/microlog.properties");
    }


    private static interface ResourceConsumer {


        void consume(InputStream resource) throws Exception;


    }


    private static void consumeFamilXml(final ResourceConsumer consumer)
        throws Exception {

        final Logger logger = LoggerFactory.getLogger(Application.class);

        logger.info("consumeFamilyXml(" + consumer + ")");

        final InputStream resource
            = Application.class.getResourceAsStream("/example/family.xml");
        if (resource == null) {
            logger.error("failed to load /example/family.xml");
            return;
        }

        try {
            consumer.consume(resource);
        } finally {
            resource.close();
        }
    }


    private static void consumeFamilJson(final ResourceConsumer consumer)
        throws Exception {

        final Logger logger = LoggerFactory.getLogger(Application.class);

        logger.info("consumeFamilyXml(" + consumer + ")");

        final InputStream resource
            = Application.class.getResourceAsStream("/example/family.json");
        if (resource == null) {
            logger.error("failed to load /example/family.json");
            return;
        }

        try {
            consumer.consume(resource);
        } finally {
            resource.close();
        }
    }


    /**
     * Creates a new instance.
     */
    public Application() {

        super();

        // empty
    }


    public void initXlet(final XletContext context)
        throws XletStateChangeException {

        logger.debug("initXlet(" + context + ")");

        try {
            printProperties("/application.properties");
        } catch (final IOException ioe) {
            logger.error("failed to print /application.properties", ioe);
        }
        try {
            printProperties("/microlog.properties");
        } catch (final IOException ioe) {
            logger.error("failed to print /microlog.properties", ioe);
        }
    }


    public void startXlet() throws XletStateChangeException {

        logger.debug("startXlet()");

        final XmlPullParserFactory factory;
        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
        } catch (final XmlPullParserException xppe) {
            xppe.printStackTrace(System.err);
            throw new XletStateChangeException(xppe.getMessage());
        }

        // parse xml kdom
        try {
            consumeFamilXml(new ResourceConsumer() {

                public void consume(final InputStream resource)
                    throws Exception {

                    final Reader input
                        = new InputStreamReader(resource, "UTF-8");
                    final XmlPullParser parser = factory.newPullParser();
                    parser.setInput(input);
                    final Document document = new Document();
                    document.parse(parser);
                    final Element family = document.getRootElement();
                    logger.info("family/@lastName: "
                                + family.getAttributeValue(null, "lastName"));
                }

            });
        } catch (final Exception e) {
            e.printStackTrace(System.err);
        }

        // parse xml pull
        try {
            consumeFamilXml(new ResourceConsumer() {

                public void consume(final InputStream resource)
                    throws Exception {

                    final Reader input
                        = new InputStreamReader(resource, "UTF-8");
                    final XmlPullParser parser = factory.newPullParser();
                    parser.setInput(input);
                    parser.nextTag(); // start element
                    parser.require(XmlPullParser.START_TAG,
                                   "http://github.com/jinahya/test", "family");
                    logger.info("family/@lastName: "
                                + parser.getAttributeValue(null, "lastName"));
                }

            });
        } catch (final Exception e) {
            e.printStackTrace(System.err);
        }

        try {
            consumeFamilJson(new ResourceConsumer() {

                public void consume(final InputStream resource)
                    throws Exception {

                    final Reader reader
                        = new InputStreamReader(resource, "UTF-8");
                    final JSONTokener tokener = new JSONTokener(reader);
                    final JSONObject family = new JSONObject(tokener);
                    logger.info("family.lastName: " + family.get("lastName"));
                }

            });
        } catch (final Exception e) {
            e.printStackTrace(System.err);
        }

        try {
            consumeFamilJson(new ResourceConsumer() {

                public void consume(final InputStream resource)
                    throws Exception {

                    final Reader reader
                        = new InputStreamReader(resource, "UTF-8");
                    final JSONParser parser = new JSONParser();
                    final org.json.simple.JSONObject family
                        = (org.json.simple.JSONObject) parser.parse(reader);
                    logger.info("family.lastName: " + family.get("lastName"));
                }

            });
        } catch (final Exception e) {
            e.printStackTrace(System.err);
        }
    }


    public void pauseXlet() {

        logger.debug("pauseXlet()");
    }


    public void destroyXlet(boolean unconditional)
        throws XletStateChangeException {

        logger.debug("destroyXlet(" + unconditional + ")");

        LoggerFactory.shutdown();
    }


    private transient final Logger logger = LoggerFactory.getLogger(getClass());


}

