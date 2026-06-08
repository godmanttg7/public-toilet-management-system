package com.toilet.common;

import com.toilet.entity.Toilet;
import com.toilet.mapper.ToiletMapper;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 展示字段填充工具类。
 * <p>
 * 大厂实践：将重复的"批量查询关联表并填充展示字段"逻辑抽取为公共工具，
 * 避免 6 个 Service 中各自重复实现相同的 fetch + map + populate 模式。
 * </p>
 */
public class DisplayUtils {

    private DisplayUtils() {
        // 工具类禁止实例化
    }

    /**
     * 批量填充实体的公厕名称。
     *
     * @param list         实体列表
     * @param toiletIdFn   从实体提取 toiletId: entity -> entity.getToiletId()
     * @param toiletNameFn 设置 toiletName: (entity, name) -> entity.setToiletName(name)
     * @param toiletMapper 公厕 Mapper
     * @param <T>          实体类型
     */
    public static <T> void populateToiletName(List<T> list,
                                               Function<T, Long> toiletIdFn,
                                               BiConsumer<T, String> toiletNameFn,
                                               ToiletMapper toiletMapper) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Long> ids = list.stream()
                .map(toiletIdFn)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, String> nameMap = toiletMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Toilet::getId, Toilet::getName));
        list.forEach(entity -> {
            Long id = toiletIdFn.apply(entity);
            if (id != null) {
                toiletNameFn.accept(entity, nameMap.get(id));
            }
        });
    }

    /**
     * 批量查询关联表并填充展示字段的通用方法。
     *
     * @param list        实体列表
     * @param idFn        从实体提取关联ID: entity -> entity.getSomeId()
     * @param valueFn     设置展示字段: (entity, displayValue) -> entity.setSomeName(value)
     * @param mapper      关联表的 Mapper（需支持 selectBatchIds）
     * @param nameExtractor 从关联实体提取展示名称: entity -> entity.getName()
     * @param <T>         主实体类型
     * @param <R>         关联实体类型
     */
    public static <T, R> void populateDisplay(List<T> list,
                                               Function<T, Long> idFn,
                                               BiConsumer<T, String> valueFn,
                                               com.baomidou.mybatisplus.core.mapper.BaseMapper<R> mapper,
                                               Function<R, String> nameExtractor,
                                               Function<R, Long> idExtractor) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Long> ids = list.stream()
                .map(idFn)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, String> nameMap = mapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(idExtractor, nameExtractor));
        list.forEach(entity -> {
            Long id = idFn.apply(entity);
            if (id != null) {
                valueFn.accept(entity, nameMap.get(id));
            }
        });
    }

}
