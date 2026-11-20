package com.example.elderly.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

/**
 * 阶段2：统一分页参数别名。
 *
 * <p>历史代码中同时存在 page/size、current/size、pageNum/pageSize 三套命名，
 * 导致同一系统不同接口分页行为不一致。本过滤器在请求进入 Controller 前把三套别名
 * 归一化为同一组值，使任意一种写法都能生效，前端无需区分接口差异。</p>
 *
 * <p>优先级：page > current > pageNum；size > pageSize。</p>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PaginationAliasFilter extends OncePerRequestFilter {

    private static final List<String> PAGE_KEYS = Arrays.asList("page", "current", "pageNum");
    private static final List<String> SIZE_KEYS = Arrays.asList("size", "pageSize");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        filterChain.doFilter(new PaginationAliasRequestWrapper(request), response);
    }

    private static class PaginationAliasRequestWrapper extends HttpServletRequestWrapper {
        private final Map<String, String[]> params;

        PaginationAliasRequestWrapper(HttpServletRequest request) {
            super(request);
            Map<String, String[]> map = new LinkedHashMap<>();
            request.getParameterMap().forEach((k, v) -> map.put(k, v.clone()));

            normalize(map, PAGE_KEYS);
            normalize(map, SIZE_KEYS);
            this.params = Collections.unmodifiableMap(map);
        }

        private void normalize(Map<String, String[]> map, List<String> aliases) {
            String chosen = null;
            for (String key : aliases) {
                String[] v = map.get(key);
                if (v != null && v.length > 0 && v[0] != null && !v[0].isEmpty()) {
                    chosen = v[0];
                    break;
                }
            }
            if (chosen == null) return;
            for (String key : aliases) {
                map.put(key, new String[]{chosen});
            }
        }

        @Override
        public String getParameter(String name) {
            String[] v = params.get(name);
            return v == null || v.length == 0 ? null : v[0];
        }

        @Override
        public String[] getParameterValues(String name) {
            return params.get(name);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            return params;
        }

        @Override
        public Enumeration<String> getParameterNames() {
            return Collections.enumeration(params.keySet());
        }
    }
}
