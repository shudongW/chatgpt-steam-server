package com.tech.chatgpt.utils;

import com.baidu.aip.contentcensor.AipContentCensor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;


@Slf4j
public class BaiDuAiCheck {

    /**
     *文本审核功能
     *@return
     */
    public static boolean checkText(String text){
        AipContentCensor client = BaiDuAiConfig.getInstance();
        // 参数为输入文本
        JSONObject response = client.textCensorUserDefined(text);
        if (response.isNull("conclusion")){
            log.info("合规词检测失败"+ response.toString());
            return false;
        }
        return !response.get("conclusion").equals("合规");
    }
}
