package com.netconfig.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class NoteModerationService {

    private static final List<String> BLOCKED_KEYWORDS = Arrays.asList(
        // 政治敏感
        "习近平", "毛泽东", "邓小平", "江泽民", "胡锦涛",
        "共产党", "国民党", "民进党", "法轮功", "六四",
        "天安门事件", "文化大革命", "大跃进", "反右",
        "台独", "藏独", "疆独", "港独",
        "翻墙", "VPN", "翻墙软件",
        // 暴力血腥
        "杀人", "砍人", "炸弹", "爆炸", "枪击", "恐怖袭击",
        "自杀", "自残", "割腕", "上吊",
        // 色情赌博
        "色情", "裸体", "性爱", "做爱", "口交", "肛交",
        "赌博", "博彩", "麻将", "老虎机", "彩票",
        // 诈骗传销
        "诈骗", "传销", "庞氏骗局", "杀猪盘", "刷单",
        "兼职刷单", "日赚千元", "躺赚", "被动收入",
        // 违法违规
        "毒品", "冰毒", "大麻", "海洛因", "摇头丸",
        "枪支", "弹药", "管制刀具",
        // 辱骂攻击
        "傻逼", "操你", "妈的", "狗日", "去死",
        "废物", "垃圾人", "脑残", "智障",
        // 广告推销
        "加微信", "加QQ", "扫码领取", "免费领取",
        "限时优惠", "点击链接", "复制口令"
    );

    private static final List<String> BLOCKED_PATTERNS = Arrays.asList(
        "http[s]?://[^\\s]+",  // URL链接
        "www\\.[^\\s]+",       // 网址
        "\\d{11}",            // 手机号（11位数字）
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}" // 邮箱
    );

    private static final List<Pattern> COMPILED_PATTERNS = new ArrayList<>();
    static {
        for (String p : BLOCKED_PATTERNS) {
            COMPILED_PATTERNS.add(Pattern.compile(p, Pattern.CASE_INSENSITIVE));
        }
    }

    public ModerationResult check(String content) {
        if (content == null || content.isBlank()) {
            return ModerationResult.pass();
        }

        String lower = content.toLowerCase();

        // 关键词检查
        for (String keyword : BLOCKED_KEYWORDS) {
            if (lower.contains(keyword.toLowerCase())) {
                return ModerationResult.reject("内容包含违禁词汇，请修改后重试");
            }
        }

        // 正则模式检查
        for (Pattern pattern : COMPILED_PATTERNS) {
            if (pattern.matcher(content).find()) {
                return ModerationResult.reject("内容包含链接或联系方式，请移除后重试");
            }
        }

        // 长度检查
        if (content.length() > 2000) {
            return ModerationResult.reject("笔记内容过长，请控制在2000字以内");
        }

        return ModerationResult.pass();
    }

    public static class ModerationResult {
        private final boolean passed;
        private final String message;

        private ModerationResult(boolean passed, String message) {
            this.passed = passed;
            this.message = message;
        }

        public static ModerationResult pass() {
            return new ModerationResult(true, null);
        }

        public static ModerationResult reject(String message) {
            return new ModerationResult(false, message);
        }

        public boolean isPassed() { return passed; }
        public String getMessage() { return message; }
    }
}
