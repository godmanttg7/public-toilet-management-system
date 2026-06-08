package com.toilet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.toilet.entity.ConsumableRecord;
import com.toilet.entity.ConsumableStock;
import com.toilet.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class ConsumableServiceTest {

    @Autowired
    private ConsumableService consumableService;

    @Test
    void testStockInNew() {
        // 入库新耗材
        assertDoesNotThrow(() -> consumableService.stockIn(1L, "空气清新剂", 20, 1L));

        // 验证库存已创建
        ConsumableStock stock = consumableService.getOne(new LambdaQueryWrapper<ConsumableStock>()
                .eq(ConsumableStock::getToiletId, 1L)
                .eq(ConsumableStock::getConsumableName, "空气清新剂"));
        assertNotNull(stock);
        assertEquals(20, stock.getCurrentStock());

        System.out.println("ConsumableService - 新增入库测试通过！");
    }

    @Test
    void testStockInExisting() {
        // 入库已有耗材（卫生纸当前库存50）
        assertDoesNotThrow(() -> consumableService.stockIn(1L, "卫生纸", 30, 1L));

        // 验证库存增加
        ConsumableStock stock = consumableService.getOne(new LambdaQueryWrapper<ConsumableStock>()
                .eq(ConsumableStock::getToiletId, 1L)
                .eq(ConsumableStock::getConsumableName, "卫生纸"));
        assertNotNull(stock);
        assertEquals(80, stock.getCurrentStock()); // 50 + 30

        System.out.println("ConsumableService - 追加入库测试通过！");
    }

    @Test
    void testStockOutSuccess() {
        // 出库卫生纸（当前库存50）
        assertDoesNotThrow(() -> consumableService.stockOut(1L, "卫生纸", 10, 1L));

        // 验证库存减少
        ConsumableStock stock = consumableService.getOne(new LambdaQueryWrapper<ConsumableStock>()
                .eq(ConsumableStock::getToiletId, 1L)
                .eq(ConsumableStock::getConsumableName, "卫生纸"));
        assertNotNull(stock);
        assertEquals(40, stock.getCurrentStock()); // 50 - 10

        System.out.println("ConsumableService - 出库成功测试通过！");
    }

    @Test
    void testStockOutInsufficient() {
        // 出库数量超过库存
        assertThrows(BusinessException.class, () ->
                consumableService.stockOut(1L, "卫生纸", 100, 1L));

        System.out.println("ConsumableService - 库存不足测试通过！");
    }

    @Test
    void testStockOutNonExistent() {
        // 出库不存在的耗材
        assertThrows(BusinessException.class, () ->
                consumableService.stockOut(1L, "不存在耗材", 5, 1L));

        System.out.println("ConsumableService - 库存不存在测试通过！");
    }

    @Test
    void testStockInInvalidQuantity() {
        // 入库数量为0
        assertThrows(BusinessException.class, () ->
                consumableService.stockIn(1L, "测试耗材", 0, 1L));

        // 入库数量为负数
        assertThrows(BusinessException.class, () ->
                consumableService.stockIn(1L, "测试耗材", -5, 1L));

        System.out.println("ConsumableService - 入库数量校验测试通过！");
    }

    @Test
    void testStockOutInvalidQuantity() {
        // 出库数量为0
        assertThrows(BusinessException.class, () ->
                consumableService.stockOut(1L, "卫生纸", 0, 1L));

        // 出库数量为负数
        assertThrows(BusinessException.class, () ->
                consumableService.stockOut(1L, "卫生纸", -5, 1L));

        System.out.println("ConsumableService - 出库数量校验测试通过！");
    }

    @Test
    void testPopulateDisplay() {
        List<ConsumableStock> stocks = consumableService.list();
        assertFalse(stocks.isEmpty());

        consumableService.populateDisplay(stocks);
        stocks.forEach(s -> {
            assertNotNull(s.getToiletName());
        });

        System.out.println("ConsumableService - 库存展示填充测试通过！");
    }

    @Test
    void testPopulateRecordDisplay() {
        // 直接构造记录列表模拟
        ConsumableRecord r1 = new ConsumableRecord();
        r1.setToiletId(1L);
        ConsumableRecord r2 = new ConsumableRecord();
        r2.setToiletId(2L);
        List<ConsumableRecord> records = Arrays.asList(r1, r2);

        consumableService.populateRecordDisplay(records);
        assertEquals("测试公厕A", r1.getToiletName());
        assertEquals("测试公厕B", r2.getToiletName());

        System.out.println("ConsumableService - 记录展示填充测试通过！");
    }
}
