package com.contest.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 元对象处理器 — 自动填充创建时间和更新时间
 *
 * <p>实体类中标注 @TableField(fill = FieldFill.INSERT) 的字段在插入时自动填充，
 * 标注 @TableField(fill = FieldFill.INSERT_UPDATE) 的字段在插入和更新时自动填充。
 *
 * <p>可用性说明：Service 层无需手动设置 createTime/updateTime，减少重复代码。
 * 适用于所有实体类（通过 MetaObject 的泛型处理）。
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
