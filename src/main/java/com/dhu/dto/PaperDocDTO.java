package com.dhu.dto;

import com.alibaba.fastjson.annotation.JSONField;

public class PaperDocDTO {
    @JSONField(name = "page_num")
    private Integer pageNum;
    @JSONField(name = "line_num")
    private Integer lineNum;
    private String text;

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getLineNum() {
        return lineNum;
    }

    public void setLineNum(Integer lineNum) {
        this.lineNum = lineNum;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
