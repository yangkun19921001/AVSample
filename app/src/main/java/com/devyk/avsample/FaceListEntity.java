package com.devyk.avsample;

import com.blankj.utilcode.util.GsonUtils;

import java.util.List;

/**
 * Created by yangk on 2019/3/26.
 */

public class FaceListEntity {
    /**
     * MSG_KEY : FACE_REPORT
     * id : 识别请求唯一ID
     * REPORT_TIME : 报告时间[格式2018-10-01 12:00:00]
     * attachment : [{"URL":"/yanshi/upload/2017/09/12/488a2830-a862-402d-a5ad-7312f3463755.jpg"}]
     * List : [{"Desc":"身份证号码:231003195003271638;姓名:孙金;年龄:68;性别:男;身高:175cm;体重:79kg;对比库:市局涉军库;籍贯:231003;匹配度:0.9226182699203492;","attachment":[{"URL":"/yanshi/upload/2017/09/12/488a2830-a862-402d-a5ad-7312f3463755.jpg"}]}]
     */

    private String MSG_KEY;
    private String ID;
    private String REPORT_TIME;
    /**
     * 上传的原始图片
     */
    private List<AttachmentBean> ATTACHMENT;
    /**
     * 返回的结果信息
     */
    private List<ListBean> LIST;

    public String getMSG_KEY() {
        return MSG_KEY;
    }

    public void setMSG_KEY(String MSG_KEY) {
        this.MSG_KEY = MSG_KEY;
    }

    public String getId() {
        return ID;
    }

    public void setId(String id) {
        this.ID = id;
    }

    public String getREPORT_TIME() {
        return REPORT_TIME;
    }

    public void setREPORT_TIME(String REPORT_TIME) {
        this.REPORT_TIME = REPORT_TIME;
    }

    public List<AttachmentBean> getAttachment() {
        return ATTACHMENT;
    }

    public void setAttachment(List<AttachmentBean> attachment) {
        this.ATTACHMENT = attachment;
    }

    public List<ListBean> getList() {
        return LIST;
    }

    public void setList(List<ListBean> List) {
        this.LIST = List;
    }

    public static class AttachmentBean {
        /**
         * URL : /yanshi/upload/2017/09/12/488a2830-a862-402d-a5ad-7312f3463755.jpg
         */

        private String URL;

        public String getURL() {
            return URL;
        }

        public void setURL(String URL) {
            this.URL = URL;
        }
    }

    public static class ListBean {
        /**
         * Desc : 身份证号码:231003195003271638;姓名:孙金;年龄:68;性别:男;身高:175cm;体重:79kg;对比库:市局涉军库;籍贯:231003;匹配度:0.9226182699203492;
         * attachment : [{"URL":"/yanshi/upload/2017/09/12/488a2830-a862-402d-a5ad-7312f3463755.jpg"}]
         */

        private String DESC;
        private List<AttachmentBeanX> ATTACHMENT;

        public String getDesc() {
            return DESC;
        }

        public void setDesc(String Desc) {
            this.DESC = Desc;
        }

        public List<AttachmentBeanX> getAttachment() {
            return ATTACHMENT;
        }

        public void setAttachment(List<AttachmentBeanX> attachment) {
            this.ATTACHMENT = attachment;
        }

        public static class AttachmentBeanX {
            /**
             * URL : /yanshi/upload/2017/09/12/488a2830-a862-402d-a5ad-7312f3463755.jpg
             */

            private String URL;

            public String getURL() {
                return URL;
            }

            public void setURL(String URL) {
                this.URL = URL;
            }
        }
    }


    public static void main(String []srt){

        String json = "{\"attachment\":[{\"URL\":\"\\/rstoneCmd\\/upload\\/2020\\/06\\/05\\/083f3f06-189d-450b-b9d8-7fc786046b59.jpg\"}],\"MSG_KEY\":null,\"RECEIVE_TIME\":\"2020-06-12\",\"id\":\"识别请求唯22 ID\",\"REPORT_TIME\":\"2029-06-03 12:00:00\",\"List\":[{\"attachment\":[{\"URL\":\"\\/rstoneCmd\\/upload\\/2020\\/06\\/05\\/083f3f06-189d-450b-b9d8-7fc786046b59.jpg\"}],\"Desc\":\"身份证号码:231003195003271638;姓名:孙金;年龄:68;性别:男;身高:175cm;体重:79kg;对比库:市局涉军库;籍贯:231003;匹配度:0.9226182699203492;\"}],\"TOKEN\":\"342RFDSFGER@#YSTRUERTH%$^#&#YRGFDAD\",\"REPORT_COUNT\":1}";
        FaceListEntity model = GsonUtils.fromJson(json,FaceListEntity.class);
    }
}
