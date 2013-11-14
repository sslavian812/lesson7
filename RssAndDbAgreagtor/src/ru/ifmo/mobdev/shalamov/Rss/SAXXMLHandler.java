package ru.ifmo.mobdev.shalamov.Rss;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: slavian
 * Date: 24.10.13
 * Time: 17:09
 * To change this template use File | Settings | File Templates.
 */
public class SAXXMLHandler extends DefaultHandler {

    private ArrayList<FeedItem> feed;
    private String tempVal;
    private FeedItem tempFeedItem;
    private boolean inItem = false;

    public static String KEY_LINK = "link";
    public static String KEY_TITLE = "title";
    public static String KEY_DATE = "pubDate";
    public static String KEY_DESC = "description";
    public static String KEY_ITEM = "item";
    public static String KEY_ID = "guid";


    public static String KEY_LINK2 = "link";
    public static String KEY_TITLE2 = "title";
    public static String KEY_DATE2 = "published";
    public static String KEY_DESC2 = "summary";
    public static String KEY_ITEM2 = "entry";
    public static String KEY_ID2 = "id";

    private StringBuffer buffer;

    public SAXXMLHandler() {
        feed = new ArrayList<FeedItem>();
    }

    public ArrayList<FeedItem> getFeed() {
        return feed;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {

        buffer = new StringBuffer();
        tempVal = "";
        if (qName.equalsIgnoreCase(KEY_ITEM) || qName.equalsIgnoreCase(KEY_ITEM2)) {

            tempFeedItem = new FeedItem();
            inItem = true;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {

        String readChars = new String(ch, start, length);
        if (buffer != null) buffer.append(readChars);
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        tempVal = buffer.toString();
        if (qName.equalsIgnoreCase(KEY_ITEM) || qName.equalsIgnoreCase(KEY_ITEM2)) {

            feed.add(tempFeedItem);
            inItem = false;

        } else if ((qName.equalsIgnoreCase(KEY_LINK) || qName.equalsIgnoreCase(KEY_LINK2)) && tempFeedItem != null &&
                tempFeedItem.getLink().equals("") && inItem) {
            tempFeedItem.setLink(tempVal);
        } else if ((qName.equalsIgnoreCase(KEY_ID) || qName.equalsIgnoreCase(KEY_ID2)) && tempFeedItem != null &&
                tempFeedItem.getLink().equals("") && (tempVal.indexOf("http://") >= 0) && inItem) {
            tempFeedItem.setLink(tempVal);
        } else if ((qName.equalsIgnoreCase(KEY_TITLE) || qName.equalsIgnoreCase(KEY_TITLE2)) && inItem) {
            tempFeedItem.setTitle(tempVal);
        } else if ((qName.equalsIgnoreCase(KEY_DESC) || qName.equalsIgnoreCase(KEY_DESC2)) && inItem) {
            String desc = tempVal.trim();
            if (desc.indexOf("<p>") != -1) desc = desc.replaceAll("<p>", "");
            if (desc.indexOf("</p>") != -1) desc = desc.replaceAll("</p>", "");
            if (desc.indexOf("<br>") != -1) desc = desc.replaceAll("<br>", "\n");
            if (desc.indexOf("<br/>") != -1) desc = desc.replaceAll("<br/>", "\n");
            if (desc.indexOf("&amp;") != -1) desc = desc.replaceAll("&amp;", "&");
            if (desc.indexOf("&quot;") != -1) desc = desc.replaceAll("&quot;", "\"");
            if (desc.indexOf("&gt;") != -1) desc = desc.replaceAll("&gt;", ">");
            if (desc.indexOf("&lt;") != -1) desc = desc.replaceAll("&lt;", "<");
            if (desc.indexOf("&apos;") != -1) desc = desc.replaceAll("&apos;", "'");
            tempVal = desc;
            tempFeedItem.setDesc(tempVal);
        } else if ((qName.equalsIgnoreCase(KEY_DATE) || qName.equalsIgnoreCase(KEY_DATE2)) && inItem) {
            tempFeedItem.setDate(tempVal);
        }
    }


}
