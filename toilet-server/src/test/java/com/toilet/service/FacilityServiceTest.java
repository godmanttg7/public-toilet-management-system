package com.toilet.service;

import com.toilet.entity.Facility;
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
class FacilityServiceTest {

    @Autowired
    private FacilityService facilityService;

    @Test
    void testPopulateDisplay() {
        Facility f1 = new Facility();
        f1.setToiletId(1L);
        Facility f2 = new Facility();
        f2.setToiletId(2L);
        List<Facility> list = Arrays.asList(f1, f2);

        facilityService.populateDisplay(list);

        assertEquals("测试公厕A", f1.getToiletName());
        assertEquals("测试公厕B", f2.getToiletName());

        System.out.println("FacilityService - populateDisplay 测试通过！");
    }

    @Test
    void testPopulateDisplayEmptyList() {
        // 空列表不抛异常
        assertDoesNotThrow(() -> facilityService.populateDisplay(Arrays.asList()));

        System.out.println("FacilityService - 空列表测试通过！");
    }

    @Test
    void testPopulateDisplayNullToiletId() {
        Facility f = new Facility();
        f.setToiletId(null);
        List<Facility> list = Arrays.asList(f);

        facilityService.populateDisplay(list);

        assertNull(f.getToiletName());

        System.out.println("FacilityService - null toiletId 测试通过！");
    }
}
