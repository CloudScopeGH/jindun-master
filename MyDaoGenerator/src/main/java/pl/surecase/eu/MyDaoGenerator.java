package pl.surecase.eu;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class MyDaoGenerator {

    public static void main(String args[]) throws Exception {
        // 1: 数据库版本号
        // com.xxx.bean:自动生成的Bean对象会放到/java-gen/com/xxx/bean中
        Schema schema = new Schema(1, "com.cloudspace.jindun.dao");

        addMessage(schema);

        new DaoGenerator().generateAll(schema, "./app/src/main/java-gen");
    }

    private static void addMessage(Schema schema) {

        Entity noticeMessage = schema.addEntity("NoticeMessage");   // 表名
        noticeMessage.addIdProperty();  // 主键，索引;

        noticeMessage.addStringProperty("userid");
        noticeMessage.addStringProperty("bigtypeid");       //主要用于消息以及页面分类显示 各类消息  0,通知；1,评论提醒；2,打招呼；4,礼物；5.合唱邀请；6,游戏；7,群组；10,公众帐号
        noticeMessage.addStringProperty("typeid");
        noticeMessage.addStringProperty("title");
        noticeMessage.addStringProperty("icon");            //目标用户头像
        noticeMessage.addStringProperty("content");
        noticeMessage.addStringProperty("clickurl");
        noticeMessage.addStringProperty("extra");
        noticeMessage.addStringProperty("addtime");
        noticeMessage.addStringProperty("image");
        noticeMessage.addStringProperty("noticeid");        //只有系统消息的时候，此ID有效，其余的通知消息都是同一个noticeid(无效)
        noticeMessage.addStringProperty("actionuserid");    //目标用户id
        noticeMessage.addStringProperty("nickname");    //将去除，客户端没有使用
        noticeMessage.addIntProperty("readstatus");     //读取状态
    }
}
