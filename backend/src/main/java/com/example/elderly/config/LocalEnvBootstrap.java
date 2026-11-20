package com.example.elderly.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 本地开发环境引导：让 `java -jar` / `mvn spring-boot:run` 在没有手工设置环境变量时也能启动。
 *
 * <p>行为（只在对应配置**缺失**时生效，不覆盖已存在的环境变量，不内置任何默认密钥）：
 * <ol>
 *   <li>读取 <code>backend/.env.local</code>（key=value，支持 # 注释与空行），把未设置的键写入系统属性；</li>
 *   <li>若仍无 JWT_SECRET：读取 `backend/.jwt-secret.local`（与 scripts/02-start-backend.bat 同一个文件），
 *       文件不存在或长度不足 32 位时生成 64 位随机十六进制串并写回该文件；</li>
 *   <li>两项都无法补齐时**不阻断启动流程**，由 JwtUtil / 数据源按原逻辑抛出明确错误。</li>
 * </ol>
 *
 * <p>生产部署请直接设置环境变量（JWT_SECRET / DB_PASSWORD），此时本类不会写入任何东西。
 */
public final class LocalEnvBootstrap {

    private static final Logger log = LoggerFactory.getLogger(LocalEnvBootstrap.class);

    /** 可能的工作目录：IDE/终端通常在 backend/ 或项目根目录启动。 */
    private static final List<Path> CANDIDATE_DIRS = List.of(
            Paths.get("").toAbsolutePath(),
            Paths.get("").toAbsolutePath().resolve("backend"));

    private static final String ENV_LOCAL_FILE = ".env.local";
    private static final String JWT_SECRET_FILE = ".jwt-secret.local";
    private static final int MIN_SECRET_LENGTH = 32;

    private LocalEnvBootstrap() {
    }

    /** 在 Spring 容器启动前调用。 */
    public static void prime() {
        loadDotEnvLocal();
        ensureJwtSecret();
    }

    /** 解析 .env.local，仅补充未设置的键；文件不存在时按模板生成一份，保证 clone 后可直接启动。 */
    private static void loadDotEnvLocal() {
        for (Path dir : CANDIDATE_DIRS) {
            Path file = dir.resolve(ENV_LOCAL_FILE);
            if (Files.isRegularFile(file)) {
                applyEnvFile(file);
                return;
            }
        }
        // 首次启动（例如刚 clone 仓库）：从模板生成 .env.local，避免"没有数据库密码"直接启动失败
        Path template = backendModuleDir().resolve(ENV_LOCAL_FILE + ".example");
        Path target = backendModuleDir().resolve(ENV_LOCAL_FILE);
        if (!Files.isRegularFile(template)) {
            log.info("[本地引导] 未找到 {}，跳过本地变量加载", ENV_LOCAL_FILE);
            return;
        }
        try {
            Files.copy(template, target);
            log.info("[本地引导] 首次启动，已根据 {} 生成 {}（含本地演示默认值，请按需修改）", template.getFileName(), target);
            applyEnvFile(target);
        } catch (IOException e) {
            log.warn("[本地引导] 生成 {} 失败（{}），请手动复制 {} 后重试", target, e.getMessage(), template.getFileName());
        }
    }

    private static void applyEnvFile(Path file) {
        Map<String, String> values = parse(file);
        List<String> applied = new ArrayList<>();
        values.forEach((key, value) -> {
            if (isBlank(System.getenv(key)) && isBlank(System.getProperty(key)) && !value.isBlank()) {
                System.setProperty(key, value);
                applied.add(key);
            }
        });
        log.info("[本地引导] 已读取 {}，补充配置项: {}", file, applied.isEmpty() ? "(无需补充)" : applied);
    }

    /** 保证存在可用的 jwt.secret。 */
    private static void ensureJwtSecret() {
        String existing = firstNonBlank(System.getenv("JWT_SECRET"), System.getProperty("JWT_SECRET"));
        if (existing != null && existing.trim().length() >= MIN_SECRET_LENGTH) {
            return;
        }
        // 第一轮：优先复用任意候选目录下已存在的密钥文件，避免在根目录与 backend/ 各生成一份
        for (Path dir : CANDIDATE_DIRS) {
            Path file = dir.resolve(JWT_SECRET_FILE);
            try {
                if (!Files.isRegularFile(file)) {
                    continue;
                }
                String saved = Files.readString(file, StandardCharsets.UTF_8).trim();
                if (saved.length() >= MIN_SECRET_LENGTH) {
                    System.setProperty("JWT_SECRET", saved);
                    log.info("[本地引导] 已从 {} 加载 JWT 签名密钥（长度 {}）", file, saved.length());
                    return;
                }
                log.warn("[本地引导] {} 中的密钥长度不足 {} 位，将重新生成", file, MIN_SECRET_LENGTH);
            } catch (IOException e) {
                log.warn("[本地引导] 读取 {} 失败: {}", file, e.getMessage());
            }
        }
        // 第二轮：都不存在时，统一生成到后端工程目录（含 pom.xml 或名为 backend 的目录）
        Path target = backendModuleDir();
        Path file = target.resolve(JWT_SECRET_FILE);
        try {
            String generated = generateSecret();
            Files.writeString(file, generated, StandardCharsets.UTF_8);
            System.setProperty("JWT_SECRET", generated);
            log.info("[本地引导] 未设置 JWT_SECRET，已生成随机密钥并保存到 {}（该文件已被 .gitignore 排除）", file);
        } catch (IOException e) {
            log.warn("[本地引导] 写入 {} 失败（{}），本次启动使用内存中的临时密钥，重启后 Token 会失效", file, e.getMessage());
            System.setProperty("JWT_SECRET", generateSecret());
        }
    }

    /** 定位后端工程目录：优先含 pom.xml 的候选目录，其次名为 backend 的目录。 */
    private static Path backendModuleDir() {
        for (Path dir : CANDIDATE_DIRS) {
            if (Files.isRegularFile(dir.resolve("pom.xml"))) {
                return dir;
            }
        }
        for (Path dir : CANDIDATE_DIRS) {
            if (dir.getFileName() != null && "backend".equals(dir.getFileName().toString())) {
                return dir;
            }
        }
        return CANDIDATE_DIRS.get(0);
    }

    private static Map<String, String> parse(Path file) {
        Map<String, String> map = new LinkedHashMap<>();
        try {
            for (String raw : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
                    continue;
                }
                int idx = line.indexOf('=');
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                if (value.length() >= 2
                        && (value.startsWith("\"") && value.endsWith("\"")
                        || value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }
                if (!key.isEmpty()) {
                    map.put(key, value);
                }
            }
        } catch (IOException e) {
            log.warn("[本地引导] 读取 {} 失败: {}", file, e.getMessage());
        }
        return map;
    }

    private static String generateSecret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        StringBuilder sb = new StringBuilder(64);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (!isBlank(v)) {
                return v;
            }
        }
        return null;
    }
}
