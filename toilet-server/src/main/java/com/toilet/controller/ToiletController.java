package com.toilet.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.toilet.common.Result;
import com.toilet.entity.Toilet;
import com.toilet.service.ToiletService;
import com.toilet.vo.ToiletVO;
import javax.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/toilet")
public class ToiletController {

    private final ToiletService toiletService;

    public ToiletController(ToiletService toiletService) {
        this.toiletService = toiletService;
    }

    @GetMapping("/page")
    public Result<IPage<ToiletVO>> page(@RequestParam(defaultValue = "1") Integer current,
                                        @RequestParam(defaultValue = "10") Integer size,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(required = false) String district) {
        LambdaQueryWrapper<Toilet> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(name), Toilet::getName, name)
               .eq(StringUtils.hasText(district), Toilet::getDistrict, district)
               .orderByDesc(Toilet::getCreateTime);
        IPage<Toilet> page = toiletService.page(new Page<>(current, size), wrapper);
        return Result.success(page.convert(ToiletVO::fromEntity));
    }

    @GetMapping("/list")
    public Result<List<ToiletVO>> list() {
        LambdaQueryWrapper<Toilet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Toilet::getStatus, 1).orderByDesc(Toilet::getCreateTime);
        List<Toilet> list = toiletService.list(wrapper);
        return Result.success(list.stream().map(ToiletVO::fromEntity).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public Result<ToiletVO> getById(@PathVariable Long id) {
        Toilet toilet = toiletService.getById(id);
        return Result.success(ToiletVO.fromEntity(toilet));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Result<Void> save(@Valid @RequestBody Toilet toilet) {
        toiletService.save(toilet);
        return Result.success();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody Toilet toilet) {
        toiletService.updateById(toilet);
        return Result.success();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        toiletService.removeById(id);
        return Result.success();
    }
}
