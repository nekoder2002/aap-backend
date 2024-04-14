package com.dhu.utils;

import com.dhu.dto.EchartDTO;
import com.dhu.exception.OperationException;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.nlpcn.commons.lang.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ContentUtils {
    /**
     * 获取 分词-词频 列表
     */
    public static List<EchartDTO> getWordList(String text) {
        Map<String, Integer> map = new HashMap<>(16);
        String result = ToAnalysis.parse(text).toStringWithOutNature();

        //分词后的内容，分词间使用英文逗号分隔。
        String[] words = result.split(",");
        for (String word : words) {
            String str = word.trim();
            // 过滤空白字符
            if (StringUtil.isBlank(str)) {
                continue;
            }
            // 过滤一些高频率的符号
            else if (str.matches("[）|（|.|，|。|+|-|“|”|：|？|\\s]")) {
                continue;
            }
            // 此处过滤长度为1的str
            else if (str.length() < 2) {
                continue;
            }
            if (!map.containsKey(word)) {
                map.put(word, 1);

            } else {
                int n = map.get(word);
                map.put(word, ++n);
            }
        }
        return sortByValue(map);
    }

    /**
     * 根据词频从高到低排序
     */
    private static List<EchartDTO> sortByValue(Map<String, Integer> map) {
        if (map == null) {
            return null;
        }
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());

        list.sort(new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        list = list.subList(0, Math.min(list.size(), 100));
        List<EchartDTO> res = new ArrayList<>();
        for (Map.Entry<String, Integer> it : list) {
            EchartDTO freqDTO = new EchartDTO();
            freqDTO.setName(it.getKey());
            freqDTO.setValue(it.getValue());
            res.add(freqDTO);
        }
        return res;
    }

    public static String readPDF(File file) {
        PDDocument document=null;
        try {
            document = PDDocument.load(file);
            //Instantiate PDFTextStripper class
            PDFTextStripper pdfStripper = new PDFTextStripper();
            //Retrieving text from PDF document
            return pdfStripper.getText(document);
        } catch (IOException e) {
            throw new OperationException("读取pdf异常");
        }finally {
            if (document!=null){
                try {
                    document.close();
                } catch (IOException e) {

                }
            }
        }
    }
}
