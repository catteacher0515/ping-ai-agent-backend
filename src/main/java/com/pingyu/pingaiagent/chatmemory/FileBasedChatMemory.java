package com.pingyu.pingaiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.util.Pool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import org.objenesis.strategy.StdInstantiatorStrategy;



/**
 * 案件 AI-009: 文件持久化聊天记忆
 * 目标: 使用 Kryo 将对话记录序列化到磁盘，重启不丢失。
 */
@Slf4j
public class FileBasedChatMemory implements ChatMemory {

    private final String storageDir;

    // 内存缓存，减少频繁IO读取
    private final Map<String, List<Message>> conversationCache = new ConcurrentHashMap<>();

    // Kryo 是非线程安全的，使用 Pool 或 ThreadLocal 管理
    private final Pool<Kryo> kryoPool = new Pool<Kryo>(true, false, 8) {
        @Override
        protected Kryo create() {
            Kryo kryo = new Kryo();
            // 允许序列化未注册的类
            kryo.setRegistrationRequired(false);

            // [新增] 关键修复: 配置实例化策略
            // 允许 Kryo 使用 Objenesis 绕过构造函数创建对象 (解决 missing no-arg constructor 问题)
            kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));

            return kryo;
        }
    };

    public FileBasedChatMemory() {
        // 默认存储在项目根目录的 chat_memory 文件夹下
        this.storageDir = "chat_memory";
        initStorageDir();
    }

    private void initStorageDir() {
        try {
            Files.createDirectories(Paths.get(storageDir));
        } catch (IOException e) {
            log.error("无法创建记忆存储目录: {}", storageDir, e);
        }
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        // 1. 更新内存缓存
        List<Message> currentHistory = this.conversationCache.computeIfAbsent(conversationId, this::loadFromFile);
        currentHistory.addAll(messages);

        // 2. 异步或同步写入磁盘 (此处为保证一致性，暂时使用同步写)
        saveToFile(conversationId, currentHistory);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        // 1. 获取完整历史 (优先走缓存)
        List<Message> allMessages = this.conversationCache.computeIfAbsent(conversationId, this::loadFromFile);

        // 2. 截取最后 N 条
        if (lastN <= 0 || lastN >= allMessages.size()) {
            return new ArrayList<>(allMessages);
        }
        return new ArrayList<>(allMessages.subList(allMessages.size() - lastN, allMessages.size()));
    }

    @Override
    public void clear(String conversationId) {
        // 1. 清除缓存
        conversationCache.remove(conversationId);

        // 2. 删除文件
        try {
            Path filePath = Paths.get(storageDir, conversationId + ".bin");
            Files.deleteIfExists(filePath);
            log.info("已清除记忆文件: {}", conversationId);
        } catch (IOException e) {
            log.error("清除记忆文件失败: {}", conversationId, e);
        }
    }

    private void saveToFile(String conversationId, List<Message> messages) {
        Kryo kryo = kryoPool.obtain();
        Path filePath = Paths.get(storageDir, conversationId + ".bin");
        try (Output output = new Output(new FileOutputStream(filePath.toFile()))) {
            kryo.writeObject(output, new ArrayList<>(messages)); // 确保存储为 ArrayList
            // log.debug("记忆已保存: {} (条数: {})", conversationId, messages.size());
        } catch (Exception e) {
            log.error("保存记忆失败: {}", conversationId, e);
        } finally {
            kryoPool.free(kryo);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Message> loadFromFile(String conversationId) {
        File file = Paths.get(storageDir, conversationId + ".bin").toFile();
        if (!file.exists()) {
            return new ArrayList<>();
        }

        Kryo kryo = kryoPool.obtain();
        try (Input input = new Input(new FileInputStream(file))) {
            return kryo.readObject(input, ArrayList.class);
        } catch (Exception e) {
            log.error("读取记忆失败: {}, 将重置为空。", conversationId, e);
            return new ArrayList<>();
        } finally {
            kryoPool.free(kryo);
        }
    }
}