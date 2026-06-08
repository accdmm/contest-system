package com.contest.common.util;

import java.util.regex.Pattern;

/**
 * HTML 标签属性白名单过滤器 — 服务端 XSS 防护
 *
 * <p>前端已通过 sanitizeHtml() 过滤富文本中的危险标签，但后端作为数据存储的最后一道防线，
 * 必须在入库前再次过滤，防止绕过前端直接调用 API 注入恶意代码。
 *
 * <p>过滤规则：
 * <ul>
 *   <li>移除 &lt;script&gt;、&lt;iframe&gt;、&lt;object&gt;、&lt;embed&gt; 标签及其内容</li>
 *   <li>移除 on* 事件处理器属性（如 onclick、onload、onerror 等）</li>
 *   <li>移除 href=/src=javascript: 伪协议</li>
 * </ul>
 *
 * <p>安全性说明：白名单策略比黑名单更安全，此处采用"移除已知危险模式"的方式，
 * 适用于课程设计的防护需求。生产环境建议使用 OWASP Java HTML Sanitizer 或 Jsoup。
 */
public class HtmlSanitizer {

    private static final Pattern SCRIPT_TAG = Pattern.compile(
            "<script[^>]*>[\\s\\S]*?</script>",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern IFRAME_TAG = Pattern.compile(
            "<iframe[^>]*>[\\s\\S]*?</iframe>",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern OBJECT_TAG = Pattern.compile(
            "<object[^>]*>[\\s\\S]*?</object>",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern EMBED_TAG = Pattern.compile(
            "<embed[^>]*>[\\s\\S]*?</embed>",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern EVENT_HANDLER = Pattern.compile(
            "\\son\\w+\\s*=\\s*['\"][^'\"]*['\"]",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern JAVASCRIPT_URI = Pattern.compile(
            "\\s+(href|src)\\s*=\\s*['\"]\\s*javascript:",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * 过滤 HTML，移除 XSS 风险内容
     *
     * @param html 原始 HTML
     * @return 过滤后的安全 HTML，null 输入返回 null
     */
    public static String sanitize(String html) {
        if (html == null || html.isEmpty()) {
            return html;
        }
        String result = html;
        result = SCRIPT_TAG.matcher(result).replaceAll("");
        result = IFRAME_TAG.matcher(result).replaceAll("");
        result = OBJECT_TAG.matcher(result).replaceAll("");
        result = EMBED_TAG.matcher(result).replaceAll("");
        result = EVENT_HANDLER.matcher(result).replaceAll("");
        result = JAVASCRIPT_URI.matcher(result).replaceAll("");
        return result;
    }
}
