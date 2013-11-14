package ru.ifmo.mobdev.shalamov.Rss;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Created with IntelliJ IDEA.
 * User: slavian
 * Date: 14.10.13
 * Time: 0:04
 * To change this template use File | Settings | File Templates.
 */
public class FeedItem {
    private String link;
    private String title;
    private String date;
    private String description;
    private int rank;

//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//
//        dest.writeInt(rank);
//        dest.writeString(description);
//        dest.writeString(date);
//        dest.writeString(title);
//        dest.writeString(link);
//    }

//    public FeedItem(Parcel source) {
//
//        rank = source.readInt();
//        description = source.readString();
//        date = source.readString();
//        title = source.readString();
//        link = source.readString();
//    }
//
//    @Override
//    public int describeContents() {
//        return 0;
//    }
//

    public String getLink() {
        return link;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getDesc() {
        return description;
    }

    public int getRank() {
        return rank;
    }

    public void setLink(String s) {
        link = s;
    }

    public void setTitle(String s) {
        title = s;
    }

    public void setDate(String s) {
        date = s;
    }


    public void setDesc(String s) {
        description = s;
    }

    public void setRank(int r) {
        rank = r;
    }

    FeedItem(String _link, String _title, String _date, String _description, int _rank) {
        link = _link;
        title = _title;
        date = _date;
        description = _description;
        rank = _rank;
    }

    FeedItem() {
        link = "";
        title = "no title";
        date = "no date";
        description = "life is pain";
        rank = 0;
    }
}
