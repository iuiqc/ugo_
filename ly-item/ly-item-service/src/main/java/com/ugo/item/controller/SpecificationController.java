package com.ugo.item.controller;

import com.ugo.item.pojo.SpecGroup;
import com.ugo.item.pojo.SpecParam;
import com.ugo.item.service.SpecService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: qc
 * @Date: 2019-4-19 16:21
 */
@Slf4j
@RestController
@RequestMapping("spec")
public class SpecificationController {
    @Autowired
    private SpecService specService;

    @GetMapping("groups/{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecGroups(@PathVariable("cid") Long cid) {
        List<SpecGroup> specGroupList = specService.querySpecGroupsByCid(cid);

        if (null == specGroupList || 0 == specGroupList.size()) {
            //204
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(specGroupList);
    }

    /**
     * @Author: qc
     * @Date: 2019/4/22 12:51
     * 新增分组->spec_group
     */
    @PostMapping("group")
    public ResponseEntity<Void> saveSpecGroups(@RequestBody SpecGroup specGroup){//@RequestParam("cid") Long cid, @RequestParam("name") String name) {
        System.out.println("==========");
        if (StringUtils.isBlank(specGroup.getName()) || StringUtils.isBlank(specGroup.getCid().toString())) {
            log.error("name:" + specGroup.getName() + "cid:" + specGroup.getCid());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        try {
            specService.saveSpecGroups(specGroup);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("新增分组");
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        //return ResponseEntity.ok().build();
    }

    //params?gid=1
    @GetMapping("params")
    public ResponseEntity<List<SpecParam>> querySpecParamByGid(
            @RequestParam(value = "gid", required = false) Long gid,
            @RequestParam(value = "cid", required = false) Long cid,
            @RequestParam(value = "searching", required = false) Boolean searching,
            @RequestParam(value = "generic", required = false) Boolean generic) {

        List<SpecParam> specParamList = specService.querySpecParamByGid(gid, cid, searching, generic);

        if (null == specParamList || 0 == specParamList.size()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return ResponseEntity.ok(specParamList);
    }

    @GetMapping("{cid}")
    public ResponseEntity<List<SpecGroup>> querySpecsByCid(@PathVariable("cid") Long cid) {
        List<SpecGroup> list = this.specService.querySpecsByCid(cid);
        if (list == null || list.size() == 0) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(list);
    }
}
