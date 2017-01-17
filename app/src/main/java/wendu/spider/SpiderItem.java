package wendu.spider;

import java.io.Serializable;

/**
 * Created by du on 16/12/28.
 */

public class SpiderItem implements Serializable {
    public int sid;
    public int icon;
    public String name;
    public String startUrl = "";
    public String debugSrc = "";

    public SpiderItem(int sid, int icon, String name) {
        this.sid = sid;
        this.icon = icon;
        this.name = name;
    }

    public SpiderItem(int sid, int icon, String name, String startUrl, String debugSrc) {
        this.sid = sid;
        this.icon = icon;
        this.name = name;
        this.startUrl = startUrl;
        this.debugSrc = debugSrc;
    }
}
